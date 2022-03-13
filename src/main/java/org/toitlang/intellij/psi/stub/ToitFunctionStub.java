package org.toitlang.intellij.psi.stub;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ast.ToitFunction;

import java.io.IOException;

public class ToitFunctionStub extends ToitStubElement<ToitFunction> {
    String name;
    public ToitFunctionStub(StubElement<? extends PsiElement> parentStub, ToitFunctionElementType elementType, ToitFunction psi) {
        super(parentStub, elementType);
        name = psi.getName();
    }

    public ToitFunctionStub(StubElement parentStub, ToitFunctionElementType elementType, StubInputStream dataStream) throws IOException {
        super(parentStub, elementType);
        name = dataStream.readNameString();
    }


    public void serialize(StubOutputStream dataStream) throws IOException {
        dataStream.writeName(name);
    }

    public String getName() {
        return name;
    }
}
