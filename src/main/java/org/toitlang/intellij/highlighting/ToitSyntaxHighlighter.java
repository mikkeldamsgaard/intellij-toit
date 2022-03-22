package org.toitlang.intellij.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.lexer.ToitLexerAdapter;
import org.toitlang.intellij.lexer.ToitRestartableLexerAdapter;
import org.toitlang.intellij.psi.ToitTypes;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class ToitSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey STRING =
            createTextAttributesKey("STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey COMMENT =
            createTextAttributesKey("SIMPLE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey FUNCTION_DECLARATION =
            createTextAttributesKey("FUNCTION_DECLARATION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey LOCAL_VARIABLE =
            createTextAttributesKey("LOCAL_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey INSTANCE_FIELD =
            createTextAttributesKey("INSTANCE_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey CONSTANT =
            createTextAttributesKey("CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey GLOBAL_VARIABLE =
            createTextAttributesKey("GLOBAL_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    public static final TextAttributesKey CLASS_NAME =
            createTextAttributesKey("CLASS_NAME", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey CLASS_REFERENCE =
            createTextAttributesKey("CLASS_REFERENCE", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    public static final TextAttributesKey INTERFACE_NAME =
            createTextAttributesKey("INTERFACE_NAME", DefaultLanguageHighlighterColors.INTERFACE_NAME);
    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("SIMPLE_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);


    public static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
    public static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{DefaultLanguageHighlighterColors.KEYWORD};
    public static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{STRING};
    public static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
    public static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{NUMBER};
    public static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new ToitRestartableLexerAdapter();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        // Is called once per tokenType to determine its class
        if (tokenType.equals(ToitTypes.STRING_PART) ||
                tokenType.equals(ToitTypes.STRING_END) ||
                tokenType.equals(ToitTypes.STRING_START) ||
                tokenType.equals(ToitTypes.CHARACTER)) {
            return STRING_KEYS;
        }

        if (tokenType.equals(ToitTypes.COMMENT)) {
            return COMMENT_KEYS;
        }

        if (tokenType.equals(TokenType.BAD_CHARACTER)) {
            return BAD_CHAR_KEYS;
        }

        if (tokenType.equals(ToitTypes.RETURN) ||
                tokenType.equals(ToitTypes.ASSERT) ||
                tokenType.equals(ToitTypes.WHILE) ||
                tokenType.equals(ToitTypes.FOR) ||
                tokenType.equals(ToitTypes.IF) ||
                tokenType.equals(ToitTypes.ELSE) ||
                tokenType.equals(ToitTypes.ABSTRACT) ||
                tokenType.equals(ToitTypes.CLASS) ||
                tokenType.equals(ToitTypes.BREAK) ||
                tokenType.equals(ToitTypes.CONTINUE) ||
                tokenType.equals(ToitTypes.FINALLY) ||
                tokenType.equals(ToitTypes.TRY) ||
                tokenType.equals(ToitTypes.IMPORT) ||
                tokenType.equals(ToitTypes.EXPORT) ||
                tokenType.equals(ToitTypes.NULL) ||
                tokenType.equals(ToitTypes.BOOLEAN) ||
                tokenType.equals(ToitTypes.OR) ||
                tokenType.equals(ToitTypes.NOT) ||
                tokenType.equals(ToitTypes.IS) ||
                tokenType.equals(ToitTypes.AS) ||
                tokenType.equals(ToitTypes.IS_NOT) ||
                tokenType.equals(ToitTypes.AND) ||
                tokenType.equals(ToitTypes.STATIC)
        ) {
            return KEYWORD_KEYS;
        }

        if (tokenType.equals(ToitTypes.FLOAT) ||
                tokenType.equals(ToitTypes.INTEGER)) {
            return NUMBER_KEYS;
        }

        return EMPTY_KEYS;
    }
}
