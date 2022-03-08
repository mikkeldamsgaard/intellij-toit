package org.toitlang.intellij.parser;

import java.io.IOException;

public class SimpleParserTest extends ParserTest {
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

    public void testInterfaceParameterLess() throws IOException {
        var p = parseFile("tmp", ""
                + "interface A:\n"
                + "  open\n"
                + "    --x\n"
                + "  close\n"
                + "class B:\n"
        );

        checkError(p, "");
    }

    public void testParsingTestMultipleArgumentOnNewLine() throws IOException {
        var p = parseFile("tmp", "" +
                "main:\n" +
                "  f = method\n" +
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

    public static final String PARSE_OPERATOR_NEWLINE = "" +
            "x ::= a is b\n" +
            "  and b is c";

    public void testOperatorNewline() throws IOException {
        var p = parseFile("t", PARSE_OPERATOR_NEWLINE);
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


    public final static String NEW_PARSER_TEST = "" +
            "import log\n" +

            "export *\n" +
            "export a\n" +
            "  b\n" +
            "export \n" +
            " z\n" +
            "  z\n" +
            "   z\n" +

            "";

    public void testNewParser() throws IOException {
        var p = parseFile("tmp", NEW_PARSER_TEST);

        checkError(p, "");
    }

    public void testNewParserExternalFiles() throws IOException {

    }

    public void testConstructor() throws IOException {
        var p = parseFile("tmp", ""
                + "class A:\n"
                + "  constructor.x .x_:\n"
                + "  constructor .x:\n"
        );

        checkError(p, "");
    }

    public void testConstructorDeafultValues() throws IOException {
        var p = parseFile("tmp", ""
                + "class A:\n"
                + "  constructor .from_=p .to_=byte_array_.size:\n"
        );

        checkError(p, "");
    }

    public void testNamedArgument() throws IOException {
        var p = parseFile("tmp", ""
                + "main:\n"
                + "  a 1 --p=true\n"
        );

        checkError(p, "");
    }

    public void testNonInitializedVar() throws IOException {
        var p = parseFile("tmp", ""
                + "class A:\n"
                + "  a/int\n"
                + "  b := 1\n"
        );

        checkError(p, "");
    }

    public void testDoubleAssignment() throws IOException {
        var p = parseFile("tmp", "" +
                "a := {}\n" +
                "c := 1\n"
        );

        checkError(p, "");
    }

    public void testIndexing() throws IOException {
        var p = parseFile("tmp", "" +
                "c := r[1].b\n"
        );

        checkError(p, "");
    }

    public void testDerefCall() throws IOException {
        var p = parseFile("tmp", "" +
                "c := a.b x\n"
        );

        checkError(p, "");
    }

    public void testConstDeclare() throws IOException {
        var p = parseFile("tmp", ""
                + "f:\n"
                + "  y --p=: \n"
                + "    a\n"
                + "  a := 1 \n"
        );

        checkError(p, "");
    }

    public void testCatch() throws IOException {
        var p = parseFile("tmp", ""
                + "f:\n"
                + "  a := catch: \n"
                + "    a \n"
        );

        checkError(p, "");
    }

    public void testDotParam() throws IOException {
        var p = parseFile("tmp", ""
                + "class A:\n"
                + "  constructor --logger=log.default --pwr_on=null --.reset_n=null: \n"
        );

        checkError(p, "");
    }

    public void testNewLineParam() throws IOException {
        var p = parseFile("tmp", ""
                + "f: \n"
                + "  p := a.b\n"
                + "    1 \n"
        );

        checkError(p, "");
    }

    public void testDotContructorType() throws IOException {
        var p = parseFile("tmp", ""
                + "class a: \n"
                + "  constructor a/b c/d .p_/d:\n"
        );

        checkError(p, "");
    }

    public void testoperatorSlash() throws IOException {
        var p = parseFile("tmp", ""
                + "class a: \n"
                + "  operator / other:\n"
        );

        checkError(p, "");
    }

    public void testSemiColon() throws IOException {
        var p = parseFile("tmp", ""
                + "a := (: a; true )\n"
        );

        checkError(p, "");
    }

    public void testSpacedElvis() throws IOException {
        var p = parseFile("tmp", ""
                + "f:\n" +
                "    a\n" +
                "      ? l\n" +
                "      : z\n" );

        checkError(p, "");
    }

    public void testStringExpression() throws IOException {
        var p = parseFile("tmp", ""
                + "f:= \"\\n\" \n" // Escape
                + "f:= \"\" \n"  // Empty string
                + "f:= \"\"\"\"\"\" \n"  // Empty triple string
                + "f:= \"\"\" \"abc\" \"\"\" \n"  // Triple string with single string in it
                + "f:= \"\"\" \"abc\" \\p \\n abcdefggeug \"\"\" \n"  // Triple string with single string in it++
        );

        checkError(p, "");
    }

    public void testInlineStringExpression() throws IOException {
        var p = parseFile("tmp", ""
                + "f:= \"$n+\" \n"
                + "f:= \"$n[1+2*x[8]]\" \n"
                + "f:= \"$n.k[1+2*x[8]]\" \n"
                + "f:= \"$(n.k[1+2*x[8]]+4)\" \n"
                + "f:= \"$(join \", \")...\" \n"
        );

        checkError(p, "");
    }

    public void testFormat() throws IOException {
        var p = parseFile("tmp", ""
                + "f:= \"$(%02x n)\" \n"
        );

        checkError(p, "");
    }

    public void testNestedComments() throws IOException {
        var p = parseFile("tmp", ""
                + "/** /* */ */ \n"
        );

        checkError(p, "");
    }

    public void testMoreElvis() throws IOException {
        var p = parseFile("tmp", ""+
                "alignment_width := start == pos - 1\n" +
                "            ? 0\n" +
                "            : int.parse_ format start (pos - 1) --on_error=: throw it\n \n"
        );

        checkError(p, "");
    }

    public void testIfAssignemnt() throws IOException {
        var p = parseFile("tmp", ""+
                "f: \n" +
                "  if p := 1:\n" +
                "    return\n"
        );

        checkError(p, "");
    }

    public void testMultilineAssignemnt() throws IOException {
        var p = parseFile("tmp", ""+
                "f := x\n" +
                "  --p=:\n" +
                "     f\n" +
                "  --j=: \n" +
                "     p\n"
        );

        checkError(p, "");
    }

    public void testParenBlock() throws IOException {
        var p = parseFile("tmp", ""+
                "f:\n" +
                "  map_.update id --if_absent=(: return ): | existing |\n" +
                "     f\n"
        );

        checkError(p, "");
    }

    public void testFor()  throws IOException {
        var p = parseFile("tmp", ""+
                "f:\n" +
                "  for i := 1; i< 10; i++: \n" +
                "     f\n"
        );
        checkError(p, "");
    }
}
