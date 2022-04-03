package org.toitlang.intellij.psi.calls;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.*;

/** Helper function to resolve expressions to calls. Both PrimaryExpression (with on reference identifier inside) and
 * PostfixExpression can be calls in disguise with zero arguments */
public class ToitCallHelper {
    public static ToitFunction resolveCall(ToitExpression expression) {
        return expression.accept(new ToitExpressionVisitor<>() {
            @Override
            public ToitFunction visit(ToitCallExpression toitCallExpression) {
                List<IToitElement> arguments = new ArrayList<>();
                var children = toitCallExpression.getChildren();
                if (children.length <= 1) return null;
                for (int i= 1;i<children.length;i++) {
                    if (children[i] instanceof ToitExpression || children[i] instanceof ToitNamedArgument) arguments.add((IToitElement) children[i]);
                }

                for (ToitFunction function : toitCallExpression.getFunctions()) {
                    if (parametersMatches(function, arguments)) return function;
                }
                return null;
            }

            @Override
            public ToitFunction visit(ToitPostfixExpression toitPostfixExpression) {
                return resolveFunction(toitPostfixExpression);
            }

            @Override
            public ToitFunction visit(ToitPrimaryExpression toitPrimaryExpression) {
                return resolveFunction(toitPrimaryExpression);
            }

            @Override
            public ToitFunction visit(ToitDerefExpression toitDerefExpression) {
                return resolveFunction(toitDerefExpression);
            }

            private ToitFunction resolveFunction(ToitExpression expression) {
                var ref = expression.getLastDescendentOfType(ToitReferenceIdentifier.class);
                if (ref == null) return null;
                for (ResolveResult resolveResult : ref.getReference().multiResolve(false)) {
                    var resolved = resolveResult.getElement();
                    if (resolved instanceof ToitFunction) {
                        var function = (ToitFunction) resolved;
                        if (parametersMatches(function, Collections.emptyList())) return function;
                    } else if (resolved instanceof ToitStructure) {
                        ToitStructure structure = (ToitStructure) resolved;
                        for (ToitFunction defaultConstructor : structure.getDefaultConstructors()) {
                            if (parametersMatches(defaultConstructor,Collections.emptyList())) return defaultConstructor;
                        }
                    }
                }
                return null;
            }
        });
    }

    // Is this expression an immediate function call. Not including nested expression;
    // so "(func_call)" will not return true, but "func_call" will return true
    public static boolean isFunctionCall(ToitExpression expression) {
        Boolean res= expression.accept(new ToitExpressionVisitor<>() {
            @Override
            public Boolean visit(ToitCallExpression toitCallExpression) {
                return true;
            }

            @Override
            public Boolean visit(ToitDerefExpression toitDerefExpression) {
                return check(toitDerefExpression);
            }

            @Override
            public Boolean visit(ToitPrimaryExpression toitPrimaryExpression) {
                for (ToitReferenceIdentifier toitReferenceIdentifier : toitPrimaryExpression.getChildrenOfType(ToitReferenceIdentifier.class)) {
                    if (check(toitReferenceIdentifier)) return true;
                }
                return false;
            }

            @Override
            public Boolean visit(ToitPostfixExpression toitPostfixExpression) {
                return check(toitPostfixExpression);
            }

            private boolean check(ToitExpression expression) {
                var ref = expression.getLastDescendentOfType(ToitReferenceIdentifier.class);
                if (ref == null) return false;
                return check(ref);
            }

            private boolean check(ToitReferenceIdentifier ref) {
                for (ResolveResult resolveResult : ref.getReference().multiResolve(false)) {
                    if (resolveResult.getElement() instanceof ToitFunction) return true;
                }
                return false;
            }
        });
        return res != null && res;
    }

    public static List<ToitFunction> resolveFunctions(ToitExpression expression) {
        return null;
    }


    public static boolean parametersMatches(ToitFunction toitFunction, List<IToitElement> parameters) {
        ParametersInfo parametersInfo = toitFunction.getParameterInfo();
        int positionalCount = 0;
        Set<String> names = new HashSet<>();

        for (IToitElement parameter : parameters) {
            if (parameter instanceof ToitExpression) positionalCount++;
            if (parameter instanceof ToitNamedArgument) {
                ToitNamedArgument namedArgument = (ToitNamedArgument) parameter;
                names.add(namedArgument.getName());
            }
        }

        if (positionalCount < parametersInfo.getNumberOfNonDefaultPositionals() || positionalCount > parametersInfo.getNumberOfPositionals()) return false;
        for (String nonDefaultNamedParameter : parametersInfo.getNonDefaultNamedParameters()) {
            if (!names.contains(nonDefaultNamedParameter)) return false;
        }
        for (String name : names) {
            if (!parametersInfo.hasNamedParameter(name)) return false;
        }
        return true;
    }
}
