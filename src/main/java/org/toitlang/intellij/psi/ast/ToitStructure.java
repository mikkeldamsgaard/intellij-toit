// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.icons.AllIcons;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.stubs.IStubElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.structureview.IStructureViewable;
import org.toitlang.intellij.psi.stub.ToitStructureElementType;
import org.toitlang.intellij.psi.stub.ToitStructureStub;
import org.toitlang.intellij.psi.visitor.ToitVisitor;
import org.toitlang.intellij.psi.scope.ToitScope;

import javax.swing.*;
import java.util.Collections;
import java.util.List;


public class ToitStructure extends ToitPrimaryLanguageElement<ToitStructure, ToitStructureStub> {
    public ToitStructure(@NotNull ASTNode node) {
        super(node);
    }

    public ToitStructure(@NotNull ToitStructureStub stub, @NotNull IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @Override
    protected void accept(ToitVisitor visitor) {
        visitor.visit(this);
    }

    public boolean isClass() {
        return ((ToitStructureElementType) getElementType()).isClass();
    }

    public boolean isInterface() {
        return ((ToitStructureElementType) getElementType()).isInterface();
    }

    public boolean isMonitor() {
        return ((ToitStructureElementType) getElementType()).isMonitor();
    }

    public boolean isAbstract() {
        var stub = getStub();
        if (stub != null) return stub.isAbstract();
        return hasToken(ABSTRACT);
    }

    public String getName() {
        var stub = getStub();
        if (stub != null) return stub.getName();

        return getNameIdentifier().getName();
    }

    public ToitScope getScope() {
        ToitScope scope = new ToitScope();
        getScope(scope);
        return scope;
    }

    public void getScope(ToitScope scope) {
        childrenOfType(ToitBlock.class).forEach(b ->
                b.acceptChildren(new ToitVisitor() {
                    @Override
                    public void visit(ToitVariableDeclaration toitVariableDeclaration) {
                        scope.add(toitVariableDeclaration.getName(), toitVariableDeclaration);
                    }

                    @Override
                    public void visit(ToitFunction toitFunction) {
                        if (toitFunction.isConstructor()) {
                         if (toitFunction.hasFactoryName()) {
                             scope.add(toitFunction.getFactoryName(), toitFunction);
                         }
                        } else {
                            scope.add(toitFunction.getName(), toitFunction);
                        }
                    }


                }));
    }


    @Override
    public @NlsSafe @Nullable String getPresentableText() {
        return getName();
    }

    @Override
    public List<IStructureViewable> getStructureChildren() {
        var blocks = childrenOfType(ToitBlock.class);
        if (blocks.isEmpty()) return Collections.emptyList();
        return blocks.get(0).childrenOfType(IStructureViewable.class);
    }

    @Override
    protected @NotNull Icon getElementTypeIcon() {
        return isAbstract()?AllIcons.Nodes.AbstractClass:AllIcons.Nodes.Class;
    }
}
