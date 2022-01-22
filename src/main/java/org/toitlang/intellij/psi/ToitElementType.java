package org.toitlang.intellij.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.ToitLanguage;

public class ToitElementType extends IElementType {
    public ToitElementType(@NotNull @NonNls String debugName) {
        super(debugName, ToitLanguage.INSTANCE);
    }
}
