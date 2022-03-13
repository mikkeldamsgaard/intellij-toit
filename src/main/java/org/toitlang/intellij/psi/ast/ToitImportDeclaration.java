// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.scope.ToitFileScopeCalculator;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.ArrayList;
import java.util.List;

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

  private final static TokenSet DOT_AND_IMPORT_IDENTIFIERS = TokenSet.create(DOT, DOT_DOT, ToitTypes.IMPORT_IDENTIFIER);
  public void computeScope(ToitFileScopeCalculator toitFileScopeCalculator) {
    int prefixDots = 0;
    for (ASTNode child : getNode().getChildren(DOT_AND_IMPORT_IDENTIFIERS)) {
      if (child.getElementType() == DOT) {
        prefixDots++;
      } else if (child.getElementType() == DOT_DOT) {
        prefixDots += 2;
      } else break;
    }

    List<String> paths = new ArrayList<>();
    List<String> shows = new ArrayList<>();
    ToitIdentifier as = null;
    for (ToitIdentifier toitIdentifier : childrenOfType(ToitIdentifier.class)) {
      if (toitIdentifier.isImport()) paths.add(toitIdentifier.getName());
      if (toitIdentifier.isShow()) shows.add(toitIdentifier.getName());
      if (toitIdentifier.isImportAs()) as = toitIdentifier;
    }

    toitFileScopeCalculator.addImport(prefixDots, paths,shows,as, this);
  }
}
