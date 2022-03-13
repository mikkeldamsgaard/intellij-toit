package org.toitlang.intellij.psi.stub;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ast.ToitStructure;
import org.toitlang.intellij.psi.ast.ToitVariableDeclaration;

import java.io.IOException;

public class ToitVariableDeclarationStub extends ToitStubElement<ToitVariableDeclaration> {
    boolean static_;
    String name;

    public ToitVariableDeclarationStub(@Nullable StubElement parent, IStubElementType elementType, @NotNull StubInputStream dataStream) throws IOException {
        super(parent, elementType);
        static_ = dataStream.readBoolean();
        name = dataStream.readNameString();
    }

    public ToitVariableDeclarationStub(StubElement<? extends PsiElement> parentStub, IStubElementType elementType, ToitVariableDeclaration psi) {
        super(parentStub, elementType);
        static_ = psi.isStatic();
        name = psi.getName();
    }

    public boolean isStatic() { return static_; };
    public String getName() { return name; };

    public void serialize(StubOutputStream dataStream) throws IOException {
        dataStream.writeBoolean(static_);
        dataStream.writeName(name);
    }
}
