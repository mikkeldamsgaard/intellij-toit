// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.icons.AllIcons;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.stubs.IStubElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.calls.FunctionSignature;
import org.toitlang.intellij.psi.stub.ToitVariableDeclarationStub;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import javax.swing.*;
import java.util.Collections;

public class ToitVariableDeclaration extends ToitPrimaryLanguageElement<ToitVariableDeclaration, ToitVariableDeclarationStub> {
    public ToitVariableDeclaration(@NotNull ASTNode node) {
        super(node);
    }

    public ToitVariableDeclaration(@NotNull ToitVariableDeclarationStub stub, @NotNull IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @Override
    protected void accept(ToitVisitor visitor) {
        visitor.visit(this);
    }

    public boolean isStatic() {
        var stub = getStub();
        if (stub != null) return stub.isStatic();

        return getNode().getChildren(STATIC).length > 0;
    }


    // TODO: Stub this
    public ToitType getType() {
        for (ToitType toitType : getChildrenOfType(ToitType.class)) {
            return toitType;
        }

        return null;
    }

    @Override
    protected @NotNull Icon getElementTypeIcon() {
        return AllIcons.Nodes.Field;
    }

    @Override
    public @NlsSafe @Nullable String getPresentableText() {
        return getName();
    }

    public boolean isGlobal() {
        return getParent() instanceof ToitFile;
    }

    public boolean isField() {
        return getParent().getParent() instanceof ToitStructure;
    }

    public boolean isConstant() {
        for (ToitOperator toitOperator : getChildrenOfType(ToitOperator.class)) {
            return toitOperator.isConstDeclare();
        }
        return false;
    }

    public boolean isReturnTypeNullable() {
        var type = getType();
        if (type == null) return true;
        return type.getNextSibling() != null && type.getNextSibling().getNode().getElementType() == ToitTypes.QUESTION;
    }

    public FunctionSignature getGetterSignature() {
        return new FunctionSignature(getName(), Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());
    }
}
