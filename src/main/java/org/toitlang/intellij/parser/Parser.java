package org.toitlang.intellij.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Producer;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.toitlang.intellij.psi.ToitTypes;

import static org.toitlang.intellij.psi.ToitTypes.*;

import java.util.function.Function;

public class Parser {
    private final IElementType root;
    private final ToitPsiBuilderAdapter builder;
    private int currentBaseIndentLevel = 0; // The indent level for the current "block"
    private int currentIndentLevel = 0; // The current indent level
    // Invariant: currentIndentLevel >= currentBaseIndentLevel
    private int currentLineNo = 1;
    private boolean currentTokenIsAttached = false;
    private boolean currentIsAtBeggingOfLine = true;

    public Parser(IElementType root, ToitPsiBuilderAdapter builder) {
        this.root = root;
        this.builder = builder;
    }

    public void parse() {
        var root = mark();
        if (is(TokenSet.WHITE_SPACE)) consume();
        importsAndExports();
        declarations();
        root.done(this.root);
    }

    private void importsAndExports() {
        while (!builder.eof()) {
            if (is(ToitTypes.IMPORT)) {
                if (!importDeclaration()) skipToDedent();
            } else if (is(EXPORT)) {
                if (!exportDeclaration()) skipToDedent();
            } else return;
        }
    }

    private boolean importDeclaration() {
        var importPart = mark();
        consumeAllowNewlines();

        if (is(DOT) || is(DOT_DOT)) {
            var m = mark();
            while (is(DOT) || is(DOT_DOT)) consumeAllowNewlines();
            m.done(ToitTypes.LOCAL_IMPORT);
        }

        var qn = mark();
        while (true) {
            if (!variableName(true)) {
                qn.error("Expected identifier");
                return error("Expected identifier", qn, importPart);
            }
            if (!is(ToitTypes.DOT)) break;
            consumeAllowNewlines();
        }
        qn.done(ToitTypes.QUALIFIED_NAME);

        if (is(ToitTypes.AS)) {
            var as = mark();
            consumeAllowNewlines();

            if (!variableName(true)) return error("Expected identifier after as", as, importPart);

            as.done(ToitTypes.IMPORT_AS);
        } else if (isIdentifier("show")) {
            var show = mark();
            consumeAllowNewlines();

            if (is(STAR)) consumeAllowNewlines();
            else {
                if (!variableName(true)) return drop(show, importPart);

                while (isIdentifier() && !atNextStatement()) {
                    variableName(true);
                }
            }
            show.done(ToitTypes.IMPORT_SHOW);
        }

        if (isNewLine()) consumeAllowNewlines();

        importPart.done(ToitTypes.IMPORT_DECLARATION);
        return true;
    }

    private boolean exportDeclaration() {
        var export = mark();
        consumeAllowNewlines();

        if (is(ToitTypes.STAR)) {
            var m = mark();
            consumeAllowNewlines();
            m.done(ToitTypes.EXPORT_STAR);
        } else if (isIdentifier()) {
            variableName(true);
            while (isIdentifier()) variableName(true);
        } else {
            export.error("Expected * or identifier");
            return false;
        }

        if (isNewLine()) consumeAllowNewlines();
        export.done(ToitTypes.EXPORT_DECLARATION);
        return true;
    }

    private void declarations() {
        while (!builder.eof()) {
            consumeDedents();
            if (builder.eof()) break;
            if (is(ABSTRACT) || is(CLASS)) {
                if (!classDeclaration()) skipToDedent();
            } else if (isIdentifier("interface")) {
                if (!interfaceDeclaration()) skipToDedent();
            } else if (isIdentifier("monitor")) {
                if (!monitorDeclaration()) skipToDedent();
            } else if (isNewLine()) {
                // Empty files?
                consumeAllowNewlines();
            } else {
                if (!variableDeclarationOrFunctionDeclaration()) skipToDedent();
            }
        }
    }

    private boolean variableDeclarationOrFunctionDeclaration() {
        if (is(ABSTRACT)) return functionDeclaration();

        var m = mark();
        if (is(STATIC)) consumeAllowNewlines();
        if (!isIdentifier())
            return m.error("Expected variable/constant declaration or method declaration, found " + tokenType());
        if (isIdentifier("operator")) {
            m.rollback();
            return functionDeclaration();
        }
        consumeAllowNewlines();
        if (is(CONST_DECLARE) || is(DECLARE) || is(SLASH)) {
            m.rollback();
            return variableDeclaration(true);
        } else {
            m.rollback();
            return functionDeclaration();
        }
    }

