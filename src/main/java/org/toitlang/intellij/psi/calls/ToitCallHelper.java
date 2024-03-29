package org.toitlang.intellij.psi.calls;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import lombok.Getter;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.reference.ToitExpressionReferenceTarget;

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
                List<ToitElement> arguments = toitCallExpression.getArguments();

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
                var element = ref.getReference().resolve();
                return element instanceof ToitFunction;
            }
        });
        return res != null && res;
    }


    public static ResolvedFunctionCall parametersMatches(ToitFunction toitFunction, List<ToitElement> parameters) {
        ResolvedFunctionCall resolvedFunctionCall = new ResolvedFunctionCall(toitFunction);
        ParametersInfo parametersInfo = toitFunction.getParameterInfo();
        List<ToitElement> positionalArgs = new ArrayList<>();
        Map<String, ToitNamedArgument> names = new HashMap<>();
        for (ToitElement parameter : parameters) {
            if (parameter instanceof ToitExpression) positionalArgs.add(parameter);
            if (parameter instanceof ToitNamedArgument) {
                ToitNamedArgument namedArgument = (ToitNamedArgument) parameter;
                String name = namedArgument.getName();
                if (name != null) {
                    if (name.startsWith("no-")) {
                        names.put(name.substring(3), namedArgument);
                    } else {
                        names.put(name, namedArgument);
                    }
                }
            }
        }

        /* Positional args */
        if (positionalArgs.size() < parametersInfo.getNumberOfNonDefaultPositionalParameters() ||
                positionalArgs.size() > parametersInfo.getNumberOfPositionalParameters()) return null;

        int position = 0;
        for (ToitElement positionalArg : positionalArgs) {
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

    public static ToitExpressionReferenceTarget isDisguisedConstructorCall(ToitExpressionReferenceTarget referenceTarget,
                                                                           ToitExpression expression) {
        // A primary expression or a deref expression can be disguised constructor calls, if the identifier
        // is a reference to a structure and
        // if its the right most leaf in the expression tree and close to the ToitTopLevelExpression
        // ToitPrimaryExpression needs to be a direct child of ToitTopLevelExpression
        // A deref expression needs to have top level expression as the grand parent
        if (!(referenceTarget.getTarget() instanceof ToitStructure) || expression.getNextSibling() != null)
            return referenceTarget;

        if (expression instanceof ToitPrimaryExpression &&
            expression.getParentChain(ToitTopLevelExpression.class, List.of()) == null ||
          expression instanceof ToitDerefExpression &&
            expression.getParentChain(ToitTopLevelExpression.class, List.of(ToitPostfixExpression.class)) == null) {
            return referenceTarget;
        }

        // Check to see if any of the constructors in the target has no arguments
        ToitStructure structure = (ToitStructure) referenceTarget.getTarget();
        for (ToitFunction defaultConstructor : structure.getDefaultConstructors()) {
            ResolvedFunctionCall resolvedFunctionCall = parametersMatches(defaultConstructor, Collections.emptyList());
            if (resolvedFunctionCall != null) {
                return new ToitExpressionReferenceTarget(defaultConstructor);
            }
        }

        return referenceTarget;
    }

    static public class ResolvedFunctionCall {
        private final Map<ToitElement, ParameterInfo> argsToParams = new HashMap<>();
        @Getter
        private final ToitFunction toitFunction;

        public ResolvedFunctionCall(ToitFunction toitFunction) {
            this.toitFunction = toitFunction;
        }

        private void addMatch(ToitElement arg, ParameterInfo param) {
            argsToParams.put(arg, param);
        }

        public ParameterInfo getParamForArg(ToitElement arg) {
            return argsToParams.get(arg);
        }

        public Set<ToitElement> getArguments() {
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
