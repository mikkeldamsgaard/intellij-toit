// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;

import static org.toitlang.intellij.psi.ToitTypes.*;

public class ToitSimpleLiteral extends ToitExpression {
  public ToitSimpleLiteral(@NotNull ASTNode node) { super(node); }

  @Override
  public <T> T accept(ToitExpressionVisitor<T> expressionVisitor) {
    return expressionVisitor.visit(this);
  }

  public boolean isString() {
    return getNode().getFirstChildNode().getElementType() == STRING_START;
  }

  public boolean isInt() {
    return getNode().getFirstChildNode().getElementType() == INTEGER;
  }

  public boolean isFloat() {
    return getNode().getFirstChildNode().getElementType() == FLOAT;
  }
}
