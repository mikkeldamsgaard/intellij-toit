package org.toitlang.intellij.psi.reference;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.files.ToitSdkFiles;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.ast.*;
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
        throw new IncorrectOperationException("Rebind cannot be performed for " + getClass());
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
        ToitScope localScope = ToitLocalScopeCalculator.calculate(source, toitFileScopeScope);
        var scope = ToitScope.chain(localScope, toitFileScopeScope, core);

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
                List<ToitReferenceIdentifier> refs = source.getParentOfType(ToitType.class).childrenOfType(ToitReferenceIdentifier.class);
                int idx = refs.indexOf(source);
                if (idx == 1) {
                    var prevRef = refs.get(0).getReference().resolve();
                    if (prevRef instanceof ToitFile) {
                        ToitScope exportedScope = ((ToitFile) prevRef).getToitFileScope().getExportedScope();
                        var elm = exportedScope.resolve(name);
                        destinations.addAll(elm);
                    }
                }
            } else {
                if ("none".equals(name) || "any".equals(name)) {
                    soft = true;
                } else {
                    destinations.addAll(scope.resolve(name));
                }
            }
        } else if (sType == ToitTypes.IMPORT_SHOW_IDENTIFIER || sType == ToitTypes.EXPORT_IDENTIFIER) {
            destinations.addAll(scope.resolve(name));
        } else if (sType == ToitTypes.BREAK_CONTINUE_LABEL_IDENTIFIER) {
            soft = true;
        } else if (sType == ToitTypes.IMPORT_IDENTIFIER) {
            var importDecl = source.getParentOfType(ToitImportDeclaration.class);
            var imports = importDecl.childrenOfType(ToitReferenceIdentifier.class).stream().filter(ToitReferenceIdentifier::isImport).collect(Collectors.toList());
            if (importDecl.hasShow() || importDecl.isShowStar() || importDecl.hasAs() || importDecl.getPrefixDots() > 0) {
                String fqn = "$"+importDecl.getPrefixDots()+"$"+imports.stream().map(ToitIdentifier::getName).collect(Collectors.joining("."));
                var toitFile = scope.getImportedLibrary(fqn);
                if (toitFile != null) destinations.add(toitFile);
            } else {
                var last = imports.get(imports.size()-1);
                List<PsiElement> resolved = scope.resolve(last.getName());
                destinations.addAll(resolved);
            }
            // Todo: add to variants
        } else if (sType == ToitTypes.REFERENCE_IDENTIFIER) {
            source.getExpressionParent().accept(new ToitExpressionVisitor<>() {
                @Override
                public Object visit(ToitDerefExpression toitDerefExpression) {
                    var prevType = ((ToitExpression) toitDerefExpression.getPrevSibling()).getType(scope.getScope());
                    if (prevType.isUnresolved()) {
                        soft= true;
                    } else if (prevType.getFile() != null) {
                        destinations.addAll(prevType.getFile().getToitFileScope().getToitScope().resolve(name));
                    } else if (prevType.getStructure() != null) {
                        destinations.addAll(prevType.getStructure().getScope(prevType.isStatic()).resolve(name));
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
                    if (!toitPrimaryExpression.childrenOfType(ToitPrimitive.class).isEmpty()) {
                        soft = true;
                    }
                    return visitExpression(toitPrimaryExpression);
                }
            });
        }



        return this;
    }

    public static ToitReference create(ToitReferenceIdentifier source) {
        return new ToitReference(source).build();
    }
}
