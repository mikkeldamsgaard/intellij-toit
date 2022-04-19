package org.toitlang.intellij.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.reference.ToitEvaluatedType;
import org.toitlang.intellij.psi.reference.ToitReference;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.List;

public class SemanticErrorInspector extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new ToitVisitor() {
            @Override
            public void visit(ToitFunction toitFunction) {
                checkStaticAbstract(toitFunction, holder);
                checkReturn(toitFunction, holder);
            }
        };
    }

    private void checkReturn(ToitFunction toitFunction, ProblemsHolder holder) {
        ToitType returnType = toitFunction.getType();
        boolean requireReturn = returnType != null && !"none".equals(returnType.getName());
        boolean noReturnValue = returnType != null && "none".equals(returnType.getName());
        ToitBlock body = toitFunction.getLastChildOfType(ToitBlock.class);
        if (body == null) return;

        boolean returns = checkReturn(body, holder, returnType, noReturnValue);
        if (requireReturn && !returns)
            holder.registerProblem(toitFunction.getProblemIdentifier(), "Function does not return a value", ProblemHighlightType.ERROR);
    }

    private boolean checkReturn(ToitBlock block, ProblemsHolder holder, ToitType returnType, boolean noReturnValue) {
        boolean[] result = { false };
        block.acceptChildren(new ToitVisitor() {
            @Override
            public void visit(ToitReturn toitReturn) {
                ToitExpression expression = toitReturn.getFirstChildOfType(ToitExpression.class);
                if (returnType != null) {
                    if (expression == null) {
                        holder.registerProblem(toitReturn, "Missing return expression", ProblemHighlightType.ERROR);
                    } else {
                        ToitEvaluatedType evaluatedType = expression.getType(expression.getLocalToitResolveScope());
                        ToitStructure returnStructure = returnType.resolve();
                        if (evaluatedType != null && returnStructure != null) {
                            if (!evaluatedType.isAssignableTo(returnStructure)) {
                                holder.registerProblem(toitReturn, "Wrong return type. "+evaluatedType.getStructure().getName()+" is not assignable to "+ returnStructure.getName());
                            }
                        }
                    }
                } else if (noReturnValue && expression != null) {
                    holder.registerProblem(expression, "Function does not return a value, but one was provided");

                }
                result[0] = true;
            }

            @Override
            public void visit(ToitIf toitIf) {
                List<ToitBlock> blocks = toitIf.getChildrenOfType(ToitBlock.class);
                List<ToitExpression> expressions = toitIf.getChildrenOfType(ToitExpression.class);
                if (blocks.size()>expressions.size()) {
                    // There is an else without condition
                    boolean res = true;
                    for (ToitBlock toitBlock : blocks) {
                        res = res && checkReturn(toitBlock,holder,returnType, noReturnValue);
                    }
                    if (res) result[0] = true;
                }
            }


            @Override
            public void visit(ToitTry toitTry) {
                ToitBlock toitBlock = toitTry.getFirstChildOfType(ToitBlock.class);
                if (toitBlock != null && checkReturn(toitBlock, holder, returnType, noReturnValue)) result[0] = true;
                super.visit(toitTry);
            }

            // TODO: Needs to use the body of functions to determine if it throws by scanning for __throw__ instead of checking for the function names in exceptions.toit
            @Override
            public void visit(ToitExpression toitExpression) {
                if (toitExpression.getChildren().length > 0) {
                    ((ToitExpression)toitExpression.getChildren()[0]).accept(new ToitExpressionVisitor<>() {
                        @Override
                        public Object visit(ToitCallExpression toitCallExpression) {

                            ToitFunction function = toitCallExpression.getFunction();
                            if (function != null) {
                                String name = function.getName();
                                if (name != null && (name.equals("throw") || name.equals("rethrow"))) result[0] = true;
                            }

                            for (ToitBlock toitBlock : toitCallExpression.getChildrenOfType(ToitBlock.class)) {
                                if (checkReturn(toitBlock, holder, returnType, noReturnValue)) result[0] = true;
                            }

                            return null;
                        }

                        @Override
                        public Object visit(ToitPrimaryExpression toitPrimaryExpression) {
                            ToitReferenceIdentifier ref = toitPrimaryExpression.getFirstChildOfType(ToitReferenceIdentifier.class);
                            if (ref != null && ref.getName().equals("unreachable")) result[0] = true;
                            return null;
                        }
                    });
                    super.visit(toitExpression);
                }
            }
        });

        return result[0];
    }

    private void checkStaticAbstract(ToitFunction toitFunction, @NotNull ProblemsHolder holder) {
        boolean hasBody = !toitFunction.getChildrenOfType(ToitBlock.class).isEmpty();

        if (toitFunction.getParent() instanceof ToitFile) {
            if (toitFunction.isStatic())
                holder.registerProblem(toitFunction, ToitBaseStubableElement.getRelativeRangeInParent(toitFunction.getStatic()), "Top level functions cannot be static");
            if (!hasBody) holder.registerProblem(toitFunction.getFunctionName(), "Missing body");
        } else {
            ToitStructure parent = toitFunction.getParentOfType(ToitStructure.class);
            ToitNameableIdentifier functionName = toitFunction.getFunctionName();
            if (functionName == null) return;
            if (parent.isClass()) {
                boolean isAbstractClass = parent.isAbstract();
                if (toitFunction.isAbstract() && !isAbstractClass)
                    holder.registerProblem(toitFunction, ToitBaseStubableElement.getRelativeRangeInParent(toitFunction.getAbstract()), "Abstract functions not allowed in non-abstract class");
                if (!toitFunction.isAbstract() && !hasBody) {
                    holder.registerProblem(functionName, "Missing body");
                }
            }

            if (parent.isInterface()) {
                if (hasBody && !toitFunction.isStatic())
                    holder.registerProblem(functionName, "Only static interface methods may have a body");
            }
        }
    }
}
