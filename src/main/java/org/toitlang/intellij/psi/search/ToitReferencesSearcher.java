package org.toitlang.intellij.psi.search;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.ToitFileType;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ast.ToitIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ToitReferencesSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
  <T> T runReadAction(@NotNull Computable<T> computation) {
    return ApplicationManager.getApplication().runReadAction(computation);
  }

  @Override
  public void processQuery(ReferencesSearch.@NotNull SearchParameters queryParameters, @NotNull Processor<? super PsiReference> consumer) {
    if (!(queryParameters.getElementToSearch() instanceof PsiNamedElement)) return;
    PsiNamedElement elementToSearch = (PsiNamedElement) queryParameters.getElementToSearch();
    String nameToSearch = runReadAction(elementToSearch::getName);

    List<LocalSearchScope> localScopes = new ArrayList<>();
    if (queryParameters.getEffectiveSearchScope() instanceof GlobalSearchScope) {
      var scope = (GlobalSearchScope) queryParameters.getScopeDeterminedByUser();
      ApplicationManager.getApplication().runReadAction(() -> {
        FileBasedIndex.getInstance().getFilesWithKey(
          ToitReferenceNameIndex.INDEX_NAME,
          Set.of(nameToSearch),
          virtualFile -> {
            var toitFile = PsiManager.getInstance(elementToSearch.getProject()).findFile(virtualFile);
            if (toitFile != null) {
              localScopes.add(new LocalSearchScope(toitFile, null));
            }
            return true;
          },
          scope);
      });
    } else {
      localScopes.add((LocalSearchScope) queryParameters.getEffectiveSearchScope());
    }

    for (LocalSearchScope localScope : localScopes) {
      ApplicationManager.getApplication().runReadAction(() -> {
        for (PsiElement psiElement : localScope.getScope()) {
          for (ToitIdentifier elm : PsiTreeUtil.findChildrenOfType(psiElement, ToitIdentifier.class)) {
            if (Objects.equals(elm.getName(), nameToSearch)) {
              PsiReference reference = elm.getReference();
              if (reference != null && reference.isReferenceTo(elementToSearch)) {
                if (!consumer.process(reference)) return;
              }
            }
          }
        }
      });
    }
  }
}