    private boolean variableDeclaration(boolean allowBlock) {
        var assignment = mark();
        var multiLine = startMultilineConstruct();

        if (is(STATIC)) consumeAllowNewlines();

        if (!variableName(true)) return assignment.drop();

        if (is(SLASH)) {
            consumeAllowNewlines();
            if (!typeName(false)) return assignment.error("Expected type name");
            if (is(QUESTION)) consumeAllowNewlines();
        }

        if (!is(DECLARE) && !is(CONST_DECLARE) && !multiLine.atStartOfNextLine())
            return assignment.error("Expected ::=, := or new line");

        if (!multiLine.atStartOfNextLine()) {
            if (assignmentOperator()) {
                if (is(QUESTION)) consumeAllowNewlines();
                else if (!expression(allowBlock)) return assignment.drop();
            }
            if (isNewLine()) consumeAllowNewlines();
            consumeDedents();
            if (!atStatementTerminator(allowBlock)) return assignment.error("Expected newline or ';'");
        }
        return assignment.done(VARIABLE_DECLARATION);
    }

    private static final TokenSet OVERLOADABLE_OPERATORS =
            TokenSet.create(EQUALS_EQUALS, SLASH, PLUS,
                    MINUS, PERCENT, STAR, GREATER_OR_EQUALS, GREATER,
                    LESS_OR_EQUALS, LESS, HAT, PIPE, AMPERSAND,
                    GREATER_GREATER, GREATER_GREATER_GREATER,
                    LESS_LESS, LESS_LESS_LESS, TILDE);

    private boolean functionDeclaration() {
        var function = mark();
        var multiLine = startMultilineConstruct();
        boolean constructor = false;
        boolean abstract_ = false;
        boolean static_ = false;
        if (is(ABSTRACT)) {
            consumeAllowNewlines();
            abstract_ = true;
        } else if (is(STATIC)) {
            consumeAllowNewlines();
            static_ = true;
        }
        if (isIdentifier("constructor")) {
            if (abstract_) return function.error("Constructors may not be abstract");
            if (static_) return function.error("Constructors may not be static");
            consume();
            if (is(NEWLINE)) {
                // constructor
                //       --param:
                consumeAllowNewlines();
            } else if (is(DOT) && currentTokenIsAttached) {
                // constructor.type
                consumeAllowNewlines();
                if (!variableName(true)) return function.error("Expected constructor override name");
            }
            constructor = true;
        } else if (isIdentifier("operator")) {
            if (static_) return function.error("Operators may not be static");
            consumeAllowNewlines();
            if (is(OVERLOADABLE_OPERATORS)) {
                consumeAllowNewlines();
            } else if (isSequence(LBRACKET, DOT_DOT, RBRACKET) ||
                    isSequence(LBRACKET, RBRACKET, EQUALS)) {
                consume();
                consume();
                consumeAllowNewlines();
            } else if (isSequence(LBRACKET, RBRACKET)) {
                consume();
                consumeAllowNewlines();
            } else {
                function.error("Invalid overloaded operator");
            }
        } else {
            if (!variableName(true)) return function.error("Expected function name");

            if (is(EQUALS) && currentTokenIsAttached) consumeAllowNewlines(); // Setter
        }

        while (!is(COLON) && !(multiLine.isDone())) {
            if (is(LBRACKET)) {
                // [name]
                consumeAllowNewlines();
                if (!parameterName()) return function.drop();

                if (!is(RBRACKET)) return function.error("Missing ']' for block parameter'");
                consumeAllowNewlines();
            } else if (is(RETURN_TYPE)) {
                consumeAllowNewlines();
                if (!typeName(false)) return function.error("Expected return type name");
                if (is(QUESTION)) consumeAllowNewlines();
            } else {
                if (!parameterName()) return function.drop();

                if (is(SLASH)) { // Type
                    consumeAllowNewlines();
                    if (!typeName(constructor)) return function.error("Expected type name");
                    if (is(QUESTION)) consumeAllowNewlines();
                }

                if (is(EQUALS)) { // Default value
                    consumeAllowNewlines();
                    if (!postfixExpression(true)) return function.error("Expected expression");
                }
            }
            if (isNewLine()) consumeAllowNewlines();
        }

        if (is(COLON)) {
            if (!block(false, this::functionStatement)) return function.drop();
        }

        return function.done(FUNCTION_DECLARATION);
    }

    private boolean parameterName() {
        var parameter = mark();
        if (is(MINUS_MINUS)) {
            consume();
            if (is(DOT) && currentTokenIsAttached) consume();
            if (!(isIdentifier() && currentTokenIsAttached)) return parameter.error("Named parameter format error");
        } else {
            if (is(DOT)) {
                consume();
                if (!(isIdentifier() && currentTokenIsAttached)) return parameter.error("Field parameter format error");
            } else if (!isIdentifier())
                return parameter.error("Expected parameter name, got " + tokenType());
        }
        variableName(true);
        return parameter.done(PARAMETER_NAME);
    }


    private boolean typeName(boolean requireAttached) {
        var type = mark();
        if (!variableName(true)) return type.drop();

        while (!builder.eof()) {
            if (!is(DOT)) break;
            if (requireAttached && !currentTokenIsAttached) break;
            consumeAllowNewlines();
            if (!variableName(true)) return type.error("Expected qualified type");
        }
        return type.done(QUALIFIED_NAME);
    }

