// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.toitlang.intellij.impl.*;

public interface ToitTypes {

  IElementType PROPERTY = new ToitElementType("PROPERTY");

  IElementType COMMENT = new ToitTokenType("COMMENT");
  IElementType CRLF = new ToitTokenType("CRLF");
  IElementType KEY = new ToitTokenType("KEY");
  IElementType SEPARATOR = new ToitTokenType("SEPARATOR");
  IElementType VALUE = new ToitTokenType("VALUE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == PROPERTY) {
        return new ToitPropertyImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
