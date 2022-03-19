package org.toitlang.intellij.psi.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import lombok.Data;
import org.toitlang.intellij.psi.ast.ToitReferenceIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class ReferenceCalculation {
    private Collection<PsiElement> referencedValue;
    private ToitReferenceIdentifier reference;
    private Object[] variants = new Object[0];
    private List<PsiElement> dependencies = new ArrayList<>();
    private boolean soft = false;

    public Object[] getVariants() {
        if (variants == null) return new Object[0];
        return variants;
    }

    public ResolveResult[] getResolveResult() {
        if (referencedValue == null || referencedValue.size() == 0) return new ResolveResult[0];
        return referencedValue.stream().map(PsiElementResolveResult::new).toArray(ResolveResult[]::new);
    }

    public void setSoft(boolean soft) {
        this.soft = soft;
    }

    public boolean isSoft() {
        return soft;
    }
}
