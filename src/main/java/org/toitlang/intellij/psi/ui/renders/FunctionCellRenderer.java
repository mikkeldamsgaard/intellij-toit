package org.toitlang.intellij.psi.ui.renders;

import com.intellij.ide.util.DelegatingPsiElementCellRenderer;
import com.intellij.ide.util.PsiElementRenderingInfo;
import org.toitlang.intellij.psi.ast.ToitFunction;

import javax.swing.*;
import java.awt.*;

public class FunctionCellRenderer extends DelegatingPsiElementCellRenderer<ToitFunction> {
    public FunctionCellRenderer() {
        super(new ToitFunctionRenderingInfo());
    }
}
