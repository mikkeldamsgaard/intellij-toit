package org.toitlang.intellij.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.ToitFileType;
import org.toitlang.intellij.ToitLanguage;
import org.toitlang.intellij.psi.scope.ToitFileScope;
import org.toitlang.intellij.structureview.IStructureViewable;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class ToitFile extends PsiFileBase implements IStructureViewable {
    CachedValue<ToitFileScope> cachedScope =
            CachedValuesManager.getManager(getProject()).createCachedValue(() -> {
                ToitFileScope toitFileScope = new ToitFileScope(this);
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
        String fileName = getName();
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

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        if (getVirtualFile().getNameWithoutExtension().equals(getVirtualFile().getParent().getNameWithoutExtension())) {
            try {
                getVirtualFile().getParent().rename(this,name);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!name.endsWith(ToitFileType.INSTANCE.getDefaultExtension()))
            name = name + "." + ToitFileType.INSTANCE.getDefaultExtension();
        
        return super.setName(name);
    }

    public ToitFile findProjectToitFile(VirtualFile vf) {
        if (vf == null) return null;
        return (ToitFile) PsiManager.getInstance(getProject()).findFile(vf);
    }
}
