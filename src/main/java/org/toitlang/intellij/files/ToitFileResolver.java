package org.toitlang.intellij.files;

import com.intellij.openapi.project.Project;
import org.toitlang.intellij.psi.ToitFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ToitFileResolver {
    public static ToitFile resolve(ToitFile source, int prefixDots, List<String> paths) {
        String path = String.join(File.separator, paths);
        String lastPath = paths.get(paths.size() - 1);
        List<String> filesToFind = Arrays.asList(
                String.format("%s.toit", path),
                String.format("%s%s%s.toit", path, File.separator, lastPath));

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
}
