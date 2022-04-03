// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;

public class ToitDerefExpression extends ToitExpression {

  public ToitDerefExpression(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public <T> T accept(ToitExpressionVisitor<T> expressionVisitor) {
    return expressionVisitor.visit(this);
  }

  public ToitReferenceIdentifier getToitReferenceIdentifier() {
    for (ToitReferenceIdentifier toitReferenceIdentifier : getChildrenOfType(ToitReferenceIdentifier.class)) {
      return toitReferenceIdentifier;
    }
    return null;
  }

  @Override
  public String getName() {
    var ref = getToitReferenceIdentifier();
    if (ref != null) return ref.getName();
    return null;
  }
}
