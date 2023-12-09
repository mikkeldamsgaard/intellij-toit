// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTypes;

public abstract class ToitIdentifier extends ToitElementBase {
    private String name;

    public ToitIdentifier(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void subtreeChanged() {
        name = null;
    }

    private final static TokenSet IDENTIFIER_SET = TokenSet.create(ToitTypes.IDENTIFIER);

    public @NotNull PsiElement getIdentifierToken() {
        ASTNode[] children = getNode().getChildren(IDENTIFIER_SET);
        return children[children.length - 1].getPsi();
    }

    @Override
    public @NotNull String getName() {
        if (name == null) name = getIdentifierToken().getText();
        if (name == null) name = "__UnknownToit__";
        name = normalizeMinusUnderscore(name);
        return name.trim();
    }

    @Override
    public String getText() {
        String t = super.getText();
        if (t == null) return t;
        return normalizeMinusUnderscore(t.trim());
    }

    public boolean isImport() {
        return getNode().getElementType() == ToitTypes.IMPORT_IDENTIFIER;
    }

    public boolean isShow() {
        return getNode().getElementType() == ToitTypes.IMPORT_SHOW_IDENTIFIER;
    }

    public boolean isImportAs() {
        return getNode().getElementType() == ToitTypes.IMPORT_AS_IDENTIFIER;
    }

    public boolean isFunctionName() {
        return getNode().getElementType() == ToitTypes.FUNCTION_IDENTIFIER;
    }

    public boolean isVariableName() {
        return getNode().getElementType() == ToitTypes.VARIABLE_IDENTIFIER;
    }

    public boolean isStructureName() {
        return getNode().getElementType() == ToitTypes.STRUCTURE_IDENTIFIER;
    }

    public boolean isTypeName() {
        return getNode().getElementType() == ToitTypes.TYPE_IDENTIFIER;
    }

    public boolean isReference() {
        return getNode().getElementType() == ToitTypes.REFERENCE_IDENTIFIER;
    }

    public boolean isFactoryName() {
        return getNode().getElementType() == ToitTypes.FACTORY_IDENTIFIER;
    }

    public abstract PsiElement setName(String name);

    protected PsiElement replaceIdentifierToken(ToitIdentifier newRef) {
        return getIdentifierToken().replace(newRef.getIdentifierToken());
    }

    public static String normalizeMinusUnderscore(String s) {
        try {
            int prefix_ = 0;
            int suffix_ = s.length();
            while (prefix_ < s.length() && s.charAt(prefix_) == '_') prefix_++;
            while (suffix_ > 0 && s.charAt(suffix_ - 1) == '_') suffix_--;
            if (suffix_ < prefix_) return s; // special case, all underscores
            return String.format("%s%s%s",
                    s.substring(0, prefix_),
                    s.substring(prefix_, suffix_).replaceAll("_", "-"),
                    s.substring(suffix_));
        } catch (Exception e) {
            throw new RuntimeException("Failed to converted "+s, e);
        }

    }

}