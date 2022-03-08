// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi;

import com.intellij.psi.tree.IElementType;
import org.toitlang.intellij.psi.elements.*;

public interface ToitTypes {

  IElementType ADDITION_EXPRESSION = new ToitElementType("ADDITION_EXPRESSION");
  IElementType ARGUMENTS = new ToitElementType("ARGUMENTS");
  IElementType ARGUMENT_START_NEWLINING = new ToitElementType("ARGUMENT_START_NEWLINING");
  IElementType ARGUMENT_STOP_NEWLINING = new ToitElementType("ARGUMENT_STOP_NEWLINING");
  IElementType ASSIGMENT_DECLARATION = new ToitElementType("ASSIGMENT_DECLARATION");
  IElementType ASSIGNMENT_OPERATOR = new ToitElementType("ASSIGNMENT_OPERATOR");
  IElementType BITWISE_AND_EXPRESSION = new ToitElementType("BITWISE_AND_EXPRESSION");
  IElementType BITWISE_NOT_EXPRESSION = new ToitElementType("BITWISE_NOT_EXPRESSION");
  IElementType BITWISE_OR_EXPRESSION = new ToitElementType("BITWISE_OR_EXPRESSION");
  IElementType BLOCK_EXPRESSION = new ToitElementType("BLOCK_EXPRESSION");
  IElementType BLOCK_PARAMETERS = new ToitElementType("BLOCK_PARAMETERS");
  IElementType BLOCK_TERMINATOR = new ToitElementType("BLOCK_TERMINATOR");
  IElementType CALL_OR_REFERENCE_EXPRESSION = new ToitElementType("CALL_OR_REFERENCE_EXPRESSION");
  IElementType CLASS_BODY = new ToitElementType("CLASS_BODY");
  IElementType CLASS_NAME = new ToitElementType("CLASS_NAME");
  IElementType COMPARISON_EXPRESSION = new ToitElementType("COMPARISON_EXPRESSION");
  IElementType COMPARISON_OPERATOR = new ToitElementType("COMPARISON_OPERATOR");
  IElementType CONSTRUCTOR_DECLARATION = new ToitElementType("CONSTRUCTOR_DECLARATION");
  IElementType CONSTRUCTOR_PARAMETER = new ToitElementType("CONSTRUCTOR_PARAMETER");
  IElementType DIVISION_EXPRESSION = new ToitElementType("DIVISION_EXPRESSION");
  IElementType EXPONENTIAL_EXPRESSION = new ToitElementType("EXPONENTIAL_EXPRESSION");
  IElementType FUNCTIONAL_PARAMETER = new ToitElementType("FUNCTIONAL_PARAMETER");
  IElementType FUNCTIONAL_RETURN_TYPE = new ToitElementType("FUNCTIONAL_RETURN_TYPE");
  IElementType FUNCTION_NAME = new ToitElementType("FUNCTION_NAME");
  IElementType INTERFACE_BODY = new ToitElementType("INTERFACE_BODY");
  IElementType LAMBDA_EXPRESSION = new ToitElementType("LAMBDA_EXPRESSION");
  IElementType LITERAL_EXPRESSION = new ToitElementType("LITERAL_EXPRESSION");
  IElementType LOGICAL_AND_EXPRESSION = new ToitElementType("LOGICAL_AND_EXPRESSION");
  IElementType LOGICAL_AND_OPERATOR = new ToitElementType("LOGICAL_AND_OPERATOR");
  IElementType LOGICAL_NOT_EXPRESSION = new ToitElementType("LOGICAL_NOT_EXPRESSION");
  IElementType LOGICAL_NOT_OPERATOR = new ToitElementType("LOGICAL_NOT_OPERATOR");
  IElementType LOGICAL_OR_EXPRESSION = new ToitElementType("LOGICAL_OR_EXPRESSION");
  IElementType LOGICAL_OR_OPERATOR = new ToitElementType("LOGICAL_OR_OPERATOR");
  IElementType LOOP_CONTROL = new ToitElementType("LOOP_CONTROL");
  IElementType MEMBER_DECLARATION = new ToitElementType("MEMBER_DECLARATION");
  IElementType METHOD_DECLARATION = new ToitElementType("METHOD_DECLARATION");
  IElementType MULTIPLICATION_EXPRESSION = new ToitElementType("MULTIPLICATION_EXPRESSION");
  IElementType NAMED_BLOCK_ARGUMENT = new ToitElementType("NAMED_BLOCK_ARGUMENT");
  IElementType NON_BLOCK_ARGUMENT = new ToitElementType("NON_BLOCK_ARGUMENT");
  IElementType OPERATOR_DECLARATION = new ToitElementType("OPERATOR_DECLARATION");
  IElementType OVERLOADABLE_OPERATOR = new ToitElementType("OVERLOADABLE_OPERATOR");
  IElementType PARENTHESIS_EXPRESSION = new ToitElementType("PARENTHESIS_EXPRESSION");
  IElementType POSTFIX_OPERATOR = new ToitElementType("POSTFIX_OPERATOR");
  IElementType PRIMITIVE_NAME = new ToitElementType("PRIMITIVE_NAME");
  IElementType QUALIFIED_PARAMETER = new ToitElementType("QUALIFIED_PARAMETER");
  IElementType REMAINDER_EXPRESSION = new ToitElementType("REMAINDER_EXPRESSION");
  IElementType SHIFT_EXPRESSION = new ToitElementType("SHIFT_EXPRESSION");
  IElementType SHIFT_OPERATOR = new ToitElementType("SHIFT_OPERATOR");
  IElementType STATEMENT_TERMINATOR = new ToitElementType("STATEMENT_TERMINATOR");
  IElementType SUBTRACTION_EXPRESSION = new ToitElementType("SUBTRACTION_EXPRESSION");
  IElementType TYPE_EXPRESSION = new ToitElementType("TYPE_EXPRESSION");
  IElementType TYPE_INDICATOR = new ToitElementType("TYPE_INDICATOR");
  IElementType TYPE_NAME = new ToitElementType("TYPE_NAME");
  IElementType TYPE_OPERATOR = new ToitElementType("TYPE_OPERATOR");

