// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;

import java.util.ArrayList;
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
        var callee = getFirstChildOfType(ToitExpression.class);
        if (callee == null) return null;
        ToitReferenceIdentifier lastRef = callee.getLastDescendentOfType(ToitReferenceIdentifier.class);
        if (lastRef == null) return null;
        var function = lastRef.getReference().resolve();
        if (function instanceof ToitFunction) return (ToitFunction) function;
        return null;
    }

    public @NotNull List<ToitFunction> getFunctions() {
        List<ToitFunction> result = new ArrayList<>();
        var callee = getFirstChildOfType(ToitExpression.class);
        if (callee == null) return result;
        ToitReferenceIdentifier lastRef = callee.getLastDescendentOfType(ToitReferenceIdentifier.class);
        if (lastRef == null) return result;
        var functions = lastRef.getReference().multiResolve(false);
        for (ResolveResult resolveResult : functions) {
            var resolvedElement = resolveResult.getElement();
            if (resolvedElement instanceof ToitFunction) result.add((ToitFunction) resolvedElement);
            if (resolvedElement instanceof ToitStructure) result.addAll(((ToitStructure)resolvedElement).getDefaultConstructors());
        }
        return result;
    }
}
