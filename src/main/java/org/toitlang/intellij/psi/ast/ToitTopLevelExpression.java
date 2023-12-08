// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.reference.EvaluationScope;
import org.toitlang.intellij.psi.reference.ToitExpressionReferenceTarget;

import java.util.Collection;
import java.util.stream.Collectors;

public class ToitTopLevelExpression extends ToitExpression {

  public ToitTopLevelExpression(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public <T> T accept(ToitExpressionVisitor<T> expressionVisitor) {
    return expressionVisitor.visit(this);
  }

  @Override
  public Collection<ToitExpressionReferenceTarget> getReferenceTargets(EvaluationScope scope) {
    return super.getReferenceTargets(scope).stream().map(ToitExpressionReferenceTarget::toInstance).collect(Collectors.toList());
  }
}
