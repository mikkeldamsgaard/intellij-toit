package org.toitlang.intellij.psi.expression;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.reference.ReferenceCalculation;
import org.toitlang.intellij.psi.visitor.ToitVisitor;
import org.toitlang.intellij.psi.scope.ToitScope;

import java.util.*;

public class ToitExpressionTypeEvaluator  extends ToitExpressionVisitor<Set<PsiElement>> {
    private final ToitScope scope;
    private ReferenceCalculation calc;
    private Object[] variants;

    private final static String LIST_CLASS_NAME = "List";
    private final static String INT_CLASS_NAME = "int";
    private final static String FLOAT_CLASS_NAME = "float";
    private final static String STRING_CLASS_NAME = "string";
    private final static String MAP_CLASS_NAME = "Map";
    private final static String SET_CLASS_NAME = "Set";

    private final static String SUPER = "super";
    private final static String THIS = "this";
    private final static String IT = "it";
    private final static String PRIMITIVE = "#primitive";

    public ToitExpressionTypeEvaluator(ToitScope scope, ReferenceCalculation calc) {
        this.scope = scope;
        this.calc = calc;
    }

    public Object[] getVariants() {
        return variants;
    }

    private void recurse(ToitExpression expression, Set<PsiElement> res) {
        List<Set<PsiElement>> subTypes = expression.acceptChildren(this);
        for (Set<PsiElement> subType : subTypes) {
            res.addAll(subType);
        }
    }

    private Set<PsiElement> safeResolve(ToitScope scope, String name) {
        List<PsiElement> res = scope.resolve(name);
        if (res == null) return Collections.emptySet();
        return new HashSet<>(res);
    }

    @Override
    public Set<PsiElement> visit(ToitDerefExpression toitDerefExpression) {
        var prevType = ((ToitExpression)toitDerefExpression.getPrevSibling()).accept(this);

        if (prevType == null) {
            calc.setSoft(true);
            return null;
        }

        final Set<PsiElement> res = new HashSet<>();
        String name = toitDerefExpression.childrenOfType(ToitReferenceIdentifier.class).get(0).getName();
        for (PsiElement type : prevType) {
            type.accept(new ToitVisitor() {
                @Override
                public void visitFile(@NotNull PsiFile file) {
                    ToitScope scope = ((ToitFile) file).getToitFileScope().getToitScope();
                    res.addAll(safeResolve(scope,name));
                    variants = scope.asVariant();
                }

                @Override
                public void visit(ToitStructure toitStructure) {
                    processStructure(toitStructure);
                }

                private Object[] processStructure(ToitStructure toitStructure) {
                    ToitScope structureScope = toitStructure.getScope(scope);
                    res.addAll(safeResolve(structureScope,name));
                    variants = structureScope.asVariant();
                    return variants;
                }

                @Override
                public void visit(ToitFunction toitFunction) {
                    // Use the return type (declared or inferred) to resolve
                    ToitType variableType = toitFunction.getType();
                    processDeferredType(variableType);
                }

                @Override
                public void visit(ToitVariableDeclaration toitVariableDeclaration) {
                    // Use the variables type (declared or inferred) to resolve
                    ToitType variableType = toitVariableDeclaration.getType();
                    processDeferredType(variableType);
                }

                @Override
                public void visit(ToitParameterName toitParameterName) {
                    var toitType = toitParameterName.getType();
                    processDeferredType(toitType);
                }

                private void processDeferredType(ToitType type) {
                    variants = null;
                    if (type == null) {
                        calc.setSoft(true);
                        return;
                    }

                    ToitStructure toitStructure = type.resolve(scope);
                    if (toitStructure == null) return;

                    ToitExpressionTypeEvaluator.this.variants = processStructure(toitStructure);
                }
            });
        }

        return res;
    }

    @Override
    public Set<PsiElement> visit(ToitPrimaryExpression toitPrimaryExpression) {
        final Set<PsiElement> res = new HashSet<>();
        recurse(toitPrimaryExpression, res);

        toitPrimaryExpression.acceptChildren(new ToitVisitor() {
            @Override
            public void visit(ToitReferenceIdentifier toitReferenceIdentifier) {
                String name = toitReferenceIdentifier.getName();
                switch (name) {
                    case SUPER:
                        var function = toitReferenceIdentifier.getParentOfType(ToitFunction.class);
                        if (function != null && function.isConstructor()) calc.setSoft(true);

                        break;
                    case THIS:
                        var struct = toitReferenceIdentifier.getParentOfType(ToitStructure.class);
                        if (struct != null) {
                            res.add(struct);
                            return;
                        }
                        break;
                    case IT:
                        var block = toitReferenceIdentifier.getParentOfType(ToitBlock.class);
                        if (block != null) {
                            if (block.getParentOfType(ToitExpression.class) != null) calc.setSoft(true);
                        }
                        break;
                    case PRIMITIVE:
                        calc.setSoft(true);
                        break;
                }
                if (!calc.isSoft()) {
                    res.addAll(scope.resolve(name));
                }
            }
        });

        return res;
    }

    @Override
    public Set<PsiElement> visit(ToitTopLevelExpression toitTopLevelExpression) {
        final Set<PsiElement> res = new HashSet<>();
        recurse(toitTopLevelExpression, res);
        return res;
    }

    @Override
    public Set<PsiElement> visit(ToitListLiteral toitListLiteral) {
        return safeResolve(scope, LIST_CLASS_NAME);
    }

    @Override
    public Set<PsiElement> visit(ToitAssignmentExpression toitAssignmentExpression) {
        return ((ToitExpression)toitAssignmentExpression.getLastChild()).accept(this);
    }

    @Override
    public Set<PsiElement> visit(ToitSetLiteral toitSetLiteral) {
        return safeResolve(scope, SET_CLASS_NAME);
    }

    @Override
    public Set<PsiElement> visit(ToitMapLiteral toitMapLiteral) {
        return safeResolve(scope, MAP_CLASS_NAME);
    }

    @Override
    public Set<PsiElement> visit(ToitSimpleLiteral toitSimpleLiteral) {
        if (toitSimpleLiteral.isString()) {
            return safeResolve(scope, STRING_CLASS_NAME);
        } else if (toitSimpleLiteral.isInt()) {
            return safeResolve(scope, INT_CLASS_NAME);
        } else if (toitSimpleLiteral.isFloat()) {
            return safeResolve(scope, FLOAT_CLASS_NAME);
        }
        return null;
    }
}
