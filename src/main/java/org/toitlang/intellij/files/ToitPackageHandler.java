package org.toitlang.intellij.files;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.ui.ToitNotifier;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import static org.toitlang.intellij.psi.ast.ToitIdentifier.normalizeMinusUnderscore;

public class ToitPackageHandler {
    private final static Logger LOG = Logger.getLogger(ToitPackageHandler.class.getSimpleName());
    private final static ObjectMapper mapper = new ObjectMapper(new YAMLFactory().configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true));

    public static ToitFile findPackageSourceFile(ToitFile toitFile, List<String> paths) {
        VirtualFile packageLockFile = findPackageLockFile(toitFile.getProject(), toitFile.getVirtualFile());
        if (packageLockFile == null) return null;

        try {
            PackageLock packageLock = mapper.readValue(packageLockFile.getInputStream(), PackageLock.class);

            String prefix = paths.get(0);
            prefix = normalizeMinusUnderscore(prefix);


            String packageId = packageLock.resolvePrefix(prefix);
            if (packageId == null) return null;

            PackageLock.PackageInfo packageInfo = packageLock.packages.get(packageId);
            if (packageInfo == null) return null;

            List<List<String>> pathsToFind;
            if (paths.size() == 1) {
                String packageName = packageInfo.getName();
                if (packageName == null) packageName = prefix;
                else packageName = normalizeMinusUnderscore(packageName);

                // importing just the prefix, should find the file "src/$(prefix).toit"
                pathsToFind = List.of(List.of(String.format("%s.toit", packageName)));
            } else {
                pathsToFind = ToitFileResolver.constructSearchPaths(paths.subList(1, paths.size()));
            }

            String pckPath = getPackageSourcePath(packageInfo);
            VirtualFile searchRoot = getPackageSearchRoot(packageLockFile, packageInfo);
            var vf = ToitFileResolver.findRelativeIgnoreUnderscoreMinus(searchRoot, pckPath, pathsToFind);
            return toitFile.findProjectToitFile(vf);
        } catch (ProcessCanceledException e) {
            // Ignore
        } catch (Exception e) {
            // Ignore
            LOG.severe("Failed to parse lock file: " + e.getMessage());
        }

        return null;
    }

    public static VirtualFile listVariantsForPackage(ToitFile toitFile, List<String> path) {
        VirtualFile packageLockFile = findPackageLockFile(toitFile.getProject(), toitFile.getVirtualFile());
        if (packageLockFile == null) return null;

        try {
            PackageLock packageLock = mapper.readValue(packageLockFile.getInputStream(), PackageLock.class);
            var package_ = packageLock.resolvePrefix(normalizeMinusUnderscore(path.get(0)));
            if (package_ == null) return null;
            var packageInfo = packageLock.getPackages().get(package_);
            if (packageInfo == null) return null;

            String pckPath = getPackageSourcePath(packageInfo);
            VirtualFile searchRoot = getPackageSearchRoot(packageLockFile, packageInfo);
            return searchRoot.findFileByRelativePath(pckPath + String.join(File.separator, path.subList(1, path.size())));
        } catch (ProcessCanceledException e) {
            // Ignore
        } catch (Exception e) {
            // Ignore
            LOG.severe("Failed to parse lock file: " + e.getMessage());
        }

        return null;
    }

    private static String getPackageSourcePath(PackageLock.PackageInfo packageInfo) {
        if (packageInfo.url != null) {
            return String.format(".packages/%s/%s/src/", packageInfo.url, packageInfo.version);
        } else {
            return "src/";
        }
    }

    private static VirtualFile getPackageSearchRoot(VirtualFile packageLockFile, PackageLock.PackageInfo packageInfo) {
        if (packageInfo.getUrl() != null) {
            return packageLockFile.getParent();
        } else {
            if (new File(packageInfo.getPath()).isAbsolute()) {
                return packageLockFile.getFileSystem().findFileByPath(packageInfo.getPath());
            } else {
                return packageLockFile.getParent().findFileByRelativePath(packageInfo.getPath());
            }
        }
    }

    public static Collection<String> listPrefixes(ToitFile toitFile) {
        List<String> result = new ArrayList<>();
        VirtualFile packageLockFile = findPackageLockFile(toitFile.getProject(), toitFile.getVirtualFile());
        if (packageLockFile == null) return result;
        try {
            PackageLock packageLock = mapper.readValue(packageLockFile.getInputStream(), PackageLock.class);
            result.addAll(packageLock.getNormalizedPrefixes().keySet());
        } catch (Exception e) {
            // Ignore
        }
        return result;
    }


    private static VirtualFile findPackageLockFile(@NotNull Project project, VirtualFile virtualFile) {
        if (virtualFile == null) return null;
        virtualFile = virtualFile.getParent();
        if (virtualFile == null) return null;
        VirtualFile packageFile = virtualFile.findFileByRelativePath("package.yaml");
        VirtualFile lockFile = virtualFile.findFileByRelativePath("package.lock");

        if (packageFile != null) {
            if (lockFile == null) {
                if (!packageFile.getPath().contains(".packages"))
                    ToitNotifier.notifyPackageLockFileMissing(project, packageFile);
            }
            else if (packageFile.getTimeStamp() > lockFile.getTimeStamp() + 2500)
                ToitNotifier.notifyStaleLockFile(project, packageFile);
        }
        if (lockFile != null) return lockFile;
        return findPackageLockFile(project, virtualFile);
    }

    @Data
    @NoArgsConstructor
    public static class PackageLock {
        Map<String, String> prefixes;
        Map<String, PackageInfo> packages;
        String sdk;

        transient Map<String,String> normalizedPrefixes;
        @Data
        @NoArgsConstructor
        public static class PackageInfo {
            String path;
            String url;
            String version;
            String hash;
            String name;
            Map<String, String> prefixes;
        }

        public String resolvePrefix(String normalizedPrefix) { // assumes prefix is already normalized
            return getNormalizedPrefixes().get(normalizedPrefix);
        }

        public Map<String, String> getNormalizedPrefixes() {
            if (normalizedPrefixes == null) {
                normalizedPrefixes = new HashMap<>();
                prefixes.keySet().forEach(k -> normalizedPrefixes.put(normalizeMinusUnderscore(k), prefixes.get(k)));
            }
            return normalizedPrefixes;
        }
    }
}
