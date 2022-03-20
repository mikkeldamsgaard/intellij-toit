package org.toitlang.intellij.psi.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.scope.ToitScope;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.*;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class ToitEvaluatedType {
    private final ToitFile file;
    private final ToitStructure structure;
    private final boolean isStatic;

    private final static ToitEvaluatedType UNRESOLVED = new ToitEvaluatedType(null,null,false);

    public boolean isUnresolved() { return file == null && structure == null; }

    private final static String LIST_CLASS_NAME = "List";
    private final static String INT_CLASS_NAME = "int";
    private final static String FLOAT_CLASS_NAME = "float";
    private final static String BOOL_CLASS_NAME = "bool";
    private final static String STRING_CLASS_NAME = "string";
    private final static String MAP_CLASS_NAME = "Map";
    private final static String SET_CLASS_NAME = "Set";

    private final static String SUPER = "super";
    private final static String THIS = "this";
    private final static String IT = "it";
    private final static String PRIMITIVE = "#primitive";


    private static ToitEvaluatedType resolveTypeOfNameInScope(String name, ToitScope scope, boolean isStatic) {
        var resolved = scope.resolve(name);
        if (resolved.isEmpty()) return new ToitEvaluatedType(null, null, isStatic);
        List<ToitEvaluatedType> types = new ArrayList<>();
        for (PsiElement resolvedElement : resolved) {
            resolvedElement.accept(new ToitVisitor() {
                @Override
                public void visitFile(@NotNull PsiFile file) {
                    types.add(new ToitEvaluatedType((ToitFile) file, null, false));
                }

                @Override
                public void visit(ToitStructure toitStructure) {
                    types.add(new ToitEvaluatedType(null, toitStructure, isStatic));
                }

                @Override
                public void visit(ToitFunction toitFunction) {
                    // Use the return type (declared or inferred) to resolve
                    processDeferredType(toitFunction.getType());
                }

                @Override
                public void visit(ToitVariableDeclaration toitVariableDeclaration) {
                    // Use the variables type (declared or inferred) to resolve
                    processDeferredType(toitVariableDeclaration.getType());
                }

                @Override
                public void visit(ToitParameterName toitParameterName) {
                    processDeferredType(toitParameterName.getType());
                }


                private void processDeferredType(ToitType type) {
                    if (type == null || Objects.equals(type.getName(), "any")) {
                        return;
                    }

                    ToitStructure toitStructure = type.resolve(scope);

                    if (toitStructure == null) {
                        return;
                    }

                    types.add(new ToitEvaluatedType(null, toitStructure, false));
                }
            });
        }

        if (types.isEmpty()) return UNRESOLVED;
        return types.get(0);
    }


    public static ToitEvaluatedType evaluate(ToitExpression expression,ToitScope scope) {
        return expression.accept(new ToitExpressionVisitor<>() {
            private List<ToitEvaluatedType> recurse(ToitExpression expression) {
                return expression.acceptChildren(this);
            }

            private ToitEvaluatedType singularRecurse(ToitExpression expression) {
                var recursed = recurse(expression);
                if (recursed.isEmpty()) return UNRESOLVED;
                if (recursed.size() > 1) {
                    System.err.println("Multiple recurses::: "+recursed);
                }
                var result = recursed.get(0);
                if (result == null) return UNRESOLVED;
                return result;
            }

            private ToitEvaluatedType resolveToStruct(ToitScope scope, String name) {
                List<PsiElement> resolved = scope.resolve(name);
                for (PsiElement psiElement : resolved) {
                    if (psiElement instanceof ToitStructure)
                        return new ToitEvaluatedType(null, (ToitStructure) psiElement, false);
                }
                return UNRESOLVED;
            }

            @Override
            public ToitEvaluatedType visit(ToitDerefExpression toitDerefExpression) {
                var prevType = ((ToitExpression) toitDerefExpression.getPrevSibling()).getType(scope);

                String name = toitDerefExpression.childrenOfType(ToitReferenceIdentifier.class).get(0).getName();

                if (prevType.isUnresolved()) {
                    return UNRESOLVED;
                } else if (prevType.getFile() != null) {
                    ToitScope prevFileScope = prevType.getFile().getToitFileScope().getToitScope();
                    return resolveTypeOfNameInScope(name, prevFileScope, true);
                } else {
                    ToitScope scructureScope = prevType.getStructure().getScope(scope);
                    return resolveTypeOfNameInScope(name, scructureScope, false);
                }
            }

            @Override
            public ToitEvaluatedType visit(ToitPrimaryExpression toitPrimaryExpression) {
                var referenceIdentifiers = toitPrimaryExpression.childrenOfType(ToitReferenceIdentifier.class);
                if (referenceIdentifiers.isEmpty()) {
                    return singularRecurse(toitPrimaryExpression);
                }

                var toitReferenceIdentifier = referenceIdentifiers.get(0);
                return resolveTypeOfNameInScope(toitReferenceIdentifier.getName(), scope, false);
            }

            @Override
            public ToitEvaluatedType visit(ToitTopLevelExpression toitTopLevelExpression) {
                return singularRecurse(toitTopLevelExpression);
            }

            @Override
            public ToitEvaluatedType visit(ToitAssignmentExpression toitAssignmentExpression) {
                return evaluate((ToitExpression)toitAssignmentExpression.getLastChild(),scope);
            }

            @Override
            public ToitEvaluatedType visit(ToitListLiteral toitListLiteral) {
                return resolveToStruct(scope, LIST_CLASS_NAME);
            }

            @Override
            public ToitEvaluatedType visit(ToitSetLiteral toitSetLiteral) {
                return resolveToStruct(scope, SET_CLASS_NAME);
            }

            @Override
            public ToitEvaluatedType visit(ToitMapLiteral toitMapLiteral) {
                return resolveToStruct(scope, MAP_CLASS_NAME);
            }

            @Override
            public ToitEvaluatedType visit(ToitSimpleLiteral toitSimpleLiteral) {
                if (toitSimpleLiteral.isString()) {
                    return resolveToStruct(scope, STRING_CLASS_NAME);
                } else if (toitSimpleLiteral.isInt()) {
                    return resolveToStruct(scope, INT_CLASS_NAME);
                } else if (toitSimpleLiteral.isFloat()) {
                    return resolveToStruct(scope, FLOAT_CLASS_NAME);
                } else if (toitSimpleLiteral.isBoolean()) {
                    return resolveToStruct(scope, BOOL_CLASS_NAME);
                }
                return null;
            }

            @Override
            public ToitEvaluatedType visit(ToitRelationalExpression toitRelationalExpression) {
                return resolveToStruct(scope, FLOAT_CLASS_NAME);
            }

            @Override
            public ToitEvaluatedType visit(ToitShiftExpression toitShiftExpression) {
                return resolveToStruct(scope, INT_CLASS_NAME);
            }

            @Override
            public ToitEvaluatedType visit(ToitUnaryExpression toitUnaryExpression) {
                return singularRecurse(toitUnaryExpression);
            }

            @Override
            public ToitEvaluatedType visitExpression(ToitExpression expression) {
                return UNRESOLVED;
            }

            // Todo: Binary expression, call expression
        });
    }
}