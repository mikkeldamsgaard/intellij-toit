// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.visitor.ToitVisitor;
import org.toitlang.intellij.psi.scope.ToitScope;

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

  public boolean isReturnType() {
    return getNode().getElementType() == RETURN_TYPE;
  }

  public boolean isImplementsType() {
    return getNode().getElementType() == IMPLEMENTS_TYPE;
  }

  public boolean isExtendsType() {
    return getNode().getElementType() == EXTENDS_TYPE;
  }

  public boolean isVariableType() {
    return getNode().getElementType() == VARIABLE_TYPE;
  }

  @Override
  public @NotNull String getName() {
    return getChildrenOfType(ToitReferenceIdentifier.class).stream()
            .map(ToitReferenceIdentifier::getName)
            .collect(Collectors.joining("."));
  }

  public ToitStructure resolve() {
    var refs = getChildrenOfType(ToitReferenceIdentifier.class);
    if (refs.size() == 1) {
      return firstStructureInScope(getToitResolveScope(), refs.get(0).getName());
    } else if (refs.size() == 2) {
      for (PsiElement fileRef : getToitResolveScope().resolve(refs.get(0).getName())) {
        if (fileRef instanceof ToitFile) {
          ToitFile toitFile = (ToitFile) fileRef;
          var res = firstStructureInScope(toitFile.getToitFileScope().getToitScope(ToitScope.ROOT), refs.get(1).getName());
          if (res != null) return res;
        }
      }
    }
    return null;
  }

  private ToitStructure firstStructureInScope(ToitScope scope, String name) {
    for (PsiElement elementRef : scope.resolve(name)) {
      if (elementRef instanceof ToitStructure) return (ToitStructure) elementRef;
    }
    return null;
  }
}
