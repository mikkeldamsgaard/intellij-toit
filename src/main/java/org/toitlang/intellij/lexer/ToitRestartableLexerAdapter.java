package org.toitlang.intellij.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.RestartableLexer;
import com.intellij.lexer.TokenIterator;
import org.jetbrains.annotations.NotNull;

public class ToitRestartableLexerAdapter extends FlexAdapter implements RestartableLexer {
    private final ToitLexer lexer;

    public ToitRestartableLexerAdapter() {
        this(new ToitLexer(null));
    }

    private ToitRestartableLexerAdapter(ToitLexer toitLexer) {
        super(toitLexer);
        this.lexer = toitLexer;
        this.lexer.setTrackIndents(false);
    }

    @Override
    public int getStartState() {
        return 0;
    }

    @Override
    public boolean isRestartableState(int state) {
        return state == ToitLexer.NORMAL || state == ToitLexer.YYINITIAL;
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState, TokenIterator tokenIterator) {
        start(buffer,startOffset,endOffset,initialState);
    }
}
