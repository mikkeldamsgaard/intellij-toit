package org.toitlang.intellij.psi.ui.renders;

import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.MemberChooserObject;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.ToitFunction;
import org.toitlang.intellij.psi.ast.ToitStructure;
import org.toitlang.intellij.psi.calls.ParameterInfo;

import javax.swing.*;

public class MemberFunctionRender implements ClassMember {
    private final ToitFunction function;

    public MemberFunctionRender(ToitFunction function) {
        this.function = function;
    }

    @Override
    public MemberChooserObject getParentNodeDelegate() {
        return new StructureRender(function.getParentOfType(ToitStructure.class));
    }

    @Override
    public void renderTreeNode(SimpleColoredComponent component, JTree tree) {
        var signature = function.getSignature();
        component.append(signature.getFunctionName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
        for (ParameterInfo positionalParameter : signature.getPositionalParameters()) {
            component.append(" ");
            component.append(positionalParameter.getName());
            if (positionalParameter.getType() != null) {
                component.append("/", SimpleTextAttributes.GRAYED_ATTRIBUTES);
                component.append(positionalParameter.getType().getName(), SimpleTextAttributes.GRAYED_ATTRIBUTES);
            }
        }

        for (ParameterInfo namedParameter : signature.getNamedParameters().values()) {
            component.append(" --");
            component.append(namedParameter.getName());
            if (namedParameter.getType() != null) {
                component.append("/", SimpleTextAttributes.GRAYED_ATTRIBUTES);
                component.append(namedParameter.getType().getName(), SimpleTextAttributes.GRAYED_ATTRIBUTES);
            }
        }
    }

    @Override
    public @NlsContexts.Label @NotNull String getText() {
        return function.getName();
    }

    public ToitFunction getFunction() {
        return function;
    }
}
