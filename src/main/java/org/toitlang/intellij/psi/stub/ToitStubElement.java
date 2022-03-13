package org.toitlang.intellij.psi.stub;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ast.ToitVariableDeclaration;

public abstract class ToitStubElement<T extends PsiElement>  extends StubBase<T> {
    public ToitStubElement(@Nullable StubElement parent, IStubElementType elementType) {
        super(parent, elementType);
    }

    public abstract String getName();
}
