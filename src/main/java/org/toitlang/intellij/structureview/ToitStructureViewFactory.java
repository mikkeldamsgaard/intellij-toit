package org.toitlang.intellij.structureview;

import com.intellij.ide.structureView.*;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ToitFile;

public class ToitStructureViewFactory implements PsiStructureViewFactory {
    @Override
    public @Nullable StructureViewBuilder getStructureViewBuilder(@NotNull PsiFile psiFile) {
        return new TreeBasedStructureViewBuilder() {
            @Override
            public @NotNull StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                return new TextEditorBasedStructureViewModel(editor) {
                    @Override
                    public @NotNull StructureViewTreeElement getRoot() {
                        return new StructureWrapper((ToitFile) psiFile);
                    }

                    @Override
                    protected Class @NotNull [] getSuitableClasses() {
                        return new Class[] { IStructureViewable.class };
                    }
                };
            }

            @Override
            public boolean isRootNodeShown() {
                return true;
            }
        };
    }

    static class StructureWrapper implements StructureViewTreeElement {
        IStructureViewable viewable;

        public StructureWrapper(IStructureViewable viewable) {
            this.viewable = viewable;
        }

        @Override
        public Object getValue() {
            return viewable;
        }

        @Override
        public @NotNull ItemPresentation getPresentation() {
            return viewable;
        }

        @Override
        public TreeElement @NotNull [] getChildren() {
            return viewable.getStructureChildren().stream().map(StructureWrapper::new).toArray(StructureWrapper[]::new);
        }

        @Override
        public void navigate(boolean requestFocus) {
            viewable.navigate(requestFocus);
        }

        @Override
        public boolean canNavigate() {
            return viewable.canNavigate();
        }

        @Override
        public boolean canNavigateToSource() {
            return viewable.canNavigateToSource();
        }
    }
}
