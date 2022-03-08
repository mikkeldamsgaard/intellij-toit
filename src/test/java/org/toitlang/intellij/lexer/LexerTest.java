package org.toitlang.intellij.lexer;

import com.google.common.base.Charsets;
import junit.framework.TestCase;
import org.toitlang.intellij.parser.SimpleParserTest;

import java.io.FileInputStream;
import java.io.IOException;

public class LexerTest extends TestCase {
    public void testLexer() throws IOException {
        String text = SimpleParserTest.PARSING_ARGUMENTS_ON_NEW_LINES;
        dumpTokens(text);
    }

    public void testDumpMultilineParams() throws IOException {
        dumpTokens(SimpleParserTest.PARSE_LITERAL_METHOD_INVOCATION);
    }
    private void dumpTokens(String text) throws IOException {
        ToitLexer toitLexer = new ToitLexer(null);
        toitLexer.reset(text,0, text.length(),0);
        while (true) {
            var elm = toitLexer.advance();
            if (elm == null) break;
            System.out.println(elm.getDebugName() + "(" + text.substring(toitLexer.getTokenStart(), toitLexer.getTokenEnd()).replace("\n","\\n") + ")" );
        }
    }
}
