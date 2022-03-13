package org.toitlang.intellij.ui;


import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.ToitIcons;
import org.toitlang.intellij.highlighting.ToitSyntaxHighlighter;

import javax.swing.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ToitColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Keyword", ToitSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor("Strings", ToitSyntaxHighlighter.STRING),
            new AttributesDescriptor("Comments", ToitSyntaxHighlighter.COMMENT),
            new AttributesDescriptor("Numbers", ToitSyntaxHighlighter.NUMBER),
            new AttributesDescriptor("Function declaration", ToitSyntaxHighlighter.FUNCTION_DECLARATION),
            new AttributesDescriptor("Local variable", ToitSyntaxHighlighter.LOCAL_VARIABLE),
            new AttributesDescriptor("Instance field", ToitSyntaxHighlighter.INSTANCE_FIELD),
            new AttributesDescriptor("Global variable", ToitSyntaxHighlighter.GLOBAL_VARIABLE),
            new AttributesDescriptor("Constant", ToitSyntaxHighlighter.CONSTANT),
            new AttributesDescriptor("Class name", ToitSyntaxHighlighter.CLASS_NAME),
            new AttributesDescriptor("Interface name", ToitSyntaxHighlighter.INTERFACE_NAME),
            new AttributesDescriptor("Type reference", ToitSyntaxHighlighter.CLASS_REFERENCE),

            new AttributesDescriptor("Bad value", ToitSyntaxHighlighter.BAD_CHARACTER)
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return ToitIcons.FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new ToitSyntaxHighlighter();
    }

    @SneakyThrows
    @NotNull
    @Override
    public String getDemoText() {
        return new String(getClass().getResourceAsStream("/color_example.toit").readAllBytes(),
                StandardCharsets.UTF_8);
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return Map.of("Constant", ToitSyntaxHighlighter.CONSTANT,
                "GlobalVariable", ToitSyntaxHighlighter.GLOBAL_VARIABLE,
                "FunctionDeclaration", ToitSyntaxHighlighter.FUNCTION_DECLARATION,
                "Keyword", ToitSyntaxHighlighter.KEYWORD,
                "InterfaceName", ToitSyntaxHighlighter.INTERFACE_NAME,
                "ClassName", ToitSyntaxHighlighter.CLASS_NAME,
                "InstanceField", ToitSyntaxHighlighter.INSTANCE_FIELD,
                "Type", ToitSyntaxHighlighter.CLASS_REFERENCE,
                "MethodDeclaration", ToitSyntaxHighlighter.FUNCTION_DECLARATION,
                "LocalVariable", ToitSyntaxHighlighter.LOCAL_VARIABLE
        );
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Toit";
    }
}
