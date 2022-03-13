package org.toitlang.intellij.psi.scope;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.toitlang.intellij.files.ToitProjectFiles;
import org.toitlang.intellij.files.ToitSdkFiles;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.visitor.ToitVisitor;
import org.toitlang.intellij.utils.ToitScope;

import java.io.File;
import java.util.*;

public class ToitFileScopeCalculator extends ToitVisitor {
    ToitScope scope;
    Map<PsiElement, Set<String>> missingShows = new HashMap<>();

    List<PsiElement> dependencies = new ArrayList<>();
    private final ToitFile file;

    public ToitFileScopeCalculator(ToitFile file) {
        this.file = file;
        scope = ToitSdkFiles.coreClosure(file.getProject()).derive();
        file.acceptChildren(this);
        dependencies.add(file);
    }

    public ToitScope getScope() {
        return scope;
    }

    @Override
    public void visit(ToitImportDeclaration toitImportDeclaration) {
        toitImportDeclaration.computeScope(this);
    }

    @Override
    public void visit(ToitStructure toitStructure) { scope.add(toitStructure.getName(), toitStructure); }

    @Override
    public void visit(ToitFunction toitFunction) { scope.add(toitFunction.getName(), toitFunction); }

    @Override
    public void visit(ToitVariableDeclaration toitVariableDeclaration) { scope.add(toitVariableDeclaration.getName(), toitVariableDeclaration); }

    public Object[] dependencies() {
        return dependencies.toArray(new PsiElement[0]);
    }

    public void addImport(int prefixDots, List<String> paths, List<String> shows, ToitIdentifier as, ToitImportDeclaration importDeclaration) {
        if (paths.isEmpty()) return;

        String path = String.join(File.separator, paths);
        String lastPath = paths.get(paths.size() - 1);
        List<String> filesToFind = Arrays.asList(
                String.format("%s.toit", path),
                String.format("%s%s%s.toit", path, File.separator, lastPath));

        ToitFile psiFile;
        if (prefixDots == 0) {
            psiFile = ToitSdkFiles.findLibraryFile(file.getProject(), filesToFind);
        } else {
            psiFile = ToitProjectFiles.findProjectFile(file, prefixDots, filesToFind);
        }
        if (psiFile == null) return;

        dependencies.add(psiFile);
        String prefix = "";
        if (as != null) {
            prefix = as + ".";
            scope.add(as.getName(), as);
        } else if (prefixDots == 0 && shows.isEmpty()) {
            scope.add(lastPath, psiFile);
            prefix = lastPath + ".";
        }

        if (shows.isEmpty()) {
            for (PsiNameIdentifierOwner elm : psiFile.childrenOfType(PsiNameIdentifierOwner.class)) {
                scope.add(prefix + elm.getName(), elm);
            }
        } else {
            calcExported(psiFile, scope, shows);
        }

        // Todo: add static fields
    }

    public static void calcExported(ToitFile psiFile, ToitScope scope, List<String> limitedSymbols) {
        // TODO: Handle export statements
        for (PsiNameIdentifierOwner elm : psiFile.childrenOfType(PsiNameIdentifierOwner.class)) {
            if (limitedSymbols == null || limitedSymbols.contains(elm.getName()))
                scope.add(elm.getName(),elm);
        }
    }

}
