// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.elements;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTokenType;

public class ToitOperator extends ASTWrapperPsiElement {

  private ToitTokenType tokenType;
  public ToitOperator(@NotNull ASTNode node) { super(node); }
  public ToitOperator(@NotNull ASTNode node, ToitTokenType tokenType) {
    super(node);
    this.tokenType = tokenType;
  }

}