    private boolean block(boolean allowParameters, Producer<Boolean> elementParser) {
        var block = mark();
        if (!expect(COLON)) return block.error("Expected :");
        return blockOrLambdaBody(allowParameters, elementParser, block);
    }

    private boolean lambda() {
        var lambda = mark();
        if (!expect(COLON_COLON)) return lambda.error("Expected :");
        return blockOrLambdaBody(true, this::functionStatement, lambda);
    }

    private static final TokenSet PARAMETER_TOKEN = TokenSet.create(PIPE);

    private boolean blockOrLambdaBody(boolean allowParameters, Producer<Boolean> elementParser, Marker block) {
        int startLine = currentLineNo;
        int prevBaseIndentLevel = currentBaseIndentLevel;
        if (isAllowingNewlines(PARAMETER_TOKEN)) {
            if (!allowParameters) return block.error("Parameter specification is not allowed here");
            consumeAllowNewlines();
            while (!builder.eof() && !is(PIPE)) {
                if (!variableName(true)) return block.error("Expected parameter name");
                if (is(SLASH)) {
                    consumeAllowNewlines();
                    if (!typeName(false)) return block.error("Expected type name");
                }
            }

            if (!is(PIPE)) return block.error("Missing closing '|'");
            consume();
        }

        if (startLine != currentLineNo || isNewLine()) {
            if (startLine != currentLineNo) {

                if (is(INDENT)) {
                    assert (currentBaseIndentLevel == currentIndentLevel);
                    consume();
                    currentBaseIndentLevel++;
                    currentIndentLevel++;
                } else {
                    if (currentBaseIndentLevel == currentIndentLevel) {
                        // Empty
                        return block.done(BLOCK);
                    } else {
                        if (currentBaseIndentLevel + 1 != currentIndentLevel)
                            throw new RuntimeException("Assertion failed");

                    }
                }
            } else { // isNewline
                consumeAllowNewlines();
                if (currentBaseIndentLevel == currentIndentLevel) {
                    // empty
                    return block.done(BLOCK);
                }
                currentBaseIndentLevel = currentIndentLevel;
            }

            try {
                while (!isDedent()) {
                    if (!elementParser.produce()) return block.drop();
                }
                consume();
            } finally {
                currentBaseIndentLevel = prevBaseIndentLevel;
                currentIndentLevel--;
            }

        } else {
            // Single line
            if (!elementParser.produce()) return block.drop();
        }

        return block.done(BLOCK);
    }

    private boolean functionStatement() {
        while (!builder.eof()) {
            consumeDedents();
            var m = mark();
            if (is(WHILE)) {
                if (!whileStatement()) return m.drop();
            } else if (is(IF)) {
                if (!ifStatement()) return m.drop();
            } else if (is(RETURN)) {
                if (!returnStatement()) return m.drop();
            } else if (is(ASSERT)) {
                if (!assertStatement()) return m.drop();
            } else if (is(FOR)) {
                if (!forStatement()) return m.drop();
            } else if (is(BREAK) || is(CONTINUE)) {
                consumeAllowNewlines();
                if (is(DOT) && currentTokenIsAttached) {
                    consume();
                    if (!variableName(true)) return m.error("Expected label name");
                    tryRule(() -> expression(true));
                }

            } else if (is(TRY)) {
                if (!tryStatement()) return m.drop();
            } else if (isNewLine() || is(SEMICOLON)) {
                var empty = mark();
                consumeAllowNewlines();
                empty.done(EMPTY_STATEMENT);
            } else {
                if (!tryRule(() -> variableDeclaration(true))) {
                    if (!expression(true)) return m.drop();
                }
            }
            m.drop();
            if (!is(SEMICOLON)) {
                break;
            }
            consume();
        }
        if (is(NEWLINE)) consume();
        if (isDedent()) consumeDedents();
        return true;
    }

    private boolean smellsLikeVariableDeclaration() {
        var m = mark();
        boolean res = false;
        if (expect(IDENTIFIER)) {
            if (is(DECLARE) || is(CONST_DECLARE) || is(SLASH)) res = true;
            m.rollback();
        } else m.drop();
        return res;
    }

    private boolean tryStatement() {
        var try_ = mark();
        consumeAllowNewlines();
        if (!block(false, this::functionStatement)) return try_.drop();

        if (!is(FINALLY)) try_.error("Missing finally for try");
        consumeAllowNewlines();
        if (!block(true, this::functionStatement)) return try_.drop();

        return try_.done(TRY_STATEMENT);
    }

