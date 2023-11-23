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
import java.util.*;
import java.util.stream.Collectors;


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

    public @NotNull String getName() {
        var stub = getStub();
        if (stub != null) return stub.getName();

        ToitNameableIdentifier nameIdentifier = getNameIdentifier();
        if (nameIdentifier == null) return "";
        return nameIdentifier.getName();
    }

    public ToitScope getScope(boolean staticOnly, ToitScope parent) {
        return getScope(staticOnly, parent, new HashSet<>());
    }

    private ToitScope getScope(boolean static_, ToitScope parent, Set<ToitStructure> seenClasses) {
        seenClasses.add(this);
        if (!static_) {
            var baseClass = getBaseClass();
            if (baseClass != null && !seenClasses.contains(baseClass)) {
                parent = baseClass.getScope(false, parent, seenClasses);
            }
        }

        ToitScope structureScope = parent.sub(getName()+"-structure");
        populateScope(structureScope, static_);
        return structureScope;
    }

    public void populateScope(ToitScope scope, boolean static_) {
        getChildrenOfType(ToitBlock.class).forEach(b ->
                b.acceptChildren(new ToitVisitor() {
                    @Override
                    public void visit(ToitVariableDeclaration toitVariableDeclaration) {
                        if (static_ == toitVariableDeclaration.isStatic())
                            scope.add(toitVariableDeclaration.getName(), toitVariableDeclaration);
                    }

                    @Override
                    public void visit(ToitFunction toitFunction) {
                        if (toitFunction.isConstructor()) {
                            if (toitFunction.hasFactoryName() && static_) {
                                scope.add(toitFunction.getFactoryName(), toitFunction);
                            }
                        } else if (!toitFunction.isOperator()) {
                            if (static_ == toitFunction.isStatic())
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
        var blocks = getChildrenOfType(ToitBlock.class);
        if (blocks.isEmpty()) return Collections.emptyList();
        return blocks.get(0).getChildrenOfType(IStructureViewable.class);
    }

    @Override
    protected @NotNull Icon getElementTypeIcon() {
        return isAbstract() ? AllIcons.Nodes.AbstractClass : AllIcons.Nodes.Class;
    }

    public ToitStructure getBaseClass() {
        var extendsTypes = getChildrenOfType(ToitType.class).stream()
                .filter(ToitType::isExtendsType).collect(Collectors.toList());

        for (ToitType extendsType : extendsTypes) {
            ToitStructure base = extendsType.resolve();
            if (base != null) return base;
        }

        return null;
    }


    public List<ToitStructure> getInterfaces() {
        var implementsTypes = getChildrenOfType(ToitType.class).stream()
                .filter(ToitType::isImplementsType).collect(Collectors.toList());

        List<ToitStructure> interfaces = new ArrayList<>();
        for (ToitType implementsType : implementsTypes) {
            ToitStructure _interface = implementsType.resolve();
            if (_interface != null) interfaces.add(_interface);
        }

        return interfaces;
    }


    public List<ToitFunction> getFactoryConstructors(String name) {
        ToitBlock block = getFirstChildOfType(ToitBlock.class);
        List<ToitFunction> result = new ArrayList<>();
        if (block == null) return result;

        var functions = block.getChildrenOfType(ToitFunction.class);
        for (ToitFunction function : functions) {
            if (function.isConstructor() && function.hasFactoryName() && name.equals(function.getFactoryName())) {
                result.add(function);
            }
        }
        return result;

    }

    public List<ToitFunction> getDefaultConstructors() {
        ToitBlock block = getFirstChildOfType(ToitBlock.class);
        List<ToitFunction> result = new ArrayList<>();
        if (block == null) return result;

        var functions = block.getChildrenOfType(ToitFunction.class);
        for (ToitFunction function : functions) {
            if (function.isConstructor() && !function.hasFactoryName()) {
                result.add(function);
            }
        }
        return result;
    }

    public boolean isAssignableTo(ToitStructure structure) {
        if (equals(structure)) return true;
        return structure.isBaseClassOrInterfaceOf(this);
    }

    private boolean isBaseClassOrInterfaceOf(ToitStructure structure) {
        if (equals(structure)) return true;

        return isBaseClassOrInterfaceOf(structure, new Stack<>());
    }

    private boolean isBaseClassOrInterfaceOf(ToitStructure structure, Stack<ToitStructure> seen) {
        if (equals(structure)) return true;

        try {
            seen.push(structure);
            for (ToitStructure _interface : structure.getInterfaces()) {
                if (isBaseClassOrInterfaceOf(_interface, seen)) return true;
            }
            var baseClass = structure.getBaseClass();
            if (baseClass != null && !seen.contains(baseClass)) {
                return isBaseClassOrInterfaceOf(baseClass, seen);
            }
            return false;
        } finally {
            seen.pop();
        }
    }

    public List<ToitFunction> getAllFunctions() {
        List<ToitFunction> functions = new ArrayList<>();
        List<ToitVariableDeclaration> variables = new ArrayList<>();
        Set<ToitStructure> seen = new HashSet<>();
        getAllFunctions(functions, variables, seen);
        return functions;
    }

    public List<ToitVariableDeclaration> getAllVariables() {
        List<ToitFunction> functions = new ArrayList<>();
        List<ToitVariableDeclaration> variables = new ArrayList<>();
        Set<ToitStructure> seen = new HashSet<>();
        getAllFunctions(functions, variables, seen);
        return variables;
    }

    private void getAllFunctions(List<ToitFunction> result, List<ToitVariableDeclaration> variables, Set<ToitStructure> seen) {
        seen.add(this);
        var block = getFirstChildOfType(ToitBlock.class);
        if (block == null) return; // Malformed syntax

        result.addAll(block.getChildrenOfType(ToitFunction.class));
        variables.addAll(block.getChildrenOfType(ToitVariableDeclaration.class));

        var base = getBaseClass();
        if (base != null && !seen.contains(base)) {
            base.getAllFunctions(result, variables, seen);
        }

        for (ToitStructure interface_ : getInterfaces()) {
            if (!seen.contains(interface_)) {
                interface_.getAllFunctions(result, variables, seen);
            }
        }
    }

    public Set<ToitFunction> getOwnFunctions() {
        var block = getFirstChildOfType(ToitBlock.class);
        if (block == null) return Collections.emptySet(); // Malformed syntax
        return new HashSet<>(block.getChildrenOfType(ToitFunction.class));
    }
}
