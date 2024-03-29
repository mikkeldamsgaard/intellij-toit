// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitElementFactory;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.reference.ToitReference;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

public class ToitReferenceIdentifier extends ToitIdentifier {
    public ToitReferenceIdentifier(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    protected void accept(ToitVisitor visitor) {
        visitor.visit(this);
    }

    public PsiElement setName(String newElementName) {
        if (getNode().getElementType() == ToitTypes.IMPORT_SHOW_IDENTIFIER)
            return replaceIdentifierToken(ToitElementFactory.createImportShowIdentifier(getProject(), newElementName));
        else if (getNode().getElementType() == ToitTypes.EXPORT_IDENTIFIER)
            return replaceIdentifierToken(ToitElementFactory.createExportIdentifier(getProject(), newElementName));
        else if (getNode().getElementType() == ToitTypes.IMPORT_IDENTIFIER)
            return replaceIdentifierToken(ToitElementFactory.createImportIdentifier(getProject(), newElementName));
        else if (getNode().getElementType() == ToitTypes.TYPE_IDENTIFIER)
            return replaceIdentifierToken(ToitElementFactory.createTypeIdentifier(getProject(), newElementName));
        else if (getNode().getElementType() == ToitTypes.REFERENCE_IDENTIFIER)
            return replaceIdentifierToken(ToitElementFactory.createReferenceIdentifier(getProject(), newElementName));
        else if (getNode().getElementType() == ToitTypes.BREAK_CONTINUE_LABEL_IDENTIFIER)
            return replaceIdentifierToken(ToitElementFactory.createBreakContinueLabelIdentifier(getProject(), newElementName));
        else if (getNode().getElementType() == ToitTypes.NAMED_ARGUMENT_IDENTIFIER) {
            if (getText().startsWith("no-")) newElementName = "no-"+newElementName;
            return replaceIdentifierToken(ToitElementFactory.createNamedArgumentIdentifier(getProject(), newElementName));
        }
        return null;
    }

    @Override
    public @NotNull ToitReference getReference() {
        return ToitReference.create(this);
    }

    public ToitExpression getExpressionParent() {
        return getParentOfType(ToitExpression.class);
    }

    public String getText() {
        var t = super.getText();
        if (t == null) return t;
        return t.trim();
    }
}
