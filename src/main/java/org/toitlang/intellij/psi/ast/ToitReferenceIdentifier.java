// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitElementFactory;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.expression.ToitExpressionTypeEvaluator;
import org.toitlang.intellij.psi.reference.ReferenceCalculation;
import org.toitlang.intellij.psi.reference.ToitReferenceBase;
import org.toitlang.intellij.psi.scope.ToitLocalScopeCalculator;
import org.toitlang.intellij.psi.visitor.ToitVisitor;
import org.toitlang.intellij.utils.ToitScope;

import java.util.Collection;
import java.util.List;

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

    public ReferenceCalculation calculateReference(ToitScope fileScope) {
        var calc = new ReferenceCalculation();
        calc.setReference(this);

        if (getNode().getElementType() == ToitTypes.TYPE_IDENTIFIER) {
            ToitType parent = getParentOfType(ToitType.class);
            var siblings = parent.childrenOfType(ToitReferenceIdentifier.class);
            int position = siblings.indexOf(this);
            StringBuilder scopeIdentifier = new StringBuilder();
            for (int i=0;i<position;i++) {
                scopeIdentifier.append(siblings.get(i).getName()).append(".");
            }
            scopeIdentifier.append(getName());
            List<PsiElement> resolved = fileScope.resolve(scopeIdentifier.toString());
            recordResolveResult(resolved, calc);
        } else if (getNode().getElementType() == ToitTypes.IMPORT_SHOW_IDENTIFIER || getNode().getElementType() == ToitTypes.EXPORT_IDENTIFIER) {
            List<PsiElement> resolved = fileScope.resolve(getName());
            recordResolveResult(resolved, calc);
        } else if (getNode().getElementType() == ToitTypes.IMPORT_IDENTIFIER) {
            ToitReferenceIdentifier last = this;
            while (true) {
                var next = getNextSibling();
                if (!(next instanceof ToitReferenceIdentifier)) break;
                var nextIdent = (ToitReferenceIdentifier)next;
                if (nextIdent.getNode().getElementType() != ToitTypes.IMPORT_IDENTIFIER) break;
            }

            List<PsiElement> resolved = fileScope.resolve(last.getName());
            recordResolveResult(resolved, calc);
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
