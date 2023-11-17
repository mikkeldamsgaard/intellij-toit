package org.toitlang.intellij.files;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.toitlang.intellij.psi.ast.ToitIdentifier.normalizeMinusUnderscore;

public class ToitFileResolver {
    public static ToitFile resolve(ToitFile source, int prefixDots, List<String> paths) {

//        String lastPath = paths.get(paths.size() - 1);
//        String lastPathToitFile = String.format("%s.toit", lastPath);
//        List<FileInfo> filesToFind = Arrays.asList(
//                new FileInfo(basePath, lastPathToitFile),
//                new FileInfo(String.format("%s%s%s", basePath, File.separator, lastPath), lastPathToitFile));
//
        ToitFile psiFile;
        if (prefixDots == 0) {
            psiFile = ToitSdkFiles.findLibraryFile(source.getProject(), paths);
            if (psiFile == null) {
                psiFile = ToitPackageHandler.findPackageSourceFile(source, paths);
            }
        } else {
            psiFile = ToitProjectFiles.findProjectFile(source, prefixDots, paths);
        }

        return psiFile;
    }

    static VirtualFile findRelativeIgnoreUnderscoreMinus(VirtualFile root, String basePath, List<List<String>> paths) {
        var dir = root.findFileByRelativePath(basePath);
        if (dir == null) return null;
        for (List<String> path : paths) {
            var vf = findRelativeIgnoreUnderscoreMinus(dir, path);
            if (vf != null) return vf;
        }
        return null;
    }


    private static VirtualFile findRelativeIgnoreUnderscoreMinus(VirtualFile cur, List<String> path) {
        if (path.isEmpty()) return cur;
        String next = path.get(0);
        String normalizedNext = normalizeMinusUnderscore(next);
        for (VirtualFile child : cur.getChildren()) {
            if (normalizeMinusUnderscore(child.getName()).equals(normalizedNext)) return findRelativeIgnoreUnderscoreMinus(child, path.subList(1, path.size()));
        }
        return null;
    }

    @NotNull
    static List<List<String>> constructSearchPaths(List<String> paths) {
        // import x.y, should find either 'x/y.toit' or 'x/y/y.toit'
        List<List<String>> pathsToFind;
        List<String> head = paths.subList(0, paths.size() - 1);
        String last = paths.get(paths.size() - 1);

        List<String> first = new ArrayList<>(head);
        first.add(String.format("%s.toit", last));

        List<String> second = new ArrayList<>(head);
        second.add(last);
        second.add(String.format("%s.toit", last));

        pathsToFind = List.of(first, second);
        return pathsToFind;
    }
}
