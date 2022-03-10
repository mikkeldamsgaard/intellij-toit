// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

public class ToitIdentifier extends ToitElement {

  public ToitIdentifier(@NotNull ASTNode node) {
    super(node);
    System.out.println(node.getElementType());
  }

  @Override
  protected void accept(ToitVisitor visitor) {
    visitor.visit(this);
  }
}
