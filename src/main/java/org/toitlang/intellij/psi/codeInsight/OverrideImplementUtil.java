package org.toitlang.intellij.psi.codeInsight;

import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitElementFactory;
import org.toitlang.intellij.psi.ToitPsiHelper;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.ast.IToitElement;
import org.toitlang.intellij.psi.ast.ToitBlock;
import org.toitlang.intellij.psi.ast.ToitFunction;
import org.toitlang.intellij.psi.ast.ToitStructure;
import org.toitlang.intellij.psi.calls.FunctionSignature;
import org.toitlang.intellij.psi.ui.renders.MemberFunctionRender;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OverrideImplementUtil {
    public static ToitStructure getContextStructure(Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if (element == null) element = file.getLastChild();
        IToitElement toitElm = ToitPsiHelper.findClosestIToitElement(element);
        if (toitElm == null) return null;
        if (toitElm instanceof ToitStructure) return (ToitStructure) toitElm;
        return toitElm.getParentOfType(ToitStructure.class);
    }

    @NotNull
    private static Set<ToitFunction> getNotOwnFunctions(ToitStructure structure) {
        Set<FunctionSignature> own = structure.getOwnFunctions().stream().map(ToitFunction::getSignature).collect(Collectors.toSet());
        return structure.getAllFunctions().stream().filter(f -> !own.contains(f.getSignature())).collect(Collectors.toSet());
    }

    public static List<ToitFunction> getFunctionsToOverride(ToitStructure structure) {
        return getNotOwnFunctions(structure).stream()
                .filter(f -> !f.isAbstract() &&
                        !f.getParentOfType(ToitStructure.class).isInterface())
                .collect(Collectors.toList());
    }

    public static List<ToitFunction> getFunctionsToImplement(ToitStructure structure) {
        return getNotOwnFunctions(structure).stream()
                .filter(f -> (f.isAbstract() ||
                        f.getParentOfType(ToitStructure.class).isInterface()))
                .collect(Collectors.toList());
    }


    public static void chooseAndOverrideImplementMethods(Project project, Editor editor, ToitStructure structure, List<ToitFunction> functionList, boolean toImplement) {
        ApplicationManager.getApplication().assertReadAccessAllowed();

        MemberFunctionRender[] candidates = functionList.stream().map(MemberFunctionRender::new).distinct().toArray(MemberFunctionRender[]::new);

        final MemberChooser<MemberFunctionRender> chooser = new MemberChooser<>(candidates, false, true, project, false);
        chooser.setCopyJavadocVisible(false);

        chooser.show();
        if (chooser.getExitCode() != DialogWrapper.OK_EXIT_CODE) return;

        final List<MemberFunctionRender> selectedElements = chooser.getSelectedElements();
        if (selectedElements == null || selectedElements.isEmpty()) return;

        PsiElement anchor = getAnchor(structure, editor);

        WriteCommandAction.writeCommandAction(project, structure.getContainingFile()).run(() -> {
            var block = structure.getFirstChildOfType(ToitBlock.class);
            assert block != null;

            int indent = 2;
            if (block.getChildren().length > 0) {
                var ws = block.getNode().getChildren(TokenSet.create(ToitTypes.NEWLINE));
                if (ws.length > 0) {
                    var n = ws[0].getTreeNext();
                    if (n != null) indent = n.getTextLength();
                }
            }

            String indentStr = " ".repeat(indent);
            StringBuilder b = new StringBuilder();

            for (MemberFunctionRender selectedElement : selectedElements) {
                ToitFunction function = selectedElement.getFunction();
                var signature = function.getSignature();
                b.append(String.format("%s%s", indentStr, signature.render()));
                if (function.getType() != null) {
                    b.append(" -> ").append(function.getType().getName());
                }
                b.append(":\n");
                b.append(indentStr).append("  ");
                if (function.getType() == null || !function.getType().getName().equals("none")) {
                    b.append("return ");
                }
                b.append("super").append(signature.asArguments()).append("\n\n");
            }
            if (anchor != block)
                b.append(indentStr).append("f:\n");

            List<ToitFunction> functions = ToitElementFactory.createFunctions(project, b.toString());

            if (anchor != block)
                functions = functions.subList(0,functions.size()-1);

            PsiElement prev;
            if (anchor != block) {
                prev = anchor.getPrevSibling();
                if (prev == null) {
                    prev = block.getNode().getFirstChildNode().getPsi(); // The :
                }
            } else {
                prev = block;
            }

            var n = prev.getNode().getLastChildNode();
            while (n.getLastChildNode() != null) n = n.getLastChildNode();

            while (n instanceof  PsiWhiteSpace) {
                var p = n;
                n = n.getTreePrev();
                p.getTreeParent().removeChild(p);
            }

            n.getTreeParent().addChild(new PsiWhiteSpaceImpl("\n"+indentStr));

            if (prev != block) {
                Collections.reverse(functions);
                for (ToitFunction function : functions) {
                    block.addAfter(function, prev);
                }
            } else {
                functions.forEach(block::add);
            }
        });

    }

    private static PsiElement getAnchor(ToitStructure structure, Editor editor) {
        var block =structure.getFirstChildOfType(ToitBlock.class);
        assert block != null;
        for (var function : block.getChildrenOfType(IToitElement.class)) {
            if (function.getTextRange().getStartOffset() >= editor.getCaretModel().getOffset()) {
                return function;
            }
        }
        return structure.getLastChild();
    }

}
