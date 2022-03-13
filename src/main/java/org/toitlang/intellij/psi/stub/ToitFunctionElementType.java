package org.toitlang.intellij.psi.stub;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.ToitLanguage;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ToitPsiCreator;
import org.toitlang.intellij.psi.ast.ToitFunction;

import java.io.IOException;

public class ToitFunctionElementType extends IStubElementType<ToitFunctionStub, ToitFunction> implements ToitPsiCreator {
    public ToitFunctionElementType(@NotNull @NonNls String debugName) {
        super(debugName, ToitLanguage.INSTANCE);
    }

    @Override
    public ToitFunction createPsi(@NotNull ToitFunctionStub stub) {
        return new ToitFunction(stub, this);
    }

    @Override
    public @NotNull ToitFunctionStub createStub(@NotNull ToitFunction psi, StubElement<? extends PsiElement> parentStub) {
        return new ToitFunctionStub(parentStub,this, psi);
    }

    @Override
    public @NotNull String getExternalId() {
        return "stub."+getDebugName();
    }

    @Override
    public void serialize(@NotNull ToitFunctionStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        stub.serialize(dataStream);
    }

    @Override
    public @NotNull ToitFunctionStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new ToitFunctionStub(parentStub,this, dataStream);
    }

    @Override
    public void indexStub(@NotNull ToitFunctionStub stub, @NotNull IndexSink sink) {
        ToitFile file = stub.getParentStubOfType(ToitFile.class);
        if (file == null)
            throw new RuntimeException();
    }

    @Override
    public PsiElement createPsiElement(ASTNode node) {
        return new ToitFunction(node);
    }
}