    private boolean forStatement() {
        var for_ = mark();
        consumeAllowNewlines();

        if (!is(SEMICOLON)) {
            if (!tryRule(() -> variableDeclaration(true))) {
                if (!expression(false)) return for_.drop();
            }
        }
        if (!is(SEMICOLON)) return for_.error("Expected ;");
        consumeAllowNewlines();

        if (!is(SEMICOLON)) {
            if (!expression(false)) return for_.drop();
        }
        if (!is(SEMICOLON)) return for_.error("Expected ;");
        consumeAllowNewlines();

        if (!is(COLON)) {
            if (!expression(false)) return for_.drop();
        }
        if (!block(false, this::functionStatement)) return for_.drop();

        return for_.done(FOR_STATEMENT);
    }

    private boolean assertStatement() {
        var assert_ = mark();
        consumeAllowNewlines();
        if (!block(false, this::functionStatement)) return assert_.error("Failed to parse block");
        return assert_.done(ASSERT_STATEMENT);
    }

    private boolean returnStatement() {
        var return_ = mark();
        consumeAllowNewlines();
        if (atStatementTerminator(true)) return return_.done(RETURN_STATEMENT);
        if (!expression(true)) return return_.drop();
        return return_.done(RETURN_STATEMENT);
    }

    private boolean ifStatement() {
        var if_ = mark();
        consumeAllowNewlines();
        if (!tryRule(() -> variableDeclaration(false))) {
            if (!expression(false)) return if_.error("Expected expression for if");
        }
        if (!block(false, this::functionStatement)) return if_.drop();

        boolean last = false;
        while (!last && is(ELSE)) {
            consumeAllowNewlines();
            if (is(IF)) {
                consumeAllowNewlines();
                if (!expression(false)) return if_.error("Expected expression for else if");
            } else last = true;

            if (!block(false, this::functionStatement)) return if_.drop();
        }
        return if_.done(IF_STATEMENT);
    }

    private boolean whileStatement() {
        var while_ = mark();
        consumeAllowNewlines();
        if (!tryRule(() -> variableDeclaration(false))) {
            if (!expression(false)) return while_.error("Expected expression");
        }
        if (!block(false, this::functionStatement)) return while_.drop();
        return while_.done(WHILE_STATEMENT);
    }

    private boolean interfaceOrClassOrMonitor(Producer<Boolean> elementParser, String type, IElementType toitType) {
        var interface_ = mark();
        if (is(ABSTRACT)) {
            consumeAllowNewlines();
            if (!is(CLASS)) return interface_.error("Expected class");
            consumeAllowNewlines();
        } else {
            consumeAllowNewlines(); // class
        }

        if (!variableName(true)) return interface_.error("Expected " + type + " name");
        if (isIdentifier("extends")) {
            consumeAllowNewlines();
            if (!typeName(false)) return interface_.error("Expected " + type + " name");
        }
        if (isIdentifier("implements")) {
            consumeAllowNewlines();
            if (!typeName(false)) return interface_.error("Expected interface name");

            while (isIdentifier()) {
                typeName(false);
            }
        }
        if (!block(false, elementParser)) return interface_.drop();
        return interface_.done(toitType);
    }

    private boolean interfaceDeclaration() {
        return interfaceOrClassOrMonitor(this::interfaceMemberDeclaration, "interface", INTERFACE_DECLARATION);
    }

    private boolean classDeclaration() {
        return interfaceOrClassOrMonitor(this::classMemberDeclaration, "class", CLASS_DECLARATION);
    }

    private boolean monitorDeclaration() {
        return interfaceOrClassOrMonitor(this::classMemberDeclaration, "monitor", MONITOR_DECLARATION);
    }

    private boolean interfaceMemberDeclaration() {
        return variableDeclarationOrFunctionDeclaration();
    }

    private boolean classMemberDeclaration() {
        // TODO: constructor
        return variableDeclarationOrFunctionDeclaration();
    }

    private boolean expression(boolean allowBlock) {
        var expr = mark();
        if (!conditionalExpression(allowBlock)) return expr.drop();
        return expr.done(EXPRESSION);
    }

    private final static TokenSet ELVIS_OPERATOR = TokenSet.create(QUESTION);
    private final static TokenSet ELVIS_ARGUMENT_SEPARATOR = TokenSet.create(COLON);
    private boolean conditionalExpression(boolean allowBlock) {
        var expr = mark();
        if (!orExpression(allowBlock)) return expr.drop();

        if (isAllowingNewlines(ELVIS_OPERATOR)) {
            consumeAllowNewlines();

            if (!expression(false)) return expr.drop();

            if (!isAllowingNewlines(ELVIS_ARGUMENT_SEPARATOR)) return expr.error("Expected : in elvis operator");
            consumeAllowNewlines();

            if (!expression(true)) return expr.drop();

            return expr.done(ELVIS_EXPRESSION);
        } else return expr.collapse();
    }

    private static final TokenSet OR_OPERATORS = TokenSet.create(OR);

    private boolean orExpression(boolean allowBlock) {
        return binaryPrecedenceExpression(allowBlock, this::andExpression, OR_OPERATORS, OR_EXPRESSION, false);
    }

