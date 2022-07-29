package org.toitlang.intellij.psi.scope;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.visitor.ToitVisitableElement;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.ArrayList;
import java.util.List;

public class ToitLocalScopeCalculator extends ToitVisitor {
    private final static String SUPER = "super";
    private final static String THIS = "this";

    private final IToitElement origin;
    private final List<ToitScope> localScopes;

    public ToitLocalScopeCalculator(IToitElement origin) {
        this.origin = origin;
        this.localScopes = new ArrayList<>();
    }

    public static ToitScope calculate(IToitElement origin) {
        return new ToitLocalScopeCalculator(origin).calculate();
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
                return toitVariableDeclaration.getToitFile().getToitFileScope().getToitScope();
            }
        }

        if (scopeStart == null) scopeStart = origin;

        scopeStart.accept(this);
        return ToitScope.chain(origin +"-"+origin.getName()+"-local", localScopes.toArray(new ToitScope[0]));
    }

    public void visitElement(@NotNull PsiElement element) {
        if (element.getParent() instanceof ToitBlock) {
            ToitScope scope = new ToitScope(element.toString(), true);
            var e = element.getPrevSibling();
            while (e != null) {
                if (e instanceof ToitVariableDeclaration) addVariableDeclarationToScope(scope, (ToitVariableDeclaration) e);
                e = e.getPrevSibling();
            }
            localScopes.add(scope);
        }
        if (!(element instanceof ToitVisitableElement)) return;
        element.getParent().accept(this);
    }

    @Override
    public void visit(ToitVariableDeclaration toitVariableDeclaration) {
        ToitScope scope = new ToitScope(toitVariableDeclaration.getName(), true);
        addVariableDeclarationToScope(scope, toitVariableDeclaration);
        localScopes.add(scope);
        visitElement(toitVariableDeclaration);
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
        ToitScope scope = new ToitScope(element.toString(), true);
        for (var v : element.getChildrenOfType(ToitVariableDeclaration.class)) {
            addVariableDeclarationToScope(scope,v);
        }
        localScopes.add(scope);
        visitElement(element);
    }


    @Override
    public void visit(ToitStructure toitStructure) {
        localScopes.add(toitStructure.getScope(false));
    }

    @Override
    public void visit(ToitFunction toitFunction) {
        localScopes.add(toitFunction.getParameterScope());
        visitElement(toitFunction);
    }

    @Override
    public void visit(ToitBlock toitBlock) {
        localScopes.add(toitBlock.getParameterScope());

        if (toitBlock.getParent() instanceof ToitFunction) {
            ToitFunction function = (ToitFunction) toitBlock.getParent();

            ToitStructure structure = toitBlock.getParentOfType(ToitStructure.class);
            if (!function.isStatic() && structure != null) {
                ToitScope superThis = new ToitScope(function.getName(), true);
                superThis.add(THIS, structure);

                ToitStructure baseClass = structure.getBaseClass();
                if (baseClass != null) {
                    if (!function.isConstructor()) {
                        superThis.add(SUPER, baseClass.getScope(false).resolve(function.getName()));
                    } else {
                        superThis.add(SUPER, baseClass);
                    }
                }

                localScopes.add(superThis);
            }
        }
        visitElement(toitBlock);
    }

    private static void addVariableDeclarationToScope(ToitScope scope, ToitVariableDeclaration toitVariableDeclaration) {
        scope.add(toitVariableDeclaration.getName(), toitVariableDeclaration);
    }
}
