package org.toitlang.intellij.psi.search;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.ToitIdentifier;

import java.util.Objects;

public class ToitReferencesSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
    @Override
    public void processQuery(ReferencesSearch.@NotNull SearchParameters queryParameters, @NotNull Processor<? super PsiReference> consumer) {
        if (!(queryParameters.getElementToSearch() instanceof PsiNamedElement)) return;
        PsiNamedElement elementToSearch = (PsiNamedElement) queryParameters.getElementToSearch();
        String nameToSearch = elementToSearch.getName();
        if (queryParameters.getEffectiveSearchScope() instanceof GlobalSearchScope) return;

        if (queryParameters.getEffectiveSearchScope() instanceof LocalSearchScope) {
            LocalSearchScope scope = (LocalSearchScope) queryParameters.getEffectiveSearchScope();
            for (PsiElement psiElement : scope.getScope()) {
                for (ToitIdentifier elm : PsiTreeUtil.findChildrenOfType(psiElement, ToitIdentifier.class)) {
                    if (Objects.equals(elm.getName(), nameToSearch)) {
                        PsiReference reference = elm.getReference();
                        if (reference != null) {
                            if (!consumer.process(reference)) return;
                        }
                    }
                }
            }
        }
    }
}
