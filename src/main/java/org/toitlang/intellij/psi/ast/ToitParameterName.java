// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.reference.ToitEvaluatedType;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.List;

public class ToitParameterName extends ToitElementBase implements ToitReferenceTarget, PsiNameIdentifierOwner {

  public ToitParameterName(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected void accept(ToitVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public ToitIdentifier getNameIdentifier() {
    var toitNameableIdentifier = getChildrenOfType(ToitNameableIdentifier.class).stream().findAny().orElse(null);
    if (toitNameableIdentifier != null) return toitNameableIdentifier;

    return getChildrenOfType(ToitNameableIdentifier.class).stream().findAny().orElse(null);
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
    nameIdentifier.setName(name);
    return this;
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

  @Override
  public ToitEvaluatedType getEvaluatedType() {
    // TODO: <DOT> parameters should take type from instance variable
    return ToitEvaluatedType.fromType(getType());
  }
}
