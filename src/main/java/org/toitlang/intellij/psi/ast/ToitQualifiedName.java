// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.List;

public class ToitQualifiedName extends ToitElement {

  public ToitQualifiedName(@NotNull ASTNode node) {
    super(node);
  }

  @NotNull
  public List<ToitIdentifier> getVariableNameList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ToitIdentifier.class);
  }

  @Override
  protected void accept(ToitVisitor visitor) {
    visitor.visit(this);
  }
}
