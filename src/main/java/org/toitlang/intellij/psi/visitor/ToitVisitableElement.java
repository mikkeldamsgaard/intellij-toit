package org.toitlang.intellij.psi.visitor;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitFile;

public abstract class ToitVisitableElement<T extends StubElement> extends StubBasedPsiElementBase<T> {
    public ToitVisitableElement(@NotNull ASTNode node) {
        super(node);
    }

    public ToitVisitableElement(@NotNull T stub, @NotNull IStubElementType nodeType) {
        super(stub, nodeType);
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

    public ToitFile getToitFile() {
        return (ToitFile)getContainingFile().getOriginalFile();
    }
}
