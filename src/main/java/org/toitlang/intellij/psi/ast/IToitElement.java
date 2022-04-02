package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.TokenSet;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.scope.ToitScope;

import java.util.List;

public interface IToitElement extends PsiElement {
    // Utility function
    boolean hasToken(TokenSet set);

    ASTNode firstChildToken(TokenSet set);

    <V> V firstChildOfType(Class<V> clazz);

    <V> V lastChildOfType(Class<V> clazz);

    <V> List<V> childrenOfType(Class<V> clazz);

    <V> V getParentOfType(Class<V> clazz);

    <V> V getPrevSiblingOfType(Class<V> clazz);

    ToitFile getToitFile();

    ToitScope getToitResolveScope();

    ToitScope getLocalToitResolveScope();

    <V> List<V> getDescendentsOfType(Class<V> clazz);

    <V> V getParentWithIntermediaries(Class<V> vClass, Class<? extends IToitElement> p1);
    <V> V getParentWithIntermediaries(Class<V> vClass, Class<? extends IToitElement> p1, Class<? extends IToitElement> p2);

    <V> V getParentChain(Class<V> top, List<Class<? extends IToitElement>> classes);
}
