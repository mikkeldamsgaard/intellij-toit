package org.toitlang.intellij.psi;

import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;

public class ToitPsiHelper {
    public static <T> List<T> childrenOfType(PsiElement psi, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        for (var c = psi.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (clazz.isInstance(c)) {
                result.add(clazz.cast(c));
            }
        }
        return result;
    }
}
