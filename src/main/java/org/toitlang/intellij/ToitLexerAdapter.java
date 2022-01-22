package org.toitlang.intellij;

import com.intellij.lexer.FlexAdapter;

public class ToitLexerAdapter  extends FlexAdapter {
    public ToitLexerAdapter() {
        super(new ToitLexer(null));
    }

}