    private static final TokenSet AND_OPERATORS = TokenSet.create(AND);

    private boolean andExpression(boolean allowBlock) {
        return binaryPrecedenceExpression(allowBlock, this::notExpression, AND_OPERATORS, AND_EXPRESSION, false);
    }

    private boolean notExpression(boolean allowBlock) {
        var expr = mark();

        if (is(NOT)) {
            consumeAllowNewlines();
            if (!callExpression(allowBlock)) return expr.drop();
            return expr.done(NOT_EXPRESSION);
        } else {
            expr.drop();
            return callExpression(allowBlock);
        }
    }

    private boolean callExpression(boolean allowBlock) {
        var expr = mark();
        if (!assignmentExpression(allowBlock)) return expr.drop();
        if (atStatementTerminator(allowBlock)) return expr.collapse();
        if (!isNewLine() && !(allowBlock && is(COLON)) && !is(COLON_COLON) && currentTokenIsAttached)
            return expr.collapse();
        var m = mark();
        boolean areArgumentsOnNewLines = false;
        int numArgumnets = 0;
        while (true) { // Multiline check?
            if (atStatementTerminator(allowBlock)) break;
            if (areArgumentsOnNewLines) {
                if (isNewLine()) consumeAllowNewlines();
                if (currentIndentLevel == currentBaseIndentLevel) break;
            } else {
                // NEWLINE INDENT indicates arguments on new lines
                var m2 = mark();
                if (expect(NEWLINE) && expect(INDENT)) {
                    currentIndentLevel++;
                    areArgumentsOnNewLines = true;
                    m2.drop();
                } else m2.rollback();
            }

            var namedArgument = mark();
            if (expect(MINUS_MINUS)) {
                if (!variableName(true)) return error("Variable name expected", expr, m, namedArgument);
                if (is(EQUALS)) {
                    consumeAllowNewlines();
                    if (!(areArgumentsOnNewLines ? expression(allowBlock) : assignmentExpression(allowBlock)))
                        return error("Expected expression in named parameter", expr, m, namedArgument);
                }
                namedArgument.done(NAMED_ARGUMENT);
                numArgumnets++;
            } else {
                namedArgument.drop();
                if (areArgumentsOnNewLines) {
                    if (!tryRule(() -> expression(allowBlock))) break;
                } else {
                    if (!tryRule(() -> assignmentExpression(allowBlock))) break;
                }
                numArgumnets++;
            }
        }
        if (numArgumnets > 0) {
            m.drop();
            return expr.done(CALL_EXPRESSION);
        } else {
            m.rollback();
            return expr.collapse();
        }
    }


    private static final TokenSet ASSIGNMENT_OPERATORS =
            TokenSet.create(EQUALS, ADD_ASSIGN, SUB_ASSIGN,
                    MUL_ASSIGN, DIV_ASSIGN, REMAINDER_ASSIGN,
                    OR_ASSIGN, AND_ASSIGN, NOT_ASSIGN,
                    SHIFT_LEFT_ASSIGN, SHIFT_RIGHT_ASSIGN, SHIFT_SHIFT_RIGHT_ASSIGN);

    private boolean assignmentExpression(boolean allowBlock) {
        return binaryPrecedenceExpression(allowBlock, this::equalityExpression, ASSIGNMENT_OPERATORS, ASSIGNMENT_EXPRESSION, true);
    }

    private static final TokenSet EQUALITY_OPERATORS = TokenSet.create(EQUALS_EQUALS, NOT_EQUALS);

    private boolean equalityExpression(boolean allowBlock) {
        return binaryPrecedenceExpression(allowBlock, this::relationalExpression, EQUALITY_OPERATORS, EQUALITY_EXPRESSION, false);
    }

    private static final TokenSet RELATIONAL_OPERATORS =
            TokenSet.create(AS, LESS, GREATER, LESS_OR_EQUALS,
                    GREATER_OR_EQUALS, IS, IS_NOT);

    private boolean relationalExpression(boolean allowBlock) {
        return binaryPrecedenceExpression(allowBlock, this::bitOrExpression, RELATIONAL_OPERATORS, RELATIONAL_EXPRESSION, false);
    }

    private static final TokenSet BIT_OR_OPERATORS = TokenSet.create(PIPE);

    private boolean bitOrExpression(boolean allowBlock) {
        return binaryPrecedenceExpression(allowBlock, this::bitXorExpression, BIT_OR_OPERATORS, BIT_OR_EXPRESSION, false);
    }

    private static final TokenSet BIT_XOR_OPERATORS = TokenSet.create(HAT);

    private boolean bitXorExpression(boolean allowBlock) {
        return binaryPrecedenceExpression(allowBlock, this::bitAndExpression, BIT_XOR_OPERATORS, BIT_XOR_EXPRESSION, false);
    }

    private static final TokenSet BIT_AND_OPERATORS = TokenSet.create(AMPERSAND);

