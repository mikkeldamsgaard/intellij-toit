// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.elements;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.toitlang.intellij.psi.*;

public class ToitQualifiedName extends ASTWrapperPsiElement {

  public ToitQualifiedName(@NotNull ASTNode node) {
    super(node);
  }

  @NotNull
  public List<ToitVariableName> getVariableNameList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ToitVariableName.class);
  }

}
