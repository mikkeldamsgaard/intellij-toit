package org.toitlang.intellij.psi.stub;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.ToitLanguage;
import org.toitlang.intellij.psi.ToitPsiCreator;
import org.toitlang.intellij.psi.ast.ToitStructure;
import org.toitlang.intellij.psi.stub.indecies.ToitIndexKeys;

import java.io.IOException;

public class ToitStructureElementType extends IStubElementType<ToitStructureStub, ToitStructure> implements ToitPsiCreator {
    private final StructureType type;

    public ToitStructureElementType(@NotNull @NonNls String debugName, StructureType type) {
        super(debugName, ToitLanguage.INSTANCE);
        this.type = type;
    }

    @Override
    public ToitStructure createPsi(@NotNull ToitStructureStub stub) {
        return new ToitStructure(stub,this);
    }

    @Override
    public @NotNull ToitStructureStub createStub(@NotNull ToitStructure psi, StubElement<? extends PsiElement> parentStub) {
        return new ToitStructureStub(parentStub, this, psi);
    }

    @Override
    public @NotNull String getExternalId() {
        return "stub."+getDebugName();
    }

    @Override
    public void serialize(@NotNull ToitStructureStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        stub.serialize(dataStream);
    }

    @Override
    public @NotNull ToitStructureStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new ToitStructureStub(parentStub, this, dataStream);
    }

    @Override
    public void indexStub(@NotNull ToitStructureStub stub, @NotNull IndexSink sink) {
        sink.occurrence(ToitIndexKeys.STRUCTURE_SHORT_NAME, stub.getName());
    }

    @Override
    public PsiElement createPsiElement(ASTNode node) {
        return new ToitStructure(node);
    }

    public boolean isClass() {  return type == StructureType.CLASS;  }
    public boolean isInterface() {  return type == StructureType.INTERFACE;  }
    public boolean isMonitor() {  return type == StructureType.MONITOR;  }


    public enum StructureType {
        CLASS, INTERFACE, MONITOR
    }
}
