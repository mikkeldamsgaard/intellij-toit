package org.toitlang.intellij.psi.scope;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.reference.ReferenceCalculation;
import org.toitlang.intellij.psi.visitor.ToitVisitableElement;
import org.toitlang.intellij.psi.visitor.ToitVisitor;
import org.toitlang.intellij.utils.ToitScope;

import java.util.Map;

public class ToitLocalScopeCalculator extends ToitVisitor {
    private final ToitReferenceIdentifier origin;
    private final ToitScope scope;

    public ToitLocalScopeCalculator(ToitReferenceIdentifier origin, ToitScope scope) {
        this.origin = origin;
        this.scope = scope;
    }

    public static ToitScope calculate(ToitReferenceIdentifier origin, ToitScope parentScope) {
        return new ToitLocalScopeCalculator(origin,parentScope.derive()).calculate();
    }

    private ToitScope calculate() {
        origin.accept(this);
        return scope;
    }

    public void visitElement(@NotNull PsiElement element) {
        if (element.getParent() instanceof ToitBlock) {
            var e = element.getPrevSibling();
            while (e != null) {
                if (e instanceof ToitVariableDeclaration) visit((ToitVariableDeclaration) e);
                e = e.getPrevSibling();
            }
        }
        if (!(element instanceof ToitVisitableElement)) return;
        element.getParent().accept(this);
    }

    @Override
    public void visit(ToitVariableDeclaration toitVariableDeclaration) {
        scope.add(toitVariableDeclaration.getName(), toitVariableDeclaration);
    }

    @Override
    public void visit(ToitWhile toitWhile) {
        toitWhile.getFirstChild().accept(this);
        super.visit(toitWhile);
    }

    @Override
    public void visit(ToitIf toitIf) {
        for (var k : toitIf.getChildren()) {
            if (k instanceof ToitVariableDeclaration) visit((ToitVariableDeclaration) k);
        }
        super.visit(toitIf);
    }

    @Override
    public void visit(ToitStructure toitStructure) {
        toitStructure.getScope(scope);
    }

    @Override
    public void visit(ToitFunction toitFunction) {
        toitFunction.childrenOfType(ToitParameterName.class).forEach(p -> scope.add(p.getNameIdentifier().getName(), p));
        super.visit(toitFunction);
    }
}
