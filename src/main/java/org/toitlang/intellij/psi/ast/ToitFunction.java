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
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.calls.ParameterInfo;
import org.toitlang.intellij.psi.calls.ParametersInfo;
import org.toitlang.intellij.psi.scope.ToitScope;
import org.toitlang.intellij.psi.stub.ToitFunctionStub;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import javax.swing.*;
import java.util.List;

public class ToitFunction extends ToitPrimaryLanguageElement<ToitFunction, ToitFunctionStub> {
    ParametersInfo parametersInfo;

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

    @Override
    public void subtreeChanged() {
        parametersInfo = null;
    }

    // TODO: Stub this
    public ToitType getType() {
        for (ToitType toitType : getChildrenOfType(ToitType.class)) {
            if (toitType.isReturnType()) return toitType;
        }
        return null;
    }

    @Override
    public String getName() {
        var stub = getStub();
        if (stub != null && stub.getName() != null) return stub.getName();

        if (isConstructor() && hasFactoryName()) return getFactoryName();
        var functionName = getFunctionName();
        if (functionName == null) return "";
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

    public boolean isStatic() {
        return hasToken(STATIC);
    }

    private ASTNode getStatic() {
        return getFirstChildToken(STATIC);
    }

    private ASTNode getAbstract() {
        return getFirstChildToken(ABSTRACT);
    }

    public void semanticErrorCheck(ProblemsHolder holder) {
        boolean hasBody = !getChildrenOfType(ToitBlock.class).isEmpty();

        if (getParent() instanceof ToitFile) {
            if (isStatic())
                holder.registerProblem(this, getRelativeRangeInParent(getStatic()), "Top level functions cannot be static");
            if (!hasBody) holder.registerProblem(getFunctionName(), "Missing body");
        } else {
            ToitStructure parent = getParentOfType(ToitStructure.class);
            ToitNameableIdentifier functionName = getFunctionName();
            if (functionName == null) return;
            if (parent.isClass()) {
                boolean isAbstractClass = parent.isAbstract();
                if (isAbstract() && !isAbstractClass)
                    holder.registerProblem(this, getRelativeRangeInParent(getAbstract()), "Abstract functions not allowed in non-abstract class");
                if (!isAbstract() && !hasBody) {
                    holder.registerProblem(functionName, "Missing body");
                }
            }

            if (parent.isInterface()) {
                if (hasBody && !isStatic())
                    holder.registerProblem(functionName, "Only static interface methods may have a body");
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
        return isAbstract() ? AllIcons.Nodes.AbstractMethod : AllIcons.Nodes.Method;
    }

    public boolean isConstructor() {
        for (ToitPseudoKeyword toitPseudoKeyword : getChildrenOfType(ToitPseudoKeyword.class)) {
            if ("constructor".equals(toitPseudoKeyword.getName())) return true;
        }
        return false;
    }

    public boolean isOperator() {
        for (ToitPseudoKeyword toitPseudoKeyword : getChildrenOfType(ToitPseudoKeyword.class)) {
            if ("operator".equals(toitPseudoKeyword.getName())) return true;
        }
        return false;
    }

    public boolean hasFactoryName() {
        for (ToitNameableIdentifier toitNameableIdentifier : getChildrenOfType(ToitNameableIdentifier.class)) {
            if (toitNameableIdentifier.isFactoryName()) return true;
        }
        return false;
    }


    public String getFactoryName() {
        for (ToitNameableIdentifier toitNameableIdentifier : getChildrenOfType(ToitNameableIdentifier.class)) {
            if (toitNameableIdentifier.isFactoryName()) return toitNameableIdentifier.getName();
        }
        return null;
    }

    public List<ToitParameterName> getParameters() {
        return getChildrenOfType(ToitParameterName.class);
    }

    public ToitScope getParameterScope() {
        ToitScope scope = new ToitScope(true);
        getParameters().forEach(p -> scope.add(p.getName(), p));
        return scope;
    }

    public boolean isReturnTypeNullable() {
        var type = getType();
        if (type == null) return true;
        return type.getNextSibling() != null && type.getNextSibling().getNode().getElementType() == ToitTypes.QUESTION;
    }

    public ParametersInfo getParameterInfo() {
        if (parametersInfo == null) {
            parametersInfo = new ParametersInfo();
            getChildrenOfType(ToitParameterName.class).forEach(this::parseParameterName);
        }
        return parametersInfo;
    }

    private void parseParameterName(ToitParameterName pn) {
        boolean isNamed = pn.getNode().getFirstChildNode().getElementType() == ToitTypes.MINUS_MINUS;
        boolean isMemberInitializer = pn.getNode().getChildren(DOT_SET).length != 0;
        boolean isBlock = pn.getPrevSibling() != null && pn.getPrevSibling().getNode().getElementType() == ToitTypes.LBRACKET;

        ToitType toitType = null;
        boolean nullable = false;
        boolean hasDefaultVale = false;
        try {
            PsiElement cur = pn.getNextSibling();
            if (cur == null) return;
            if (cur.getNode().getElementType() == ToitTypes.SLASH) {
                cur = cur.getNextSibling();
                if (cur == null) return;
                if (cur instanceof ToitType) {
                    toitType = (ToitType) cur;
                    cur = cur.getNextSibling();
                    if (cur == null) return;
                }
                if (cur.getNode().getElementType() == ToitTypes.QUESTION) {
                    nullable = true;
                    cur = cur.getNextSibling();
                    if (cur == null) return;
                }
            }
            if (cur.getNode().getElementType() == ToitTypes.EQUALS) {
                hasDefaultVale = true;
            }
        } finally {
            ParameterInfo info = new ParameterInfo(toitType, nullable, hasDefaultVale, isBlock, isMemberInitializer);

            if (isNamed) {
                parametersInfo.addNamed(pn.getName(), info);
            } else {
                parametersInfo.addPositional(info);
            }
        }
    }
}
