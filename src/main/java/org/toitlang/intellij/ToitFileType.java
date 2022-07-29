package org.toitlang.intellij;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ToitFileType extends LanguageFileType {
    public static final ToitFileType INSTANCE = new ToitFileType();

    public ToitFileType() {
        super(ToitLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "Toit File";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "Toit language source code file";
    }

    @Override
    public @NlsSafe @NotNull String getDefaultExtension() {
        return "toit";
    }

    @Override
    public @Nullable Icon getIcon() {
        return ToitIcons.FILE;
    }


}
