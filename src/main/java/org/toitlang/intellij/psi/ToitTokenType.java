package org.toitlang.intellij.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.ToitLanguage;

public class ToitTokenType extends IElementType {
    public ToitTokenType(@NotNull @NonNls String debugName) {
        super(debugName, ToitLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "x" + super.toString();
    }
}
