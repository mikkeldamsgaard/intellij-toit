package org.toitlang.intellij;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ToitLanguage extends Language {
    public static final ToitLanguage INSTANCE = new ToitLanguage();
    public ToitLanguage() {
        super("Toit");
    }
}
