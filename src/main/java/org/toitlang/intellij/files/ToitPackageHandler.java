package org.toitlang.intellij.files;

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

public class ToitPackageHandler {
    private final static Logger LOG = Logger.getLogger(ToitPackageHandler.class.getSimpleName());
    private final static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static ToitFile findPackageSourceFile(ToitFile toitFile, List<String> paths) {
        VirtualFile packageLockFile = findPackageLockFile(toitFile.getProject(), toitFile.getVirtualFile());
        if (packageLockFile == null) return null;

        try {
            PackageLock packageLock = mapper.readValue(packageLockFile.getInputStream(), PackageLock.class);

            String prefix = paths.get(0);
            List<String> filesToFind;
            if (paths.size() == 1) {
                filesToFind = List.of(String.format("%s.toit", prefix));
            } else {
                String path = String.join(File.separator, paths.subList(1, paths.size()));
                filesToFind = Arrays.asList(
                        String.format("%s.toit", path),
                        String.format("%s%s%s.toit", path, File.separator, paths.get(paths.size() - 1)));
            }

            if (!packageLock.prefixes.containsKey(prefix)) return null;

            String package_ = packageLock.prefixes.get(prefix);
            PackageLock.PackageInfo packageInfo = packageLock.packages.get(package_);

            if (packageInfo == null) return null;

            String pckPath = getPacketSourcePath(packageInfo);
            VirtualFile projectRoot = packageLockFile.getParent();
            for (String file : filesToFind) {
                var f = projectRoot.findFileByRelativePath(pckPath + file);
                if (f != null) return (ToitFile) PsiManager.getInstance(toitFile.getProject()).findFile(f);
            }
        } catch (ProcessCanceledException e) {
            // Ignore
        } catch (Exception e) {
            // Ignore
            LOG.severe("Failed to parse lock file: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static VirtualFile listVariantsForPackage(ToitFile toitFile, List<String> path) {
        VirtualFile packageLockFile = findPackageLockFile(toitFile.getProject(), toitFile.getVirtualFile());
        if (packageLockFile == null) return null;

        try {
            PackageLock packageLock = mapper.readValue(packageLockFile.getInputStream(), PackageLock.class);
            var package_ = packageLock.getPrefixes().get(path.get(0));
            if (package_ == null) return null;
            var packageInfo = packageLock.getPackages().get(package_);
            if (packageInfo == null) return null;

            String pckPath = getPacketSourcePath(packageInfo);
            VirtualFile projectRoot = packageLockFile.getParent();
            return projectRoot.findFileByRelativePath(pckPath+String.join(File.separator, path.subList(1, path.size())));
        } catch (ProcessCanceledException e) {
            // Ignore
        } catch (Exception e) {
            // Ignore
            LOG.severe("Failed to parse lock file: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private static String getPacketSourcePath(PackageLock.PackageInfo packageInfo) {
        String pckPath;
        if (packageInfo.url != null) {
            pckPath = String.format(".packages/%s/%s/src/", packageInfo.url, packageInfo.version);
        } else {
            pckPath = String.format("%s/src/", packageInfo.path);
        }
        return pckPath;
    }

    public static Collection<String> listPrefixes(ToitFile toitFile) {
        List<String> result = new ArrayList<>();
        VirtualFile packageLockFile = findPackageLockFile(toitFile.getProject(), toitFile.getVirtualFile());
        if (packageLockFile == null) return result;
        try {
            PackageLock packageLock = mapper.readValue(packageLockFile.getInputStream(), PackageLock.class);
            result.addAll(packageLock.getPrefixes().keySet());
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
            if (lockFile == null) ToitNotifier.notifyPackageLockFileMissing(project, packageFile);
            else if (packageFile.getTimeStamp() > lockFile.getTimeStamp())
                ToitNotifier.notityStaleLockFile(project, packageFile);
        }
        if (lockFile != null) return lockFile;
        return findPackageLockFile(project, virtualFile);
    }

    @Data
    @NoArgsConstructor
    public static class PackageLock {
        Map<String, String> prefixes;
        Map<String, PackageInfo> packages;

        @Data
        @NoArgsConstructor
        public static class PackageInfo {
            String path;
            String url;
            String version;
            String hash;
            Map<String, String> prefixes;
        }
    }
}
