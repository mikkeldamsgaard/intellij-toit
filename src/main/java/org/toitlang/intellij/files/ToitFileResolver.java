package org.toitlang.intellij.files;

import com.intellij.openapi.vfs.VirtualFile;
import org.toitlang.intellij.psi.ToitFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.toitlang.intellij.psi.ast.ToitIdentifier.normalizeMinusUnderscore;

public class ToitFileResolver {
    public static ToitFile resolve(ToitFile source, int prefixDots, List<String> paths) {
        String basePath = "";
        if (paths.size() > 1) {
            basePath = String.join(File.separator, paths.subList(0, paths.size()-1));
        }
        String lastPath = paths.get(paths.size() - 1);
        String lastPathToitFile = String.format("%s.toit", lastPath);
        List<FileInfo> filesToFind = Arrays.asList(
                new FileInfo(basePath, lastPathToitFile),
                new FileInfo(String.format("%s%s%s", basePath, File.separator, lastPath), lastPathToitFile));

        ToitFile psiFile;
        if (prefixDots == 0) {
            psiFile = ToitSdkFiles.findLibraryFile(source.getProject(), filesToFind);
            if (psiFile == null) {
                psiFile = ToitPackageHandler.findPackageSourceFile(source, paths);
            }
        } else {
            psiFile = ToitProjectFiles.findProjectFile(source, prefixDots, filesToFind);
        }

        return psiFile;
    }

    static VirtualFile findRelativeIgnoreUnderscoreMinus(VirtualFile root, String path, String file) {
        String normalizedFile = normalizeMinusUnderscore(file);
        var dir = root.findFileByRelativePath(path);
        if (dir == null)
            return null;
        for (VirtualFile child : dir.getChildren()) {
            if (normalizeMinusUnderscore(child.getName()).equals(normalizedFile)) return child;
        }
        return null;
    }

}
