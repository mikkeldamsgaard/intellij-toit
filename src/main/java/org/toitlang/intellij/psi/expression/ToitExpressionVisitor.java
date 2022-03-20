package org.toitlang.intellij.psi.expression;

import org.toitlang.intellij.psi.ast.*;

public class ToitExpressionVisitor<T> {
    public T visit(ToitShiftExpression toitShiftExpression) { return visitExpression(toitShiftExpression); }

    public T visit(ToitUnaryExpression toitUnaryExpression) {
        return visitExpression(toitUnaryExpression);
    }

    public T visit(ToitRelationalExpression toitRelationalExpression) {
        return visitExpression(toitRelationalExpression);
    }

    public T visit(ToitPostfixExpression toitPostfixExpression) {
        return visitExpression(toitPostfixExpression);
    }

    public T visit(ToitDerefExpression toitDerefExpression) {
        return visitExpression(toitDerefExpression);
    }

    public T visit(ToitAdditiveExpression toitAdditiveExpression) {
        return visitExpression(toitAdditiveExpression);
    }

    public T visit(ToitAssignmentExpression toitAssignmentExpression) {
        return visitExpression(toitAssignmentExpression);
    }

    public T visit(ToitEqualityExpression toitEqualityExpression) {
        return visitExpression(toitEqualityExpression);
    }

    public T visit(ToitElvisExpression toitElvisExpression) {
        return visitExpression(toitElvisExpression);
    }

    public T visit(ToitBitXOrExpression toitBitXOrExpression) {
        return visitExpression(toitBitXOrExpression);
    }

    public T visit(ToitBitOrExpression toitBitOrExpression) {
        return visitExpression(toitBitOrExpression);
    }

    public T visit(ToitAndExpression toitAndExpression) {
        return visitExpression(toitAndExpression);
    }

    public T visit(ToitIndexingExpression toitIndexingExpression) {
        return visitExpression(toitIndexingExpression);
    }

    public T visit(ToitOrExpression toitOrExpression) {
        return visitExpression(toitOrExpression);
    }

    public T visit(ToitSetLiteral toitSetLiteral) {
        return visitExpression(toitSetLiteral);
    }

    public T visit(ToitListLiteral toitListLiteral) {
        return visitExpression(toitListLiteral);
    }

    public T visit(ToitNotExpression toitNotExpression) {
        return visitExpression(toitNotExpression);
    }

    public T visit(ToitMultiplicativeExpression toitMultiplicativeExpression) {
        return visitExpression(toitMultiplicativeExpression);
    }

    public T visit(ToitMapLiteral toitMapLiteral) {
        return visitExpression(toitMapLiteral);
    }

    public T visit(ToitCallExpression toitCallExpression) {
        return visitExpression(toitCallExpression);
    }

    public T visit(ToitBitAndExpression toitBitAndExpression) {
        return visitExpression(toitBitAndExpression);
    }

    public T visit(ToitPrimaryExpression toitPrimaryExpression) {
        return visitExpression(toitPrimaryExpression);
    }

    public T visit(ToitTopLevelExpression toitTopLevelExpression) {
        return visitExpression(toitTopLevelExpression);
    }

    public T visit(ToitSimpleLiteral toitSimpleLiteral) {
        return visitExpression(toitSimpleLiteral);
    }
    
    public T visitExpression(ToitExpression expression ) { return null; }
}
