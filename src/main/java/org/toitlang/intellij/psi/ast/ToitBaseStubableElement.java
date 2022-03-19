package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitPsiHelper;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.visitor.ToitVisitableElement;

import java.util.ArrayList;
import java.util.List;

public abstract class ToitBaseStubableElement<T extends StubElement> extends ToitVisitableElement<T> {
    protected final static TokenSet ABSTRACT = TokenSet.create(ToitTypes.ABSTRACT);
    protected final static TokenSet STATIC = TokenSet.create(ToitTypes.STATIC);
    protected final static TokenSet STAR_SET = TokenSet.create(ToitTypes.STAR);

    public ToitBaseStubableElement(@NotNull ASTNode node) {
        super(node);
    }

    public ToitBaseStubableElement(@NotNull T stub, @NotNull IStubElementType nodeType) {
        super(stub, nodeType);
    }

    // Utility function
    protected final boolean hasToken(TokenSet set) {
        return getNode().getChildren(set).length != 0;
    }

    protected final ASTNode firstChildToken(TokenSet set) {
        var children = getNode().getChildren(set);
        if (children.length > 0) return children[0];
        return null;
    }

    public final <T> List<T> childrenOfType(Class<T> clazz) {
        return ToitPsiHelper.childrenOfType(this, clazz);
    }

    public final <T> T getParentOfType(Class<T> clazz) {
        var p = getParent();
        while (p != null && !clazz.isInstance(p)) p = p.getParent();
        return clazz.cast(p);
    }

    protected static TextRange getRelativeRangeInParent(ASTNode node) {
        return new TextRange(node.getStartOffsetInParent(), node.getStartOffsetInParent() + node.getTextLength());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
