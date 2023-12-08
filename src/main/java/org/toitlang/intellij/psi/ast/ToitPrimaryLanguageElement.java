package org.toitlang.intellij.psi.ast;

import com.intellij.psi.PsiNameIdentifierOwner;
import org.toitlang.intellij.psi.reference.ToitEvaluatedType;
import org.toitlang.intellij.structureview.IStructureViewable;

import java.util.Objects;

public interface ToitPrimaryLanguageElement extends ToitReferenceTarget, ToitElement, PsiNameIdentifierOwner, IStructureViewable {
    boolean isPrivate();
}
