package org.toitlang.intellij.ui;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToitNotifier  {
    public static void notifyMissingSDK(Project project) {
        NotificationGroupManager.getInstance().getNotificationGroup("Toit Severe Group")
                .createNotification("Toit SDK not found, please setup the SDK in settings", NotificationType.ERROR)
                .notify(project);
    }
}
