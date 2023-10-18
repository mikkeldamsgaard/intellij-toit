package org.toitlang.intellij.psi.reference;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.files.ToitSdkFiles;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.calls.ToitCallHelper;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.scope.ToitFileScope;
import org.toitlang.intellij.psi.scope.ToitLocalScopeCalculator;
import org.toitlang.intellij.psi.scope.ToitScope;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ToitReference implements PsiPolyVariantReference {
    private final ToitReferenceIdentifier source;
    private final List<PsiElement> destinations;
    @Getter
    private final List<PsiElement> dependencies;
    boolean soft;

    private ToitReference(ToitReferenceIdentifier source) {
        this.source = source;
        destinations = new ArrayList<>();
        dependencies = new ArrayList<>();
        soft = false;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return VariantsCalculator.getVariants(source, createEvaluationScope());
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        return destinations.stream().map(PsiElementResolveResult::new).toArray(ResolveResult[]::new);
    }

    @Override
    public @Nullable PsiElement resolve() {
        var res = multiResolve(false);
        if (res.length > 0) return res[0].getElement();
        return null;
    }

    @Override
    public @NotNull PsiElement getElement() {
        return source;
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        return new TextRange(0,source.getTextLength());
    }

    @Override
    public @NotNull @NlsSafe String getCanonicalText() {
        return source.getName();
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        return source.setName(newElementName);
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        destinations.clear();
        destinations.add(element);
        dependencies.add(element);
        return element;
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        return getElement().getManager().areElementsEquivalent(resolve(), element);
    }

    @Override
    public boolean isSoft() {
        return soft;
    }

    private EvaluationScope createEvaluationScope() {
        ToitFile file = source.getToitFile();
        ToitFileScope toitFileScope = file.getToitFileScope();
        ToitScope core = ToitSdkFiles.getCoreScope(source.getProject());
        ToitScope toitFileScopeScope = toitFileScope.getToitScope();
        ToitScope localScope = ToitLocalScopeCalculator.calculate(source);
        var scope = ToitScope.chain(source+"-"+source.getName()+"-eval",localScope, toitFileScopeScope, core);

        return new EvaluationScope(scope, toitFileScope);
    }


    private ToitReference build() {
        EvaluationScope scope = createEvaluationScope();
        String name = source.getName();
        dependencies.add(source);
        IElementType sType = source.getNode().getElementType();
        if (sType == ToitTypes.TYPE_IDENTIFIER) {
            var prevSib = source.getPrevSibling();
            if (prevSib != null) {
                List<ToitReferenceIdentifier> refs = source.getParentOfType(ToitType.class).getChildrenOfType(ToitReferenceIdentifier.class);
                int idx = refs.indexOf(source);
                if (idx == 1) {
                    var prevRefs = refs.get(0).getReference().multiResolve(false);
                    for (ResolveResult prevRef : prevRefs) {
                        if (prevRef.getElement() instanceof ToitFile) {
                            ToitScope exportedScope = ((ToitFile) prevRef.getElement()).getToitFileScope().getExportedScope();
                            var elm = exportedScope.resolve(name);
                            destinations.addAll(elm);
                        }
                    }
                }
            } else {
                if ("none".equals(name) || "any".equals(name)) {
                    soft = true;
                } else {
                    destinations.addAll(source.getToitResolveScope().resolve(name));
                }
            }
        } else if (sType == ToitTypes.IMPORT_SHOW_IDENTIFIER || sType == ToitTypes.EXPORT_IDENTIFIER) {
            destinations.addAll(scope.resolve(name));
        } else if (sType == ToitTypes.BREAK_CONTINUE_LABEL_IDENTIFIER) {
            soft = true;
        } else if (sType == ToitTypes.IMPORT_IDENTIFIER) {
            var importDecl = source.getParentOfType(ToitImportDeclaration.class);
            var imports = importDecl.getChildrenOfType(ToitReferenceIdentifier.class).stream().filter(ToitReferenceIdentifier::isImport).collect(Collectors.toList());
            if (importDecl.hasShow() || importDecl.isShowStar() || importDecl.hasAs() || importDecl.getPrefixDots() > 0) {
                String fqn = "$" + importDecl.getPrefixDots() + "$" + imports.stream().map(ToitIdentifier::getName).collect(Collectors.joining("."));
                var toitFile = scope.getImportedLibrary(fqn);
                if (toitFile != null) destinations.add(toitFile);
            } else {
                var last = imports.get(imports.size() - 1);
                List<PsiElement> resolved = scope.resolve(last.getName());
                destinations.addAll(resolved);
            }
        } else if (sType == ToitTypes.NAMED_ARGUMENT_IDENTIFIER) {
            var call = source.getParentOfType(ToitCallExpression.class);
            var resolved = ToitCallHelper.resolveCall(call);
            if (resolved != null) {
                var namedParameter = resolved.getToitFunction().getChildrenOfType(ToitParameterName.class);
                for (ToitParameterName toitParameterName : namedParameter) {
                    if (ToitIdentifier.compareIgnoreUnderscoreMinus(source.getName().trim(), toitParameterName.getName())) {
                        ToitIdentifier identifier = toitParameterName.getNameIdentifier();
                        if (identifier instanceof ToitReferenceIdentifier) {
                            destinations.addAll(((ToitReferenceIdentifier)identifier).getReference().destinations);
                        } else if (identifier instanceof ToitNameableIdentifier) {
                            destinations.add(toitParameterName);
                        }
                    }
                }
            } else {
                soft = true;
            }
        } else if (sType == ToitTypes.REFERENCE_IDENTIFIER) {
            var expressionParent = source.getExpressionParent();
            if (expressionParent == null) {
                // This is construction parameters
                var structure = source.getParentOfType(ToitStructure.class);
                if (structure != null) {
                    var resolved = structure.getScope(false).resolve(name);
                    destinations.addAll(resolved.stream()
                        .filter(ref -> ref instanceof ToitVariableDeclaration)
                        .collect(Collectors.toList()));
                }
            } else {
                expressionParent.accept(new ToitExpressionVisitor<>() {
                    @Override
                    public Object visit(ToitDerefExpression toitDerefExpression) {
                        var postfixEvaluatedType =
                                ToitPostfixExpressionTypeEvaluatedType
                                        .calculate(toitDerefExpression.getParentOfType(ToitPostfixExpression.class));
                        var prev = postfixEvaluatedType.getTypeForPreviousChild(toitDerefExpression);

                        if (prev == null || prev.isUnresolved()) {
                            soft = true;
                        } else if (prev.getFile() != null) {
                            destinations.addAll(prev.getFile().getToitFileScope().getExportedScope().resolve(name));
                        } else if (prev.getStructure() != null) {
                            // Special case for setters
                            boolean isPotentialSetterCall = ToitCallHelper.isPotentialSetterCall(toitDerefExpression);
                            for (PsiElement psiElement : prev.getStructure().getScope(prev.isStatic()).resolve(name)) {
                                if (psiElement instanceof ToitFunction) {
                                    if (isPotentialSetterCall && ((ToitFunction) psiElement).isSetter()) {
                                        destinations.add(psiElement);
                                    } else if (!isPotentialSetterCall && !((ToitFunction) psiElement).isSetter()) {
                                        destinations.add(psiElement);
                                    }
                                } else {
                                    destinations.add(psiElement);
                                }
                            }
                        }
                        return null;
                    }

                    @Override
                    public Object visitExpression(ToitExpression expression) {
                        destinations.addAll(scope.resolve(name));
                        if (name.equals("it")) soft = true;
                        return null;
                    }

                    @Override
                    public Object visit(ToitPrimaryExpression toitPrimaryExpression) {
                        if (!toitPrimaryExpression.getChildrenOfType(ToitPrimitive.class).isEmpty()) {
                            soft = true;
                        }
                        return visitExpression(toitPrimaryExpression);
                    }
                });
            }
        }

        return this;
    }

    public static ToitReference create(ToitReferenceIdentifier source) {
        return new ToitReference(source).build();
    }
}
