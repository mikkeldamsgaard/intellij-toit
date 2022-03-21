package org.toitlang.intellij.psi.stub.indecies;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.ToitFunction;
import org.toitlang.intellij.psi.ast.ToitStructure;

public class ToitFunctionShortNameIndex extends StringStubIndexExtension<ToitFunction> {
    public final static ToitFunctionShortNameIndex INSTANCE = new ToitFunctionShortNameIndex();

    @Override
    public @NotNull StubIndexKey<String, ToitFunction> getKey() {
        return ToitIndexKeys.FUNCTION_SHORT_NAME;
    }
}
