package org.toitlang.intellij.psi.stub.indecies;

import com.intellij.psi.stubs.StubIndexKey;
import org.toitlang.intellij.psi.ast.ToitFunction;
import org.toitlang.intellij.psi.ast.ToitStructure;
import org.toitlang.intellij.psi.ast.ToitVariableDeclaration;

public class ToitIndexKeys {
    public final static StubIndexKey<String, ToitStructure> STRUCTURE_SHORT_NAME = StubIndexKey.createIndexKey("toit.structure.short");
    public final static StubIndexKey<String, ToitStructure> STRUCTURE_FULL_NAME = StubIndexKey.createIndexKey("toit.structure.full");

    public final static StubIndexKey<String, ToitFunction> FUNCTION_SHORT_NAME = StubIndexKey.createIndexKey("toit.function.short");
    public final static StubIndexKey<String, ToitFunction> FUNCTION_FULL_NAME = StubIndexKey.createIndexKey("toit.function.full");

    public final static StubIndexKey<String, ToitVariableDeclaration> VARIABLE_SHORT_NAME = StubIndexKey.createIndexKey("toit.method.short");
    public final static StubIndexKey<String, ToitVariableDeclaration> VARIABLE_FULL_NAME = StubIndexKey.createIndexKey("toit.method.full");
}
