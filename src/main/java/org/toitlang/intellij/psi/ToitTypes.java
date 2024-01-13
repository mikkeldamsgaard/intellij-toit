// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi;

import com.intellij.psi.tree.IElementType;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.stub.ToitFunctionElementType;
import org.toitlang.intellij.psi.stub.ToitStructureElementType;
import org.toitlang.intellij.psi.stub.ToitStructureElementType.StructureType;
import org.toitlang.intellij.psi.stub.ToitVariableDeclarationElementType;

public interface ToitTypes {
  IElementType ABSTRACT = new ToitTokenType("ABSTRACT");
  IElementType ADD_ASSIGN = new ToitTokenType("ADD_ASSIGN");
  IElementType AMPERSAND = new ToitTokenType("AMPERSAND");
  IElementType AND = new ToitTokenType("AND");
  IElementType AND_ASSIGN = new ToitTokenType("AND_ASSIGN");
  IElementType AS = new ToitTokenType("AS");
  IElementType ASSERT = new ToitTokenType("ASSERT");
  IElementType BOOLEAN = new ToitTokenType("BOOLEAN");
  IElementType BREAK = new ToitTokenType("BREAK");
  IElementType CHARACTER = new ToitTokenType("CHARACTER");
  IElementType CLASS = new ToitTokenType("CLASS");
  IElementType COLON = new ToitTokenType("COLON");
  IElementType COLON_COLON = new ToitTokenType("COLON_COLON");
  IElementType COMMA = new ToitTokenType("COMMA");
  IElementType COMMENT = new ToitTokenType("COMMENT");
  IElementType START_COMMENT = new ToitTokenType("START_COMMENT");
  IElementType START_DOC_COMMENT = new ToitTokenType("START_DOC_COMMENT");
  IElementType CONST_DECLARE = new ToitTokenType("CONST_DECLARE");
  IElementType CONTINUE = new ToitTokenType("CONTINUE");
  IElementType DECLARE = new ToitTokenType("DECLARE");
  IElementType DIV_ASSIGN = new ToitTokenType("DIV_ASSIGN");
  IElementType DOT = new ToitTokenType("DOT");
  IElementType DOT_DOT = new ToitTokenType("DOT_DOT");
  IElementType ELSE = new ToitTokenType("ELSE");
  IElementType EQUALS = new ToitTokenType("EQUALS");
  IElementType EQUALS_EQUALS = new ToitTokenType("EQUALS_EQUALS");
  IElementType EXPORT = new ToitTokenType("EXPORT");
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
  IElementType IMPORT = new ToitTokenType("IMPORT");
  IElementType INDENT = new ToitTokenType("INDENT");
  IElementType DEDENT = new ToitTokenType("DEDENT");
  IElementType INTEGER = new ToitTokenType("INTEGER");
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
  IElementType RETURN_TYPE_OPERATOR = new ToitTokenType("RETURN_TYPE");
  IElementType RPAREN = new ToitTokenType("RPAREN");
  IElementType SEMICOLON = new ToitTokenType("SEMICOLON");
  IElementType SHIFT_LEFT_ASSIGN = new ToitTokenType("SHIFT_LEFT_ASSIGN");
  IElementType SHIFT_RIGHT_ASSIGN = new ToitTokenType("SHIFT_RIGHT_ASSIGN");
  IElementType SHIFT_SHIFT_RIGHT_ASSIGN = new ToitTokenType("SHIFT_SHIFT_RIGHT_ASSIGN");
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

  // pseudo keyword (constructor, interface, ...)
  IElementType PSEUDO_KEYWORD = new ToitElementType("PSEUDO_KEYWORD", ToitPseudoKeyword::new);

  // Named indentifiers
  IElementType STRUCTURE_IDENTIFIER = new ToitElementType("STRUCTURE_IDENTIFIER", ToitNameableIdentifier::new);
  IElementType FUNCTION_IDENTIFIER = new ToitElementType("FUNCTION_IDENTIFIER", ToitNameableIdentifier::new);
  IElementType IMPORT_AS_IDENTIFIER = new ToitElementType("IMPORT_AS_IDENTIFIER", ToitNameableIdentifier::new);
  IElementType FACTORY_IDENTIFIER = new ToitElementType("FACTORY_IDENTIFIER", ToitNameableIdentifier::new);
  IElementType NAMED_PARAMETER_IDENTIFIER = new ToitElementType("NAMED_PARAMETER_IDENTIFIER", ToitNameableIdentifier::new);
  IElementType SIMPLE_PARAMETER_IDENTIFIER = new ToitElementType("SIMPLE_PARAMETER_IDENTIFIER", ToitNameableIdentifier::new);
  IElementType VARIABLE_IDENTIFIER = new ToitElementType("VARIABLE_IDENTIFIER", ToitNameableIdentifier::new);

