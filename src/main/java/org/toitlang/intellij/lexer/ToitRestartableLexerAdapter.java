package org.toitlang.intellij.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.RestartableLexer;
import com.intellij.lexer.TokenIterator;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTypes;

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
        return state == ToitLexer.NORMAL ||
                state == ToitLexer.YYINITIAL ||
                state == ToitLexer.COMMENT_PARSING;
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState, TokenIterator tokenIterator) {
        lexer.commentStack.clear();
        if (initialState == ToitLexer.COMMENT_PARSING) {
            int index = 0;
            int stackSize = 0;
            while (index < tokenIterator.getTokenCount()) {
                var elm = tokenIterator.getType(index);
                if (elm == ToitTypes.START_COMMENT || elm == ToitTypes.START_DOC_COMMENT) {
                    stackSize++;
                } else if (elm == ToitTypes.COMMENT) {
                    stackSize--;
                } else {
                    break;
                }
                index++;
            }
            for (int i = 0; i < stackSize; i++) {
                lexer.commentStack.push(0);
            }
        }
        start(buffer,startOffset,endOffset,initialState);
    }
}
