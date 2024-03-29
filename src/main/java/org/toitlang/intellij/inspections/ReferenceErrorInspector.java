package org.toitlang.intellij.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.ToitReferenceIdentifier;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

public class ReferenceErrorInspector extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly,
                                                   @NotNull LocalInspectionToolSession session) {
        return new ToitVisitor() {
            @Override
            public void visit(ToitReferenceIdentifier toitReferenceIdentifier) {
                if (!toitReferenceIdentifier.getReference().isSoft() &&
                        toitReferenceIdentifier.getReference().resolve() == null &&
                        !ignoreMissingReference(toitReferenceIdentifier.getText()) ) {
                    holder.registerProblem(toitReferenceIdentifier, "Unresolved reference: "+toitReferenceIdentifier.getText(), ProblemHighlightType.ERROR);
                }
            }
        };
    }

    boolean ignoreMissingReference(String text) {
        return "".equals(text) ||
                "__yield__".equals(text) ||
                "__exit__".equals(text);
    }
}
