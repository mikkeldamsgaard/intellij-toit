package org.toitlang.intellij.psi.calls;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;

import java.util.*;

/**
 * Helper function to resolve expressions to calls. Both PrimaryExpression (with on reference identifier inside) and
 * PostfixExpression can be calls in disguise with zero arguments
 */
public class ToitCallHelper {
    public static ResolvedFunctionCall resolveCall(ToitExpression expression) {
        return expression.accept(new ToitExpressionVisitor<>() {
            @Override
            public ResolvedFunctionCall visit(ToitCallExpression toitCallExpression) {
                List<IToitElement> arguments = new ArrayList<>();
                var children = toitCallExpression.getChildren();
                if (children.length <= 1) return null;
                for (int i = 1; i < children.length; i++) {
                    if (children[i] instanceof ToitExpression || children[i] instanceof ToitNamedArgument)
                        arguments.add((IToitElement) children[i]);
                }

                for (ToitFunction function : toitCallExpression.getFunctions()) {
                    ResolvedFunctionCall resolved = parametersMatches(function, arguments);
                    if (resolved != null) return resolved;
                }

                return null;
            }

            @Override
            public ResolvedFunctionCall visit(ToitPostfixExpression toitPostfixExpression) {
                return resolveFunction(toitPostfixExpression);
            }

            @Override
            public ResolvedFunctionCall visit(ToitPrimaryExpression toitPrimaryExpression) {
                return resolveFunction(toitPrimaryExpression);
            }

            @Override
            public ResolvedFunctionCall visit(ToitDerefExpression toitDerefExpression) {
                return resolveFunction(toitDerefExpression);
            }

            private ResolvedFunctionCall resolveFunction(ToitExpression expression) {
                var ref = expression.getLastDescendentOfType(ToitReferenceIdentifier.class);
                if (ref == null) return null;
                for (ResolveResult resolveResult : ref.getReference().multiResolve(false)) {
                    var resolved = resolveResult.getElement();
                    if (resolved instanceof ToitFunction) {
                        var function = (ToitFunction) resolved;
                        ResolvedFunctionCall resolvedFunctionCall = parametersMatches(function, Collections.emptyList());
                        if (resolvedFunctionCall != null) return resolvedFunctionCall;
                    } else if (resolved instanceof ToitStructure) {
                        ToitStructure structure = (ToitStructure) resolved;
                        for (ToitFunction defaultConstructor : structure.getDefaultConstructors()) {
                            ResolvedFunctionCall resolvedFunctionCall = parametersMatches(defaultConstructor, Collections.emptyList());
                            if (resolvedFunctionCall != null) return resolvedFunctionCall;
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
        Boolean res = expression.accept(new ToitExpressionVisitor<>() {
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


    public static ResolvedFunctionCall parametersMatches(ToitFunction toitFunction, List<IToitElement> parameters) {
        ResolvedFunctionCall resolvedFunctionCall = new ResolvedFunctionCall(toitFunction);
        ParametersInfo parametersInfo = toitFunction.getParameterInfo();
        List<IToitElement> positionalArgs = new ArrayList<>();
        Map<String, ToitNamedArgument> names = new HashMap<>();

        for (IToitElement parameter : parameters) {
            if (parameter instanceof ToitExpression) positionalArgs.add(parameter);
            if (parameter instanceof ToitNamedArgument) {
                ToitNamedArgument namedArgument = (ToitNamedArgument) parameter;
                names.put(namedArgument.getName(), namedArgument);
            }
        }

        /* Positional args */
        if (positionalArgs.size() < parametersInfo.getNumberOfNonDefaultPositionalParameters() ||
                positionalArgs.size() > parametersInfo.getNumberOfPositionalParameters()) return null;

        int position = 0;
        for (IToitElement positionalArg : positionalArgs) {
            ParameterInfo param = parametersInfo.getPositional(position++);
            resolvedFunctionCall.addMatch(positionalArg, param);
        }

        /* Named non default parameters */
        for (String nonDefaultNamedParameter : parametersInfo.getNonDefaultNamedParameters().keySet()) {
            if (!names.containsKey(nonDefaultNamedParameter)) return null;
            resolvedFunctionCall.addMatch(names.get(nonDefaultNamedParameter), parametersInfo.getNamedParameter(nonDefaultNamedParameter));
        }

        for (String name : names.keySet()) {
            if (!parametersInfo.hasNamedParameter(name)) return null;
            resolvedFunctionCall.addMatch(names.get(name), parametersInfo.getNamedParameter(name));
        }

        return resolvedFunctionCall;
    }

    static public class ResolvedFunctionCall {
        private final Map<IToitElement, ParameterInfo> argsToParams = new HashMap<>();
        private ToitFunction toitFunction;

        public ResolvedFunctionCall(ToitFunction toitFunction) {
            this.toitFunction = toitFunction;
        }

        public ToitFunction getToitFunction() {
            return toitFunction;
        }

        private void addMatch(IToitElement arg, ParameterInfo param) {
            argsToParams.put(arg, param);
        }

        public ParameterInfo getParamForArg(IToitElement arg) {
            return argsToParams.get(arg);
        }

        public Set<IToitElement> getArguments() {
            return argsToParams.keySet();
        }
    }

    public static boolean isPotentialSetterCall(ToitDerefExpression toitDerefExpression) {
        return toitDerefExpression.getParent() != null && toitDerefExpression.getParent().getParent() instanceof ToitAssignmentExpression;
    }

    public static boolean isSetterCall(ToitExpression toitExpression) {
        if (toitExpression instanceof ToitDerefExpression) {
            PsiElement resolved = ((ToitDerefExpression) toitExpression).getToitReferenceIdentifier().getReference().resolve();
            return resolved instanceof ToitFunction && ((ToitFunction) resolved).isSetter();
        }
        if (toitExpression instanceof ToitPrimaryExpression) {
            for (ToitReferenceIdentifier toitReferenceIdentifier : toitExpression.getChildrenOfType(ToitReferenceIdentifier.class)) {
                for (ResolveResult resolveResult : toitReferenceIdentifier.getReference().multiResolve(false)) {
                    if (resolveResult.getElement() instanceof ToitFunction && ((ToitFunction)resolveResult.getElement()).isSetter()) return true;
                }
            }

        }
        return false;
    }
}
