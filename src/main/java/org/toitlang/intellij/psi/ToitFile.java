package org.toitlang.intellij.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.ToitFileType;
import org.toitlang.intellij.ToitLanguage;
import org.toitlang.intellij.psi.scope.ToitFileScope;
import org.toitlang.intellij.structureview.IStructureViewable;

import javax.swing.*;
import java.util.List;

public class ToitFile extends PsiFileBase implements IStructureViewable {
    CachedValue<ToitFileScope> cachedScope =
            CachedValuesManager.getManager(getProject()).createCachedValue(() -> {
                ToitFileScope toitFileScope = new ToitFileScope();
                toitFileScope.build(this);
                return new CachedValueProvider.Result<>(toitFileScope, toitFileScope.dependencies(this));
            });

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
        return "Toit File: " + getOriginalFile().getVirtualFile().getPath();
    }


    public ToitFileScope getToitFileScope() {
        return cachedScope.getValue();
    }

    public <T> List<T> childrenOfType(Class<T> clazz) {
        return ToitPsiHelper.childrenOfType(this, clazz);
    }

    @Override
    public @NlsSafe @Nullable String getPresentableText() {
        return super.getName();
    }


    @Override
    public @NotNull String getName() {
        String fileName = super.getName();
        if (fileName.endsWith(".toit")) fileName = fileName.substring(0,fileName.lastIndexOf(".toit"));
        return fileName;
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
