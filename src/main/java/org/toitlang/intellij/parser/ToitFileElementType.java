package org.toitlang.intellij.parser;

import com.intellij.lang.Language;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.tree.IStubFileElementType;
import org.toitlang.intellij.ToitLanguage;
import org.toitlang.intellij.psi.ToitFile;

public class ToitFileElementType extends IStubFileElementType<PsiFileStub<ToitFile>> {
    public final static ToitFileElementType INSTANCE = new ToitFileElementType();
    public ToitFileElementType() {
        super(ToitLanguage.INSTANCE);
    }

    @Override
    public int getStubVersion() {
        return 1;
    }
}
