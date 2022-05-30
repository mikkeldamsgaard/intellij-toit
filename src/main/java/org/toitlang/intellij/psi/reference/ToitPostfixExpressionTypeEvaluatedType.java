package org.toitlang.intellij.psi.reference;

import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.scope.ToitScope;

import java.util.LinkedHashMap;

import static org.toitlang.intellij.psi.reference.ToitEvaluatedType.resolveTypeOfNameInScope;

public class ToitPostfixExpressionTypeEvaluatedType {
    private final LinkedHashMap<ToitExpression, ToitEvaluatedType> prevModel;
    private ToitEvaluatedType last;

    public ToitPostfixExpressionTypeEvaluatedType() {
        prevModel = new LinkedHashMap<>();
    }

    public ToitEvaluatedType getLast() {
        return last;
    }

    public ToitEvaluatedType getTypeForPreviousChild(ToitExpression child) {
        return prevModel.get(child);
    }

    private void addType(ToitExpression child, ToitEvaluatedType type) {
        prevModel.put(child, last);
        last = type;
    }



    public static ToitPostfixExpressionTypeEvaluatedType calculate(ToitPostfixExpression toitPostfixExpression) {
        var result = new ToitPostfixExpressionTypeEvaluatedType();

        ToitEvaluatedType prev = null;

        for (ToitExpression child : toitPostfixExpression.getChildrenOfType(ToitExpression.class)) {
            if (prev == null) {
                ToitScope resolveScope = null;

                var topLevelExpr = toitPostfixExpression.getParentOfType(ToitTopLevelExpression.class);
                if (topLevelExpr != null && topLevelExpr.getParent() instanceof ToitVariableDeclaration) {
                    ToitVariableDeclaration toitVariableDeclaration = (ToitVariableDeclaration) topLevelExpr.getParent();
                    var prevParentSibling = toitVariableDeclaration.getPrevSiblingOfType(ToitElement.class);
                    if (prevParentSibling != null) {
                        resolveScope = prevParentSibling.getLocalToitResolveScope();
                    }

                    if (resolveScope == null) {
                        var parent = toitVariableDeclaration.getParentOfType(ToitElement.class);
                        if (parent != null) resolveScope = parent.getLocalToitResolveScope();
                    }

                    if (resolveScope == null) {
                        resolveScope = toitVariableDeclaration.getToitFile().getToitFileScope().getToitScope();
                    }
                }

                if (resolveScope == null) {
                    resolveScope = toitPostfixExpression.getLocalToitResolveScope();
                }

                prev = child.getType(resolveScope);
                result.addType(child, prev);
                continue;
            }

            if (!prev.isUnresolved()) {
                final var fPrev = prev;
                prev = child.accept(new ToitExpressionVisitor<>() {
                    @Override
                    public ToitEvaluatedType visit(ToitDerefExpression toitDerefExpression) {
                        String name = toitDerefExpression.getName();
                        if (name == null) return ToitEvaluatedType.UNRESOLVED;
                        if (fPrev.getFile() != null) {
                            ToitScope prevFileScope = fPrev.getFile().getToitFileScope().getExportedScope();
                            return resolveTypeOfNameInScope(name, prevFileScope);
                        } else {
                            ToitScope scructureScope = fPrev.getStructure().getScope(fPrev.isStatic());
                            return resolveTypeOfNameInScope(name, scructureScope);
                        }
                    }

                    @Override
                    public ToitEvaluatedType visit(ToitPostfixIncrementExpression toitPostfixIncrementExpression) {
                        // TODO: Lookup the operator and find the return type.
                        //   For now, return UNRESOLVED
                        return ToitEvaluatedType.UNRESOLVED;
                    }
                });
            }
            result.addType(child,prev);
        }

        return result;

    }
}
