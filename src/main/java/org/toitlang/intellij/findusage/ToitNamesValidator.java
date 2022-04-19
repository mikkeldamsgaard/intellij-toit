package org.toitlang.intellij.findusage;

import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ToitNamesValidator implements NamesValidator {
    private final static Set<String> keywords = new HashSet<>(Arrays.asList("as", "abstract", "assert",
            "break", "class", "continue",
            "else", "false", "finally",
            "for", "if", "import",
            "export", "null", "return",
            "static", "true", "try",
            "while", "or", "and", "not"));

    @Override
    public boolean isKeyword(@NotNull String name, Project project) {
        return keywords.contains(name);
    }

    @Override
    public boolean isIdentifier(@NotNull String name, Project project) {
        return name.matches("[_A-Za-z][_A-Za-z0-9-]+");
    }
}