    private boolean bitAndExpression(boolean allowBlock) {
        return binaryPrecedenceExpression(allowBlock, this::bitShiftExpression, BIT_AND_OPERATORS, BIT_AND_EXPRESSION, false);
    }

    private static final TokenSet BIT_SHIFT_OPERATORS = TokenSet.create(LESS_LESS, LESS_LESS_LESS, GREATER_GREATER, GREATER_GREATER_GREATER);

    private boolean bitShiftExpression(boolean allowBlock) {
        return binaryPrecedenceExpression(allowBlock, this::additiveExpression, BIT_SHIFT_OPERATORS, BIT_SHIFT_EXPRESSION, false);
    }

    private static final TokenSet ADDITIVE_OPERATORS = TokenSet.create(PLUS, MINUS);

    private boolean additiveExpression(boolean allowBlock) {
        return binaryPrecedenceExpression(allowBlock, this::multiplicativeExpression, ADDITIVE_OPERATORS, ADDITIVE_EXPRESSION, false);
    }

    private static final TokenSet MULTIPLICATIVE_OPERATORS = TokenSet.create(SLASH, STAR, PERCENT);

    private boolean multiplicativeExpression(boolean allowBlock) {
        return binaryPrecedenceExpression(allowBlock, this::postfixExpression, MULTIPLICATIVE_OPERATORS, MULTIPLICATIVE_EXPRESSION, false);
    }

    private boolean binaryPrecedenceExpression(boolean allowBlock, Function<Boolean, Boolean> nextPrecedenceLevel,
                                               TokenSet operatorSet, IElementType expressionType, boolean rightHandFullExpression) {
        var expr = mark();
        //if (!nextPrecedenceLevel.apply(allowBlock)) return expr.drop();
        if (!nextPrecedenceLevel.apply(allowBlock)) return expr.drop();
        if (atStatementTerminator(allowBlock)) return expr.collapse();
        if (isAllowingNewlines(operatorSet)) {
            while (isAllowingNewlines(operatorSet)) {
                var oper = mark();
                if (isNewLine()) consumeAllowNewlines();
                consumeAllowNewlines();
                oper.done(operator(tokenType()));
                if (rightHandFullExpression) {
                    if (!expression(allowBlock)) return expr.drop();
                } else {
                    if (!nextPrecedenceLevel.apply(allowBlock)) return expr.drop();
                }
            }
            return expr.done(expressionType);
        } else return expr.collapse();
    }

    private final static TokenSet POSTFIX_OPERATORS = TokenSet.create(LBRACKET, PLUS_PLUS, MINUS_MINUS, DOT);

    private boolean postfixExpression(boolean allowBlock) {
        var expr = mark();
        if (!unaryExpression(allowBlock)) return expr.drop();
        while (currentTokenIsAttached && is(POSTFIX_OPERATORS)) {
            if (is(LBRACKET)) {
                if (!indexing(allowBlock)) return expr.drop();
            } else if (is(DOT)) {
                if (!deref()) return expr.drop();
            } else if (is(PLUS_PLUS) || is(MINUS_MINUS)) {
                var m = mark();
                consume();
                m.done(POSTFIX_EXPRESSION);
            }
        }
        return expr.collapse();
    }

    private boolean deref() {
        var deref = mark();
        if (isNewLine()) consumeAllowNewlines();
        consumeAllowNewlines();
        if (!variableName(false)) return deref.error("Expected expression after .");
        return deref.done(DEREF_EXPRESSION);
    }

    private boolean indexing(boolean allowBlock) {
        var indexer = mark();
        consumeAllowNewlines();

        if (!is(DOT_DOT)) {
            if (!expression(allowBlock)) return indexer.drop();
        }

        if (is(DOT_DOT)) {
            var oper = mark();
            consumeAllowNewlines();
            oper.done(operator(DOT_DOT));

            if (!is(RBRACKET)) {
                if (!expression(allowBlock)) return indexer.drop();
            }
        }

        if (!expect(RBRACKET)) return indexer.error("Expected ']''");
        return indexer.done(INDEXING_EXPRESSION);
    }

    private static final TokenSet UNARY_OPERATORS =
            TokenSet.create(MINUS, MINUS_MINUS, PLUS_PLUS, TILDE);

    private boolean unaryExpression(boolean allowBlock) {
        var unary = mark();
        if (is(UNARY_OPERATORS)) {
            consumeAllowNewlines();
            if (!primaryExpression(allowBlock)) return unary.error("Expected expression after unary operator");
            return unary.done(UNARY_EXPRESSION);
        } else {
            unary.drop();
            return primaryExpression(allowBlock);
        }
    }

