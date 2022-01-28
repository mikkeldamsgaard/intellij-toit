package org.toitlang.intellij.lexer;

import com.intellij.lexer.FlexAdapter;

public class ToitLexerAdapter  extends FlexAdapter {
    private final ToitLexer lexer;
    public ToitLexerAdapter() {
        this(new ToitLexer(null));
    }
    private ToitLexerAdapter(ToitLexer toitLexer) {
        super(toitLexer);
        this.lexer = toitLexer;
    }

    @Override
    public int getState() {
        // The intellij scanner assumes that something changes between calls to advance.
        // As the variables that the built in mechanism is considering can not be easily extended
        // to include the stack size of the indent, it is built into the state.
        int superState = super.getState();
        return lexer.getIndenStackSize()*5+superState;
    }
}
