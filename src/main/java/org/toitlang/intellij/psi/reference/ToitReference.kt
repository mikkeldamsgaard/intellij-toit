package org.toitlang.intellij.psi.reference

import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult
import com.intellij.util.IncorrectOperationException
import lombok.Getter
import org.toitlang.intellij.files.ToitSdkFiles
import org.toitlang.intellij.psi.ToitFile
import org.toitlang.intellij.psi.ToitTypes
import org.toitlang.intellij.psi.ast.*
import org.toitlang.intellij.psi.ast.ToitStructure.StaticScope
import org.toitlang.intellij.psi.calls.ToitCallHelper
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor
import org.toitlang.intellij.psi.scope.ToitLocalScopeCalculator
import org.toitlang.intellij.psi.scope.ToitScope
import org.toitlang.intellij.psi.visitor.ToitVisitor
import java.util.stream.Collectors

class ToitReference private constructor(private val source: ToitReferenceIdentifier) : PsiPolyVariantReference {
    val destinations: MutableSet<ToitReferenceTarget?>
    private val dependencies: MutableList<PsiElement>
    var soft: Boolean

    init {
        destinations = LinkedHashSet()
        dependencies = ArrayList()
        soft = false
    }

    override fun getVariants(): Array<Any> {
        return VariantsCalculator.getVariants(source, createEvaluationScope())
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return destinations.stream().map { element: ToitReferenceTarget? -> PsiElementResolveResult(element!!) }.toArray { length: Int -> arrayOfNulls(length) }
    }

    override fun resolve(): PsiElement? {
        return destinations.stream().findAny().orElse(null)
    }

    override fun getElement(): PsiElement {
        return source
    }

    override fun getRangeInElement(): TextRange {
        return TextRange(0, source.textLength)
    }

    override fun getCanonicalText(): @NlsSafe String {
        return source.getName()
    }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        return source.setName(newElementName)
    }

    @Throws(IncorrectOperationException::class)
    override fun bindToElement(element: PsiElement): PsiElement {
        destinations.clear()
        if (element is ToitReferenceTarget) {
            destinations.add(element)
        }
        dependencies.add(element)
        return element
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        return getElement().manager.areElementsEquivalent(resolve(), element)
    }

    override fun isSoft(): Boolean {
        return soft
    }

    private fun createEvaluationScope(): ToitScope {
        val coreScope = ToitSdkFiles.getCoreScope(source.getProject())
        val file = source.toitFile
        val toitFileScope = file.toitFileScope
        val fileScope = toitFileScope.getToitScope(coreScope)
        return ToitLocalScopeCalculator.calculate(source, fileScope)
    }

    private fun build(): ToitReference {
        val scope = createEvaluationScope()
        val name = source.getName()
        dependencies.add(source)
        val sType = source.getNode().elementType
        if (sType === ToitTypes.TYPE_IDENTIFIER) {
            val prevSib = source.prevSibling
            if (prevSib != null) {
                val refs = source.getParentOfType(ToitType::class.java).getChildrenOfType(ToitReferenceIdentifier::class.java)
                val idx = refs.indexOf(source)
                if (idx == 1) {
                    val prevRefs = refs[0].reference.multiResolve(false)
                    for (prevRef in prevRefs) {
                        if (prevRef.element is ToitFile) {
                            val exportedScope = (prevRef.element as ToitFile?)!!.toitFileScope.exportedScope
                            val elm = exportedScope.resolve(name)
                            destinations.addAll(elm)
                        }
                    }
                }
            } else {
                if ("none" == name || "any" == name) {
                    soft = true
                } else {
                    destinations.addAll(source.toitResolveScope.resolve(name))
                }
            }
        } else if (sType === ToitTypes.IMPORT_SHOW_IDENTIFIER || sType === ToitTypes.EXPORT_IDENTIFIER) {
            destinations.addAll(scope.resolve(name))
        } else if (sType === ToitTypes.BREAK_CONTINUE_LABEL_IDENTIFIER) {
            soft = true
        } else if (sType === ToitTypes.IMPORT_IDENTIFIER) {
            val importDecl = source.getParentOfType(ToitImportDeclaration::class.java)
            val evaluatedType = importDecl.getEvaluatedType()
            if (evaluatedType.resolved() && evaluatedType.file != null) {
                destinations.add(evaluatedType.file)
            }
        } else if (sType === ToitTypes.NAMED_ARGUMENT_IDENTIFIER) {
            val call = source.getParentOfType(ToitCallExpression::class.java)
            val resolved = ToitCallHelper.resolveCall(call)
            if (resolved != null) {
                val parameterInfo = resolved.getParamForArg(source.getParentOfType(ToitNamedArgument::class.java))
                if (parameterInfo != null) {
                    val toitParameterName = parameterInfo.parameterName
                    val identifier = toitParameterName.getNameIdentifier()
                    if (identifier is ToitReferenceIdentifier) {
                        destinations.addAll(identifier.reference.destinations)
                    } else if (identifier is ToitNameableIdentifier) {
                        destinations.add(toitParameterName)
                    }
                }
            } else {
                soft = true
            }
        } else if (sType === ToitTypes.REFERENCE_IDENTIFIER) {
            val parent = source.getParent()
            when(parent) { // Handle all possible parent types
                is ToitPrimitive -> soft = true
                is ToitParameterName -> {
                    // This is dotted parameters
                    val structure = source.getParentOfType(ToitStructure::class.java)
                    if (structure != null) {
                        val resolved = structure.getScope(StaticScope.INSTANCE, ToitScope.ROOT).resolve(name)
                        destinations.addAll(resolved.stream()
                                .filter { ref: ToitReferenceTarget? -> ref is ToitVariableDeclaration }
                                .collect(Collectors.toList()))
                    }
                }
                is ToitDerefExpression -> processExpressionParent(parent, scope)
                is ToitPrimaryExpression -> {
                    if (name == ToitEvaluatedType.IT) soft = true;
                    processExpressionParent(parent, scope)
                }
            }
        }
        return this
    }

    private fun processExpressionParent(parent: ToitExpression, scope: ToitScope) {
        val referenceTargets = parent.getReferenceTargets(scope)
        soft = soft or referenceTargets.isSoft
        for (referenceTarget in referenceTargets.getTargets()) {
            destinations.add(referenceTarget.getTarget())
        }
    }


    companion object {
        @JvmStatic
        fun create(source: ToitReferenceIdentifier): ToitReference {
            return ToitReference(source).build()
        }
    }
}