  IElementType ABSTRACT = new ToitTokenType("ABSTRACT");
  IElementType ADD_ASSIGN = new ToitTokenType("ADD_ASSIGN");
  IElementType AMPERSAND = new ToitTokenType("AMPERSAND");
  IElementType AND = new ToitTokenType("AND");
  IElementType AND_ASSIGN = new ToitTokenType("AND_ASSIGN");
  IElementType AS = new ToitTokenType("AS");
  IElementType ASSERT = new ToitTokenType("ASSERT");
  IElementType BLOCK_START = new ToitTokenType("block_start");
  IElementType BOOLEAN = new ToitTokenType("BOOLEAN");
  IElementType BREAK = new ToitTokenType("BREAK");
  IElementType CHARACTER = new ToitTokenType("CHARACTER");
  IElementType CLASS = new ToitTokenType("CLASS");
  IElementType COLON = new ToitTokenType("COLON");
  IElementType COLON_COLON = new ToitTokenType("COLON_COLON");
  IElementType COMMA = new ToitTokenType("COMMA");
  IElementType COMMENT = new ToitTokenType("COMMENT");
  IElementType CONSTRUCTOR = new ToitTokenType("CONSTRUCTOR");
  IElementType CONST_DECLARE = new ToitTokenType("CONST_DECLARE");
  IElementType CONTINUE = new ToitTokenType("CONTINUE");
  IElementType DECLARE = new ToitTokenType("DECLARE");
  IElementType DEPRECATED_AND = new ToitTokenType("DEPRECATED_AND");
  IElementType DEPRECATED_NOT = new ToitTokenType("DEPRECATED_NOT");
  IElementType DEPRECATED_OR = new ToitTokenType("DEPRECATED_OR");
  IElementType DIV_ASSIGN = new ToitTokenType("DIV_ASSIGN");
  IElementType DOT = new ToitTokenType("DOT");
  IElementType DOT_DOT = new ToitTokenType("DOT_DOT");
  IElementType ELSE = new ToitTokenType("ELSE");
  IElementType EQUALS = new ToitTokenType("EQUALS");
  IElementType EQUALS_EQUALS = new ToitTokenType("EQUALS_EQUALS");
  IElementType EXPORT = new ToitTokenType("EXPORT");
  IElementType EXTENDS = new ToitTokenType("EXTENDS");
  IElementType FINALLY = new ToitTokenType("FINALLY");
  IElementType FLOAT = new ToitTokenType("FLOAT");
  IElementType FOR = new ToitTokenType("FOR");
  IElementType GREATER = new ToitTokenType("GREATER");
  IElementType GREATER_GREATER = new ToitTokenType("GREATER_GREATER");
  IElementType GREATER_GREATER_GREATER = new ToitTokenType("GREATER_GREATER_GREATER");
  IElementType GREATER_OR_EQUALS = new ToitTokenType("GREATER_OR_EQUALS");
  IElementType HASH = new ToitTokenType("HASH");
  IElementType HAT = new ToitTokenType("HAT");
  IElementType IDENTIFIER = new ToitTokenType("IDENTIFIER");
  IElementType IF = new ToitTokenType("IF");
  IElementType IMPLEMENTS = new ToitTokenType("IMPLEMENTS");
  IElementType IMPORT = new ToitTokenType("IMPORT");
  IElementType INDENT = new ToitTokenType("INDENT");
  IElementType DEDENT = new ToitTokenType("DEDENT");
  IElementType INTEGER = new ToitTokenType("INTEGER");
  IElementType INTERFACE = new ToitTokenType("INTERFACE");
  IElementType IS = new ToitTokenType("IS");
  IElementType IS_NOT = new ToitTokenType("IS_NOT");
  IElementType LBRACKET = new ToitTokenType("LBRACKET");
  IElementType LCURLY = new ToitTokenType("LCURLY");
  IElementType LESS = new ToitTokenType("LESS");
  IElementType LESS_LESS = new ToitTokenType("LESS_LESS");
  IElementType LESS_LESS_LESS = new ToitTokenType("LESS_LESS_LESS");
  IElementType LESS_OR_EQUALS = new ToitTokenType("LESS_OR_EQUALS");
  IElementType LPAREN = new ToitTokenType("LPAREN");
  IElementType MINUS = new ToitTokenType("MINUS");
  IElementType MINUS_MINUS = new ToitTokenType("MINUS_MINUS");
  IElementType MUL_ASSIGN = new ToitTokenType("MUL_ASSIGN");
  IElementType NEWLINE = new ToitTokenType("NEWLINE");
  IElementType NOT = new ToitTokenType("NOT");
  IElementType NOT_ASSIGN = new ToitTokenType("NOT_ASSIGN");
  IElementType NOT_EQUALS = new ToitTokenType("NOT_EQUALS");
  IElementType NULL = new ToitTokenType("NULL");
  IElementType OPERATOR = new ToitTokenType("OPERATOR");
  IElementType OR = new ToitTokenType("OR");
  IElementType OR_ASSIGN = new ToitTokenType("OR_ASSIGN");
  IElementType PERCENT = new ToitTokenType("PERCENT");
  IElementType PIPE = new ToitTokenType("PIPE");
  IElementType PLUS = new ToitTokenType("PLUS");
  IElementType PLUS_PLUS = new ToitTokenType("PLUS_PLUS");
  IElementType PRIMITIVE = new ToitTokenType("PRIMITIVE");
  IElementType QUESTION = new ToitTokenType("QUESTION");
  IElementType RBRACKET = new ToitTokenType("RBRACKET");
  IElementType RCURLY = new ToitTokenType("RCURLY");
  IElementType REMAINDER_ASSIGN = new ToitTokenType("REMAINDER_ASSIGN");
  IElementType RETURN = new ToitTokenType("RETURN");
  IElementType RETURN_TYPE = new ToitTokenType("RETURN_TYPE");
  IElementType RPAREN = new ToitTokenType("RPAREN");
  IElementType SEMICOLON = new ToitTokenType("SEMICOLON");
  IElementType SHIFT_LEFT_ASSIGN = new ToitTokenType("SHIFT_LEFT_ASSIGN");
  IElementType SHIFT_RIGHT_ASSIGN = new ToitTokenType("SHIFT_RIGHT_ASSIGN");
  IElementType SHIFT_SHIFT_RIGHT_ASSIGN = new ToitTokenType("SHIFT_SHIFT_RIGHT_ASSIGN");
  IElementType SHOW = new ToitTokenType("SHOW");
  IElementType SLASH = new ToitTokenType("SLASH");
  IElementType STAR = new ToitTokenType("STAR");
  IElementType STATIC = new ToitTokenType("STATIC");
  IElementType STRING_START = new ToitTokenType("STRING_START");
  IElementType STRING_PART = new ToitTokenType("STRING_PART");
  IElementType STRING_END = new ToitTokenType("STRING_END");
  IElementType SUB_ASSIGN = new ToitTokenType("SUB_ASSIGN");
  IElementType TILDE = new ToitTokenType("TILDE");
  IElementType TRY = new ToitTokenType("TRY");
  IElementType WHILE = new ToitTokenType("WHILE");

