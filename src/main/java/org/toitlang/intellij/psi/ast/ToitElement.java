package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.scope.ToitScope;

import java.util.List;

public interface ToitElement extends PsiElement {
    // Utility function
    boolean hasToken(TokenSet set);

    ASTNode getFirstChildToken(TokenSet set);

    <V> V getFirstChildOfType(Class<V> clazz);

    <V> V getLastChildOfType(Class<V> clazz);

    <V> List<V> getChildrenOfType(Class<V> clazz);

    <V> V getParentOfType(Class<V> clazz);

    <V> V getPrevSiblingOfType(Class<V> clazz);

    PsiElement getPrevNonWhiteSpaceSibling();

    ToitFile getToitFile();

    ToitScope getToitResolveScope();

    ToitScope getLocalToitResolveScope();

    <V> List<V> getDescendentsOfType(Class<V> clazz);
    <V> V getLastDescendentOfType(Class<V> clazz);

    <V> V getParentWithIntermediaries(Class<V> vClass, Class<? extends ToitElement> p1);
    <V> V getParentWithIntermediaries(Class<V> vClass, Class<? extends ToitElement> p1, Class<? extends ToitElement> p2);

    <V> V getParentChain(Class<V> top, List<Class<? extends ToitElement>> classes);

    List<ToitElement> getAncestorsUntil(Class<? extends ToitElement> clazz);

    String getName();
}
