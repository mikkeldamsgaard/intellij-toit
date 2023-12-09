// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.calls.ToitCallHelper;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.reference.ToitEvaluatedType;
import org.toitlang.intellij.psi.reference.ToitExpressionReferenceTarget;
import org.toitlang.intellij.psi.reference.ToitReferenceTargets;
import org.toitlang.intellij.psi.scope.ToitScope;

import java.util.*;

public class ToitCallExpression extends ToitExpression {

    public ToitCallExpression(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public <T> T accept(ToitExpressionVisitor<T> expressionVisitor) {
        return expressionVisitor.visit(this);
    }

    public ToitFunction getFunction() {
        var callee = getFirstChildOfType(ToitExpression.class);
        if (callee == null) return null;
        ToitReferenceIdentifier lastRef = callee.getLastDescendentOfType(ToitReferenceIdentifier.class);
        if (lastRef == null) return null;
        var function = lastRef.getReference().resolve();
        if (function instanceof ToitFunction) return (ToitFunction) function;
        return null;
    }

    public @NotNull List<ToitFunction> getFunctions() {
        List<ToitFunction> result = new ArrayList<>();
        var callee = getFirstChildOfType(ToitExpression.class);
        if (callee == null) return result;
        ToitReferenceIdentifier lastRef = callee.getLastDescendentOfType(ToitReferenceIdentifier.class);
        if (lastRef == null) return result;
        var functions = lastRef.getReference().multiResolve(false);
        for (ResolveResult resolveResult : functions) {
            var resolvedElement = resolveResult.getElement();
            if (resolvedElement instanceof ToitFunction) result.add((ToitFunction) resolvedElement);
            if (resolvedElement instanceof ToitStructure) result.addAll(((ToitStructure)resolvedElement).getDefaultConstructors());
        }
        return result;
    }

    public List<ToitElement> getArguments() {
        List<ToitElement> arguments = new ArrayList<>();
        var children = getChildren();
        if (children.length <= 1) return null;
        for (int i = 1; i < children.length; i++) {
            if (children[i] instanceof ToitExpression || children[i] instanceof ToitNamedArgument)
                arguments.add((ToitElement) children[i]);
        }
        return arguments;
    }

    @Override
    public ToitReferenceTargets getReferenceTargets(ToitScope scope) {
        var result = new LinkedList<ToitExpressionReferenceTarget>();
        var expressions = getChildrenOfType(ToitExpression.class);
        if (expressions.isEmpty()) return new ToitReferenceTargets();
        ToitExpression method = expressions.get(0);
        ToitReferenceTargets referenceTargets = method.getReferenceTargets(scope);
        for (ToitExpressionReferenceTarget referenceTarget : referenceTargets.getTargets()) {
            if (referenceTarget.getTarget() instanceof ToitFunction) {
                ToitFunction function = (ToitFunction) referenceTarget.getTarget();
                if (ToitCallHelper.parametersMatches(function, getArguments()) != null) {
                   result.add(new ToitExpressionReferenceTarget(function));
                }
            } else if (referenceTarget.getTarget() instanceof ToitStructure) {
                ToitStructure structure = (ToitStructure) referenceTarget.getTarget();
                for (ToitFunction defaultConstructor : structure.getDefaultConstructors()) {
                    if (ToitCallHelper.parametersMatches(defaultConstructor, getArguments()) != null) {
                        result.add(new ToitExpressionReferenceTarget(defaultConstructor));
                    }
                }
            }
        }
        return new ToitReferenceTargets(result);
    }
}
