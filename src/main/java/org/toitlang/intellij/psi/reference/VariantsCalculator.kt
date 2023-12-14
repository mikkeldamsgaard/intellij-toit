package org.toitlang.intellij.psi.reference

import com.intellij.openapi.vfs.VirtualFile
import org.toitlang.intellij.ToitFileType
import org.toitlang.intellij.files.ToitPackageHandler
import org.toitlang.intellij.files.ToitSdkFiles.Util.getSdkRoot
import org.toitlang.intellij.psi.ToitFile
import org.toitlang.intellij.psi.ToitTypes
import org.toitlang.intellij.psi.ast.*
import org.toitlang.intellij.psi.ast.ToitStructure.StaticScope
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor
import org.toitlang.intellij.psi.scope.ToitScope
import java.util.stream.Collectors

class VariantsCalculator private constructor(source: ToitReferenceIdentifier, evaluationScope: ToitScope) {
    private val source: ToitReferenceIdentifier?
    private val variants: MutableSet<Any?>
    private val scope: ToitScope

    init {
        this.source = source
        scope = evaluationScope
        variants = HashSet()
    }

    private fun build(): VariantsCalculator {
        if (source == null) return this
        val sType = source.getNode().elementType
        if (sType === ToitTypes.REFERENCE_IDENTIFIER) {
            val parent = source.getParent()
            if (parent is ToitDerefExpression) {
                val previousExpression = parent.prevSibling ?: return this
                if (previousExpression is ToitExpression) {
                    previousExpression.getReferenceTargets(scope).targets.forEach {target ->
                        val type = target.target.evaluatedType
                        if (type.file != null) {
                            variants.addAll(type.file.toitFileScope.exportedScope.asVariant())
                            if (isTypeSelectingRelationalExpression(parent)) filterVariants(ToitStructure::class.java, ToitFile::class.java)
                        } else if (type.structure != null) {
                            if (type.isStatic) {
                                variants.addAll(type.structure.getScope(StaticScope.STATIC, ToitScope.ROOT).asVariant())
                                variants.addAll(type.structure.getScope(StaticScope.FACTORY, ToitScope.ROOT).asVariant())
                            } else {
                                var structureScope = type.structure.getScope(StaticScope.INSTANCE, ToitScope.ROOT)
                                val functionParent = source.getParentOfType(ToitFunction::class.java)
                                if (functionParent != null && functionParent.isConstructor()) {
                                    structureScope = type.structure.getScope(StaticScope.FACTORY, structureScope)
                                }
                                variants.addAll(structureScope.asVariant())
                            }
                            if (isTypeSelectingRelationalExpression(parent)) variants.clear()
                        }
                    }
                }
            } else if (parent is ToitPrimaryExpression) {
                variants.addAll(scope.asVariant())
                if (isTypeSelectingRelationalExpression(parent)) filterVariants(ToitFile::class.java, ToitStructure::class.java)
                if (isInCodeBlockWithoutParameters(parent)) variants.add("it")
            }
        } else if (sType === ToitTypes.TYPE_IDENTIFIER) {
            val prevSib = source.getPrevSiblingOfType(ToitReferenceIdentifier::class.java)
            if (prevSib != null) {
                val prevRef = prevSib.reference.resolve()
                if (prevRef is ToitFile) {
                    variants.addAll(prevRef.toitFileScope.exportedScope.asVariant().stream()
                            .filter { v: ToitReferenceTarget? -> v is ToitStructure }
                            .collect(Collectors.toList()))
                }
            } else {
                variants.addAll(scope.asVariant().stream()
                        .filter { v: ToitReferenceTarget? -> v is ToitStructure || v is ToitFile }
                        .collect(Collectors.toList()))
            }
        } else if (sType === ToitTypes.IMPORT_SHOW_IDENTIFIER) {
            val import_ = source.getParentOfType(ToitImportDeclaration::class.java)
            var last: ToitReferenceIdentifier? = null
            for (toitReferenceIdentifier in import_.getChildrenOfType(ToitReferenceIdentifier::class.java)) {
                if (toitReferenceIdentifier.getNode().elementType === ToitTypes.IMPORT_IDENTIFIER) last = toitReferenceIdentifier
            }
            if (last != null) {
                val resolved = last.reference.resolve()
                if (resolved is ToitFile) {
                    variants.addAll(resolved.toitFileScope.exportedScope.asVariant())
                }
            }
        } else if (sType === ToitTypes.EXPORT_IDENTIFIER) {
            val toitScope = source.toitFile.toitFileScope.getToitScope(ToitScope.ROOT)
            variants.addAll(toitScope.asVariant().stream()
                    .filter { v: ToitReferenceTarget? -> v !is ToitFile }
                    .collect(Collectors.toList()))
            variants.add("*")
        } else if (sType === ToitTypes.NAMED_ARGUMENT_IDENTIFIER) {
            val call = source.getParentOfType(ToitCallExpression::class.java)
            for (function in call.getFunctions()) {
                variants.addAll(function.getChildrenOfType(ToitParameterName::class.java)
                        .stream().map { obj: ToitParameterName -> obj.getName() }
                        .collect(Collectors.toList()))
            }
        } else if (sType === ToitTypes.IMPORT_IDENTIFIER) {
            val import_ = source.getParentOfType(ToitImportDeclaration::class.java)
            val path: MutableList<ToitReferenceIdentifier> = ArrayList()
            for (tr in import_.getChildrenOfType<ToitReferenceIdentifier>(ToitReferenceIdentifier::class.java)) {
                if (tr === source) break
                if (tr.getNode().elementType === ToitTypes.IMPORT_IDENTIFIER) path.add(tr)
            }
            var curDir: VirtualFile?
            if (import_.getPrefixDots() != 0) {
                var prefixDots = import_.getPrefixDots()
                curDir = source.toitFile.getVirtualFile()
                while (prefixDots-- > 0 && curDir != null) {
                    curDir = curDir.parent
                }
                variants.add(".")
            } else {
                curDir = getSdkRoot(source.toitFile)
            }
            if (curDir != null) {
                for (toitReferenceIdentifier in path) {
                    curDir = curDir!!.findFileByRelativePath(toitReferenceIdentifier.getText())
                    if (curDir == null) break
                }
                curDir?.let { addImportVariantInDir(it) }
            }

            // Packages
            if (path.isEmpty()) {
                variants.addAll(ToitPackageHandler.listPrefixes(source.toitFile))
            } else {
                val packageSourceDir = ToitPackageHandler.listVariantsForPackage(source.toitFile,
                        path.stream().map { obj: ToitReferenceIdentifier -> obj.getText() }.collect(Collectors.toList()))
                packageSourceDir?.let { addImportVariantInDir(it) }
            }
        }
        return this
    }