    private boolean primaryExpression(boolean allowBlock) {
        var expression = mark();
        if (is(LPAREN)) {
            consumeAllowNewlines();
            if (!expression(true)) return expression.drop();
            if (!is(RPAREN)) return expression.error("Expected )");
            consume();
        } else if (is(STRING_START)) {
            if (!string()) return expression.drop();
        } else if (is(INTEGER) || is(FLOAT) || is(NULL) || is(BOOLEAN) || is(CHARACTER)) {
            var literal = mark();
            consume();
            literal.done(ToitTypes.simple_literal(tokenType()));
        } else if (is(LCURLY)) {
            if (!setOrMapLiteral()) return expression.drop();
        } else if (is(LBRACKET)) {
            if (!listLiteral()) return expression.drop();
        } else if (is(HASH)) {
            consumeAllowNewlines();
            if (!listLiteral()) return expression.drop();
        } else if (isIdentifier()) {
            variableName(false);
        } else if (is(PRIMITIVE)) {
            var primitive = mark();
            consume();
            while (!builder.eof() && is(DOT) && currentTokenIsAttached) {
                consumeAllowNewlines();
                if (!variableName(false)) return error("Incorrect primitive declaration", primitive, expression);
            }
            if (is(COLON)) {
                if (!block(false, this::functionStatement)) return drop(primitive, expression);
            }
            primitive.done(PRIMITIVE_STATEMENT);
        } else if (allowBlock && is(COLON)) {
            if (!block(true, this::functionStatement)) return expression.drop();
        } else if (is(COLON_COLON)) {
            if (!lambda()) return expression.drop();
        } else {
            return expression.error("Expected literal, variable or nested expression, got " + tokenType().toString());
        }

        return expression.done(EXPRESSION);
    }

    private boolean string() {
        var string = mark();
        consume();
        while (!builder.eof() && !is(STRING_END)) {
            if (is(STRING_PART)) consume();
            if (!expression(true)) return string.drop();
        }
        if (!is(STRING_END)) return string.error("Missing closing \"");
        consume();
        return string.done(ToitTypes.simple_literal(STRING_END));
    }

    private boolean setOrMapLiteral() {
        var literal = mark();
        consumeAllowNewlines();

        // Special case, empty map/set
        if (is(RCURLY)) {
            consumeAllowNewlines();
            return literal.done(SET_LITERAL);
        }
        if (is(COLON)) {
            consumeAllowNewlines();
            if (!is(RCURLY)) return literal.error("Missing closing '}");
            consumeAllowNewlines();
            return literal.done(MAP_LITERAL);
        }

        boolean map = false;
        boolean first = true;
        while (!builder.eof() && !is(RCURLY)) {
            if (is(LCURLY)) break; // Trailing comma handling

            if (!expression(false)) return literal.drop();

            if (first) {
                if (is(COLON)) {
                    consumeAllowNewlines();
                    map = true;
                }
                first = false;
            } else {
                if (map && !is(COLON)) return literal.error("Expected element separator ':'");
                consumeAllowNewlines();
            }

            if (!expression(false)) return literal.drop();

            if (!is(COMMA)) break;
            consumeAllowNewlines();
        }

        if (!is(RCURLY)) {
            return mark().error("Missing closing '}'");
        }
        consumeAllowNewlines();

        return literal.done(map ? MAP_LITERAL : SET_LITERAL);
    }

    private boolean listLiteral() {
        var list = mark();
        consumeAllowNewlines();

        while (!builder.eof()) {
            if (is(RBRACKET)) {
                break;
            }
            if (!expression(true)) return list.drop();
            if (!is(COMMA)) break;
            consumeAllowNewlines();
        }
        if (isNewLine()) consumeAllowNewlines();
        if (!expect(RBRACKET)) return list.error("Expected ]");

        return list.done(LIST_LITERAL);
    }

    private boolean assignmentOperator() {
        var assignmentOperator = mark();
        if (is(DECLARE) || is(CONST_DECLARE)) {
            consumeAllowNewlines();
            return assignmentOperator.done(ToitTypes.operator(tokenType()));
        }
        return assignmentOperator.collapse();
    }

    private boolean variableName(boolean allowNewlines) {
        var i = mark();
        if (!isIdentifier()) return i.error("Expected identifier");

        if (allowNewlines) consumeAllowNewlines();
        else consume();

        return i.done(ToitTypes.VARIABLE_NAME);
    }

    boolean tryRule(Producer<Boolean> rule) {
        var m = mark();
        boolean res = rule.produce();
        if (res) {
            m.drop();
        } else {
            m.rollback();
        }
        return res;
    }

    private boolean isIdentifier() {
        return is(ToitTypes.IDENTIFIER);
    }

    private boolean isIdentifier(String val) {
        if (tokenType() != ToitTypes.IDENTIFIER) return false;
        var tokenVal = builder.getTokenText();
        if (tokenVal == null) return false;
        tokenVal = tokenVal.trim();
        return val.equals(tokenVal);
    }

    private boolean isNewLine() {
        return is(ToitTypes.NEWLINE);
    }

    private boolean isDedent() {
        return is(ToitTypes.DEDENT);
    }