  IElementType QUALIFIED_NAME = new ToitElementType("QUALIFIED_NAME", ToitQualifiedName::new);
  IElementType VARIABLE_NAME = new ToitElementType("VARIABLE_NAME", ToitVariableName::new);
  IElementType VARIABLE_DECLARATION = new ToitElementType("VARIABLE_NAME", ToitVariableDeclaration::new);
  IElementType PARAMETER_NAME = new ToitElementType("PARAMETER_NAME", ToitParameterName::new);

  IElementType IMPORT_DECLARATION = new ToitElementType("IMPORT_STATEMENT", ToitImportDeclaration::new);
  IElementType LOCAL_IMPORT = new ToitElementType("LOCAL_IMPORT", ToitLocalImport::new);
  IElementType IMPORT_AS = new ToitElementType("IMPORT_AS", ToitImportAs::new);

  IElementType IMPORT_SHOW = new ToitElementType("IMPORT_SHOW", ToitImportShow::new);
  IElementType EXPORT_STAR = new ToitElementType("EXPORT_STAR", ToitExportStar::new);
  IElementType EXPORT_DECLARATION = new ToitElementType("EXPORT_DECLARATION", ToitExportDeclaration::new);

  IElementType EXPRESSION = new ToitElementType("EXPRESSION", ToitExpression::new);
  IElementType ELVIS_EXPRESSION = new ToitElementType("ELVIS_EXPRESSION", ToitElvisExpression::new);
  IElementType OR_EXPRESSION = new ToitElementType("OR_EXPRESSION", ToitOrExpression::new);
  IElementType AND_EXPRESSION = new ToitElementType("AND_EXPRESSION", ToitAndExpression::new);
  IElementType NOT_EXPRESSION = new ToitElementType("NOT_EXPRESSION", ToitNotExpression::new);

