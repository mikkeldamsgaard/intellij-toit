// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.reference.ToitEvaluatedType;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.stream.Collectors;

import static org.toitlang.intellij.psi.ToitTypes.DOT;
import static org.toitlang.intellij.psi.ToitTypes.DOT_DOT;

public class ToitImportDeclaration extends ToitElementBase implements ToitReferenceTarget, PsiNameIdentifierOwner {

  public ToitImportDeclaration(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  protected void accept(ToitVisitor visitor) {
    visitor.visit(this);
  }

  public boolean hasShow() {
    return getChildrenOfType(ToitIdentifier.class).stream().anyMatch(ToitIdentifier::isShow);
  }

  public boolean hasAs() {
    return getChildrenOfType(ToitIdentifier.class).stream().anyMatch(ToitIdentifier::isImportAs);
  }

  private final static TokenSet DOT_AND_IMPORT_IDENTIFIERS = TokenSet.create(DOT, DOT_DOT, ToitTypes.IMPORT_IDENTIFIER);

  public int getPrefixDots() {
    int prefixDots = 0;
    for (ASTNode child : getNode().getChildren(DOT_AND_IMPORT_IDENTIFIERS)) {
      if (child.getElementType() == DOT) {
        prefixDots++;
      } else if (child.getElementType() == DOT_DOT) {
        prefixDots += 2;
      } else break;
    }
    return prefixDots;
  }

  public boolean isShowStar() {
    return hasToken(STAR_SET);
  }

  @Override
  public @Nullable PsiElement getNameIdentifier() {
    if (hasAs()) {
      return getChildrenOfType(ToitIdentifier.class).stream().filter(ToitIdentifier::isImportAs).findFirst().orElseThrow();
    }
    return getChildrenOfType(ToitIdentifier.class).stream().reduce((f,s) -> s).orElseThrow();
  }

  @Override
  public PsiElement setName(@NlsSafe @NotNull String name) throws IncorrectOperationException {
    var identifier = getNameIdentifier();
    if (!(identifier instanceof ToitNameableIdentifier)) return this;
    ToitNameableIdentifier toitNameableIdentifier = (ToitNameableIdentifier) identifier;

    if (toitNameableIdentifier.isImportAs()) {
        toitNameableIdentifier.setName(name);
    } else {
      // Lookup the ToitEvaluatedType (Should be a ToitFile).
      // Rename the toitFile.
      throw new IncorrectOperationException();
    }
    return this;
  }

  @Override
  public ToitEvaluatedType getEvaluatedType() {
    var imports = getChildrenOfType(ToitReferenceIdentifier.class).stream().filter(ToitReferenceIdentifier::isImport).collect(Collectors.toList());
    String fqn = "$" + getPrefixDots() + "$" + imports.stream().map(ToitIdentifier::getName).collect(Collectors.joining("."));
    return ToitEvaluatedType.file(getToitFile().getToitFileScope().getImportedLibrary(fqn));
  }
}