package org.toitlang.intellij.psi.scope;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.visitor.ToitVisitableElement;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

public class ToitLocalScopeCalculator extends ToitVisitor {
    private final ToitReferenceIdentifier origin;
    private ToitScope scope;

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
                if (e instanceof ToitVariableDeclaration) addVariableDeclarationToScope((ToitVariableDeclaration) e);
                e = e.getPrevSibling();
            }
        }
        if (!(element instanceof ToitVisitableElement)) return;
        element.getParent().accept(this);
    }

    @Override
    public void visit(ToitVariableDeclaration toitVariableDeclaration) {
        addVariableDeclarationToScope(toitVariableDeclaration);
        visitElement(toitVariableDeclaration);
    }

    @Override
    public void visit(ToitWhile toitWhile) {
        processNesterVariableDeclarations(toitWhile);
    }

    @Override
    public void visit(ToitIf toitIf) {
        processNesterVariableDeclarations(toitIf);
    }

    @Override
    public void visit(ToitFor toitFor) {
        processNesterVariableDeclarations(toitFor);
    }

    private void processNesterVariableDeclarations(ToitElement element) {
        for (var v : element.childrenOfType(ToitVariableDeclaration.class)) {
            addVariableDeclarationToScope(v);
        }
        visitElement(element);
    }


    @Override
    public void visit(ToitStructure toitStructure) {
        scope = scope.chain(toitStructure.getScope(scope));
    }

    @Override
    public void visit(ToitFunction toitFunction) {
        toitFunction.getParameters().forEach(p -> scope.add(p.getNameIdentifier().getName(), p));
        visitElement(toitFunction);
    }

    @Override
    public void visit(ToitBlock toitBlock) {
        toitBlock.getParameters().forEach(p -> scope.add(p.getNameIdentifier().getName(), p));
        visitElement(toitBlock);
    }

    private void addVariableDeclarationToScope(ToitVariableDeclaration toitVariableDeclaration) {
        scope.add(toitVariableDeclaration.getName(), toitVariableDeclaration);
    }
}
