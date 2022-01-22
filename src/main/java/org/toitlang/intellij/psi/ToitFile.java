package org.toitlang.intellij.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.ToitFileType;
import org.toitlang.intellij.ToitLanguage;

public class ToitFile extends PsiFileBase {
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
        return "Toit File";
    }
}
