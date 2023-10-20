package org.toitlang.intellij.psi.scope;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.visitor.ToitVisitableElement;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

public class ToitLocalScopeCalculator extends ToitVisitor {
    private final static String SUPER = "super";

    private final static String THIS = "this";

    private final IToitElement origin;

    private ToitScope current;

    public ToitLocalScopeCalculator(IToitElement origin, ToitScope parent) {
        this.origin = origin;
        this.current = parent;
    }

    public static ToitScope calculate(IToitElement origin, ToitScope parent) {
        return new ToitLocalScopeCalculator(origin, parent).calculate();
    }

    private ToitVariableDeclaration getImmediateVariableDeclarationParentOfOrigin() {
        PsiElement p = origin;
        while (p != null) {
            if (p instanceof ToitBlock) return null;
            if (p instanceof ToitVariableDeclaration) return (ToitVariableDeclaration) p;
            p = p.getParent();
        }
        return null;
    }

    private ToitScope calculate() {
        IToitElement scopeStart = null;
        ToitVariableDeclaration toitVariableDeclaration = getImmediateVariableDeclarationParentOfOrigin();
        if (toitVariableDeclaration != null) { // Catch cases such as "x := x.y"
            scopeStart = toitVariableDeclaration.getPrevSiblingOfType(IToitElement.class);

            if (scopeStart == null) {
                scopeStart = toitVariableDeclaration.getParentOfType(ToitElement.class);
            }

            if (scopeStart == null) {
                return toitVariableDeclaration.getToitFile().getToitFileScope().getToitScope(current);
            }
        }

        if (scopeStart == null) scopeStart = origin;

        scopeStart.accept(this);

        return current;
    }

    void pushScope(String name) {
        current = current.sub(name);
    }

    public void visitElement(@NotNull PsiElement element) {
        if (!(element instanceof ToitVisitableElement)) return;
        element.getParent().accept(this);

        if (element.getParent() instanceof ToitBlock) {
            pushScope(element.toString());
            var e = element.getPrevSibling();
            while (e != null) {
                if (e instanceof ToitVariableDeclaration) addVariableDeclarationToCurrent((ToitVariableDeclaration) e);
                e = e.getPrevSibling();
            }
        }

    }

    @Override
    public void visit(ToitVariableDeclaration toitVariableDeclaration) {
        visitElement(toitVariableDeclaration);
        pushScope(toitVariableDeclaration.getName());
        addVariableDeclarationToCurrent(toitVariableDeclaration);
    }

    @Override
    public void visit(ToitWhile toitWhile) {
        processNestedVariableDeclarations(toitWhile);
    }

    @Override
    public void visit(ToitIf toitIf) {
        processNestedVariableDeclarations(toitIf);
    }

    @Override
    public void visit(ToitFor toitFor) {
        processNestedVariableDeclarations(toitFor);
    }

    private void processNestedVariableDeclarations(ToitElement element) {
        visitElement(element);
        pushScope(element.toString());
        for (var v : element.getChildrenOfType(ToitVariableDeclaration.class)) {
            addVariableDeclarationToCurrent(v);
        }
    }


    @Override
    public void visit(ToitStructure toitStructure) {
        current = toitStructure.getScope(false, current);
    }

    @Override
    public void visit(ToitFunction toitFunction) {
        visitElement(toitFunction);

        ToitStructure structure = toitFunction.getParentOfType(ToitStructure.class);
        if (structure != null && !toitFunction.isStatic()) {
            pushScope(toitFunction.getName() + "::super-this");
            current.add(THIS, structure);

            ToitStructure baseClass = structure.getBaseClass();
            if (baseClass != null) {
                if (!toitFunction.isConstructor()) {
                    current.add(SUPER, baseClass.getScope(false, current).resolve(toitFunction.getName()));
                } else {
                    current.add(SUPER, baseClass);
                }
            }
        }

        current = toitFunction.getParameterScope(current);
    }

    @Override
    public void visit(ToitBlock toitBlock) {
        visitElement(toitBlock);

        current = toitBlock.getParameterScope(current);
    }

    private void addVariableDeclarationToCurrent(ToitVariableDeclaration toitVariableDeclaration) {
        current.add(toitVariableDeclaration.getName(), toitVariableDeclaration);
    }
}
