package org.toitlang.intellij.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

public class ToitApplicationSettings implements Configurable {
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Toit";
    }

    TextFieldWithBrowseButton sdkPathDialog;

    @Override
    public @Nullable JComponent createComponent() {
        var sdkPath = ToitPersistentStateComponent.getInstance().getState().getSdkPath();

        sdkPathDialog = new TextFieldWithBrowseButton();
        sdkPathDialog.setText(sdkPath != null?sdkPath:"");
        sdkPathDialog.addBrowseFolderListener(new TextBrowseFolderListener(new FileChooserDescriptor(false,true,false,false,false,false)));
        return FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Toit sdk dir: "), sdkPathDialog)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    @Override
    public void disposeUIResources() {
        sdkPathDialog = null;
    }

    @Override
    public boolean isModified() {
        String sdkPath = ToitPersistentStateComponent.getInstance().getState().getSdkPath();
        if (sdkPath == null) return true;
        return !sdkPath.equals(this.sdkPathDialog.getText());
    }

    @Override
    public void apply() throws ConfigurationException {
        var newPath = sdkPathDialog.getText();
        if (isSdkValid(newPath)) {
            ToitPersistentStateComponent.getInstance().getState().setSdkPath(newPath);
        } else {
            throw new ConfigurationException("Invalid sdk dir");
        }
    }

    public static boolean isSdkValid(String sdkRoot) {
        return new File(sdkRoot+"/core/numbers.toit").exists();
    }



    @State(
            name = "org.toitlang.intellij",
            storages = @Storage("ApplicationSettings.xml")
    )
    public static class ToitPersistentStateComponent implements PersistentStateComponent<ToitApplicationsSettingsState> {
        public static ToitPersistentStateComponent getInstance() {
            return ApplicationManager.getApplication().getService(ToitPersistentStateComponent.class);
        }

        private ToitApplicationsSettingsState state = new ToitApplicationsSettingsState();

        @NotNull
        @Override
        public ToitApplicationsSettingsState getState() {
            return state;
        }

        @Override
        public void loadState(@NotNull ToitApplicationsSettingsState state) {
            this.state = state;
        }

    }

    public static class ToitApplicationsSettingsState {
        private String sdkPath = null;

        public ToitApplicationsSettingsState() {
        }

        public String getSdkPath() {
            return sdkPath;
        }

        public void setSdkPath(String sdkPath) {
            this.sdkPath = sdkPath;
        }
    }
}
