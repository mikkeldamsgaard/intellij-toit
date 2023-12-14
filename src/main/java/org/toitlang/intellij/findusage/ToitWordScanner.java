package org.toitlang.intellij.findusage;

import com.intellij.lang.cacheBuilder.VersionedWordsScanner;
import com.intellij.lang.cacheBuilder.WordOccurrence;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.lexer.ToitLexerAdapter;
import org.toitlang.intellij.psi.ast.ToitIdentifier;

import static org.toitlang.intellij.psi.ToitTypes.*;

public class ToitWordScanner extends VersionedWordsScanner {
    private final Lexer lexer = new ToitLexerAdapter(false);

    private final TokenSet identifierTokenSet = TokenSet.create(IDENTIFIER);
    private final TokenSet commentTokenSet = TokenSet.create(COMMENT);
    private final TokenSet literalTokenSet = TokenSet.create(STRING_PART, STRING_PART, STRING_END, INTEGER, FLOAT);

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public void processWords(@NotNull CharSequence fileText, @NotNull Processor<? super WordOccurrence> processor) {
        lexer.start(fileText);

        IElementType type;
        while ((type = lexer.getTokenType()) != null) {
            WordOccurrence.Kind kind = null;
            String word = null;
            if (identifierTokenSet.contains(type)) {
                kind = WordOccurrence.Kind.CODE;
                word = ToitIdentifier.normalizeMinusUnderscore(lexer.getTokenText().trim());
            } else if (commentTokenSet.contains(type)) {
                kind = WordOccurrence.Kind.COMMENTS;
                word = lexer.getTokenText().trim();
            } else if (literalTokenSet.contains(type)) {
                kind = WordOccurrence.Kind.LITERALS;
                word = lexer.getTokenText().trim();
            }

            if (word != null) {
                WordOccurrence occurrence =  new WordOccurrence(word, 0, word.length(), kind);
                if (!processor.process(occurrence))
                    return;
            }

            lexer.advance();
        }
    }
}
