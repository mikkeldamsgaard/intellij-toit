package org.toitlang.intellij.files;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.util.indexing.IndexableSetContributor;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.model.IToitPrimaryLanguageElement;
import org.toitlang.intellij.ui.ToitApplicationSettings;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.ui.ToitNotifier;
import org.toitlang.intellij.psi.scope.ToitScope;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ToitSdkFiles extends IndexableSetContributor {
    private final static Map<Project, ToitScope> coreMap = new WeakHashMap<>();

    public static VirtualFile getSdkRoot(@NotNull Project project) {
        String sdkRoot = ToitApplicationSettings.ToitPersistentStateComponent.getInstance().getState().getSdkPath();
        if (sdkRoot == null || !ToitApplicationSettings.isSdkValid(sdkRoot)) {
            return null;
        }
        return LocalFileSystem.getInstance().findFileByIoFile(new File(sdkRoot));
    }

    public static ToitFile findLibraryFile(@NotNull Project project, List<String> filesToFind) {
        var root = getSdkRoot(project);
        if (root == null) return null;
        for (String path : filesToFind) {
            var f = root.findFileByRelativePath(path);
            if (f != null) return (ToitFile) PsiManager.getInstance(project).findFile(f);
        }
        return null;
    }

    public static ToitScope coreClosure(Project project) {
        var cache = coreMap.get(project);
        if (cache == null) {
            var root = getSdkRoot(project);
            if (root == null) return new ToitScope();

            Map<String, PsiElement> coreElements = new HashMap<>();

            VirtualFile core = root.findFileByRelativePath("core");
            if (core != null) {
                var psiM = PsiManager.getInstance(project);
                List<ToitFile> coreFiles = Arrays.stream(core.getChildren())
                        .map(psiM::findFile)
                        .filter(Objects::nonNull)
                        .map(ToitFile.class::cast)
                        .collect(Collectors.toList());

                for (ToitFile toitFile : coreFiles) {
                    coreElements.putAll(toitFile.getToitFileScope().getLocals());
                }
            }

            cache = new ToitScope(coreElements);

            coreMap.put(project,cache);
        }
        return cache;
    }

    public static boolean isLibraryFile(@NotNull Project project, VirtualFile virtualFile) {
        var sdkRoot = getSdkRoot(project);
        var cur = virtualFile;
        while (cur != null) {
            if (cur.equals(sdkRoot)) return true;
            cur=cur.getParent();
        }
        return false;
    }

    @Override
    public @NotNull Set<VirtualFile> getAdditionalProjectRootsToIndex(@NotNull Project project) {
        var root = getSdkRoot(project);
        if (root == null) {
            ToitNotifier.notifyMissingSDK(project);
            return Collections.emptySet();
        }
        return Collections.singleton(getSdkRoot(project));
    }

    @Override
    public @NotNull Set<VirtualFile> getAdditionalRootsToIndex() {
        return Collections.emptySet();
    }
}
