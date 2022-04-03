// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

public class ToitNamedArgument extends ToitElement {

  public ToitNamedArgument(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected void accept(ToitVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String getName() {
    var identifier = getFirstChildOfType(ToitNameableIdentifier.class);
    if (identifier != null) return identifier.getName();
    return super.getName();
  }
}
