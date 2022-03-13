// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.toitlang.intellij.lexer;

import com.intellij.psi.tree.IElementType;
import lombok.AllArgsConstructor;import lombok.Data;import org.toitlang.intellij.psi.ToitTypes;
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
%x STRING_PARSING
%x TRIPLE_STRING_PARSING
%x INLINE_STRING_EXPRESSION
%x INLINE_DELIMITED_STRING_EXPRESSION
%s NORMAL
%eofval{

    return handleEof();
%eofval}
%{
   int yyline;
            int yycolumn;

            Stack<Integer> indentStack = new Stack<>();

            public int getIndenStackSize() { return indentStack.size(); }
            IElementType indentOut() {
                indentStack.pop();
                return ToitTypes.DEDENT;
            }
            IElementType indentIn() {
                indentStack.push(yycolumn);
                yybegin(NORMAL);
                return ToitTypes.INDENT;
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

            IElementType handleEof() {
                if (yystate() != INDENT_TRACKING) {
                    yybegin(INDENT_TRACKING);
                    return ToitTypes.NEWLINE;
                } else {
                    if (indentStack.isEmpty()) return null;
                    indentStack.pop();
                    return ToitTypes.DEDENT;
                }
            }
            @Data
            @AllArgsConstructor
            class StringType {
                int stringState;
                int prevState;
            }
            Stack<StringType> stringType = new Stack<>();

            void pushStringType(int stringState) {
                stringType.push(new StringType(stringState, yystate()));
                yybegin(stringState);
            }

            void exitStringState() {
                var ss = stringType.pop();
                yybegin(ss.prevState);
            }

            void resumeStringState() {
                var ss = stringType.peek();
                yybegin(ss.stringState);
            }

            final static int PAREN = 0;
            final static int BRACKET = 1;
            @Data
            @AllArgsConstructor
            class StringDelimitedCount {
                int type;
                int count;
            }
            Stack<StringDelimitedCount> stringDelimitedStack = new Stack<>();
            void startDelimitedStringExpression(int type) {
                stringDelimitedStack.push(new StringDelimitedCount(type,0));
                yybegin(INLINE_DELIMITED_STRING_EXPRESSION);
            }

            void inlineDelimitedCountOpen(int type) {
                var top = stringDelimitedStack.peek();
                if (top.type == type) {
                    top.count++;
                }
            }

            boolean shouldExitInlineState(int type) {
                var top = stringDelimitedStack.peek();
                if (top.type == type) {
                    if (top.count == 0) {
                        stringDelimitedStack.pop();
                        resumeStringState();
                        return true;
                    }
                    top.count--;
                }
                return false;
            }

%}


LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

/* comments */
TraditionalComment = "/*" ( [^*] | (\*+ [^/]) )* \*+ "/"
EndOfLineCommentPrefix = "//" {InputCharacter}*
DocComment = "/**" ( {TraditionalComment} | [^*] | (\*+ [^/]) )* \*+ "/"

/* identifiers */
IndentifierStart = [\w_--\d]
IndentifierContinue = [\w_]
Identifer = {IndentifierStart}{IndentifierContinue}*

///* string literals */
//StringLiteral = {QuotedString} | {TripleQuotedString}
//QuotedString = \"([^\\\"\r\n]|{EscapeSequence})*\"
//ThreeQuotes = (\"\"\")
//OneOrTwoQuotes = (\"[^\\\"]) | (\"\\[^]) | (\"\"[^\\\"]) | (\"\"\\[^])
//TripleQuoteStringCharacter = [^\\\"] | {EscapeSequence} | {OneOrTwoQuotes}
//TripleQuotedString = {ThreeQuotes} {TripleQuoteStringCharacter}* {ThreeQuotes}?

EscapeSequence = \\[^\r\n]

Format = % [^ ]+

/* character literals */
CharacterLiteral = "'" ([^']|{EscapeSequence}) "'"

/* numerics */
Digit = [0-9]
NonZeroDigit = [1-9]
OctalDigit = [0-7]
HexDigit = [0-9A-Fa-f]
BinaryDigit = [01]

HexIntegeer = 0[Xx]("_"?{HexDigit})+("p"("-")?{DecimalInteger})?
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
Spacing=[\ \t]


%%
<YYINITIAL, INDENT_TRACKING> {
  {NotWhiteSpace} { var res = handleIndent(); if (res != null) return res; }
  {LineTerminator} { return TokenType.WHITE_SPACE;  } // Totally blank lines are treated as whitespace
}
<YYINITIAL, INDENT_TRACKING, NORMAL, INLINE_DELIMITED_STRING_EXPRESSION> {
 {WhiteSpace}                                    { return TokenType.WHITE_SPACE; }
 {TraditionalComment}                            { return ToitTypes.COMMENT; }
 {DocComment}                                    { return ToitTypes.COMMENT; }
 {EndOfLineCommentPrefix}  / {LineTerminator}    { return ToitTypes.COMMENT; }
}

<NORMAL, INLINE_DELIMITED_STRING_EXPRESSION> {
 {IntegerLiteral}                                { return ToitTypes.INTEGER; }
 {FloatLiteral}                                  { return ToitTypes.FLOAT; }
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
 ">>>"                                           { return ToitTypes.GREATER_GREATER_GREATER; }
 "&"                                             { return ToitTypes.AMPERSAND; }
 "~"                                             { return ToitTypes.TILDE; }
 "^"                                             { return ToitTypes.HAT; }

 // Postfix
 "++"                                            { return ToitTypes.PLUS_PLUS; }
 "--"                                            { return ToitTypes.MINUS_MINUS; }

 // Misc
 "?"                                             { return ToitTypes.QUESTION; }
 ":"                                             { return ToitTypes.COLON; }
 "::"                                            { return ToitTypes.COLON_COLON; }
 ";"                                             { return ToitTypes.SEMICOLON; }
 ".."                                            { return ToitTypes.DOT_DOT; }
 "/"                                             { return ToitTypes.SLASH; }
 "->"                                            { return ToitTypes.RETURN_TYPE_OPERATOR; }
 "="                                             { return ToitTypes.EQUALS; }
 "|"                                             { return ToitTypes.PIPE; }
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
 ":="                                            { return ToitTypes.DECLARE; }
 "::="                                           { return ToitTypes.CONST_DECLARE; }
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

 // Almost keywords
 "is"                                            { return ToitTypes.IS; }
 "is" {Spacing}+ "not"                           { return ToitTypes.IS_NOT; }

 // Keywords
 "as"                                            { return ToitTypes.AS; }
 "abstract"                                      { return ToitTypes.ABSTRACT; }
 "assert"                                        { return ToitTypes.ASSERT; }
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
 "or"                                            { return ToitTypes.OR; }
 "and"                                           { return ToitTypes.AND; }
 "not"                                           { return ToitTypes.NOT; }

 "#primitive"                                    { return ToitTypes.PRIMITIVE; }


 {LineTerminator}                                { yybegin(INDENT_TRACKING); return ToitTypes.NEWLINE; }

// Strings
 "\"\"\""                                        { pushStringType(TRIPLE_STRING_PARSING); return ToitTypes.STRING_START; }
 "\""                                            { pushStringType(STRING_PARSING); return ToitTypes.STRING_START; }
 }

<NORMAL, INLINE_STRING_EXPRESSION, INLINE_DELIMITED_STRING_EXPRESSION> {
 {Identifer}                                     { return ToitTypes.IDENTIFIER; }
 "."                                             { return ToitTypes.DOT; }
}

<NORMAL> {
 "("                                             { return ToitTypes.LPAREN; }
 ")"                                             { return ToitTypes.RPAREN; }
 "["                                             { return ToitTypes.LBRACKET; }
 "]"                                             { return ToitTypes.RBRACKET; }
}

<INLINE_DELIMITED_STRING_EXPRESSION> {
 "("                                             { inlineDelimitedCountOpen(PAREN); return ToitTypes.LPAREN; }
 ")"                                             { if(!shouldExitInlineState(PAREN)) return ToitTypes.RPAREN; }
 "["                                             { inlineDelimitedCountOpen(BRACKET); return ToitTypes.LBRACKET; }
 "]"                                             { shouldExitInlineState(BRACKET); return ToitTypes.RBRACKET;}
}

<INLINE_STRING_EXPRESSION> {
  "["                                            { startDelimitedStringExpression(BRACKET); return ToitTypes.LBRACKET; }
  [^]                                            { yypushback(1); resumeStringState();}
}

<TRIPLE_STRING_PARSING> {
 "\"\"\""                                        { exitStringState(); return ToitTypes.STRING_END; }
 "\"\""                                          { /* advance to next character */ }
 "\""                                            { /* advance to next character */ }
}

<STRING_PARSING> {
 "\""                                            { exitStringState(); return ToitTypes.STRING_END; }
}

<STRING_PARSING, TRIPLE_STRING_PARSING> {
 "$(" {Format}?                                  { startDelimitedStringExpression(PAREN); return ToitTypes.STRING_PART; }
 "$"                                             { yybegin(INLINE_STRING_EXPRESSION); return ToitTypes.STRING_PART; }
 {EscapeSequence}                                { /* advance to next character */ }
 [^\\\"$]                                        { /* advance to next character */ }
}

[^]                                              { return TokenType.BAD_CHARACTER; }
