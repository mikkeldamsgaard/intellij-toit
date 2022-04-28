package org.toitlang.intellij.parser;

import junit.framework.AssertionFailedError;
import org.toitlang.intellij.lexer.ToitLexerAdapter;
import org.toitlang.intellij.lexer.ToitRestartableLexerAdapter;
import org.toitlang.intellij.psi.ast.ToitAssignmentExpression;
import org.toitlang.intellij.psi.ast.ToitExpression;
import org.toitlang.intellij.psi.ast.ToitVariableDeclaration;


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

    public void testReturnWithMethodCall() {
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

    public void testParsingTestArgumentOnNewLine() {
        var p = parseFile("tmp", PARSING_ARGUMENTS_ON_NEW_LINES);

        checkError(p, "");
    }

    public void testElvis() {
        var p = parseFile("tmp", "" +
                "main:\n" +
                "  f = mosi ? mosi.num : -1\n");

        checkError(p, "");
    }

    public void testInterfaceParameterLess() {
        var p = parseFile("tmp", ""
                + "interface A:\n"
                + "  open\n"
                + "    --x\n"
                + "  close\n"
                + "class B:\n"
        );

        checkError(p, "");
    }

    public void testParsingTestMultipleArgumentOnNewLine() {
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


    public void testIfElse() {
        var p = parseFile("t", PARSE_TEST_IF_ELSE);

        checkError(p, "");
    }

    public static final String PARSE_OPERATOR_NEWLINE = "" +
            "x ::= a is b\n" +
            "  and b is c";

    public void testOperatorNewline() {
        var p = parseFile("t", PARSE_OPERATOR_NEWLINE);
        checkError(p, "");
    }

    public static final String MULTILINE_PARAMATER_TESTDATA = "" +
            "fun\n" +
            "  --p:\n" +
            " j\n";

    public void testMultilineParameters() {
        var p = parseFile("tmp", MULTILINE_PARAMATER_TESTDATA);

        checkError(p, "");
    }

    public void testMultilineListLiteral() {
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

    public void testLiteralMethodInvocation() {
        var p = parseFile("tmp", PARSE_LITERAL_METHOD_INVOCATION);

        checkError(p, "");
    }


    public final static String NEW_PARSER_TEST = "" +
            "import log\n" +
            "import x\n" +

            "export *\n" +
            "export a\n" +
            "  b\n" +
            "export \n" +
            " z\n" +
            "  z\n" +
            "   z\n" +

            "";

    public void testNewParser() {
        var p = parseFile("tmp", NEW_PARSER_TEST);

        checkError(p, "");
    }

    public void testNewParserExternalFiles() {

    }

    public void testConstructor() {
        var p = parseFile("tmp", ""
                + "class A:\n"
                + "  constructor.x .x_:\n"
                + "  constructor .x:\n"
        );

        checkError(p, "");
    }

    public void testConstructorDeafultValues() {
        var p = parseFile("tmp", ""
                + "class A:\n"
                + "  constructor .from_=p .to_=byte_array_.size:\n"
        );

        checkError(p, "");
    }

    public void testNamedArgument() {
        var p = parseFile("tmp", ""
                + "main:\n"
                + "  a 1 --p=true\n"
        );

        checkError(p, "");
    }

    public void testNonInitializedVar() {
        var p = parseFile("tmp", ""
                + "class A:\n"
                + "  a/int\n"
                + "  b := 1\n"
        );

        checkError(p, "");
    }

    public void testDoubleAssignment() {
        var p = parseFile("tmp", "" +
                "a := {}\n" +
                "c := 1\n"
        );

        checkError(p, "");
    }

    public void testIndexing() {
        var p = parseFile("tmp", "" +
                "c := r[1].b\n"
        );

        checkError(p, "");
    }

    public void testDerefCall() {
        var p = parseFile("tmp", "" +
                "c := a.b x\n"
        );

        checkError(p, "");
    }

    public void testConstDeclare() {
        var p = parseFile("tmp", ""
                + "f:\n"
                + "  y --p=: \n"
                + "    a\n"
                + "  a := 1 \n"
        );

        checkError(p, "");
    }

    public void testCatch() {
        var p = parseFile("tmp", ""
                + "f:\n"
                + "  a := catch: \n"
                + "    a \n"
        );

        checkError(p, "");
    }

    public void testDotParam() {
        var p = parseFile("tmp", ""
                + "class A:\n"
                + "  constructor --logger=log.default --pwr_on=null --.reset_n=null: \n"
        );

        checkError(p, "");
    }

    public void testNewLineParam() {
        var p = parseFile("tmp", ""
                + "f: \n"
                + "  p := a.b\n"
                + "    1 \n"
        );

        checkError(p, "");
    }

    public void testDotContructorType() {
        var p = parseFile("tmp", ""
                + "class a: \n"
                + "  constructor a/b c/d .p_/d:\n"
        );

        checkError(p, "");
    }

    public void testoperatorSlash() {
        var p = parseFile("tmp", ""
                + "class a: \n"
                + "  operator / other:\n"
        );

        checkError(p, "");
    }

    public void testSemiColon() {
        var p = parseFile("tmp", ""
                + "a := (: a; true )\n"
        );

        checkError(p, "");
    }

    public void testSpacedElvis() {
        var p = parseFile("tmp", ""
                + "f:\n" +
                "    a\n" +
                "      ? l\n" +
                "      : z\n");

        checkError(p, "");
    }

    public void testStringExpression() {
        var p = parseFile("tmp", ""
                + "f:= \"\\n\" \n" // Escape
                + "f:= \"\" \n"  // Empty string
                + "f:= \"\"\"\"\"\" \n"  // Empty triple string
                + "f:= \"\"\" \"abc\" \"\"\" \n"  // Triple string with single string in it
                + "f:= \"\"\" \"abc\" \\p \\n abcdefggeug \"\"\" \n"  // Triple string with single string in it++
        );

        checkError(p, "");
    }

    public void testInlineStringExpression() {
        var p = parseFile("tmp", ""
                + "f:= \"$n+\" \n"
                + "f:= \"$n[1+2*x[8]]\" \n"
                + "f:= \"$n.k[1+2*x[8]]\" \n"
                + "f:= \"$(n.k[1+2*x[8]]+4)\" \n"
                + "f:= \"$(join \", \")...\" \n"
        );

        checkError(p, "");
    }

    public void testFormat() {
        var p = parseFile("tmp", ""
                + "f:= \"$(%02x n)\" \n"
        );

        checkError(p, "");
    }

    public void testNestedComments() {
        var p = parseFile("tmp", ""
                + "/** /* */ */ \n"
        );

        checkError(p, "");
    }

    public void testMoreElvis() {
        var p = parseFile("tmp", "" +
                "alignment_width := start == pos - 1\n" +
                "            ? 0\n" +
                "            : int.parse_ format start (pos - 1) --on_error=: throw it\n \n"
        );

        checkError(p, "");
    }

    public void testIfAssignemnt() {
        var p = parseFile("tmp", "" +
                "f: \n" +
                "  if p := 1:\n" +
                "    return\n"
        );

        checkError(p, "");
    }

    public void testMultilineAssignemnt() {
        var p = parseFile("tmp", "" +
                "f := x\n" +
                "  --p=:\n" +
                "     f\n" +
                "  --j=: \n" +
                "     p\n"
        );

        checkError(p, "");
    }

    public void testParenBlock() {
        var p = parseFile("tmp", "" +
                "f:\n" +
                "  map_.update id --if_absent=(: return ): | existing |\n" +
                "     f\n"
        );

        checkError(p, "");
    }

    public void testFor() {
        var p = parseFile("tmp", "" +
                "f:\n" +
                "  for i := 1; i< 10; i++: \n" +
                "     f\n"
        );
        checkError(p, "");
    }

    public void testMapLiteral() {
        var p = parseFile("tmp", "" +
                "respond_error res/RestResponse error/string:\n" +
                "  respond_ res {\n" +
                "      1: 2\n" +
                "    ,\n" +
                "      2: 3,\n" +
                "      x: y,\n" +
                "    }\n"+
                "  f := {\n" +
                "   :\n" +
                "    }\n" +
                "  g := {\n" +
                "    }\n"
        );
        checkError(p, "");
    }

    public void testRecursion() {
        var p = parseFile("tmp", "" +
                "main:\n" +
                "    unresolved\n" +
                "    x := [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[ ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]\n" +
                "    unresolved\n"
        );
        //checkError(p, "");
    }

    public void testIndentPipe() {
        var p = parseFile("tmp", "" +
                "x := :: |\n" +
                "     x\n" +
                "      |\n" +
                "    unresolved\n"
        );
        checkError(p, "");
    }

    public void testRedundantSemiColon() {
        var p = parseFile("tmp", "" +
                "x:\n" +
                "  c1;c2\n" +
                "  c3;\n"
        );
        checkError(p, "");
    }

    public void testNamedArgsOnNewline() {
        var p = parseFile("tmp", "" +
                "x:\n" +
                "  c1\n" +
                "    --j=1\n"+
                "    --p=not f\n"
        );
        checkError(p, "");
    }

    public void testEndsWithComment() {
        var p = parseFile("tmp", "" +
                "x:\n" +
                "  //a\n"
        );
        checkError(p, "");
    }

    public void testOperatorParseError() {
        var p = parseFile("tmp", "" +
                "class x:\n" +
                "  operator\n"
        );
        //checkError(p, "");
    }

    public void testLexerWeirdnessError() {
        var p = parseFile("tmp", "" +
                "main:\n" +
                "            \"\"\"\\"
        );
        //checkError(p, "");
    }

    public void testUnaryWithSpace() {
        var p = parseFile("tmp", "" +
                "main: a := a +- 1\n"
        );

        boolean gotException = false;
        try {
            checkError(p, "");
        } catch (AssertionFailedError e) {
            gotException = true;
        }
        if (!gotException) fail("Expected parse error");

    }


    public void testMinusExpression() {
        var p = parseFile("tmp", "" +
                "x ::= a-1"
        );
        checkError(p, "");
    }

    public void testDashInParams() {
        var p = parseFile("tmp", "" +
                "x --p-a:"
        );
        checkError(p, "");
    }

    public void testParenNoSpace() {
        var p = parseFile("tmp", "" +
                "a := f(1)"
        );

        assertTrue(p.getFirstChild() instanceof ToitVariableDeclaration);
        ToitVariableDeclaration vd = (ToitVariableDeclaration) p.getFirstChild();
        var e = vd.getFirstChildOfType(ToitExpression.class);
        assertEquals("f(1)", e.getText());

    }
}
