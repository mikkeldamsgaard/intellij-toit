package org.toitlang.intellij.psi.scope;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.toitlang.intellij.files.ToitPackageHandler;
import org.toitlang.intellij.files.ToitProjectFiles;
import org.toitlang.intellij.files.ToitSdkFiles;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.visitor.ToitVisitor;
import org.toitlang.intellij.utils.ToitScope;

import java.io.File;
import java.util.*;

public class ToitFileScopeCalculator extends ToitVisitor {
    ToitFileScope toitFileScope;
    private Set<ToitFile> visitedFiles;
    List<PsiElement> dependencies = new ArrayList<>();

    public ToitFileScopeCalculator(ToitFile file) {
        this(file, new HashSet<>());
    }

    public ToitFileScopeCalculator(ToitFile file, Set<ToitFile> visitedFiles) {
        toitFileScope = new ToitFileScope(file);
        this.visitedFiles = visitedFiles;
        file.acceptChildren(this);
        dependencies.add(file);
    }

    public ToitFileScope getToitFileScope() {
        return toitFileScope;
    }

    @Override
    public void visit(ToitImportDeclaration toitImportDeclaration) {
        toitImportDeclaration.computeScope(this);
    }

    @Override
    public void visit(ToitStructure toitStructure) { toitFileScope.locals.add(toitStructure.getName(), toitStructure); }

    @Override
    public void visit(ToitFunction toitFunction) { toitFileScope.locals.add(toitFunction.getName(), toitFunction); }

    @Override
    public void visit(ToitVariableDeclaration toitVariableDeclaration) { toitFileScope.locals.add(toitVariableDeclaration.getName(), toitVariableDeclaration); }

    @Override
    public void visit(ToitExportDeclaration toitExportDeclaration) {
        if (toitExportDeclaration.isStar()) toitFileScope.exported.add("*");
        else toitFileScope.exported.addAll(toitExportDeclaration.getExportedNames());
    }

    public Object[] dependencies() {
        return dependencies.toArray(new PsiElement[0]);
    }

    public void addImport(int prefixDots, List<String> paths, List<String> shows, ToitIdentifier as) {
        if (paths.isEmpty()) return;

        String path = String.join(File.separator, paths);
        String lastPath = paths.get(paths.size() - 1);
        List<String> filesToFind = Arrays.asList(
                String.format("%s.toit", path),
                String.format("%s%s%s.toit", path, File.separator, lastPath));

        ToitFile psiFile;
        if (prefixDots == 0) {
            psiFile = ToitSdkFiles.findLibraryFile(toitFileScope.getToitFile().getProject(), filesToFind);
            if (psiFile == null) {
                psiFile = ToitPackageHandler.findPackageSourceFile(toitFileScope.getToitFile(), paths);
            }
        } else {
            psiFile = ToitProjectFiles.findProjectFile(toitFileScope.getToitFile(), prefixDots, filesToFind);
        }
        if (psiFile == null || visitedFiles.contains(psiFile)) return;
        visitedFiles.add(psiFile);

        dependencies.add(psiFile);

        toitFileScope.importedLibPackageFiles.put("$"+prefixDots+"$"+String.join(".",paths),psiFile);


        var importedFileScope = new ToitFileScopeCalculator(psiFile,visitedFiles).getToitFileScope();
        System.out.println(lastPath);
        if (as != null) {
            toitFileScope.importedWithPrefix.put(as.getName(), importedFileScope);
        } else if (!shows.isEmpty()) {
            var exportedScope = importedFileScope.getExportedScope();
            for (String show : shows) {
                var showElement = exportedScope.resolve(show);
                toitFileScope.locals.add(show, showElement);
            }
        } else if (prefixDots == 0) {
            toitFileScope.importedWithPrefix.put(lastPath, importedFileScope);
        } else {
            toitFileScope.importedLocals.add(importedFileScope);
        }
    }
}
