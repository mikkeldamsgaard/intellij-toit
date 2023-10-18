package org.toitlang.intellij.files;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import org.toitlang.intellij.psi.ToitFile;

import java.io.File;
import java.util.List;

public class ToitProjectFiles {
    public static ToitFile findProjectFile(ToitFile file, int prefixDots, List<String> filesToFind) {
        if (file == null || file.getVirtualFile() == null || file.getVirtualFile().getParent() == null) return null;

        StringBuilder prefix = new StringBuilder();
        for (int i = 1; i < prefixDots; i++) {
            prefix.append("..").append(File.separator);
        }
        VirtualFile dir = file.getVirtualFile().getParent();
        for (String fileName : filesToFind) {
            var vf = ToitFileResolver.findRelativeIgnoreUnderscoreMinus(dir, prefix.toString(), fileName);
            if (vf != null) return (ToitFile) PsiManager.getInstance(file.getProject()).findFile(vf);
        }
        return null;
    }
}
