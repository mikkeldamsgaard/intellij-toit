package org.toitlang.intellij;

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
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ToitApplicationSettings implements Configurable {
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Toit";
    }

    TextFieldWithBrowseButton sdkPath;

    @Override
    public @Nullable JComponent createComponent() {
        sdkPath = new TextFieldWithBrowseButton();
        sdkPath.addBrowseFolderListener(new TextBrowseFolderListener(new FileChooserDescriptor(false,true,false,false,false,false)));
        return FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Toit sdk dir: "), sdkPath)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    @Override
    public void disposeUIResources() {
        sdkPath = null;
    }

    @Override
    public boolean isModified() {
        return !SettingsState.getInstance().sdkPath.equals(sdkPath.getText());
    }

    @Override
    public void apply() throws ConfigurationException {
        SettingsState.getInstance().sdkPath = sdkPath.getText();
    }


    @State(
            name = "org.toitlang.intellij",
            storages = @Storage("SdkSettingsPlugin.xml")
    )
    public static class SettingsState implements PersistentStateComponent<SettingsState> {

        public String sdkPath = "/Users/mikkel/proj/application/esp32/common/esp-toit/toit/lib";

        public static SettingsState getInstance() {
            return ApplicationManager.getApplication().getService(SettingsState.class);
        }

        @Nullable
        @Override
        public SettingsState getState() {
            return this;
        }

        @Override
        public void loadState(@NotNull SettingsState state) {
            XmlSerializerUtil.copyBean(state, this);
        }

    }
}
