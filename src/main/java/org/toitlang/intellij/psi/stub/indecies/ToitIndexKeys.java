package org.toitlang.intellij.psi.stub.indecies;

import com.intellij.psi.stubs.StubIndexKey;
import org.toitlang.intellij.psi.ast.ToitStructure;

public class ToitIndexKeys {
    public final static StubIndexKey<String, ToitStructure> STRUCTURE_SHORT_NAME = StubIndexKey.createIndexKey("toit.structure.short");
    public final static StubIndexKey<String, ToitStructure> STRUCTURE_FULL_NAME = StubIndexKey.createIndexKey("toit.structure.full");

    public final static StubIndexKey<String, ToitStructure> METHOD_SHORT_NAME = StubIndexKey.createIndexKey("toit.method.short");
    public final static StubIndexKey<String, ToitStructure> METHOD_FULL_NAME = StubIndexKey.createIndexKey("toit.method.full");

    public final static StubIndexKey<String, ToitStructure> VARIABLE_SHORT_NAME = StubIndexKey.createIndexKey("toit.method.short");
    public final static StubIndexKey<String, ToitStructure> VARIABLE_FULL_NAME = StubIndexKey.createIndexKey("toit.method.full");
}
