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

    public void visit(ToitStructure toitStructure) {
        visitElement(toitStructure);
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

    public void visit(ToitImportDeclaration toitImportDeclaration) {
        visitElement(toitImportDeclaration);
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

    public void visit(ToitType toitType) {
        visitElement(toitType);
    }

    public void visit(ToitReturn toitReturn) {
        visitElement(toitReturn);
    }

    public void visit(ToitTry toitTry) {
        visitElement(toitTry);
    }

    public void visit(ToitVariableDeclaration toitVariableDeclaration) {
        visitElement(toitVariableDeclaration);
    }

    public void visit(ToitReferenceIdentifier toitReferenceIdentifier) {
        visitElement(toitReferenceIdentifier);
    }

    public void visit(ToitNameableIdentifier toitNameableIdentifier) {
        visitElement(toitNameableIdentifier);
    }

    public void visit(ToitPseudoKeyword toitPseudoKeyword) {
        visitElement(toitPseudoKeyword);
    }

    public void visit(ToitAssert toitAssert) {
        visitElement(toitAssert);
    }

    public void visit(ToitRecover toitRecover) {
        visitElement(toitRecover);
    }
}
