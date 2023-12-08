package org.toitlang.intellij.files;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.indexing.IndexableSetContributor;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.model.IToitPrimaryLanguageElement;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.scope.ToitScope;
import org.toitlang.intellij.ui.ToitApplicationSettings;
import org.toitlang.intellij.ui.ToitNotifier;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ToitSdkFiles extends IndexableSetContributor {
    public static VirtualFile getSdkRoot(@NotNull Project project) {
        String sdkRoot = ToitApplicationSettings.ToitPersistentStateComponent.getInstance().getState().getSdkPath();
        if (sdkRoot == null || !ToitApplicationSettings.isSdkValid(sdkRoot)) {
            return null;
        }
        return LocalFileSystem.getInstance().findFileByIoFile(new File(sdkRoot + "/lib"));
    }

    public static ToitFile findLibraryFile(@NotNull Project project, List<String> path) {
        var root = getSdkRoot(project);
        if (root == null) return null;
        var pathsToFind = ToitFileResolver.constructSearchPaths(path);
        var vf = ToitFileResolver.findRelativeIgnoreUnderscoreMinus(root, "", pathsToFind);
        if (vf == null) return null;
        return (ToitFile) PsiManager.getInstance(project).findFile(vf);
    }


    private final static Map<Project, CachedValue<ToitScope>> cachedScopes = new HashMap<>();

    public static ToitScope getCoreScope(Project project) {
        var cache = cachedScopes.get(project);
        if (cache == null) {
            cache = buildCache(project);
            cachedScopes.put(project, cache);
        }

        return cache.getValue();
    }

    private static CachedValue<ToitScope> buildCache(Project project) {
        return CachedValuesManager.getManager((project)).createCachedValue(
                () -> {
                    var root = getSdkRoot(project);
                    if (root == null) return new CachedValueProvider.Result<>(ToitScope.ROOT.sub("core"), ModificationTracker.NEVER_CHANGED);


                    List<PsiElement> dependencies = new ArrayList<>(2000);
                    Map<String, List<? extends PsiElement>> coreElements = new HashMap<>();

                    VirtualFile core = root.findFileByRelativePath("core");
                    if (core != null) {
                        var psiM = PsiManager.getInstance(project);
                        List<ToitFile> coreFiles = Arrays.stream(core.getChildren())
                                .map(psiM::findFile)
                                .filter(Objects::nonNull)
                                .map(ToitFile.class::cast)
                                .collect(Collectors.toList());

                        for (ToitFile toitFile : coreFiles) {
                            Map<String, List<IToitPrimaryLanguageElement>> locals = toitFile.getToitFileScope().getLocals();
                            coreElements.putAll(locals);

                            dependencies.add(toitFile);
                            locals.values().forEach(dependencies::addAll);
                        }
                    }

                    var scope = ToitScope.ROOT.subFromMap("core", coreElements);
                    return new CachedValueProvider.Result<>(scope, (Object[]) dependencies.toArray(new PsiElement[0]));
                }
        );
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
