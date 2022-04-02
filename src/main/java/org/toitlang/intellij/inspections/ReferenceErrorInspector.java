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

public class ReferenceErrorInspector extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new ToitVisitor() {
            @Override
            public void visit(ToitReferenceIdentifier toitReferenceIdentifier) {
                if (!toitReferenceIdentifier.getReference().isSoft() && toitReferenceIdentifier.getReference().resolve() == null && !"".equals(toitReferenceIdentifier.getText())) {
                    holder.registerProblem(toitReferenceIdentifier, "Unresolved reference: "+toitReferenceIdentifier.getText(), ProblemHighlightType.ERROR);
                }
            }
        };
    }
}
