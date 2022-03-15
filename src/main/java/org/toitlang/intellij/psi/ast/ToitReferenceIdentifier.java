// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.files.ToitSdkFiles;
import org.toitlang.intellij.psi.ToitElementFactory;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.expression.ToitExpressionTypeEvaluator;
import org.toitlang.intellij.psi.reference.ReferenceCalculation;
import org.toitlang.intellij.psi.reference.ToitReferenceBase;
import org.toitlang.intellij.psi.scope.ToitFileScope;
import org.toitlang.intellij.psi.scope.ToitLocalScopeCalculator;
import org.toitlang.intellij.psi.visitor.ToitVisitor;
import org.toitlang.intellij.utils.ToitScope;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ToitReferenceIdentifier extends ToitIdentifier {
    ToitReferenceBase reference;
    public ToitReferenceIdentifier(@NotNull ASTNode node) {
        super(node);
        reference = new ToitReferenceBase(this);
    }

    @Override
    protected void accept(ToitVisitor visitor) {
        visitor.visit(this);
    }

    public PsiElement setName(String newElementName) {
        if (getNode().getElementType() == ToitTypes.REFERENCE_IDENTIFIER)
            return replace(ToitElementFactory.createReferenceIdentifier(getProject(),newElementName));
        else if (getNode().getElementType() == ToitTypes.TYPE_IDENTIFIER)
            return replace(ToitElementFactory.createTypeIdentifier(getProject(),newElementName));

        return null;
    }

    @Override
    public @NotNull ToitReferenceBase getReference() {
        return reference;
    }


    public ReferenceCalculation calculateReference(ToitFileScope toitFileScope) {
        ToitScope fileScope = toitFileScope.getScopeForElementsInFile(ToitSdkFiles.coreClosure(toitFileScope.getToitFile().getProject()));
        var calc = new ReferenceCalculation();
        calc.setReference(this);

        if (getNode().getElementType() == ToitTypes.TYPE_IDENTIFIER) {
            var prevSib = getPrevSibling();
            if (prevSib != null) {
                List<ToitReferenceIdentifier> refs = getParentOfType(ToitType.class).childrenOfType(ToitReferenceIdentifier.class);
                int idx = refs.indexOf(this);
                if (idx == 1) {
                    var prevRef = refs.get(0).getReference().resolve();
                    if (prevRef instanceof ToitFile) {
                        var elm = ((ToitFile) prevRef).getToitFileScope().getExportedScope().resolve(getName());
                        recordResolveResult(elm, calc);
                        calc.getDependencies().add(prevRef);
                    }
                }
            } else {
                List<PsiElement> resolved = fileScope.resolve(getName());
                recordResolveResult(resolved, calc);
            }
//            if (getPrevSibling())
//
//            ToitType parent = getParentOfType(ToitType.class);
//            var siblings = parent.childrenOfType(ToitReferenceIdentifier.class);
//            int position = siblings.indexOf(this);
//            StringBuilder scopeIdentifier = new StringBuilder();
//            for (int i=0;i<position;i++) {
//                scopeIdentifier.append(siblings.get(i).getName()).append(".");
//            }
//            scopeIdentifier.append(getName());
//            List<PsiElement> resolved = fileScope.resolve(scopeIdentifier.toString());
//            recordResolveResult(resolved, calc);
        } else if (getNode().getElementType() == ToitTypes.IMPORT_SHOW_IDENTIFIER || getNode().getElementType() == ToitTypes.EXPORT_IDENTIFIER) {
            List<PsiElement> resolved = fileScope.resolve(getName());
            recordResolveResult(resolved, calc);
        } else if (getNode().getElementType() == ToitTypes.IMPORT_IDENTIFIER) {
            var importDecl = getParentOfType(ToitImportDeclaration.class);
            var imports = importDecl.childrenOfType(ToitReferenceIdentifier.class).stream().filter(ToitReferenceIdentifier::isImport).collect(Collectors.toList());
            if (importDecl.hasShow() || importDecl.hasAs() || importDecl.getPrefixDots() > 0) {
                String fqn = "$"+importDecl.getPrefixDots()+"$"+imports.stream().map(ToitIdentifier::getName).collect(Collectors.joining("."));
                var toitFile = toitFileScope.getImportedFile(fqn);
                if (toitFile != null) {
                    recordResolveResult(Collections.singletonList(toitFile),calc);
                }
            } else {
                var last = imports.get(imports.size()-1);
                List<PsiElement> resolved = fileScope.resolve(last.getName());
                recordResolveResult(resolved, calc);
            }
            // Todo: add to variants
        } else if (getNode().getElementType() == ToitTypes.REFERENCE_IDENTIFIER) {
            ToitScope localScope = ToitLocalScopeCalculator.calculate(this, fileScope);
            ToitExpressionTypeEvaluator expressionTypeEvaluator = new ToitExpressionTypeEvaluator(localScope);
            var types = getParentOfType(ToitExpression.class).accept(expressionTypeEvaluator);
            recordResolveResult(types, calc);
            calc.setVariants(expressionTypeEvaluator.getVariants());
        }

        return calc;
    }

    private void recordResolveResult(Collection<PsiElement> resolved, ReferenceCalculation calc) {
        if (resolved != null) {
            calc.setReferencedValue(resolved);
            calc.getDependencies().addAll(resolved);
        }
    }

}
