package org.toitlang.intellij.psi.visitor;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;

public abstract class ToitVisitableElement extends StubBasedPsiElementBase<StubElement> {
    public ToitVisitableElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof ToitVisitor) {
            accept((ToitVisitor) visitor);
        } else {
            super.accept(visitor);
        }
    }

    protected abstract void accept(ToitVisitor visitor);
}