  IElementType CALL_EXPRESSION = new ToitElementType("CALL_EXPRESSION", ToitCallExpression::new);
  IElementType NAMED_ARGUMENT = new ToitElementType("NAMED_ARGUMENT", ToitNamedArgument::new);

  IElementType ASSIGNMENT_EXPRESSION = new ToitElementType("ASSIGNMENT_EXPRESSION", ToitAssignmentExpression::new);
  IElementType EQUALITY_EXPRESSION = new ToitElementType("EQUALITY_EXPRESSION", ToitEqualityExpression::new);
  IElementType RELATIONAL_EXPRESSION = new ToitElementType("RELATIONAL_EXPRESSION", ToitRelationalExpression::new);
  IElementType BIT_OR_EXPRESSION = new ToitElementType("BIT_OR_EXPRESSION", ToitBitOrExpression::new);
  IElementType BIT_XOR_EXPRESSION = new ToitElementType("BIT_XOR_EXPRESSION", ToitBitXOrExpression::new);
  IElementType BIT_AND_EXPRESSION = new ToitElementType("BIT_AND_EXPRESSION", ToitBitAndExpression::new);
  IElementType BIT_SHIFT_EXPRESSION = new ToitElementType("BIT_SHIFT_EXPRESSION", ToitBitAndExpression::new);
  IElementType ADDITIVE_EXPRESSION = new ToitElementType("ADDITIVE_EXPRESSION", ToitAdditiveExpression::new);
  IElementType MULTIPLICATIVE_EXPRESSION = new ToitElementType("MULTIPLICATIVE_EXPRESSION", ToitMultiplicativeExpression::new);
  IElementType INDEXING_EXPRESSION = new ToitElementType("INDEXING_EXPRESSION", ToitIndexingExpression::new);
  IElementType DEREF_EXPRESSION = new ToitElementType("DEREF_EXPRESSION", ToitDerefExpression::new);
  IElementType UNARY_EXPRESSION = new ToitElementType("UNARY_EXPRESSION", ToitUnaryExpression::new);
  IElementType POSTFIX_EXPRESSION = new ToitElementType("POSTFIX_EXPRESSION", ToitPostfixExpression::new);

