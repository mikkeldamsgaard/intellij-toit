package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;

public abstract class ToitElement extends ToitBaseStubableElement<StubElement<ToitElement>> {
    public ToitElement(@NotNull ASTNode node) {
        super(node);
    }
}
