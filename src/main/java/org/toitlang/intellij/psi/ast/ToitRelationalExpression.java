// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;

public class ToitRelationalExpression extends ToitExpression {
    private final static TokenSet AS = TokenSet.create(ToitTypes.AS);
    private final static TokenSet TYPE_SELECTOR = TokenSet.create(ToitTypes.AS, ToitTypes.IS, ToitTypes.IS_NOT);

    public ToitRelationalExpression(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public <T> T accept(ToitExpressionVisitor<T> expressionVisitor) {
        return expressionVisitor.visit(this);
    }

    public boolean isAs() {
        return hasOperatorToken(AS);
    }

    public boolean isTypeSelectingOperator() {
        return hasOperatorToken(TYPE_SELECTOR);
    }

    private boolean hasOperatorToken(TokenSet as) {
        var operators = childrenOfType(ToitOperator.class);
        for (ToitOperator operator : operators) {
            if (operator.hasToken(as)) return true;
        }

        return false;
    }
}