  IElementType LIST_LITERAL = new ToitElementType("LIST_LITERAL", ToitListLiteral::new);
  IElementType SET_LITERAL = new ToitElementType("SET_LITERAL", ToitSetLiteral::new);
  IElementType MAP_LITERAL = new ToitElementType("MAP_LITERAL", ToitMapLiteral::new);

  IElementType BLOCK = new ToitElementType("BLOCK", ToitBlock::new);
  IElementType WHILE_STATEMENT = new ToitElementType("WHILE_STATEMENT", ToitWhile::new);
  IElementType IF_STATEMENT = new ToitElementType("IF_STATEMENT", ToitIf::new);
  IElementType RETURN_STATEMENT = new ToitElementType("RETURN_STATEMENT", ToitReturn::new);
  IElementType ASSERT_STATEMENT = new ToitElementType("ASSERT_STATEMENT", ToitAssert::new);
  IElementType PRIMITIVE_STATEMENT = new ToitElementType("PRIMITIVE_STATEMENT", ToitPrimitive::new);
  IElementType EMPTY_STATEMENT = new ToitElementType("PRIMITIVE_STATEMENT", ToitEmptyStatement::new);
  IElementType FOR_STATEMENT = new ToitElementType("FOR_STATEMENT", ToitFor::new);
  IElementType TRY_STATEMENT = new ToitElementType("TRY_STATEMENT", ToitTry::new);

  IElementType CLASS_DECLARATION = new ToitElementType("CLASS_DECLARATION", ToitClass::new);
  IElementType INTERFACE_DECLARATION = new ToitElementType("INTERFACE_DECLARATION", ToitInterface::new);
  IElementType MONITOR_DECLARATION = new ToitElementType("MONITOR_DECLARATION", ToitMonitor::new);
  IElementType FUNCTION_DECLARATION = new ToitElementType("FUNCTION_DECLARATION", ToitFunction::new);

  IElementType O = new ToitElementType("OPERATOR", ToitOperator::new);
  static IElementType operator(IElementType tokenType) {
    return O;
//    if (!(tokenType instanceof ToitTokenType)) throw new RuntimeException("Not valid type: "+tokenType.getDebugName()+", should be a token");
//    return new ToitElementType("Operator "+tokenType.getDebugName(), n -> new ToitOperator(n, (ToitTokenType)tokenType));
  }

  IElementType L = new ToitElementType("LITERAL", ToitSimpleLiteral::new);
  static IElementType simple_literal(IElementType tokenType) {
    return L;
//    if (!(tokenType instanceof ToitTokenType)) throw new RuntimeException("Not valid type: "+tokenType.getDebugName()+", should be a token");
//    return new ToitElementType("Lietral "+tokenType.getDebugName(), n -> new ToitSimpleLiteral(n, (ToitTokenType)tokenType));
  }
}
