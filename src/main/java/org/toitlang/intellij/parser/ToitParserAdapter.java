package org.toitlang.intellij.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class ToitParserAdapter implements PsiParser {
    public ToitParserAdapter() {
    }

    @Override
    public @NotNull ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
        var adapterBuilder = new ToitPsiBuilderAdapter(builder);
        new Parser(root, adapterBuilder).parse();
        return builder.getTreeBuilt();
    }
}
