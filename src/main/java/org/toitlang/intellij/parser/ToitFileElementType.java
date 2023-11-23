package org.toitlang.intellij.parser;

import com.intellij.lang.Language;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.tree.IStubFileElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.ToitLanguage;
import org.toitlang.intellij.psi.ToitFile;

public class ToitFileElementType extends IStubFileElementType<PsiFileStub<ToitFile>> {
    public final static ToitFileElementType INSTANCE = new ToitFileElementType();

    @Override
    public @NonNls @NotNull String getExternalId() {
        return "psi.toit.file";
    }

    public ToitFileElementType() {
        super(ToitLanguage.INSTANCE);
    }

    @Override
    public int getStubVersion() {
        return 1;
    }
}
