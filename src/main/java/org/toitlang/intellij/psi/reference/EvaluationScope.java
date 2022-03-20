package org.toitlang.intellij.psi.reference;

import com.intellij.psi.PsiElement;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.scope.ToitFileScope;
import org.toitlang.intellij.psi.scope.ToitScope;

import java.util.Collection;
import java.util.List;

@Data
public class EvaluationScope {
    private final ToitScope scope;
    private final ToitFileScope currentFileScope;

    public @NotNull List<PsiElement> resolve(String key) {
        return scope.resolve(key);
    }

    public ToitFile getImportedLibrary(String name) {
        return currentFileScope.getImportedLibrary(name);
    }

    public Collection<? extends PsiElement> asVariant() {
        return scope.asVariant();
    }
}
