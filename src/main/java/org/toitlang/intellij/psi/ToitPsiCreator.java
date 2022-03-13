package org.toitlang.intellij.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

public interface ToitPsiCreator {
    PsiElement createPsiElement(ASTNode node);
}
