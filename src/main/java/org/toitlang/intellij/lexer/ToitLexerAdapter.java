package org.toitlang.intellij.lexer;

import com.intellij.lexer.FlexAdapter;

public class ToitLexerAdapter  extends FlexAdapter {
    private final ToitLexer lexer;
    private boolean trackIndents;

    public ToitLexerAdapter(boolean trackIndents) {
        this(new ToitLexer(null));
        this.trackIndents = trackIndents;
        lexer.setTrackIndents(trackIndents);
    }
    private ToitLexerAdapter(ToitLexer toitLexer) {
        super(toitLexer);
        this.lexer = toitLexer;
    }

    @Override
    public int getState() {
        if (trackIndents) {
            // The intellij scanner assumes that something changes between calls to advance.
            // As the variables that the built-in mechanism is considering can not easily be extended
            // to include the stack size of the indent, it is built into the state.
            int superState = super.getState();
            return lexer.getIndenStackSize() * 5 + superState;
        } else {
            return super.getState();
        }
    }
}
