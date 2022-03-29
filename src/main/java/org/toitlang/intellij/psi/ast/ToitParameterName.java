// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.List;

public class ToitParameterName extends ToitElement implements PsiNameIdentifierOwner {

  public ToitParameterName(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected void accept(ToitVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public ToitIdentifier getNameIdentifier() {
    List<ToitNameableIdentifier> toitNameableIdentifiers = childrenOfType(ToitNameableIdentifier.class);
    if (toitNameableIdentifiers.size() > 0) {
      return toitNameableIdentifiers.get(0);
    }
    List<ToitReferenceIdentifier> toitReferenceIdentifiers = childrenOfType(ToitReferenceIdentifier.class);
    if (toitNameableIdentifiers.size() > 0) {
      return toitNameableIdentifiers.get(0);
    }
    return null;
  }

  @Override
  public String getName() {
    ToitIdentifier identifier = getNameIdentifier();
    if (identifier == null) return "__UnknownToit__";
    return identifier.getName();
  }

  @Override
  public PsiElement setName(@NlsSafe @NotNull String name) throws IncorrectOperationException {
    ToitIdentifier nameIdentifier = getNameIdentifier();
    if (nameIdentifier == null) return this;
    return nameIdentifier.setName(name);
  }

  public ToitType getType() {
    var next = getNextSibling();
    if (next != null && next.getNode().getElementType() == ToitTypes.SLASH) {
      next = next.getNextSibling();
      if (next instanceof ToitType) {
        var toitType = (ToitType)next;
        if (toitType.isVariableType()) return toitType;
      }
    }
    return null;
  }
}
