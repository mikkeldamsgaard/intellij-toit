package org.toitlang.intellij.lexer;

import com.google.common.base.Charsets;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import junit.framework.TestCase;
import org.toitlang.intellij.parser.SimpleParserTest;
import org.toitlang.intellij.psi.ToitElementType;
import org.toitlang.intellij.psi.ToitTokenType;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.ast.ToitElement;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LexerTest extends TestCase {
    public void testLexer() throws IOException {
        String text = SimpleParserTest.PARSING_ARGUMENTS_ON_NEW_LINES;
        dumpTokens(text);
    }

    public void testComment() throws IOException {
        var tokens = tokens("// This is a comment\n");
        assertEquals(2, tokens.size());
        checkToken(tokens, 0, ToitTypes.COMMENT, "// This is a comment");
        checkToken(tokens, 1, TokenType.WHITE_SPACE, "\n");
    }

    public void testMultiComment() throws IOException {
        var tokens = tokens("""
                /* This is a comment
                   on multiple lines */
                """);
        tokens.forEach(this::dumpToken);
        assertEquals(3, tokens.size());
        checkToken(tokens, 0, ToitTypes.START_COMMENT, "/*");
        checkToken(tokens, 1, ToitTypes.COMMENT, " This is a comment\n" +
                "   on multiple lines */");
        checkToken(tokens, 2, ToitTypes.NEWLINE, "\n");
    }

    private void checkToken(List<LexerToken> tokens, int index, IElementType type, String text) {
        assertEquals(type, tokens.get(index).type);
        assertEquals(text, tokens.get(index).text);
    }

    public void testDumpMultilineParams() throws IOException {
        dumpTokens(SimpleParserTest.PARSE_LITERAL_METHOD_INVOCATION);
    }

    private List<LexerToken> tokens(String text) throws IOException {
        ToitLexer toitLexer = new ToitLexer(null);
        toitLexer.reset(text, 0, text.length(), 0);
        List<LexerToken> tokens = new ArrayList<>();
        IElementType elm;
        int tokenStart = 0;
        while ((elm = toitLexer.advance()) != null) {
            tokens.add(new LexerToken(elm, text.substring(tokenStart, toitLexer.getTokenEnd())));
            tokenStart = toitLexer.getTokenEnd();
        }
        return tokens;
    }

    private void dumpTokens(String text) throws IOException {
        tokens(text).forEach(this::dumpToken);
    }

    private void dumpToken(LexerToken token) {
        System.out.println(token.type.getDebugName() + "(" + token.text.replace("\n", "\\n") + ")");
    }

    static class LexerToken {
        public final IElementType type;
        public final String text;

        public LexerToken(IElementType type, String text) {
            this.type = type;
            this.text = text;
        }
    }
}
