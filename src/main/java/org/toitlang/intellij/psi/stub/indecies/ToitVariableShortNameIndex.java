package org.toitlang.intellij.psi.stub.indecies;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.ToitStructure;
import org.toitlang.intellij.psi.ast.ToitVariableDeclaration;

public class ToitVariableShortNameIndex extends StringStubIndexExtension<ToitVariableDeclaration> {
    public final static ToitVariableShortNameIndex INSTANCE = new ToitVariableShortNameIndex();

    @Override
    public @NotNull StubIndexKey<String, ToitVariableDeclaration> getKey() {
        return ToitIndexKeys.VARIABLE_SHORT_NAME;
    }
}
