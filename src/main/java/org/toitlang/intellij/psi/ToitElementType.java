package org.toitlang.intellij.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.ToitLanguage;

import java.util.function.Function;

public class ToitElementType extends IElementType implements ToitPsiCreator {
    private Function<? super ASTNode, ? extends PsiElement> creator;

    public ToitElementType(@NotNull @NonNls String debugName) {
        super(debugName, ToitLanguage.INSTANCE);
    }

    public ToitElementType(@NotNull @NonNls String debugName, Function<? super ASTNode, ? extends PsiElement> creator) {
        super(debugName, ToitLanguage.INSTANCE);
        this.creator = creator;
    }

    public PsiElement createPsiElement(ASTNode node) {
        if (creator == null) throw new RuntimeException("No creator for "+getDebugName());
        return creator.apply(node);
    }
}
