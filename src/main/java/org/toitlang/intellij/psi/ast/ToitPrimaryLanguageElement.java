package org.toitlang.intellij.psi.ast;

import com.intellij.icons.AllIcons;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.ui.RowIcon;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.model.IToitPrimaryLanguageElement;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.stub.ToitStubElement;

import javax.swing.*;

public abstract class ToitPrimaryLanguageElement<S extends PsiElement, T extends ToitStubElement<S>>
        extends ToitBaseStubableElement<T>
        implements IToitPrimaryLanguageElement, StubBasedPsiElement<T> {
    public ToitPrimaryLanguageElement(@NotNull T stub, @NotNull IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public ToitPrimaryLanguageElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        var stub = getStub();
        if (stub != null) return stub.getName();

        var nIdent = getNameIdentifier();
        if (nIdent != null) return nIdent.getName();
        return null;
    }

    public boolean isPrivate() {
        return getName() != null && getName().endsWith("_");
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        var nIdent = getNameIdentifier();
        if (nIdent != null) return nIdent.setName(name);
        return this;
    }

    @Override
    public  ToitNameableIdentifier getNameIdentifier() {
        var names = getChildrenOfType(ToitNameableIdentifier.class);
        if (names.isEmpty()) return null;
        return names.get(0);
    }

    @Override
    public @Nullable Icon getIcon(boolean unused) {
        return new RowIcon(getElementTypeIcon(), isPrivate()?AllIcons.Nodes.C_private:AllIcons.Nodes.C_public);
    }

    @NotNull
    protected abstract Icon getElementTypeIcon();

    public boolean isTopLevel() {
        return getParent() instanceof ToitFile;
    }

    public PsiElement getProblemIdentifier() {
        var name = getNameIdentifier();
        if (name != null) return name;
        return this;
    }
}