    private fun addImportVariantInDir(curDir: VirtualFile) {
        val children = curDir.children
        if (children != null) {
            for (child in children) {
                if (child.isDirectory) variants.add(child.name) else if (child.getExtension() == ToitFileType.INSTANCE.defaultExtension) variants.add(child.nameWithoutExtension)
            }
        }
    }

    private fun isTypeSelectingRelationalExpression(expression: ToitExpression): Boolean {
        return expression.getParent() is ToitRelationalExpression &&
                (expression.getParent() as ToitRelationalExpression).isTypeSelectingOperator
    }

    private fun isInCodeBlockWithoutParameters(expression: ToitExpression): Boolean {
        val block = expression.getParentOfType(ToitBlock::class.java) ?: return false
        return if (block.getParent() is ToitFunction || block.getParent() is ToitStructure) false else block.getChildrenOfType(ToitParameterName::class.java).isEmpty()
    }

    private fun filterVariants(vararg classes: Class<*>) {
        val iterator = variants.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            var found = false
            for (clazz in classes) {
                if (clazz.isInstance(next)) {
                    found = true
                    break
                }
            }
            if (!found) iterator.remove()
        }
    }

    private fun getVariants(): Array<Any> {
        return variants.stream().map { o: Any? -> if (o is ToitFile) o.getPresentableText() else o }.toArray()
    }

    companion object {
        fun getVariants(source: ToitReferenceIdentifier, evaluationScope: ToitScope): Array<Any> {
            return VariantsCalculator(source, evaluationScope).build().getVariants()
        }
    }
}
