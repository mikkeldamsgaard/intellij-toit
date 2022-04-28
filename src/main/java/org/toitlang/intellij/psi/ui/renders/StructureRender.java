package org.toitlang.intellij.psi.ui.renders;

import com.intellij.codeInsight.generation.MemberChooserObject;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.SimpleColoredComponent;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.ToitStructure;

import javax.swing.*;
import java.util.Objects;

public class StructureRender implements MemberChooserObject {
    private final ToitStructure structure;

    public StructureRender(ToitStructure structure) {
        this.structure = structure;
    }

    @Override
    public void renderTreeNode(SimpleColoredComponent component, JTree tree) {
        component.append(getText());
    }

    @Override
    public @NlsContexts.Label @NotNull String getText() {
        return structure.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StructureRender that = (StructureRender) o;
        return Objects.equals(structure, that.structure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(structure);
    }
}
