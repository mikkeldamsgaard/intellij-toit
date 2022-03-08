package org.toitlang.intellij.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.impl.PsiBuilderAdapter;
import org.jetbrains.annotations.NotNull;

public class ToitPsiBuilderAdapter extends PsiBuilderAdapter {
    public ToitPsiBuilderAdapter(@NotNull PsiBuilder delegate) {
        super(delegate);
    }

    @Override
    public void advanceLexer() {
        super.advanceLexer();
    }
}
