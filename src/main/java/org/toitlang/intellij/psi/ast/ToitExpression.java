// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.reference.ToitEvaluatedType;
import org.toitlang.intellij.psi.reference.ToitExpressionReferenceTarget;
import org.toitlang.intellij.psi.reference.ToitReferenceTargets;
import org.toitlang.intellij.psi.scope.ToitScope;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class ToitExpression extends ToitElementBase {

  public ToitExpression(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected void accept(ToitVisitor visitor) {
    visitor.visit(this);
  }

  public abstract  <T> T accept(ToitExpressionVisitor<T> expressionVisitor);

  public <T> List<T> acceptChildren(ToitExpressionVisitor<T> expressionVisitor) {
    return getChildrenOfType(ToitExpression.class).stream()
            .map(e -> e.accept(expressionVisitor))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
  }

  public @NotNull ToitEvaluatedType getType(ToitScope scope) {
    return ToitEvaluatedType.evaluate(this, scope);
  }

  public boolean isNull() {
    return getText().trim().equals("null");
  }

  public ToitReferenceTargets getReferenceTargets(ToitScope scope) {
    return new ToitReferenceTargets();
  }

  public ToitElement singleToitElementChild() {
    var children = getChildrenOfType(ToitElement.class);
    if (children.size() == 1) return children.get(0);
    return null;
  }
}
