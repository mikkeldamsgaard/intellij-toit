package org.toitlang.intellij.psi.reference;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IElementType;
import org.toitlang.intellij.ToitFileType;
import org.toitlang.intellij.files.ToitPackageHandler;
import org.toitlang.intellij.files.ToitSdkFiles;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.ast.ToitStructure.StaticScope;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.scope.ToitScope;

import java.util.*;
import java.util.stream.Collectors;

public class VariantsCalculator {
    private final ToitReferenceIdentifier source;
    private final Set<Object> variants;
    private final ToitScope scope;

    private VariantsCalculator(ToitReferenceIdentifier source, ToitScope evaluationScope) {
        this.source = source;
        scope = evaluationScope;
        this.variants = new HashSet<>();
    }

    private VariantsCalculator build() {
        if (source == null) return this;
        IElementType sType = source.getNode().getElementType();

        if (sType == ToitTypes.REFERENCE_IDENTIFIER) {
            ToitExpression expressionParent = source.getExpressionParent();
            if (expressionParent == null) return this;

            expressionParent.accept(new ToitExpressionVisitor<>() {
                @Override
                public Object visit(ToitDerefExpression toitDerefExpression) {
                    var previousExpression = toitDerefExpression.getPrevSibling();
                    if (previousExpression == null) return null;

                    var reference = previousExpression.getReference();
                    if (reference == null) return null;

                    for (ToitReferenceTarget target : ((ToitReference) reference).getDestinations()) {
                        ToitEvaluatedType evaluatedType = target.getEvaluatedType();
                        if (!evaluatedType.resolved()) continue;

                        if (evaluatedType.getFile() != null) {
                            variants.addAll(evaluatedType.getFile().getToitFileScope().getExportedScope().asVariant());
                            if (isTypeSelectingRelationalExpression(toitDerefExpression))
                                filterVariants(ToitStructure.class, ToitFile.class);
                        } else if (evaluatedType.getStructure() != null) {
                            if (evaluatedType.isStatic()) {
                                variants.addAll(evaluatedType.getStructure().getScope(StaticScope.STATIC, ToitScope.ROOT).asVariant());
                                variants.addAll(evaluatedType.getStructure().getScope(StaticScope.FACTORY, ToitScope.ROOT).asVariant());
                            } else {
                                var structureScope = evaluatedType.getStructure().getScope(StaticScope.INSTANCE, ToitScope.ROOT);
                                var functionParent = source.getParentOfType(ToitFunction.class);
                                if (functionParent != null && functionParent.isConstructor()) {
                                    structureScope = evaluatedType.getStructure().getScope(StaticScope.FACTORY, structureScope);
                                }

                                variants.addAll(structureScope.asVariant());
                            }
                            if (isTypeSelectingRelationalExpression(toitDerefExpression)) variants.clear();
                        }
                    }

                    return null;
                }

                @Override
                public Object visitExpression(ToitExpression expression) {
                    variants.addAll(scope.asVariant());
                    if (isTypeSelectingRelationalExpression(expression))
                        filterVariants(ToitFile.class, ToitStructure.class);
                    if (isInCodeBlockWithoutParameters(expression)) variants.add("it");
                    return null;
                }
            });
        } else if (sType == ToitTypes.TYPE_IDENTIFIER) {
            ToitReferenceIdentifier prevSib = source.getPrevSiblingOfType(ToitReferenceIdentifier.class);
            if (prevSib != null) {
                var prevRef = prevSib.getReference().resolve();
                if (prevRef instanceof ToitFile) {
                    variants.addAll(((ToitFile) prevRef).getToitFileScope().getExportedScope().asVariant().stream()
                            .filter(v -> v instanceof ToitStructure)
                            .collect(Collectors.toList()));
                }
            } else {
                variants.addAll(scope.asVariant().stream()
                        .filter(v -> v instanceof ToitStructure || v instanceof ToitFile)
                        .collect(Collectors.toList()));
            }
        } else if (sType == ToitTypes.IMPORT_SHOW_IDENTIFIER) {
            ToitImportDeclaration import_ = source.getParentOfType(ToitImportDeclaration.class);
            ToitReferenceIdentifier last = null;
            for (ToitReferenceIdentifier toitReferenceIdentifier : import_.getChildrenOfType(ToitReferenceIdentifier.class)) {
                if (toitReferenceIdentifier.getNode().getElementType() == ToitTypes.IMPORT_IDENTIFIER)
                    last = toitReferenceIdentifier;
            }
            if (last != null) {
                var resolved = last.getReference().resolve();
                if (resolved instanceof ToitFile) {
                    variants.addAll(((ToitFile) resolved).getToitFileScope().getExportedScope().asVariant());
                }
            }
        } else if (sType == ToitTypes.EXPORT_IDENTIFIER) {
            ToitScope toitScope = source.getToitFile().getToitFileScope().getToitScope(ToitScope.ROOT);
            variants.addAll(toitScope.asVariant().stream()
                    .filter(v -> !(v instanceof ToitFile))
                    .collect(Collectors.toList()));
            variants.add("*");
        } else if (sType == ToitTypes.NAMED_ARGUMENT_IDENTIFIER) {
            var call = source.getParentOfType(ToitCallExpression.class);
            for (ToitFunction function : call.getFunctions()) {
                variants.addAll(function.getChildrenOfType(ToitParameterName.class)
                        .stream().map(ToitParameterName::getName)
                        .collect(Collectors.toList()));
            }
        } else if (sType == ToitTypes.IMPORT_IDENTIFIER) {
            ToitImportDeclaration import_ = source.getParentOfType(ToitImportDeclaration.class);
            List<ToitReferenceIdentifier> path = new ArrayList<>();
            for (var tr : import_.getChildrenOfType(ToitReferenceIdentifier.class)) {
                if (tr == source) break;
                if (tr.getNode().getElementType() == ToitTypes.IMPORT_IDENTIFIER) path.add(tr);
            }

            VirtualFile curDir;
            if (import_.getPrefixDots() != 0) {
                int prefixDots = import_.getPrefixDots();
                curDir = source.getToitFile().getVirtualFile();
                while (prefixDots-->0 && curDir != null) {
                    curDir = curDir.getParent();
                }
                variants.add(".");
            } else {
                curDir = ToitSdkFiles.getSdkRoot(source.getProject());
            }

            if (curDir != null) {
                for (ToitReferenceIdentifier toitReferenceIdentifier : path) {
                    curDir = curDir.findFileByRelativePath(toitReferenceIdentifier.getText());
                    if (curDir == null) break;
                }

                if (curDir != null) {
                    addImportVariantInDir(curDir);
                }
            }

            // Packages
            if (path.isEmpty()) {
                variants.addAll(ToitPackageHandler.listPrefixes(source.getToitFile()));
            } else {
               VirtualFile packageSourceDir = ToitPackageHandler.listVariantsForPackage(source.getToitFile(),
                        path.stream().map(ToitReferenceIdentifier::getText).collect(Collectors.toList()));
               if (packageSourceDir != null) addImportVariantInDir(packageSourceDir);
            }
        }
        return this;
    }

