package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.files.ToitSdkFiles;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ToitPsiHelper;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.scope.ToitLocalScopeCalculator;
import org.toitlang.intellij.psi.scope.ToitScope;
import org.toitlang.intellij.psi.visitor.ToitVisitableElement;

import java.util.ArrayList;
import java.util.List;

public abstract class ToitBaseStubableElement<T extends StubElement<? extends PsiElement>> extends ToitVisitableElement<T> implements IToitElement {
    protected final static TokenSet ABSTRACT = TokenSet.create(ToitTypes.ABSTRACT);
    protected final static TokenSet STATIC = TokenSet.create(ToitTypes.STATIC);
    protected final static TokenSet STAR_SET = TokenSet.create(ToitTypes.STAR);
    protected final static TokenSet DOT_SET = TokenSet.create(ToitTypes.DOT);

    public ToitBaseStubableElement(@NotNull ASTNode node) {
        super(node);
    }

    public ToitBaseStubableElement(@NotNull T stub, @NotNull IStubElementType nodeType) {
        super(stub, nodeType);
    }

    // Utility function
    @Override
    public final boolean hasToken(TokenSet set) {
        return getNode().getChildren(set).length != 0;
    }

    @Override
    public final ASTNode getFirstChildToken(TokenSet set) {
        var children = getNode().getChildren(set);
        if (children.length > 0) return children[0];
        return null;
    }

    public final <V> V getFirstChildOfType(Class<V> clazz) {
        var children = getChildrenOfType(clazz);
        if (children.isEmpty()) return null;
        return children.get(0);
    }

    @Override
    public final <V> V getLastChildOfType(Class<V> clazz) {
        var children = getChildrenOfType(clazz);
        if (children.isEmpty()) return null;
        return children.get(children.size() - 1);
    }

    @Override
    public final <V> List<V> getChildrenOfType(Class<V> clazz) {
        return ToitPsiHelper.childrenOfType(this, clazz);
    }

    @Override
    public final <V> V getParentOfType(Class<V> clazz) {
        var p = getParent();
        while (p != null && !clazz.isInstance(p)) p = p.getParent();
        return clazz.cast(p);
    }

    @Override
    public final <V> V getPrevSiblingOfType(Class<V> clazz) {
        var p = getPrevSibling();
        while (p != null && !clazz.isInstance(p)) p = p.getPrevSibling();
        return clazz.cast(p);
    }

    public PsiElement getPrevNonWhiteSpaceSibling() {
        var p = getPrevSibling();
        while (p != null && TokenSet.WHITE_SPACE.contains(p.getNode().getElementType())) p = p.getPrevSibling();
        return p;
    }

    public static TextRange getRelativeRangeInParent(ASTNode node) {
        return new TextRange(node.getStartOffsetInParent(), node.getStartOffsetInParent() + node.getTextLength());
    }

    @Override
    public ToitFile getToitFile() {
        try {
            return (ToitFile) getContainingFile().getOriginalFile();
        } catch (PsiInvalidElementAccessException e) {
            return (ToitFile) getParentOfType(ToitFile.class).getOriginalFile();
        }
    }

    @Override
    public ToitScope getToitResolveScope() {
        return ToitScope.chain(this + "-" + getName() + "-resolve", getToitFile().getToitFileScope().getToitScope(), ToitSdkFiles.getCoreScope(getProject()));
    }

    @Override
    public ToitScope getLocalToitResolveScope() {
        return ToitScope.chain(this + "-" + getName() + "-local-resolve", ToitLocalScopeCalculator.calculate(this), getToitResolveScope());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public <V> List<V> getDescendentsOfType(Class<V> clazz) {
        var result = new ArrayList<V>();
        getDescendentsOfType(clazz, result);
        return result;
    }

    @Override
    public <V> V getLastDescendentOfType(Class<V> clazz) {
        var descendents = getDescendentsOfType(clazz);
        if (descendents.isEmpty()) return null;
        return descendents.get(descendents.size() - 1);
    }

    private <V> void getDescendentsOfType(Class<V> clazz, ArrayList<V> result) {
        for (PsiElement child : getChildren()) {
            if (clazz.isInstance(child)) result.add(clazz.cast(child));
            else if (child instanceof ToitBaseStubableElement) {
                //noinspection rawtypes,unchecked
                ((ToitBaseStubableElement) child).getDescendentsOfType(clazz, result);
            }
        }
    }

    private <V> V getParentWithIntermediaries(Class<V> vClass, List<Class<? extends IToitElement>> intermediaries) {
        int cur = intermediaries.size() - 1;
        PsiElement parent = this;
        while (parent != null && cur >= 0) {
            if (intermediaries.get(cur).isInstance(parent)) cur--;
            parent = parent.getParent();
        }

        if (parent == null) return null;
        if (vClass.isInstance(parent)) return vClass.cast(parent);
        return ((IToitElement) parent).getParentOfType(vClass);
    }

    @Override
    public <V> V getParentWithIntermediaries(Class<V> vClass, Class<? extends IToitElement> p1) {
        return getParentWithIntermediaries(vClass, List.of(p1));
    }

    @Override
    public <V> V getParentWithIntermediaries(Class<V> vClass, Class<? extends IToitElement> p1, Class<? extends IToitElement> p2) {
        return getParentWithIntermediaries(vClass, List.of(p1, p2));
    }

    @Override
    public <V> V getParentChain(Class<V> top, List<Class<? extends IToitElement>> classes) {
        int cur = classes.size() - 1;
        PsiElement elm = getParent();
        while (cur >= 0) {
            if (!classes.get(cur).isInstance(elm)) return null;
            cur--;
            elm = elm.getParent();
        }

        if (!top.isInstance(elm)) return null;
        return top.cast(elm);
    }
}
