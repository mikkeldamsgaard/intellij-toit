package org.toitlang.intellij.parser;

import com.intellij.lang.LighterASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.util.Key;
import org.toitlang.intellij.psi.ToitTypes;

import java.util.*;

public class ToitParserUtil extends GeneratedParserUtilBase {
//    private final static Key<Boolean> LIMITED_BLOCK_EXPRESSION = Key.create("limitedBlockExpression");
    private final static Key<Integer> PAREN_COUNT = Key.create("parenCount");
    public static boolean parseExpressionWithLimitedBlock(PsiBuilder psiBuilder, int level) {
//        psiBuilder.putUserData(LIMITED_BLOCK_EXPRESSION, true);
        psiBuilder.putUserData(PAREN_COUNT, 0);
        boolean r = parseOuterExpression(psiBuilder, level);
        psiBuilder.putUserData(PAREN_COUNT, null);
        return r;
    }
    public static boolean parseGuardedExpression(PsiBuilder psiBuilder, int level) {
//        psiBuilder.putUserData(LIMITED_BLOCK_EXPRESSION, true);
        Integer c = psiBuilder.getUserData(PAREN_COUNT);
        if (c != null) psiBuilder.putUserData(PAREN_COUNT, c+1);
        boolean r = ToitParser.expression(psiBuilder, level, -1);
        psiBuilder.putUserData(PAREN_COUNT, c);
        return r;
    }

    public static boolean parseBlockExpression(PsiBuilder psiBuilder, int level) {
        Integer c = psiBuilder.getUserData(PAREN_COUNT);
        if (c != null && c == 0) return false;
        return ToitParser.block(psiBuilder,level);
    }

    static class IndentTracker {
        Map<Integer, Integer> levelToStart = new HashMap<>();
        SortedSet<Integer> indentMarkers = new TreeSet<>();


        void startLevel(int level, int position) {
            levelToStart.put(level, position);
        }

        // Returns the number of INDENT_OUT to safely consume
        int leaveLevel(int level, int position) {
            int result = 0;
            int startPosition = levelToStart.get(level);
            for (int indentPos : indentMarkers.tailSet(startPosition)) {
                if (indentPos >= position) break;
                result ++;
            }
            return result;
        }

        void recordIndent(int position) {
            indentMarkers.add(position);
        }
    }

    private final static Key<IndentTracker> expressionIndentTrackerKey = Key.create("expressionIndentTracker");
    public static boolean parseOuterExpression(PsiBuilder psiBuilder, int level) {
        var indentTracker = new IndentTracker();
        indentTracker.startLevel(level, psiBuilder.getCurrentOffset());
        psiBuilder.putUserData(expressionIndentTrackerKey, indentTracker);

        boolean r = ToitParser.expression(psiBuilder, level, -1);
        if (!r) return false;

        int indentOutsToPop = indentTracker.leaveLevel(level, psiBuilder.getCurrentOffset());
        if (indentOutsToPop > 0 ) {
            while (Objects.equals(psiBuilder.getTokenType(), ToitTypes.NEWLINE)) {
                psiBuilder.advanceLexer(); // EAT newlines
            }

            while (indentOutsToPop-- > 0 && Objects.equals(psiBuilder.getTokenType(), ToitTypes.INDENT_OUT)) {
                psiBuilder.advanceLexer();
            }

        }

        return true;
    }

    public static boolean parseHandleExpressionNewlineAndIndent(PsiBuilder psiBuilder, int level) {
        IndentTracker indentTracker = psiBuilder.getUserData(expressionIndentTrackerKey);
        if (indentTracker == null) return false;
psiBuilder.advanceLexer();
        while (true) {
            if (Objects.equals(psiBuilder.getTokenType(), ToitTypes.NEWLINE)) {
                psiBuilder.advanceLexer();
            } else if (Objects.equals(psiBuilder.getTokenType(), ToitTypes.INDENT_IN)) {
                psiBuilder.advanceLexer();
                indentTracker.recordIndent(psiBuilder.getCurrentOffset());
            } else {
                psiBuilder.putUserData(expressionIndentTrackerKey, indentTracker);
                return true;
            }
        }
    }

//    public static boolean parsePreviousImplicitlyClosesStatement(PsiBuilder psiBuilder, int level) {
//        LighterASTNode latestDoneMarker = psiBuilder.getLatestDoneMarker();
//        if (latestDoneMarker != null && (
//                Objects.equals(latestDoneMarker.getTokenType(), ToitTypes.BLOCK_EXPRESSION)
//                        || Objects.equals(latestDoneMarker.getTokenType(), ToitTypes.CALL_OR_REFERENCE_EXPRESSION)
//        )) return true;
//
//        return false;
//    }
//


//    public static boolean parseIndentOutConvertToNewline(PsiBuilder psiBuilder, int level) {
//        String dgb = psiBuilder.getOriginalText().subSequence(Math.max(0,psiBuilder.getCurrentOffset()-40), psiBuilder.getCurrentOffset()).toString();
//        if (Objects.equals(psiBuilder.getTokenType(), ToitTypes.INDENT_OUT)) {
//            psiBuilder.remapCurrentToken(ToitTypes.NEWLINE);
//            return true;
//        }
//        return false;
//    }
}


/*
package org.toitlang.intellij.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.toitlang.intellij.psi.ToitTypes.*;
import static org.toitlang.intellij.parser.ToitParserUtil.*;

import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class ToitParser implements PsiParser, LightPsiParser {
  static int ll; static final String _spaces = "                                                                 ";
  static String ttText(PsiBuilder psiBuilder) {
    if (psiBuilder.getTokenType() == null) return "eof";
    return psiBuilder.getTokenType().getDebugName() + "/" + (psiBuilder.getTokenText() != null?psiBuilder.getTokenText().replace("\n","\\n"):"<<null>>");
  }
  public static boolean recursion_guard_(PsiBuilder builder, int level, String funcName) {
    ll = level;
    System.out.println(_spaces.substring(0, level)+funcName + "("+ttText(builder)+")");
    return true;
  }
  static void report(PsiBuilder builder, boolean result, IElementType elementType) {
    System.out.print(_spaces.substring(0, ll--)+result + "("+ttText(builder)+")");

    if (elementType != null) System.out.println(": " +elementType.getDebugName());
    else System.out.println();

    if (builder.getLatestDoneMarker() != null) System.out.println(builder.getLatestDoneMarker().getTokenType().getDebugName()+":\n"+builder.getOriginalText().subSequence(builder.getLatestDoneMarker().getStartOffset(), builder.getLatestDoneMarker().getEndOffset()));
  }

  public static void exit_section_(PsiBuilder builder,
                                   PsiBuilder.Marker marker,
                                   @Nullable IElementType elementType,
                                   boolean result) {
    GeneratedParserUtilBase.exit_section_(builder,marker,elementType,result);
    report(builder, result, elementType);
  }

  public static void exit_section_(PsiBuilder builder,
                                   int level,
                                   PsiBuilder.Marker marker,
                                   boolean result,
                                   boolean pinned,
                                   @Nullable Parser eatMore) {
    exit_section_(builder, level, marker, null, result, pinned, eatMore);
    //if (result) System.out.println(elementType.getDebugName());
  }

  public static void exit_section_(PsiBuilder builder,
                                   int level,
                                   PsiBuilder.Marker marker,
                                   @Nullable IElementType elementType,
                                   boolean result,
                                   boolean pinned,
                                   @Nullable Parser eatMore) {
    GeneratedParserUtilBase.exit_section_(builder, level,marker,elementType, result, pinned, eatMore);
    report(builder, result, elementType);
  }

 */