    private void addImportVariantInDir(VirtualFile curDir) {
        VirtualFile[] children = curDir.getChildren();
        if (children != null) {
            for (VirtualFile child : children) {
                if (child.isDirectory()) variants.add(child.getName());
                else if (Objects.equals(child.getExtension(), ToitFileType.INSTANCE.getDefaultExtension()))
                    variants.add(child.getNameWithoutExtension());
            }
        }
    }

    private boolean isTypeSelectingRelationalExpression(ToitExpression expression) {
        return expression.getParent() instanceof ToitRelationalExpression &&
                ((ToitRelationalExpression) expression.getParent()).isTypeSelectingOperator();
    }

    private boolean isInCodeBlockWithoutParameters(ToitExpression expression) {
        ToitBlock block = expression.getParentOfType(ToitBlock.class);
        if (block == null) return false;
        if (block.getParent() instanceof ToitFunction || block.getParent() instanceof ToitStructure) return false;
        return block.getChildrenOfType(ToitParameterName.class).isEmpty();
    }

    @SuppressWarnings("rawtypes")
    private void filterVariants(Class... classes) {
        for (Iterator<Object> iterator = variants.iterator(); iterator.hasNext(); ) {
            Object next = iterator.next();
            boolean found = false;
            for (Class clazz : classes) {
                if (clazz.isInstance(next)) {
                    found = true;
                    break;
                }
            }
            if (!found) iterator.remove();
        }
    }

    private Object[] getVariants() {
        return variants.stream().map(o -> (o instanceof ToitFile)?((ToitFile)o).getPresentableText():o).toArray();
    }

    public static Object[] getVariants(ToitReferenceIdentifier source, ToitScope evaluationScope) {
        return new VariantsCalculator(source, evaluationScope).build().getVariants();
    }

}
