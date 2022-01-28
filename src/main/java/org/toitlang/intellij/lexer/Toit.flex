// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.toitlang.intellij.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.toitlang.intellij.psi.ToitTypes;
import com.intellij.psi.TokenType;
import java.util.Stack;
%%

%class ToitLexer
%implements FlexLexer
%unicode
%line
%column
%function advance
%type IElementType
%x INDENT_TRACKING
%x INDENT_OUT_EXTRA
%s NORMAL
%{
 int yyline;
 int yycolumn;

 Stack<Integer> indentStack = new Stack<>();
 public int getIndenStackSize() { return indentStack.size(); }
 IElementType indentOut() {
     indentStack.pop();
     yybegin(INDENT_OUT_EXTRA);
     return ToitTypes.INDENT_OUT;
 }
 IElementType indentIn() {
     indentStack.push(yycolumn);
     yybegin(NORMAL);
     return ToitTypes.INDENT_IN;
 }
 IElementType indentIgnore() {
     yybegin(NORMAL);
     return null;
 }

 IElementType handleIndent() {
    yypushback(1);
    if (indentStack.isEmpty()) {
       if (yycolumn > 0) {
           // No indents recorded and non-space in first column. Record indent and return to normal mode
           return indentIn();
       }
       return indentIgnore();
    }

    int previousIndent = indentStack.peek();
    if (previousIndent == yycolumn) {
        // At same indentation level, so return to normal mode
        return indentIgnore();
    }

    if (previousIndent < yycolumn) {
        // Indent in detected, record it on the stack
        return indentIn();
    }

    // Indent out
    return indentOut();
 };

%}


LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

/* comments */
Comment = {TraditionalComment}
TraditionalComment = "/*" ( [^*] | (\*+ [^/]) )* \*+ "/"
EndOfLineCommentPrefix = "//" {InputCharacter}*

/* identifiers */
IndentifierStart = [\w_--\d]
IndentifierContinue = [\w_]
Identifer = {IndentifierStart}{IndentifierContinue}*

MinusMinusIdentifier = "--" {Identifer}

/* string literals */
StringLiteral = {QuotedString} | {TripleQuotedString}

QuotedString = \"([^\\\"\r\n]|{EscapeSequence})*\"
EscapeSequence = \\[^\r\n]


