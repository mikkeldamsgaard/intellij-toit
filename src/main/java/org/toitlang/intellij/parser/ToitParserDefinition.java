package org.toitlang.intellij.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.ToitLanguage;
import org.toitlang.intellij.lexer.ToitLexerAdapter;
import org.toitlang.intellij.psi.ToitElementType;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ToitPsiCreator;
import org.toitlang.intellij.psi.ToitTypes;

public class ToitParserDefinition implements ParserDefinition {
    public static final TokenSet COMMENTS = TokenSet.create(ToitTypes.COMMENT);
    public static final TokenSet STRINGS = TokenSet.create(ToitTypes.STRING_START, ToitTypes.STRING_END, ToitTypes.STRING_PART);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new ToitLexerAdapter(true);
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new ToitParserAdapter();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return ToitFileElementType.INSTANCE;
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
        return ((ToitPsiCreator)node.getElementType()).createPsiElement(node);
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
