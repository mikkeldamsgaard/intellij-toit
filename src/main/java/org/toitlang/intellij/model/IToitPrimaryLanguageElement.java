package org.toitlang.intellij.model;

import com.intellij.psi.PsiNameIdentifierOwner;
import org.toitlang.intellij.structureview.IStructureViewable;

public interface IToitPrimaryLanguageElement extends PsiNameIdentifierOwner, IStructureViewable {
    public boolean isPrivate();
}
