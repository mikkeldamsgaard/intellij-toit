package org.toitlang.intellij.findusage;

import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ast.ToitNameableIdentifier;

public class ToitFindUsageProvider implements FindUsagesProvider {
    private ToitNameableIdentifier getNamedIdentifier(PsiElement element) {
        if (element instanceof PsiNameIdentifierOwner) {
            var psiNameIdentifier = ((PsiNameIdentifierOwner)element).getNameIdentifier();
            if (psiNameIdentifier instanceof  ToitNameableIdentifier) return (ToitNameableIdentifier) psiNameIdentifier;
        }
        return null;
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return getNamedIdentifier(psiElement) != null;
    }

    @Override
    public @Nullable @NonNls String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public @Nls @NotNull String getType(@NotNull PsiElement element) {
        ToitNameableIdentifier ne = getNamedIdentifier(element);
        if (ne == null) return "unknown";
        return ne.getFindUsageTypeName();
    }

    @Override
    public @Nls @NotNull String getDescriptiveName(@NotNull PsiElement element) {
        return "";
    }

    @Override
    public @Nls @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        ToitNameableIdentifier ne = getNamedIdentifier(element);
        return ne.getName();
    }

    @Override
    public @Nullable WordsScanner getWordsScanner() {
        return new ToitWordScanner();
    }
}
