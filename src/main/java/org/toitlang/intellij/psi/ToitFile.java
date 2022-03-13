package org.toitlang.intellij.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.ToitFileType;
import org.toitlang.intellij.ToitLanguage;
import org.toitlang.intellij.psi.scope.ToitFileScopeCalculator;
import org.toitlang.intellij.structureview.IStructureViewable;
import org.toitlang.intellij.utils.ToitScope;

import javax.swing.*;
import java.util.List;

public class ToitFile extends PsiFileBase implements IStructureViewable {
    public ToitFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, ToitLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return ToitFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Toit File: "+getVirtualFile().getPath();
    }

    public ToitScope getScope() {
        return CachedValuesManager.getCachedValue(this, () -> {
            var closureCalculator = new ToitFileScopeCalculator(ToitFile.this);
            return new CachedValueProvider.Result<>(closureCalculator.getScope(), closureCalculator.dependencies());
        });
    }

    public <T> List<T> childrenOfType(Class<T> clazz) {
        return ToitPsiHelper.childrenOfType(this, clazz);
    }

    @Override
    public @NlsSafe @Nullable String getPresentableText() {
        return getName();
    }

    @Override
    public @Nullable Icon getIcon(boolean unused) {
        return null;
    }

    @Override
    public List<IStructureViewable> getStructureChildren() {
        return childrenOfType(IStructureViewable.class);
    }
}
