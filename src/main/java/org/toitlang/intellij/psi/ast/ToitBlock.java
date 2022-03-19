// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.List;

public class ToitBlock extends ToitElement {

  public ToitBlock(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected void accept(ToitVisitor visitor) {
    visitor.visit(this);
  }

  public List<ToitParameterName> getParameters() {
    return childrenOfType(ToitParameterName.class);
  }
}
