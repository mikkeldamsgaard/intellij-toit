package org.toitlang.intellij.psi.ast;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.toitlang.intellij.psi.reference.ToitEvaluatedType;

public interface ToitReferenceTarget extends PsiElement {
  ToitEvaluatedType getEvaluatedType();
}
