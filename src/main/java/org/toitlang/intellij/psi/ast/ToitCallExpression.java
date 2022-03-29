// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;

import java.util.List;

public class ToitCallExpression extends ToitExpression {

    public ToitCallExpression(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public <T> T accept(ToitExpressionVisitor<T> expressionVisitor) {
        return expressionVisitor.visit(this);
    }

    public ToitFunction getFunction() {
        var callee = firstChildOfType(ToitExpression.class);
        if (callee == null) return null;
        List<ToitReferenceIdentifier> references = callee.getDescendentsOfType(ToitReferenceIdentifier.class);
        if (references.isEmpty())   return null;
        var ref = references.get(references.size()-1).getReference();
        var function = ref.resolve();
        if (function instanceof ToitFunction) return (ToitFunction) function;
        return null;
    }
}