ThreeQuotes = (\"\"\")
OneOrTwoQuotes = (\"[^\\\"]) | (\"\\[^]) | (\"\"[^\\\"]) | (\"\"\\[^])
TripleQuoteStringCharacter = [^\\\"] | {EscapeSequence} | {OneOrTwoQuotes}
TripleQuotedString = {ThreeQuotes} {TripleQuoteStringCharacter}* {ThreeQuotes}?

/* character literals */
CharacterLiteral = "'" ([^']|{EscapeSequence}) "'"

/* numerics */
Digit = [0-9]
NonZeroDigit = [1-9]
OctalDigit = [0-7]
HexDigit = [0-9A-Fa-f]
BinaryDigit = [01]

HexIntegeer = 0[Xx]("_"?{HexDigit})+
OctalInteger = 0[Oo]?("_"?{OctalDigit})+
BinaryInteger = 0[Bb]("_"?{BinaryDigit})+
DecimalInteger = (({NonZeroDigit}("_"?{Digit})*)|0)
IntegerLiteral = {DecimalInteger}|{BinaryInteger}|{OctalInteger}|{HexIntegeer}

FloatLiteral = ({NonExponentioalFlot})|({ExponentialFloat})
NonExponentioalFlot = ({IntegralPart})?{FractionPart}
ExponentialFloat = (({IntegralPart})|({NonExponentioalFlot})){ExponentPart}
IntegralPart = {Digit}("_"?{Digit})*
FractionPart = \.{IntegralPart}
ExponentPart = [eE][+\-]?{IntegralPart}

/* Boolean */
BooleanLiteral = "true" | "false"

/* Null */
NullLiteral = "null"

WhiteSpace=([\ \t\f] | \\\n)+
NotWhiteSpace=[^\ \t\f\r\n]


%%
<YYINITIAL, INDENT_TRACKING> {
  {NotWhiteSpace} { var res = handleIndent(); if (res != null) return res; }
  {LineTerminator} { /* return ToitTypes.NEWLINE; */ } // Totally blank lines are treated as whitespace
}
<INDENT_OUT_EXTRA> {
  . { yypushback(1); yybegin(INDENT_TRACKING); return ToitTypes.NEWLINE; }
}
<YYINITIAL, INDENT_TRACKING, NORMAL> {
 {WhiteSpace}                                    { /* return TokenType.WHITE_SPACE; */ }
 {Comment}                                       { return ToitTypes.COMMENT; }
 {EndOfLineCommentPrefix}  / {LineTerminator}    { return ToitTypes.COMMENT; }
}

<NORMAL> {
 {IntegerLiteral}                                { return ToitTypes.INTEGER; }
 {FloatLiteral}                                  { return ToitTypes.FLOAT; }
 {StringLiteral}                                 { return ToitTypes.STRING; }
 {CharacterLiteral}                              { return ToitTypes.CHARACTER; }
 {BooleanLiteral}                                { return ToitTypes.BOOLEAN; }
 {NullLiteral}                                   { return ToitTypes.NULL; }

 "-"                                             { return ToitTypes.MINUS; }
 "+"                                             { return ToitTypes.PLUS; }
 "*"                                             { return ToitTypes.STAR; }
 "%"                                             { return ToitTypes.PERCENT; }
 "<<"                                            { return ToitTypes.LESS_LESS; }
 "<<<"                                           { return ToitTypes.LESS_LESS_LESS; }
 ">>"                                            { return ToitTypes.GREATER_GREATER; }
 "&"                                             { return ToitTypes.AMPERSAND; }
 "~"                                             { return ToitTypes.TILDE; }
 "^"                                             { return ToitTypes.HAT; }

 // Postfix
 "++"                                            { return ToitTypes.PLUS_PLUS; }
 "--"                                            { return ToitTypes.MINUS_MINUS; }

 // Misc
 "?"                                             { return ToitTypes.QUESTION; }
 ":"                                             { return ToitTypes.COLON; }
 ";"                                             { return ToitTypes.SEMICOLON; }
 "."                                             { return ToitTypes.DOT; }
 "("                                             { return ToitTypes.LPAREN; }
 ")"                                             { return ToitTypes.RPAREN; }
 "/"                                             { return ToitTypes.SLASH; }
 "->"                                            { return ToitTypes.RETURN_TYPE; }
 "="                                             { return ToitTypes.EQUALS; }
 "|"                                             { return ToitTypes.PIPE; }
 "["                                             { return ToitTypes.LBRACKET; }
 "]"                                             { return ToitTypes.RBRACKET; }
 "#"                                             { return ToitTypes.HASH; }
 ","                                             { return ToitTypes.COMMA; }
 "{"                                             { return ToitTypes.LCURLY; }
 "}"                                             { return ToitTypes.RCURLY; }


 // Comparison
 "=="                                            { return ToitTypes.EQUALS_EQUALS; }
 "!="                                            { return ToitTypes.NOT_EQUALS; }
 "<"                                             { return ToitTypes.LESS; }
 ">"                                             { return ToitTypes.GREATER; }
 "<="                                            { return ToitTypes.LESS_OR_EQUALS; }
 ">="                                            { return ToitTypes.GREATER_OR_EQUALS; }

  // Assignment
 ":="                                            { return ToitTypes.DEFINE; }
 "::="                                           { return ToitTypes.CONST_DEFINE; }
 "+="                                            { return ToitTypes.ADD_ASSIGN; }
 "-="                                            { return ToitTypes.SUB_ASSIGN; }
 "*="                                            { return ToitTypes.MUL_ASSIGN; }
 "/="                                            { return ToitTypes.DIV_ASSIGN; }
 "%="                                            { return ToitTypes.REMAINDER_ASSIGN; }
 "|="                                            { return ToitTypes.OR_ASSIGN; }
 "^="                                            { return ToitTypes.NOT_ASSIGN; }
 "&="                                            { return ToitTypes.AND_ASSIGN; }
 "<<="                                           { return ToitTypes.SHIFT_LEFT_ASSIGN; }
 ">>="                                           { return ToitTypes.SHIFT_RIGHT_ASSIGN; }
 ">>>="                                          { return ToitTypes.SHIFT_SHIFT_RIGHT_ASSIGN; }

 // Type
 "is"                                            { return ToitTypes.IS; }
 "as"                                            { return ToitTypes.AS; }
 "is" [\ \t]+ "not"                              { return ToitTypes.IS_NOT; }

 // Logical
 "and"                                           { return ToitTypes.AND; }
 "or"                                            { return ToitTypes.OR; }
 "not"                                           { return ToitTypes.NOT; }
 "&&"                                            { return ToitTypes.DEPRECATED_AND; }
 "||"                                            { return ToitTypes.DEPRECATED_OR; }
 "!"                                             { return ToitTypes.DEPRECATED_NOT; }

 "assert"                                        { return ToitTypes.ASSERT; }
 "abstract"                                      { return ToitTypes.ABSTRACT; }
 "break"                                         { return ToitTypes.BREAK; }
 "class"                                         { return ToitTypes.CLASS; }
 "continue"                                      { return ToitTypes.CONTINUE; }
 "else"                                          { return ToitTypes.ELSE; }
 "finally"                                       { return ToitTypes.FINALLY; }
 "for"                                           { return ToitTypes.FOR; }
 "if"                                            { return ToitTypes.IF; }
 "import"                                        { return ToitTypes.IMPORT; }
 "export"                                        { return ToitTypes.EXPORT; }
 "return"                                        { return ToitTypes.RETURN; }
 "static"                                        { return ToitTypes.STATIC; }
 "try"                                           { return ToitTypes.TRY; }
 "while"                                         { return ToitTypes.WHILE; }
 "interface"                                     { return ToitTypes.INTERFACE; }

 "implements"                                    { return ToitTypes.IMPLEMENTS; }
 "extends"                                       { return ToitTypes.EXTENDS; }
 "constructor"                                   { return ToitTypes.CONSTRUCTOR; }
 "operator"                                      { return ToitTypes.OPERATOR; }
 "show"                                          { return ToitTypes.SHOW; }

 "#primitive"                                    { return ToitTypes.PRIMITIVE; }

 {Identifer}                                     { return ToitTypes.IDENTIFIER; }
 {MinusMinusIdentifier}                          { return ToitTypes.MINUS_MINUS_IDENTIFIER; }

 {LineTerminator}                                { yybegin(INDENT_TRACKING); return ToitTypes.NEWLINE; }

}

[^]                                              { return TokenType.BAD_CHARACTER; }
