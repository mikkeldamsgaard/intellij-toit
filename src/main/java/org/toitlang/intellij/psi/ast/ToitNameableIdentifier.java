// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ToitElementFactory;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

public class ToitNameableIdentifier extends ToitIdentifier {

  public ToitNameableIdentifier(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected void accept(ToitVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public PsiReference getReference() {
    return new PsiReferenceBase<>(this, new TextRange(0, getTextLength()), false) {
      @Override
      public @Nullable PsiElement resolve() {
        return getParent();
      }

      @Override
      public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        return getElement(); // Actual rename is called by enclosing element
      }
    };

  }

  public PsiElement setName(@NlsSafe @NotNull String name) throws IncorrectOperationException {
    if (getNode().getElementType() == ToitTypes.FUNCTION_IDENTIFIER)
      return replace(ToitElementFactory.createFunctionIdentifier(getProject(),name));
    else if (getNode().getElementType() == ToitTypes.STRUCTURE_IDENTIFIER)
      return replace(ToitElementFactory.createStructureIdentifier(getProject(),name));
    else if (getNode().getElementType() == ToitTypes.IMPORT_AS_IDENTIFIER)
      return replace(ToitElementFactory.createImportAsIdentifier(getProject(),name));
    else if (getNode().getElementType() == ToitTypes.FACTORY_IDENTIFIER)
      return replace(ToitElementFactory.createFactoryIdentifier(getProject(),name));
    else if (getNode().getElementType() == ToitTypes.NAMED_PARAMETER_IDENTIFIER)
      return replace(ToitElementFactory.createNamedParameterIdentifier(getProject(),name));
    else if (getNode().getElementType() == ToitTypes.SIMPLE_PARAMETER_IDENTIFIER)
      return replace(ToitElementFactory.createSimpleParameterIdentifier(getProject(),name));
    else if (getNode().getElementType() == ToitTypes.VARIABLE_IDENTIFIER)
      return replace(ToitElementFactory.createVariableIdentifier(getProject(),name));

    return this;
  }

  public String getFindUsageTypeName() {
    if (getNode().getElementType() == ToitTypes.FUNCTION_IDENTIFIER)
      return "function";
    else if (getNode().getElementType() == ToitTypes.STRUCTURE_IDENTIFIER)
      return "class or interface";
    else if (getNode().getElementType() == ToitTypes.IMPORT_AS_IDENTIFIER)
      return "import as";
    else if (getNode().getElementType() == ToitTypes.FACTORY_IDENTIFIER)
      return "constructor factory";
    else if (getNode().getElementType() == ToitTypes.NAMED_PARAMETER_IDENTIFIER)
      return "parameter";
    else if (getNode().getElementType() == ToitTypes.SIMPLE_PARAMETER_IDENTIFIER)
      return "parameter";
    else if (getNode().getElementType() == ToitTypes.VARIABLE_IDENTIFIER)
      return "variable";
    return "unknown";
  }
}
