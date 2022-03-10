package org.toitlang.intellij;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.ToitFunction;

public class ToitAnnotationHighlighter implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof ToitFunction) {
            var name = ((ToitFunction)element).getFunctionName();
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(name.getTextRange())
                    .textAttributes(ToitSyntaxHighlighter.FUNCTION_DECL)
                    .create();
        }
    }
}
