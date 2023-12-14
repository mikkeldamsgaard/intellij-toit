package org.toitlang.intellij.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.toitlang.intellij.psi.ToitFile
import org.toitlang.intellij.psi.ToitTypes
import org.toitlang.intellij.psi.ast.*
import org.toitlang.intellij.psi.calls.FunctionSignature
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor
import org.toitlang.intellij.psi.visitor.ToitVisitor
import java.util.stream.Collectors

class SemanticErrorInspector : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : ToitVisitor() {
            override fun visit(toitFunction: ToitFunction) {
                checkStaticAbstract(toitFunction, holder)
                checkReturn(toitFunction, holder)
            }

            override fun visit(toitType: ToitType) {
                checkNoneType(toitType, holder)
            }

            override fun visit(toitExpression: ToitExpression) {
                checkInstantiationOfAbstractClass(toitExpression, holder)
            }

            override fun visit(toitStructure: ToitStructure) {
                checkMissingImplementations(toitStructure, holder)
            }

            override fun visit(toitVariableDeclaration: ToitVariableDeclaration) {
                checkIllegalShadow(toitVariableDeclaration, holder)
            }
        }
    }

    private fun checkIllegalShadow(toitVariableDeclaration: ToitVariableDeclaration, holder: ProblemsHolder) {
        if (toitVariableDeclaration.parent.parent is ToitStructure ||
                toitVariableDeclaration.parent is ToitFile) return

        var functionParent = toitVariableDeclaration.getParentOfType(ToitFunction::class.java)

        var scopeElement = toitVariableDeclaration.getPrevSiblingOfType(ToitElementBase::class.java)
        if (scopeElement == null) scopeElement = toitVariableDeclaration.getParentOfType(ToitElementBase::class.java)
        if (scopeElement == null) return
        val nameIdentifier = toitVariableDeclaration.getNameIdentifier() ?: return
        val resolved = scopeElement.localToitResolveScope.resolve(nameIdentifier.getName())
        for (referenced in resolved) {
            if (referenced is ToitVariableDeclaration) {
                if (referenced.parent.parent is ToitStructure && functionParent != null && functionParent!!.isStatic) continue
                if (referenced !== toitVariableDeclaration && !referenced.isStatic()) {
                    holder.registerProblem(toitVariableDeclaration.getProblemIdentifier(), "Illegal shadow of outer variable")
                    return
                }
            }
        }
    }

    private fun checkMissingImplementations(toitStructure: ToitStructure, holder: ProblemsHolder) {
        if (toitStructure.isInterface || toitStructure.isAbstract()) return
        val allFunctions = toitStructure.getAllFunctions()
        val implementedFunctions: MutableMap<String?, MutableSet<FunctionSignature>> = HashMap()
        for (f in allFunctions) {
            if (!f.isAbstract && !f.getParentOfType<ToitStructure>(ToitStructure::class.java).isInterface) {
                implementedFunctions.computeIfAbsent(f.getName()) { n: String? -> HashSet() }.add(f.getSignature())
            }
        }
        val allVariables = toitStructure.getAllVariables()
        for (v in allVariables) {
            for (signature in listOf(v.getterSignature, v.setterSignature)) {
                implementedFunctions.computeIfAbsent(signature.functionName) { HashSet() }.add(signature)
            }
            implementedFunctions.computeIfAbsent(v.getName()) { HashSet() }.add(v.getterSignature)
        }
        val missingImplementation: MutableList<FunctionSignature> = ArrayList()
        FunctionLoop@ for (f in allFunctions) {
            if ((f.isAbstract || f.getParentOfType<ToitStructure>(ToitStructure::class.java).isInterface) && !f.isOperator() && !f.isStatic) {
                val overloaded: Set<FunctionSignature> = implementedFunctions.computeIfAbsent(f.getName()) { n: String? -> HashSet() }
                val signature = f.getSignature()
                for (functionSignature in overloaded) {
                    if (functionSignature.implements_(signature)) continue@FunctionLoop
                }
                missingImplementation.add(signature)
            }
        }
        if (missingImplementation.isNotEmpty()) {
            holder.registerProblem(toitStructure.getProblemIdentifier(), """
                 Missing implementation of
                 >> ${missingImplementation.stream().map { obj: FunctionSignature -> obj.render() }.collect(Collectors.joining("\n>> "))}
                 """.trimIndent())
        }
    }

    private fun checkInstantiationOfAbstractClass(toitExpression: ToitExpression, holder: ProblemsHolder) {
//        var resolved = ToitCallHelper.resolveCall(toitExpression);
//        if (resolved != null && resolved.getToitFunction().isConstructor()) {
//            var structure = resolved.getToitFunction().getParentOfType(ToitStructure.class);
//            if (structure != null) {
//                if (structure.isAbstract() || structure.isInterface()) {
//                    holder.registerProblem(toitExpression, "Can not instantiate abstract class");
//                }
//            }
//        }
    }

    private fun checkNoneType(toitType: ToitType, holder: ProblemsHolder) {
        if ("none" == toitType.name) {
            val prevNonWhiteSpace = toitType.getPrevNonWhiteSpaceSibling()
            if (prevNonWhiteSpace == null || prevNonWhiteSpace.node.elementType !== ToitTypes.RETURN_TYPE_OPERATOR) holder.registerProblem(toitType, "Type none is only allowed as a return type")
        }
    }

    private fun checkReturn(toitFunction: ToitFunction, holder: ProblemsHolder) {
        val returnType = toitFunction.getType()
        val requireReturn = returnType != null && "none" != returnType.name
        val noReturnValue = returnType != null && "none" == returnType.name
        val body = toitFunction.getLastChildOfType(ToitBlock::class.java) ?: return
        val returns = checkReturn(body, holder, returnType, noReturnValue)
        if (requireReturn && !returns) holder.registerProblem(toitFunction.getProblemIdentifier(), "Function does not return a value", ProblemHighlightType.ERROR)
    }

    private fun checkReturn(block: ToitBlock, holder: ProblemsHolder, returnType: ToitType?, noReturnValue: Boolean): Boolean {
        val result = booleanArrayOf(false)

        block.acceptChildren(object : ToitVisitor() {
            override fun visit(toitReturn: ToitReturn) {
                val expression = toitReturn.getFirstChildOfType(ToitExpression::class.java)
                if (returnType != null) {
                    if (expression == null) {
                        holder.registerProblem(toitReturn, "Missing return expression", ProblemHighlightType.ERROR)
                    } else {
                        val evaluatedType = expression.getType(expression.localToitResolveScope)
                        val returnStructure = returnType.resolve()
                        if (returnStructure != null) {
                            if (!evaluatedType.isAssignableTo(returnStructure)) {
                                holder.registerProblem(toitReturn, "Wrong return type. " + evaluatedType.structure.getName() + " is not assignable to " + returnStructure.getName())
                            }
                        }
                    }
                } else if (noReturnValue && expression != null) {
                    holder.registerProblem(expression, "Function does not return a value, but one was provided")
                }
                result[0] = true
            }

            override fun visit(toitIf: ToitIf) {
                val blocks = toitIf.getChildrenOfType(ToitBlock::class.java)
                val expressions = toitIf.getChildrenOfType(ToitExpression::class.java)
                if (blocks.size > expressions.size) {
                    // There is an else without condition
                    var res = true
                    for (toitBlock in blocks) {
                        res = res && checkReturn(toitBlock, holder, returnType, noReturnValue)
                    }
                    if (res) result[0] = true
                }
            }

            override fun visit(toitTry: ToitTry) {
                val toitBlock = toitTry.getFirstChildOfType(ToitBlock::class.java)
                if (toitBlock != null && checkReturn(toitBlock, holder, returnType, noReturnValue)) result[0] = true
                super.visit(toitTry)
            }

            override fun visit(toitWhile: ToitWhile) {
                val expression = toitWhile.getFirstChildOfType(ToitExpression::class.java)
                if (expression == null || expression.text.trim { it <= ' ' } != "true") {
                    val toitBlock = toitWhile.getFirstChildOfType(ToitBlock::class.java)
                    if (toitBlock != null && checkReturn(toitBlock, holder, returnType, noReturnValue)) result[0] = true
                } else result[0] = true
            }

            // TODO: Needs to use the body of functions to determine if it throws by scanning for __throw__ instead of checking for the function names in exceptions.toit
            override fun visit(toitExpression: ToitExpression) {
                if (toitExpression.getChildren().size > 0 && toitExpression.getChildren()[0] is ToitExpression) {
                    (toitExpression.getChildren()[0] as ToitExpression).accept<Any>(object : ToitExpressionVisitor<Any?>() {
                        override fun visit(toitCallExpression: ToitCallExpression): Any? {
                            val function = toitCallExpression.getFunction()
                            if (function != null) {
                                val name = function.getName()
                                if (name == "throw" || name == "rethrow") result[0] = true
                            }
                            for (toitBlock in toitCallExpression.getChildrenOfType<ToitBlock>(ToitBlock::class.java)) {
                                if (checkReturn(toitBlock, holder, returnType, noReturnValue)) result[0] = true
                            }
                            return null
                        }

                        override fun visit(toitPrimaryExpression: ToitPrimaryExpression): Any? {
                            val ref = toitPrimaryExpression.getFirstChildOfType(ToitReferenceIdentifier::class.java)
                            if (ref != null && ref.getName() == "unreachable") result[0] = true
                            if (toitPrimaryExpression.getFirstChildOfType(ToitPrimitive::class.java) != null) result[0] = true
                            return null
                        }
                    })
                    super.visit(toitExpression)
                }
            }
        })
        return result[0]
    }

    private fun checkStaticAbstract(toitFunction: ToitFunction, holder: ProblemsHolder) {
        val hasBody = !toitFunction.getChildrenOfType(ToitBlock::class.java).isEmpty()
        if (toitFunction.getParent() is ToitFile) {
            if (toitFunction.isStatic) holder.registerProblem(toitFunction, ToitBaseStubableElement.getRelativeRangeInParent(toitFunction.static), "Top level functions cannot be static")
            if (!hasBody) holder.registerProblem(toitFunction.getFunctionName(), "Missing body")
        } else {
            val parent = toitFunction.getParentOfType(ToitStructure::class.java)
            val functionName = toitFunction.getFunctionName() ?: return
            if (parent.isClass) {
                val isAbstractClass = parent.isAbstract()
                if (toitFunction.isAbstract && !isAbstractClass) holder.registerProblem(toitFunction, ToitBaseStubableElement.getRelativeRangeInParent(toitFunction.abstract), "Abstract functions not allowed in non-abstract class")
                if (!toitFunction.isAbstract && !hasBody) {
                    holder.registerProblem(functionName, "Missing body")
                }
            }
            if (parent.isInterface) {
                if (hasBody && !toitFunction.isStatic) holder.registerProblem(functionName, "Only static interface methods may have a body")
            }
        }
    }
}
