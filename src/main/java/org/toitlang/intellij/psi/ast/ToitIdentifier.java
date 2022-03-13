// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

public abstract class ToitIdentifier extends ToitElement {
  String name;
  public ToitIdentifier(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void subtreeChanged() {
    name = null;
  }

  @Override
  public String getName() {
    if (name == null) name = getText();

    return name;
  }

  public boolean isImport() { return getNode().getElementType() == ToitTypes.IMPORT_IDENTIFIER; }
  public boolean isShow() { return getNode().getElementType() == ToitTypes.IMPORT_SHOW_IDENTIFIER; }
  public boolean isImportAs() { return getNode().getElementType() == ToitTypes.IMPORT_AS_IDENTIFIER; }
  public boolean isFunctionName() { return getNode().getElementType() == ToitTypes.FUNCTION_IDENTIFIER; }
  public boolean isVariableName() { return getNode().getElementType() == ToitTypes.VARIABLE_IDENTIFIER; }
  public boolean isStructureName() { return getNode().getElementType() == ToitTypes.STRUCTURE_IDENTIFIER; }
  public boolean isTypeName() { return getNode().getElementType() == ToitTypes.TYPE_IDENTIFIER; }
  public boolean isReference() { return getNode().getElementType() == ToitTypes.REFERENCE_IDENTIFIER; }

}
