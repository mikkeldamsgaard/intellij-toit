package org.toitlang.intellij.psi.stub;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ast.ToitStructure;

import java.io.IOException;

public class ToitStructureStub extends ToitStubElement<ToitStructure> {
    boolean abstract_;
    String name;

    public ToitStructureStub(@Nullable StubElement parent, IStubElementType elementType, @NotNull StubInputStream dataStream) throws IOException {
        super(parent, elementType);
        abstract_ = dataStream.readBoolean();
        name = dataStream.readNameString();
    }

    public ToitStructureStub(StubElement<? extends PsiElement> parentStub, ToitStructureElementType elementType, ToitStructure psi) {
        super(parentStub, elementType);
        abstract_ = psi.isAbstract();
        name = psi.getName();
    }

    public boolean isAbstract() { return abstract_; };
    public String getName() { return name; };

    public void serialize(StubOutputStream dataStream) throws IOException {
        dataStream.writeBoolean(abstract_);
        dataStream.writeName(name);
    }
}
