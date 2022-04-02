package org.toitlang.intellij.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.ast.ToitExpression;
import org.toitlang.intellij.psi.ast.ToitReferenceIdentifier;
import org.toitlang.intellij.psi.ast.ToitSimpleLiteral;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

public class StringErrorInspector extends LocalInspectionTool {
    private final TokenSet STRING_TOKENS = TokenSet.create(ToitTypes.STRING_START, ToitTypes.STRING_PART, ToitTypes.STRING_END);
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new ToitVisitor() {
            @Override
            public void visit(ToitExpression toitExpression) {
                toitExpression.accept(new ToitExpressionVisitor<>() {
                    @Override
                    public Object visit(ToitSimpleLiteral toitSimpleLiteral) {
                        var strs = toitSimpleLiteral.getNode().getChildren(STRING_TOKENS);
                        if (strs.length == 0) return null;
                        if (strs[0].getText().startsWith("\"\"\"")) return null;

                        for (ASTNode str : strs) {
                            var s = str.getText().replace("\\\n","");
                            if (s.contains("\n")) {
                                if (str.getPsi() != null) {
                                    holder.registerProblem(str.getPsi(),
                                            "Newlines in single quoted strings not allowed",
                                            ProblemHighlightType.ERROR);
                                }
                            }
                        }

                        return null;
                    }
                });
            }
        };
    }
}
