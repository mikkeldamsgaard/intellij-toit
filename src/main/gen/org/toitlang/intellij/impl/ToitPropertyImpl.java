// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.toitlang.intellij.psi.ToitTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.toitlang.intellij.psi.*;

public class ToitPropertyImpl extends ASTWrapperPsiElement implements ToitProperty {

  public ToitPropertyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ToitVisitor visitor) {
    visitor.visitProperty(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ToitVisitor) accept((ToitVisitor)visitor);
    else super.accept(visitor);
  }

}
