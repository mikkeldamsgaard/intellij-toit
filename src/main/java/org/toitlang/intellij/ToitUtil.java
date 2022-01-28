package org.toitlang.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitClassDeclaration;
import org.toitlang.intellij.psi.ToitFile;

import java.io.File;
import java.util.*;

public class ToitUtil {
    // Find all classes
    public static List<ToitClassDeclaration> findClasses(Project project) {
        List<ToitClassDeclaration> result = new ArrayList<>();
        Collection<VirtualFile> virtualFiles =
                FileTypeIndex.getFiles(ToitFileType.INSTANCE, GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            System.out.println("toitfile.virtualpath: "+virtualFile.getPath());
            ToitFile toitFile = (ToitFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (toitFile != null) {
                ToitClassDeclaration[] classDeclarations = PsiTreeUtil.getChildrenOfType(toitFile, ToitClassDeclaration.class);
                if (classDeclarations != null) {
                    Collections.addAll(result, classDeclarations);
                }
            }
        }
        return result;
    }

    /**
     * Attempts to collect any comment elements above the Simple key/value pair.
     */
    public static @NotNull String findDocumentationComment(ToitClassDeclaration property) {
        List<String> result = new LinkedList<>();
        PsiElement element = property.getPrevSibling();
        while (element instanceof PsiComment || element instanceof PsiWhiteSpace) {
            if (element instanceof PsiComment) {
                String commentText = element.getText().replaceFirst("[!# ]+", "");
                result.add(commentText);
            }
            element = element.getPrevSibling();
        }
        Collections.reverse(result);
        return String.join("\n ",result);
    }

}
