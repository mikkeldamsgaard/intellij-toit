// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

public class ToitOperator extends ToitElementBase {

  public ToitOperator(@NotNull ASTNode node) { super(node); }

  @Override
  protected void accept(ToitVisitor visitor) {
    visitor.visit(this);
  }

  private final static TokenSet CONST_OPERATOR = TokenSet.create(ToitTypes.CONST_DECLARE);
  public boolean isConstDeclare() {
    return hasToken(CONST_OPERATOR);
  }
}
