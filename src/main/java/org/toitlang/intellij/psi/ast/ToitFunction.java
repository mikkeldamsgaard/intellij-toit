// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiInvalidElementAccessException;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

public class ToitFunction extends ToitElement {

  public ToitFunction(@NotNull ASTNode node) {
    super(node);
  }

  public ToitIdentifier getFunctionName() {
    for (PsiElement child : getChildren()) {
      if (child instanceof ToitIdentifier) return (ToitIdentifier) child;
    }

    throw new PsiInvalidElementAccessException(this);
  }

  @Override
  protected void accept(ToitVisitor visitor) {
    visitor.visit(this);
  }
}
