package org.toitlang.intellij.ui;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class ToitNotifier  {
    public static void notifyMissingSDK(Project project) {
        NotificationGroupManager.getInstance().getNotificationGroup("Toit Severe Group")
                .createNotification("Configuration error", "Toit SDK not found, please setup the SDK in settings", NotificationType.ERROR)
                .notify(project);
    }

    public static void notifyPackageLockFileMissing(@NotNull Project project, VirtualFile packageFile) {
        NotificationGroupManager.getInstance().getNotificationGroup("Toit Severe Group")
                .createNotification("Package issue","Found package.yaml file with no lock file. Please run package manager. "+packageFile.getPresentableUrl(), NotificationType.WARNING)
                .notify(project);
    }

    public static void notifyStaleLockFile(Project project, VirtualFile packageFile) {
        NotificationGroupManager.getInstance().getNotificationGroup("Toit Severe Group")
                .createNotification("Package issue","Found package.yaml with stale lock file. Please run package manager. "+packageFile.getPresentableUrl(), NotificationType.WARNING)
                .notify(project);
    }
}
