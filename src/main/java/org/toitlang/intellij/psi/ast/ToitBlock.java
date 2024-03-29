// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.scope.ToitScope;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.Collections;
import java.util.List;

public class ToitBlock extends ToitElementBase {

    public ToitBlock(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    protected void accept(ToitVisitor visitor) {
        visitor.visit(this);
    }

    public List<ToitParameterName> getParameters() {
        return getChildrenOfType(ToitParameterName.class);
    }

    public ToitScope getParameterScope(ToitScope parent) {
        ToitScope scope = parent.sub("param");
        var parameters = getParameters();
        parameters.forEach(p -> scope.add(p.getName(), p));
        if (parameters.isEmpty()) {
            scope.add("it", Collections.emptyList());
        }
        return scope;
    }
}
