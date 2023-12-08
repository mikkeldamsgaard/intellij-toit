package org.toitlang.intellij.psi.codeInsight;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.codeInsight.generation.actions.PresentableCodeInsightActionHandler;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ToitPsiHelper;
import org.toitlang.intellij.psi.ast.ToitElement;
import org.toitlang.intellij.psi.ast.ToitFunction;
import org.toitlang.intellij.psi.ast.ToitStructure;
import org.toitlang.intellij.psi.ui.renders.FunctionCellRenderer;

import java.util.ArrayList;
import java.util.List;

public class ToitGotoSuperHandler implements PresentableCodeInsightActionHandler {
    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        ToitElement toitElm = ToitPsiHelper.findClosestIToitElement(file.findElementAt(editor.getCaretModel().getOffset()));
        int offset = editor.getCaretModel().getOffset();
        PsiElement[] superElements = findSuperElements(file, offset);
        if (superElements.length == 0) return;
        if (superElements.length == 1) {
            PsiElement superElement = superElements[0].getNavigationElement();
            final PsiFile containingFile = superElement.getContainingFile();
            if (containingFile == null) return;
            final VirtualFile virtualFile = containingFile.getVirtualFile();
            if (virtualFile == null) return;
            Navigatable descriptor =
                    PsiNavigationSupport.getInstance().createNavigatable(project, virtualFile, superElement.getTextOffset());
            descriptor.navigate(true);
        }
        else if (superElements[0] instanceof ToitFunction) {
            PsiElementListNavigator.openTargets(editor, (ToitFunction[])superElements,
                    CodeInsightBundle.message("goto.super.method.chooser.title"),
                    CodeInsightBundle
                            .message("goto.super.method.findUsages.title", ((ToitFunction)superElements[0]).getName()),
                    new FunctionCellRenderer());
        }
        else {
            NavigationUtil.getPsiElementPopup(superElements, "Goto Super")
                    .showInBestPositionFor(editor);
        }
    }

    private PsiElement[] findSuperElements(PsiFile file, int offset) {
        PsiElement element = getElement(file, offset);
        var toitElm = PsiTreeUtil.getParentOfType(element, ToitFunction.class, ToitStructure.class);
        if (toitElm == null) return PsiElement.EMPTY_ARRAY;
        if (toitElm instanceof ToitFunction && ((ToitFunction) toitElm).isConstructor()) {
            toitElm = PsiTreeUtil.getParentOfType(toitElm, ToitStructure.class);
        }
        if (toitElm instanceof ToitStructure) {
            var structure = (ToitStructure)toitElm;
            var baseClass = structure.getBaseClass();
            var interfaces = new ArrayList<PsiElement>(structure.getInterfaces());
            if (baseClass != null) interfaces.add(baseClass);
            return interfaces.toArray(new PsiElement[0]);
        } else {
            var func = (ToitFunction)toitElm;
            List<PsiElement> result = new ArrayList<>();
            var structure = PsiTreeUtil.getParentOfType(func, ToitStructure.class);
            if (structure != null) {
                invokeFindRecursive(func, structure, result);
            }
            return result.toArray(new PsiElement[0]);
        }
    }

    private void invokeFindRecursive(ToitFunction func, ToitStructure structure, List<PsiElement> result) {
        if (structure.getBaseClass() != null) findRecursiveSuper(func, structure.getBaseClass(), result);
        for (ToitStructure anInterface : structure.getInterfaces()) {
            findRecursiveSuper(func, anInterface, result);
        }
    }

    private void findRecursiveSuper(ToitFunction func, ToitStructure structure, List<PsiElement> result) {
        for (ToitFunction superFunction : structure.getAllFunctions()) {
            if (superFunction.getSignature().equals(func.getSignature())) {
                result.add(superFunction);
                return;
            }
        }

        invokeFindRecursive(func, structure, result);
    }

    @Nullable
    private static PsiElement getElement(PsiFile file, int offset) {
        return file.findElementAt(offset);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public void update(@NotNull Editor editor, @NotNull PsiFile file, Presentation presentation) {
        update(editor, file, presentation, null);
    }

    @Override
    public void update(@NotNull Editor editor, @NotNull PsiFile file, Presentation presentation, @Nullable String actionPlace) {
        final PsiElement element = getElement(file, editor.getCaretModel().getOffset());
        final PsiElement containingElement = PsiTreeUtil.getParentOfType(element, ToitFunction.class, ToitStructure.class);
        boolean useShortName = actionPlace != null && (ActionPlaces.MAIN_MENU.equals(actionPlace) || ActionPlaces.isPopupPlace(actionPlace));
        if (containingElement instanceof ToitStructure) {
            presentation.setText("Goto Super Class");
        }
        else {
            presentation.setText(ActionsBundle.actionText(useShortName ? "GotoSuperMethod.MainMenu" : "GotoSuperMethod"));
            presentation.setDescription(ActionsBundle.actionDescription("GotoSuperMethod"));
        }
    }

}
