// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.List;
import java.util.stream.Collectors;

import static org.toitlang.intellij.psi.ToitTypes.*;

public class ToitType extends ToitElement {

  public ToitType(@NotNull ASTNode node) {
    super(node);
  }

  @NotNull
  public List<ToitReferenceIdentifier> getVariableNameList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ToitReferenceIdentifier.class);
  }

  @Override
  protected void accept(ToitVisitor visitor) {
    visitor.visit(this);
  }

  public boolean isReturnType() { return getNode().getElementType() == RETURN_TYPE; }
  public boolean isImplementsType() { return getNode().getElementType() == IMPLEMENTS_TYPE; }
  public boolean isExtendsType() { return getNode().getElementType() == EXTENDS_TYPE; }
  public boolean isVariableType() { return getNode().getElementType() == VARIABLE_TYPE; }

  @Override
  public String getName() {
    return childrenOfType(ToitReferenceIdentifier.class).stream()
            .map(ToitReferenceIdentifier::getName)
            .collect(Collectors.joining("."));
  }
}
