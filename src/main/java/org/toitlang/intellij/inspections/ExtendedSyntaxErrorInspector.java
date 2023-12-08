package org.toitlang.intellij.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.ToitFunction;
import org.toitlang.intellij.psi.calls.ParameterInfo;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

public class ExtendedSyntaxErrorInspector extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new ToitVisitor() {
            @Override
            public void visit(ToitFunction toitFunction) {
                // Check that no named parameters starts with "no-"
                var parameterInfo = toitFunction.getParameterInfo();
                for (ParameterInfo namedParameter : parameterInfo.getNamedParameters()) {
                    var name = namedParameter.getParameterName().getName();
                    if (name != null && name.startsWith("no-")) {
                        holder.registerProblem(namedParameter.getParameterName(), "Named parameters cannot start with 'no-'", ProblemHighlightType.ERROR);
                    }
                }
            }
        };
    }
}
