package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;

public abstract class ToitElementBase extends ToitBaseStubableElement<StubElement<ToitElementBase>> {
    public ToitElementBase(@NotNull ASTNode node) {
        super(node);
    }
}
