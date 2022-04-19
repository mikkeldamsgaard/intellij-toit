package org.toitlang.intellij.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.calls.ParameterInfo;
import org.toitlang.intellij.psi.calls.ToitCallHelper;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.reference.ToitEvaluatedType;
import org.toitlang.intellij.psi.scope.ToitScope;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

public class TypeInspection extends LocalInspectionTool {
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new ToitVisitor() {
            @Override
            public void visit(ToitExpression toitExpression) {
                toitExpression.accept(new ToitExpressionVisitor<>() {
                    @Override
                    public Object visit(ToitAssignmentExpression toitAssignmentExpression) {
                        var variable = toitAssignmentExpression.getLeftHandSide();
                        if (variable == null) return null;
                        ToitScope localToitResolveScope = toitAssignmentExpression.getLocalToitResolveScope();
                        ToitEvaluatedType variableType = variable.getType(localToitResolveScope);
                        if (variableType == null || variableType.isUnresolved() || variableType.isEstimated()) return null;
                        var expression = toitAssignmentExpression.getRightHandSide();
                        if (expression == null) return null;
                        if (expression.isNull()) {
                            var ref = variable.getLastDescendentOfType(ToitReferenceIdentifier.class);
                            if (ref == null) return null;
                            var resolved = ref.getReference().resolve();
                            if (resolved instanceof ToitVariableDeclaration) {
                                var toitVariableDeclaration = (ToitVariableDeclaration) resolved;
                                if (!toitVariableDeclaration.isReturnTypeNullable()) {
                                    holder.registerProblem(expression, "Cannot assign null to variable declared without nullable type");
                                }
                            }
                        } else {
                            ToitEvaluatedType expressionType = expression.getType(localToitResolveScope);
                            if (!expressionType.isAssignableTo(variableType))
                                holder.registerProblem(variable, "Cannot assign expression of type " + expressionType + " to variable of type " + variableType);
                        }
                        return null;
                    }

                    @Override
                    public Object visit(ToitPrimaryExpression toitPrimaryExpression) {
                        if (toitPrimaryExpression.getParent() instanceof ToitCallExpression)
                            return null; // Handled by visit to ToitCallExpression
                        checkFunctionCallNoArgs(toitPrimaryExpression);
                        return null;
                    }

                    @Override
                    public Object visit(ToitPostfixExpression toitPostfixExpression) {
                        // Each of the derefs can cause a function call, so check everything up to the next to last deref
                        var children = toitPostfixExpression.getChildren();
                        for (int i = 1; i < children.length - 1; i++) {
                            if (children[i] instanceof ToitDerefExpression) {
                                var deref = (ToitDerefExpression)children[i];
                                checkFunctionCallNoArgs(deref);
                            }
                        }

                        if (toitPostfixExpression.getParent() instanceof ToitCallExpression)
                            return null; // Handled by visit to ToitCallExpression
                        checkFunctionCallNoArgs((ToitExpression) children[children.length-1]);
                        return null;
                    }

                    private void checkFunctionCallNoArgs(ToitExpression expression) {
                        if (ToitCallHelper.isFunctionCall(expression)) {
                            if (ToitCallHelper.resolveCall(expression) == null) {
                                holder.registerProblem(expression.getLastDescendentOfType(ToitReferenceIdentifier.class),
                                        "Function needs arguments, none were supplied");
                            }
                        }
                    }

                    @Override
                    public Object visit(ToitCallExpression toitCallExpression) {
                        var callee = toitCallExpression.getFirstChildOfType(ToitExpression.class);
                        if (callee == null) return null;
                        var ref = callee.getLastDescendentOfType(ToitReferenceIdentifier.class);
                        if (ref == null) return null;
                        if (ref.getReference().resolve() == null) return null;

                        // Check if the function can resolve
                        var resolvedFunctionCall = ToitCallHelper.resolveCall(toitCallExpression);
                        if (resolvedFunctionCall == null) {
                            IToitElement errorElm = toitCallExpression.getFirstChildOfType(ToitExpression.class);
                            if (errorElm != null) errorElm = errorElm.getLastDescendentOfType(ToitReferenceIdentifier.class);
                            if (errorElm == null) errorElm = toitCallExpression;
                            holder.registerProblem(errorElm, "Cannot match arguments to function call");
                        } else {
                            // Check parameter types
                            for (IToitElement argument : resolvedFunctionCall.getArguments()) {
                                if (argument instanceof ToitExpression) {
                                    ToitEvaluatedType type = ((ToitExpression)argument).getType(argument.getLocalToitResolveScope());
                                    ParameterInfo parameterInfo = resolvedFunctionCall.getParamForArg(argument);
                                    if (type != null && !type.isUnresolved() && parameterInfo.getType() != null) {
                                        ToitStructure resolvedParameterType = parameterInfo.getType().resolve();
                                        if (resolvedParameterType != null && !type.isAssignableTo(resolvedParameterType)) {
                                            holder.registerProblem(argument, "Cannot assign expression of type "+type.getStructure().getName()+" to parameter of type "+ resolvedParameterType.getName());
                                        }
                                    }
                                }
                            }
                        }

                        return null;
                    }

                });
            }

            @Override
            public void visit(ToitVariableDeclaration toitVariableDeclaration) {
                var declaredType = toitVariableDeclaration.getType();
                if (declaredType == null) return;
                ToitStructure declaredStructure = declaredType.resolve();
                if (declaredStructure == null) return;
                var expression = toitVariableDeclaration.getFirstChildOfType(ToitExpression.class);
                if (expression == null) return;
                if (expression.isNull()) {
                    if (!toitVariableDeclaration.isReturnTypeNullable()) {
                        holder.registerProblem(expression, "Cannot assign null to variable declared without nullable type");
                    }
                } else {
                    ToitEvaluatedType expressionType = expression.getType(toitVariableDeclaration.getLocalToitResolveScope());
                    if (expressionType.getStructure() == null) return;
                    if (!expressionType.isAssignableTo(declaredStructure))
                        holder.registerProblem(expression, "Cannot assign expression of type " + expressionType + " to variable of type " + declaredType.getName());
                }
            }

            @Override
            public void visit(ToitReturn toitReturn) {
                var function = toitReturn.getParentOfType(ToitFunction.class);
                if (function == null) return;
                var functionReturnType = function.getType();
                if (functionReturnType == null) return;
                var functionReturnStructure = functionReturnType.resolve();
                if (functionReturnStructure == null) return;

                var expression = toitReturn.getFirstChildOfType(ToitExpression.class);
                if (expression == null) return;

                if (expression.isNull()) {
                    if (!function.isReturnTypeNullable()) {
                        holder.registerProblem(expression, "Cannot return null from function declared without nullable return type");
                    }
                } else {
                    ToitEvaluatedType expressionType = expression.getType(toitReturn.getLocalToitResolveScope());
                    if (!expressionType.isAssignableTo(functionReturnStructure))
                        holder.registerProblem(expression, "Cannot return expression of type " + expressionType + " from function declared with return type " + functionReturnType.getName());
                }
            }
        };
    }
}
