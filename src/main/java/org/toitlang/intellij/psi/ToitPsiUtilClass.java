package org.toitlang.intellij.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;

public class ToitPsiUtilClass {
    public static String getName(ToitClassDeclaration classDeclaration) {
        ASTNode identifierNode = classDeclaration.getNode().findChildByType(ToitTypes.IDENTIFIER);
        if (identifierNode == null) return null;
        return identifierNode.getText();
    }

    public static String getSuper(ToitClassDeclaration classDeclaration) {
        ASTNode[] identifierNodes = classDeclaration.getNode().getChildren(TokenSet.create(ToitTypes.IDENTIFIER));
        if (identifierNodes.length < 2) return null;
        return identifierNodes[1].getText();
    }
}
