// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.reference.ToitExpressionReferenceTarget;
import org.toitlang.intellij.psi.reference.ToitReferenceTargets;
import org.toitlang.intellij.psi.scope.ToitScope;

import java.util.Collection;
import java.util.Collections;

public class ToitPostfixExpression extends ToitExpression {

  public ToitPostfixExpression(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public <T> T accept(ToitExpressionVisitor<T> expressionVisitor) {
    return expressionVisitor.visit(this);
  }

  @Override
  public ToitReferenceTargets getReferenceTargets(ToitScope scope) {
    var lastElement = getLastChildOfType(ToitExpression.class);
    if (lastElement == null) return new ToitReferenceTargets();
    return lastElement.getReferenceTargets(scope);
  }
}