    private boolean isIndent() {
        return is(ToitTypes.INDENT);
    }

    private boolean is(IElementType type) {
        return tokenType() == type;
    }

    private boolean is(TokenSet set) {
        return set.contains(tokenType());
    }

    private boolean isSequence(IElementType... sequence) {
        var n = mark();
        boolean res = true;
        for (IElementType iElementType : sequence) {
            if (tokenType() == iElementType) {
                consume();
            } else {
                res = false;
                break;
            }
        }
        n.rollback();
        return res;
    }

    private boolean error(String error, Marker... markers) {
        for (Marker marker : markers) {
            marker.drop();
        }
        mark().error(error);
        return false;
    }

    private boolean drop(Marker... markers) {
        for (Marker marker : markers) {
            marker.drop();
        }
        return false;
    }

    public boolean isAllowingNewlines(TokenSet set) {
        var m = mark();
        if (isNewLine()) consumeAllowNewlines();
        if (set.contains(tokenType())) {
            m.drop();
            return true;
        } else {
            m.rollback();
            return false;
        }
    }

    private boolean expect(IElementType type) {
        if (!is(type)) return false;
        consume();
        return true;
    }

    @Data
    @AllArgsConstructor
    class MultiLine {
        int startLineNo;

        boolean isDone() {
            return builder.eof() || startLineNo != currentLineNo && currentIndentLevel == currentBaseIndentLevel;
        }

        public boolean atStartOfNextLine() {
            return builder.eof() || currentLineNo > startLineNo && currentIsAtBeggingOfLine && currentIndentLevel == currentBaseIndentLevel;
        }
    }

    private MultiLine startMultilineConstruct() {
        return new MultiLine(currentLineNo);
    }

    private boolean atNextStatement() {
        return builder.eof() || currentIsAtBeggingOfLine && currentIndentLevel == currentBaseIndentLevel;
    }
    private final static TokenSet STATEMENT_TERMINATORS = TokenSet.create(RPAREN, RBRACKET, RCURLY, COMMA, QUESTION, DOT_DOT, SEMICOLON);
    private boolean atStatementTerminator(boolean allowBlock) {
        return atNextStatement() || STATEMENT_TERMINATORS.contains(tokenType()) || !allowBlock && is(COLON);
    }

    @Data
    @AllArgsConstructor
    class Marker {
        PsiBuilder.Marker marker;
        int indentLevel;
        int baseIndentLevel;
        int lineNo;
        boolean tokenIsAttached;
        boolean atBeggingOfLine;

        boolean drop() {
            marker.drop();
            return false;
        }

        boolean error(String error) {
            marker.drop();
            builder.mark().error(error);
            return false;
        }

        private boolean done(IElementType type) {
            marker.done(type);
            return true;
        }

        private boolean collapse() {
            marker.drop();
            return true;
        }

        private void rollback() {
            marker.rollbackTo();
            currentBaseIndentLevel = baseIndentLevel;
            currentIndentLevel = indentLevel;
            currentLineNo = lineNo;
            currentTokenIsAttached = tokenIsAttached;
            currentIsAtBeggingOfLine = atBeggingOfLine;
        }
    }

    Marker mark() {
        return new Marker(builder.mark(), currentIndentLevel, currentBaseIndentLevel, currentLineNo, currentTokenIsAttached, currentIsAtBeggingOfLine);
    }

    // Advanced lexer advance operations
    IElementType tokenType() {
        return builder.getTokenType();
    }

    private void consumeDedents() {
        while (isDedent() && currentIndentLevel > currentBaseIndentLevel) {
            consume();
            currentIndentLevel--;
        }

    }

    void consume() {
        // Detect if we are at begging of line and increment line number
        if (isNewLine()) {
            currentLineNo++;
            currentIsAtBeggingOfLine = true;
        } else if (!isIndent() && !isDedent()) {
            currentIsAtBeggingOfLine = false;
        }

        // Detect if the current token is seperated by white space or not and consume any whitespace.
        int loopCount = 0;
        do {
            loopCount++;
            builder.advanceLexer();
        } while (is(TokenType.WHITE_SPACE));
        currentTokenIsAttached = loopCount == 1;
    }

    void consumeAllowNewlines() {
        if (!isNewLine()) consume();
        while (!builder.eof()) {
            if (isNewLine()) consume();
            else if (isIndent()) {
                currentIndentLevel++;
                consume();
            } else if (isDedent() && currentIndentLevel > currentBaseIndentLevel) {
                currentIndentLevel--;
                consume();
            } else {
                break;
            }
        }
    }

    private void skipToDedent() {
        while (!isNewLine() && !builder.eof()) consumeAllowNewlines();
        consumeAllowNewlines(); // Eat the new line (and possibly some indent/dedent)
        while (currentIndentLevel > currentBaseIndentLevel && !builder.eof()) consumeAllowNewlines();
    }
}
