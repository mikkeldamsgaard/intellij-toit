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
                    int lineno = psi.getText().substring(0,err.getTextOffset()).split("\n").length;
                    System.out.println("\""+psi.getText().substring(Math.max(0, err.getTextOffset() - 20), err.getTextOffset()).replace("\n", "\\n") +"\"");
                    System.out.println();
                    System.out.println("Line "+lineno+": "+err.getErrorDescription());
                    System.out.println();
                    System.out.println(psi.getText().substring(err.getTextOffset(), Math.min(psi.getTextLength(), err.getTextOffset() + 20)));
                    fail(fileName+":"+lineno);
                }

                element.acceptChildren(this);
            }
        });
    }

    void parseAllSub(File d) throws IOException {
        for (var c : d.listFiles()) {
            if (c.isDirectory()) parseAllSub(c);
            else if (c.getName().endsWith("toit")) {
                String content = new String(new FileInputStream(c).readAllBytes(), Charsets.UTF_8);
                var psi = parseFile(c.getName(), content);
                checkError(psi, c.getPath());
                System.out.println("Success: "+c);
            }
        }
    }

    public void testParsingTestData() throws IOException {
        File lib = new File("/Users/mikkel/proj/application/esp32/common/esp-toit/toit/lib");
        parseAllSub(lib);
    }


    public static final String PARSION_RETURN_WiTH_METHOD_CALL = "" +
            "class A:\n" +
            "  main:\n" +
            "    if dc:\n" +
            "      main --a\n" +
//                "    return Device_.init_ this d\n" +
            "\n" +
            "/**\n" +
            " r\n" +
            "*/\n" +
            "interface Device extends L:";

    public void testReturnWithMethodCall() throws IOException {
        var p = parseFile("t", PARSION_RETURN_WiTH_METHOD_CALL);

        checkError(p, "");
    }
    public static final String PARSING_ARGUMENTS_ON_NEW_LINES = "" +
            "main:\n" +
            "  f = method\n" +
            "    p3\n" +
            "  g\n\n\n" +
            "close:\n" +
            "  abc\n";

    public void testParsingTestArgumentOnNewLine() throws IOException {
        var p = parseFile("tmp", PARSING_ARGUMENTS_ON_NEW_LINES);

        checkError(p, "");
    }

    public void testElvis() throws IOException {
        var p = parseFile("tmp", "" +
                "main:\n" +
                "  f = mosi ? mosi.num : -1\n");

        checkError(p, "");
    }

    public void testParsingTestMultipleArgumentOnNewLine() throws IOException {
        var p = parseFile("tmp", "" +
                "main:\n" +
                "  f = method\n" +
//                "      mosi ? mosi.num : -1\n" +
//                "      miso ? miso.num : -1\n" +
                "      clock.num\n" +
                "\n" +
                " \n" +
                "close:\n" +
                "  abc\n");

        checkError(p, "");
    }

    public static final String PARSE_TEST_IF_ELSE = "" +
            "main:\n" +
            "    if count_ < 2:\n" +
            "      return null\n" +
            "    else:\n" +
            "      return m2_ / (count_ - 1)\n";


    public void testIfElse() throws IOException {
        var p = parseFile("t", PARSE_TEST_IF_ELSE);

        checkError(p, "");
    }

    public static final String MULTILINE_PARAMATER_TESTDATA = "" +
            "fun\n" +
            "  --p:\n" +
            " j\n";
    public void testMultilineParameters() throws IOException {
        var p = parseFile("tmp", MULTILINE_PARAMATER_TESTDATA);

        checkError(p, "");
    }

    public void testMultilineListLiteral() throws IOException {
        var p = parseFile("tmp", "" +
                "x ::= [\n" +
                "  1, 2 ]\n" +
                "" +
                "f:");

        checkError(p, "");
    }

    public final static String PARSE_LITERAL_METHOD_INVOCATION = "" +
            "f:\n" +
            "  8.repeat";
    public void testLiteralMethodInvocation() throws IOException {
        var p = parseFile("tmp", PARSE_LITERAL_METHOD_INVOCATION);

        checkError(p, "");
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
