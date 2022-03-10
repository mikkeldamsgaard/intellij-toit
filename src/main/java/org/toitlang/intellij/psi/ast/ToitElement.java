package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.visitor.ToitVisitableElement;

import java.util.ArrayList;
import java.util.List;

public abstract class ToitElement extends ToitVisitableElement {
    public ToitElement(@NotNull ASTNode node) {
        super(node);
    }

    // Utility function
    protected final boolean hasToken(TokenSet set) {
        return getNode().getChildren(set).length != 0;
    }

    protected final <T> List<T> childrenOfType(Class<T> clazz) {
        List<T> result = new ArrayList<>();
        for (var c = getFirstChild(); c != null; c = c.getNextSibling()) {
            if (clazz.isInstance(c)) {
                result.add(clazz.cast(c));
            }
        }
        return result;
    }

}
