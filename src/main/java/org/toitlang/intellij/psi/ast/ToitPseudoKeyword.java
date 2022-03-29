// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

public class ToitPseudoKeyword extends ToitIdentifier {

  public ToitPseudoKeyword(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected void accept(ToitVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public PsiElement setName(String name) {
    throw new RuntimeException("Can not set name of a pseudo keyword");
  }
}
