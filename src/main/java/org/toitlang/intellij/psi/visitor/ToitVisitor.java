package org.toitlang.intellij.psi.visitor;

import com.intellij.psi.PsiElementVisitor;
import org.toitlang.intellij.psi.ast.*;

public class ToitVisitor extends PsiElementVisitor {
    public void visit(ToitWhile toitWhile) {
        visitElement(toitWhile);
    }

    public void visit(ToitBlock toitBlock) {
        visitElement(toitBlock);
    }

    public void visit(ToitClass toitClass) {
        visitElement(toitClass);
    }

    public void visit(ToitEmptyStatement toitEmptyStatement) {
        visitElement(toitEmptyStatement);
    }

    public void visit(ToitExportDeclaration toitExportDeclaration) {
        visitElement(toitExportDeclaration);
    }

    public void visit(ToitExpression toitExpression) {
        visitElement(toitExpression);
    }

    public void visit(ToitFor toitFor) {
        visitElement(toitFor);
    }

    public void visit(ToitFunction toitFunction) {
        visitElement(toitFunction);
    }

    public void visit(ToitIf toitIf) {
        visitElement(toitIf);
    }

    public void visit(ToitImportAs toitImportAs) {
        visitElement(toitImportAs);
    }

    public void visit(ToitImportDeclaration toitImportDeclaration) {
        visitElement(toitImportDeclaration);
    }

    public void visit(ToitImportShow toitImportShow) {
        visitElement(toitImportShow);
    }

    public void visit(ToitInterface toitInterface) {
        visitElement(toitInterface);
    }

    public void visit(ToitMonitor toitMonitor) {
        visitElement(toitMonitor);
    }

    public void visit(ToitNamedArgument toitNamedArgument) {
        visitElement(toitNamedArgument);
    }

    public void visit(ToitOperator toitOperator) {
        visitElement(toitOperator);
    }

    public void visit(ToitParameterName toitParameterName) {
        visitElement(toitParameterName);
    }

    public void visit(ToitPrimitive toitPrimitive) {
        visitElement(toitPrimitive);
    }

    public void visit(ToitQualifiedName toitQualifiedName) {
        visitElement(toitQualifiedName);
    }

    public void visit(ToitReturn toitReturn) {
        visitElement(toitReturn);
    }

    public void visit(ToitSimpleLiteral toitSimpleLiteral) {
        visitElement(toitSimpleLiteral);
    }

    public void visit(ToitTry toitTry) {
        visitElement(toitTry);
    }

    public void visit(ToitVariableDeclaration toitVariableDeclaration) {
        visitElement(toitVariableDeclaration);
    }

    public void visit(ToitIdentifier toitIdentifier) {
        visitElement(toitIdentifier);
    }
}
