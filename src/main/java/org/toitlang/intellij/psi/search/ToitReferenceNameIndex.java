package org.toitlang.intellij.psi.search;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.ToitFileType;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ast.PsiAstHelper;
import org.toitlang.intellij.psi.ast.ToitElement;
import org.toitlang.intellij.psi.ast.ToitReferenceIdentifier;
import org.toitlang.intellij.psi.reference.ToitReference;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ToitReferenceNameIndex extends ScalarIndexExtension<String> {
  public static final ID<String, Void> INDEX_NAME = ID.create("toit-ref-names");
  private static final int VERSION = 1;

  @Override
  public int getVersion() {
    return VERSION;
  }

  @Override
  public @NotNull ID<String, Void> getName() {
    return INDEX_NAME;
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return new DefaultFileTypeSpecificInputFilter(ToitFileType.INSTANCE);
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public @NotNull DataIndexer<String, Void, FileContent> getIndexer() {
    return inputData -> {
      Map<String, Void> result = new HashMap<>();

      PsiRecursiveElementVisitor visitor = new PsiRecursiveElementVisitor() {
        @Override
        public void visitElement(@NotNull PsiElement element) {
          super.visitElement(element);
          if (element instanceof ToitReferenceIdentifier) {
            result.put((element).getText(), null);
          }
        }
      };

      inputData.getPsiFile().accept(visitor);
      return result;
    };
  }

  @Override
  public @NotNull KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }
}
