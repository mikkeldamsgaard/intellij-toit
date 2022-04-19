// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTokenType;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;

public class ToitListLiteral extends ToitExpression {
  public ToitListLiteral(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public <T> T accept(ToitExpressionVisitor<T> expressionVisitor) {
    return expressionVisitor.visit(this);
  }

  public boolean isByteArray() {
    return getNode().getElementType() == ToitTypes.BYTE_ARRAY_LITERAL;
  }
}
