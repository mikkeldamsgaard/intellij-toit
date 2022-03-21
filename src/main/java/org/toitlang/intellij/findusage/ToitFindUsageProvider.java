package org.toitlang.intellij.findusage;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.lexer.ToitLexerAdapter;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.ast.ToitNameableIdentifier;
import org.toitlang.intellij.psi.ast.ToitReferenceIdentifier;

import static org.toitlang.intellij.psi.ToitTypes.*;

public class ToitFindUsageProvider implements FindUsagesProvider {
    private ToitNameableIdentifier getNamedIdentifier(PsiElement element) {
        if (element instanceof PsiNameIdentifierOwner) {
            var psiNameIdentifier = ((PsiNameIdentifierOwner)element).getNameIdentifier();
            return (ToitNameableIdentifier) psiNameIdentifier;
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
        String name = ne.getName();
        return name != null ? name : "";
    }

    @Override
    public @Nullable WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(
                new ToitLexerAdapter(false),
                TokenSet.create(
                        STRUCTURE_IDENTIFIER,
                        FUNCTION_IDENTIFIER,
                        IMPORT_AS_IDENTIFIER,
                        FACTORY_IDENTIFIER,
                        NAMED_PARAMETER_IDENTIFIER,
                        SIMPLE_PARAMETER_IDENTIFIER,
                        VARIABLE_IDENTIFIER,

                        IMPORT_SHOW_IDENTIFIER,
                        IMPORT_IDENTIFIER,
                        EXPORT_IDENTIFIER,
                        TYPE_IDENTIFIER,
                        REFERENCE_IDENTIFIER,
                        BREAK_CONTINUE_LABEL_IDENTIFIER),
                TokenSet.create(COMMENT),
                TokenSet.create(STRING_PART, STRING_PART, STRING_END, INTEGER, FLOAT)
        );
    }
}
