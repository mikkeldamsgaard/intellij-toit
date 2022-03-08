package org.toitlang.intellij.parser;

import com.google.common.base.Charsets;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.ParsingTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ParserTest extends ParsingTestCase {

    public ParserTest() {
        super("", "toit", new ToitParserDefinition());
    }

    void checkError(PsiFile psi, String fileName) {
        psi.accept(new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element instanceof PsiErrorElement) {
                    PsiErrorElement err = (PsiErrorElement) element;
                    int lineno = psi.getText().substring(0, err.getTextOffset()).split("\n").length;
                    System.out.println("\"" + psi.getText().substring(Math.max(0, err.getTextOffset() - 20), err.getTextOffset()).replace("\n", "\\n") + "\"");
                    System.out.println();
                    System.out.println("Line " + lineno + "(" + err.getTextOffset() + "): " + err.getErrorDescription());
                    System.out.println();
                    System.out.println(psi.getText().substring(err.getTextOffset(), Math.min(psi.getTextLength(), err.getTextOffset() + 20)));
                    fail(fileName + ":" + lineno);
                }

                element.acceptChildren(this);
            }
        });
    }

    /**
     * @return path to test data file directory relative to root of this module.
     */
    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    @Override
    protected boolean skipSpaces() {
        return false;
    }

    @Override
    protected boolean includeRanges() {
        return true;
    }


}
