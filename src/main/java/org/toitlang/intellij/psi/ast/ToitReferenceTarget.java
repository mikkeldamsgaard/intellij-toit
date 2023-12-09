package org.toitlang.intellij.psi.ast;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.reference.ToitEvaluatedType;

public interface ToitReferenceTarget extends PsiElement {
  @NotNull ToitEvaluatedType getEvaluatedType();
}
