// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import static org.toitlang.intellij.psi.ToitTypes.DOT;
import static org.toitlang.intellij.psi.ToitTypes.DOT_DOT;

public class ToitImportDeclaration extends ToitElement {

  public ToitImportDeclaration(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected void accept(ToitVisitor visitor) {
    visitor.visit(this);
  }

  public boolean hasShow() { return childrenOfType(ToitIdentifier.class).stream().anyMatch(ToitIdentifier::isShow); }
  public boolean hasAs() { return childrenOfType(ToitIdentifier.class).stream().anyMatch(ToitIdentifier::isImportAs); }

  private final static TokenSet DOT_AND_IMPORT_IDENTIFIERS = TokenSet.create(DOT, DOT_DOT, ToitTypes.IMPORT_IDENTIFIER);
  public int getPrefixDots() {
    int prefixDots = 0;
    for (ASTNode child : getNode().getChildren(DOT_AND_IMPORT_IDENTIFIERS)) {
      if (child.getElementType() == DOT) {
        prefixDots++;
      } else if (child.getElementType() == DOT_DOT) {
        prefixDots += 2;
      } else break;
    }
    return prefixDots;
  }
  public boolean isShowStar() {
    return hasToken(STAR_SET);
  }

}
