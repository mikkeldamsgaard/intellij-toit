package org.toitlang.intellij.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.ToitLanguage;
import org.toitlang.intellij.lexer.ToitLexerAdapter;
import org.toitlang.intellij.psi.ToitElementType;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ToitTypes;

public class ToitParserDefinition implements ParserDefinition {
    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet COMMENTS = TokenSet.create(ToitTypes.COMMENT);
    public static final TokenSet STRINGS = TokenSet.create(ToitTypes.STRING_START, ToitTypes.STRING_END, ToitTypes.STRING_PART);
    public static final IFileElementType FILE = new IFileElementType(ToitLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new ToitLexerAdapter();
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        //return WHITE_SPACES;
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new ToitParserAdapter();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return STRINGS;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        ToitElementType t = (ToitElementType) node.getElementType();

        return t.createPsiElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new ToitFile(viewProvider);
    }

    @Override
    public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }
}