  // Reference identifier
  IElementType IMPORT_SHOW_IDENTIFIER = new ToitElementType("IMPORT_SHOW_IDENTIFIER", ToitReferenceIdentifier::new);
  IElementType EXPORT_IDENTIFIER = new ToitElementType("EXPORT_IDENTIFIER", ToitReferenceIdentifier::new);
  IElementType IMPORT_IDENTIFIER = new ToitElementType("IMPORT_IDENTIFIER", ToitReferenceIdentifier::new);
  IElementType TYPE_IDENTIFIER = new ToitElementType("TYPE_IDENTIFIER", ToitReferenceIdentifier::new);
  IElementType REFERENCE_IDENTIFIER = new ToitElementType("REFERENCE_IDENTIFIER", ToitReferenceIdentifier::new);
  IElementType BREAK_CONTINUE_LABEL_IDENTIFIER = new ToitElementType("BREAK_CONTINUE_LABEL_IDENTIFIER", ToitReferenceIdentifier::new);
  IElementType NAMED_ARGUMENT_IDENTIFIER = new ToitElementType("NAMED_ARGUMENT_IDENTIFIER", ToitReferenceIdentifier::new);


  IElementType VARIABLE_TYPE = new ToitElementType("VARIABLE_TYPE", ToitType::new);
  IElementType RETURN_TYPE = new ToitElementType("RETURN_TYPE", ToitType::new);
  IElementType IMPLEMENTS_TYPE = new ToitElementType("IMPLEMENTS_TYPE", ToitType::new);
  IElementType EXTENDS_TYPE = new ToitElementType("EXTENDS_TYPE", ToitType::new);


  IElementType PARAMETER_NAME = new ToitElementType("PARAMETER_NAME", ToitParameterName::new);

  IElementType IMPORT_DECLARATION = new ToitElementType("IMPORT_STATEMENT", ToitImportDeclaration::new);
  IElementType EXPORT_DECLARATION = new ToitElementType("EXPORT_DECLARATION", ToitExportDeclaration::new);

  IElementType EXPRESSION = new ToitElementType("EXPRESSION", ToitTopLevelExpression::new);
  IElementType PRIMARY_EXPRESSION = new ToitElementType("EXPRESSION", ToitPrimaryExpression::new);
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
  IElementType POSTFIX_INCREMENT_EXPRESSION = new ToitElementType("POSTFIX_EXPRESSION", ToitPostfixIncrementExpression::new);

  IElementType LIST_LITERAL = new ToitElementType("LIST_LITERAL", ToitListLiteral::new);
  IElementType BYTE_ARRAY_LITERAL = new ToitElementType("BYTE_ARRAY_LITERAL", ToitListLiteral::new);
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
  IElementType BREAK_CONTINUE_STATEMENT = new ToitElementType("BREAK_CONTINUE_STATEMENT", ToitBreakContinue::new);

  IElementType CLASS_DECLARATION = new ToitStructureElementType(StructureType.CLASS);
  IElementType INTERFACE_DECLARATION = new ToitStructureElementType(StructureType.INTERFACE);
  IElementType MONITOR_DECLARATION = new ToitStructureElementType(StructureType.MONITOR);
  IElementType FUNCTION_DECLARATION = new ToitFunctionElementType("FUNCTION_DECLARATION");
  IElementType VARIABLE_DECLARATION = new ToitVariableDeclarationElementType("VARIABLE_DECLARATION");

  IElementType OPERATOR = new ToitElementType("OPERATOR", ToitOperator::new);
  IElementType SIMPLE_LITERAL = new ToitElementType("SIMPLE_LITERAL", ToitSimpleLiteral::new);

  IElementType RECOVER = new ToitElementType("RECOVER", ToitRecover::new);
}
