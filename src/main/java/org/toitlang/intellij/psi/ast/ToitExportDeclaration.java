// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ToitExportDeclaration extends ToitElement {
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

  public List<ToitReferenceIdentifier> getExportList() {
    return getChildrenOfType(ToitReferenceIdentifier.class);
  }


  public Collection<String> getExportedNames() {
    return getExportList().stream().map(ToitReferenceIdentifier::getName).collect(Collectors.toList());
  }
}
