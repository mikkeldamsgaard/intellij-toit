package org.toitlang.intellij.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import org.toitlang.intellij.ToitFileType;
import org.toitlang.intellij.psi.ast.*;

public class ToitElementFactory {
    public static ToitNameableIdentifier createStructureIdentifier(Project project, String name) {
        final ToitFile file = createFile(project, String.format("class %s:", name));
        var tf = (ToitStructure) file.getFirstChild();
        return tf.getChildrenOfType(ToitNameableIdentifier.class).get(0);
    }

    public static ToitNameableIdentifier createFunctionIdentifier(Project project, String name) {
        final ToitFile file = createFile(project, name);
        var tf = (ToitFunction) file.getFirstChild();
        return (ToitNameableIdentifier) tf.getFirstChild();
    }

    public static ToitNameableIdentifier createVariableIdentifier(Project project, String name) {
        final ToitFile file = createFile(project, String.format("%s/x:=0", name));
        var vd = (ToitVariableDeclaration) file.getFirstChild();
        return (ToitNameableIdentifier) vd.getFirstChild();
    }

    public static ToitNameableIdentifier createImportAsIdentifier(Project project, String name) {
        final ToitFile file = createFile(project, String.format("import l as %s", name));
        var id = (ToitImportDeclaration) file.getFirstChild();
        return (ToitNameableIdentifier) id.getFirstChild().getNextSibling();
    }

    public static ToitReferenceIdentifier createExportIdentifier(Project project, String name) {
        final ToitFile file = createFile(project, String.format("export %s", name));
        var ed = (ToitExportDeclaration) file.getFirstChild();
        return ed.getLastChildOfType(ToitReferenceIdentifier.class);
    }

    public static ToitReferenceIdentifier createImportShowIdentifier(Project project, String name) {
        final ToitFile file = createFile(project, String.format("import l show %s", name));
        var id = (ToitImportDeclaration) file.getFirstChild();
        return (ToitReferenceIdentifier) id.getLastChild();
    }

    public static ToitReferenceIdentifier createImportIdentifier(Project project, String name) {
        final ToitFile file = createFile(project, String.format("import %s", name));
        var id = (ToitImportDeclaration) file.getFirstChild();
        return id.getLastChildOfType(ToitReferenceIdentifier.class);
    }

    public static ToitNameableIdentifier createFactoryIdentifier(Project project, String name) {
        final ToitFile file = createFile(project, String.format("class f:\n  constructor.%s:", name));
        var tf = (ToitStructure) file.getFirstChild();
        var f = (ToitFunction) tf.getFirstChild();
        return (ToitNameableIdentifier) f.getFirstChild().getNextSibling();
    }

    public static ToitNameableIdentifier createNamedParameterIdentifier(Project project, String name) {
        final ToitFile file = createFile(project, String.format("f --%s", name));
        var tf = (ToitFunction) file.getFirstChild();
        var tp = tf.getFirstChildOfType(ToitParameterName.class);
        assert tp != null;
        return tp.getFirstChildOfType(ToitNameableIdentifier.class);
    }

    public static ToitNameableIdentifier createSimpleParameterIdentifier(Project project, String name) {
        final ToitFile file = createFile(project, String.format("f %s", name));
        var tf = (ToitFunction) file.getFirstChild();
        return (ToitNameableIdentifier) tf.getFirstChild().getNextSibling().getNextSibling();
    }


    public static ToitReferenceIdentifier createReferenceIdentifier(Project project, String name) {
        final ToitFile file = createFile(project, String.format("x ::= %s", name));
        var tf = (ToitVariableDeclaration) file.getFirstChild();
        var te = (ToitTopLevelExpression) tf.getLastChild();
        var tp = (ToitPrimaryExpression) te.getFirstChild();
        return (ToitReferenceIdentifier) tp.getFirstChild();
    }

    public static ToitReferenceIdentifier createBreakContinueLabelIdentifier(Project project, String name) {
        final ToitFile file = createFile(project, String.format("f: break.%s", name));
        var tf = (ToitFunction) file.getFirstChild();
        var tb = (ToitBlock) tf.getFirstChild();
        var tbc = (ToitBreakContinue) tb.getFirstChild();
        return (ToitReferenceIdentifier) tbc.getFirstChild();
    }

    public static ToitReferenceIdentifier createNamedArgumentIdentifier(Project project, String name) {
        final ToitFile file = createFile(project, String.format("p := f --%s", name));
        var tf = (ToitVariableDeclaration) file.getFirstChild();
        var te = (ToitTopLevelExpression) tf.getLastChild();
        var tc = (ToitCallExpression) te.getFirstChild();
        var tn = tc.getFirstChildOfType(ToitNamedArgument.class);
        assert tn != null;
        return tn.getFirstChildOfType(ToitReferenceIdentifier.class);
    }


    public static ToitReferenceIdentifier createTypeIdentifier(Project project, String name) {
        final ToitFile file = createFile(project, String.format("x/%s ::= 0", name));
        var tf = (ToitVariableDeclaration) file.getFirstChild();
        var tt = (ToitType) tf.getChildren()[1];
        return (ToitReferenceIdentifier) tt.getFirstChild();
    }

    public static ToitFile createFile(Project project, String text) {
        String name = "dummy.simple";
        return (ToitFile) PsiFileFactory.getInstance(project).
                createFileFromText(name, ToitFileType.INSTANCE, text);
    }
}
