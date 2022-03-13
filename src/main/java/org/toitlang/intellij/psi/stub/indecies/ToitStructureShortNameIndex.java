package org.toitlang.intellij.psi.stub.indecies;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.ToitBaseStubableElement;
import org.toitlang.intellij.psi.ast.ToitStructure;

public class ToitStructureShortNameIndex extends StringStubIndexExtension<ToitStructure> {
    public final static ToitStructureShortNameIndex INSTANCE = new ToitStructureShortNameIndex();

    @Override
    public @NotNull StubIndexKey<String, ToitStructure> getKey() {
        return ToitIndexKeys.STRUCTURE_SHORT_NAME;
    }
}
