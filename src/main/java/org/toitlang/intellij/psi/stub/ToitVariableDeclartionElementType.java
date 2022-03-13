package org.toitlang.intellij.psi.stub;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.ToitLanguage;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ToitPsiCreator;
import org.toitlang.intellij.psi.ast.ToitFunction;
import org.toitlang.intellij.psi.ast.ToitVariableDeclaration;

import java.io.IOException;

public class ToitVariableDeclartionElementType extends IStubElementType<ToitVariableDeclarationStub, ToitVariableDeclaration> implements ToitPsiCreator {
    public ToitVariableDeclartionElementType(@NotNull @NonNls String debugName) {
        super(debugName, ToitLanguage.INSTANCE);
    }

    @Override
    public ToitVariableDeclaration createPsi(@NotNull ToitVariableDeclarationStub stub) {
        return new ToitVariableDeclaration(stub, this);
    }

    @Override
    public @NotNull ToitVariableDeclarationStub createStub(@NotNull ToitVariableDeclaration psi, StubElement<? extends PsiElement> parentStub) {
        return new ToitVariableDeclarationStub(parentStub, this, psi);
    }

    @Override
    public @NotNull String getExternalId() {
        return "stub."+getDebugName();
    }

    @Override
    public void serialize(@NotNull ToitVariableDeclarationStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        stub.serialize(dataStream);
    }

    @Override
    public @NotNull ToitVariableDeclarationStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return new ToitVariableDeclarationStub(parentStub, this, dataStream);
    }

    @Override
    public void indexStub(@NotNull ToitVariableDeclarationStub stub, @NotNull IndexSink sink) {

    }

    @Override
    public PsiElement createPsiElement(ASTNode node) {
        return new ToitVariableDeclaration(node);
    }
}
