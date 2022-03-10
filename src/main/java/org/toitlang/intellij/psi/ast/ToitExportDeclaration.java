// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.List;

public class ToitExportDeclaration extends ToitElement {
  private final static TokenSet STAR_SET = TokenSet.create(ToitTypes.STAR);
  public ToitExportDeclaration(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected void accept(ToitVisitor visitor) {
    visitor.visit(this);
  }

  public boolean isStar() {
    return hasToken(STAR_SET);
  }

  public List<ToitIdentifier> getExportList() {
    return childrenOfType(ToitIdentifier.class);
  }
}
