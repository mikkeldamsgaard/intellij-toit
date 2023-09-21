package org.toitlang.intellij.psi.ui.renders;

import com.intellij.ide.util.PsiElementRenderingInfo;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ast.ToitFunction;

import javax.swing.*;

public class ToitFunctionRenderingInfo implements PsiElementRenderingInfo<ToitFunction> {
    @Override
    public @NlsSafe @NotNull String getPresentableText(@NotNull ToitFunction element) {
        return element.getText();
    }

    @Override
    public @Nullable Icon getIcon(@NotNull ToitFunction element) {
        return PsiElementRenderingInfo.super.getIcon(element);
    }

    @Override
    public @NlsSafe @Nullable String getContainerText(@NotNull ToitFunction element) {
        return PsiElementRenderingInfo.super.getContainerText(element);
    }
}
