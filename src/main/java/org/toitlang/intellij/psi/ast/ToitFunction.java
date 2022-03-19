// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.icons.AllIcons;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.stub.ToitFunctionStub;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import javax.swing.*;
import java.util.List;

public class ToitFunction extends ToitPrimaryLanguageElement<ToitFunction, ToitFunctionStub> {
    public ToitFunction(@NotNull ASTNode node) {
        super(node);
    }

    public ToitFunction(@NotNull ToitFunctionStub stub, @NotNull IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @Override
    protected void accept(ToitVisitor visitor) {
        visitor.visit(this);
    }

    // TODO: Stub this
    public ToitType getType() {
        for (ToitType toitType : childrenOfType(ToitType.class)) {
            if (toitType.isReturnType()) return toitType;
        }
        return null;
    }

    @Override
    public String getName() {
        var stub = getStub();
        if (stub != null) return stub.getName();

        var functionName = getFunctionName();
        if (functionName == null) return null;
        return functionName.getName();
    }

    // TODO: Stub this
    public boolean isAbstract() {
        return hasToken(ABSTRACT);
    }

    @Override
    public PsiElement setName(@NlsSafe @NotNull String name) throws IncorrectOperationException {
        return getFunctionName().setName(name);
    }

    public ToitNameableIdentifier getFunctionName() {
        for (PsiElement child : getChildren()) {
            if (child instanceof ToitNameableIdentifier && ((ToitNameableIdentifier) child).isFunctionName())
                return (ToitNameableIdentifier) child;
        }

        // Constructor/operator
        return null;
    }

    private ASTNode getStatic() {
        return firstChildToken(STATIC);
    }

    private ASTNode getAbstract() {
        return firstChildToken(ABSTRACT);
    }

    public void semanticErrorCheck(ProblemsHolder holder) {
        boolean hasBody = !childrenOfType(ToitBlock.class).isEmpty();
        boolean isStatic = hasToken(STATIC);

        if (getParent() instanceof ToitFile) {
            if (isStatic)
                holder.registerProblem(this, getRelativeRangeInParent(getStatic()), "Top level functions cannot be static");
            if (!hasBody) holder.registerProblem(getFunctionName(), "Missing body");
        } else {
            ToitStructure parent = getParentOfType(ToitStructure.class);
            if (parent.isClass()) {
                boolean isAbstractClass = parent.isAbstract();
                if (isAbstract() && !isAbstractClass)
                    holder.registerProblem(this, getRelativeRangeInParent(getAbstract()), "Abstract functions not allowed in non-abstract class");
                if (!isAbstract() && !hasBody) holder.registerProblem(getFunctionName(), "Missing body");
            }

            if (parent.isInterface()) {
                if (hasBody && !isStatic && getFunctionName() != null)
                    holder.registerProblem(getFunctionName(), "Only static interface methods may have a body");
            }
        }
    }

    @Override
    public @NlsSafe @Nullable String getPresentableText() {
        return getName();
    }

    @Override
    protected @NotNull Icon getElementTypeIcon() {
        if (getParent() instanceof ToitFile) return AllIcons.Nodes.Function;
        return isAbstract()? AllIcons.Nodes.AbstractMethod:AllIcons.Nodes.Method;
    }

    public boolean isConstructor() {
        for (ToitPseudoKeyword toitPseudoKeyword : childrenOfType(ToitPseudoKeyword.class)) {
            if ("constructor".equals(toitPseudoKeyword.getName())) return true;
        }
        return false;
    }

    public boolean hasFactoryName() {
        for (ToitNameableIdentifier toitNameableIdentifier : childrenOfType(ToitNameableIdentifier.class)) {
            if (toitNameableIdentifier.isFactoryName()) return true;
        }
        return false;
    }


    public String getFactoryName() {
        for (ToitNameableIdentifier toitNameableIdentifier : childrenOfType(ToitNameableIdentifier.class)) {
            if (toitNameableIdentifier.isFactoryName()) return toitNameableIdentifier.getName();
        }
        return null;
    }

    public List<ToitParameterName> getParameters() {
        return childrenOfType(ToitParameterName.class);
    }

}
