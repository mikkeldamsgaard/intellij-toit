package org.toitlang.intellij.psi.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import lombok.Data;
import org.toitlang.intellij.psi.ast.ToitReferenceIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ReferenceCalculation {
    private Collection<PsiElement> referencedValue;
    private ToitReferenceIdentifier reference;
    private Object[] variants = new Object[0];
    private List<PsiElement> dependencies = new ArrayList<>();

    public Object[] getVariants() {
        if (variants == null) return new Object[0];
        return variants;
    }

    public ResolveResult[] getResolveResult() {
        if (referencedValue == null || referencedValue.size() == 0) return new ResolveResult[0];
        return referencedValue.stream().map(PsiElementResolveResult::new).toArray(ResolveResult[]::new);
    }
}
