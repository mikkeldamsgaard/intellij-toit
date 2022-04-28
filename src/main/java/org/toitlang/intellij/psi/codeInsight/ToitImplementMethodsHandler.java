package org.toitlang.intellij.psi.codeInsight;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.ToitFileType;
import org.toitlang.intellij.psi.ast.ToitFunction;
import org.toitlang.intellij.psi.ast.ToitStructure;

import java.util.List;

public class ToitImplementMethodsHandler implements LanguageCodeInsightActionHandler {
    @Override
    public boolean isValidFor(Editor editor, PsiFile file) {
        return file != null && ToitFileType.INSTANCE.equals(file.getFileType());
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (!EditorModificationUtil.checkModificationAllowed(editor)) return;
        ToitStructure structure = OverrideImplementUtil.getContextStructure(project, editor, file);
        if (structure!= null && (structure.isClass() || structure.isMonitor())) {
            List<ToitFunction> implement = OverrideImplementUtil.getFunctionsToImplement(structure);
            if (implement.isEmpty()) {
                HintManager.getInstance().showErrorHint(editor, "No methods to implement");
                return;
            }

            OverrideImplementUtil.chooseAndOverrideImplementMethods(project, editor, structure,implement,true);
        }
    }
}
