package org.toitlang.intellij.psi.scope;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.files.ToitFileResolver;
import org.toitlang.intellij.model.IToitPrimaryLanguageElement;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.*;

public class ToitFileScope {
    private final Map<String, ToitFile> imports = new HashMap<>();
    private final Map<String, IToitPrimaryLanguageElement> locals = new HashMap<>();
    private final Map<String, ToitFile> importedLibraries = new HashMap<>();
    private final Map<ToitFile, List<String>> shows = new HashMap<>();
    private final List<ToitFile> projectImports = new ArrayList<>();
    private final List<String> exports = new ArrayList<>();

    public ToitScope getToitScope() {
        Map<String, PsiElement> scope = new HashMap<>(locals);
        scope.putAll(imports);
        shows.forEach((k,v)->{
            Map<String, PsiElement> exported = k.getToitFileScope().getExportedScope(new HashSet<>());
            if (v.contains("*")) scope.putAll(exported);
            else {
                for (String shown : v) {
                    if (exported.containsKey(shown)) scope.put(shown, exported.get(shown));
                }
            }
        });
        projectImports.forEach(p -> scope.putAll(p.getToitFileScope().getExportedScope(new HashSet<>())));
        return ToitScope.fromMap(scope);
    }


    public ToitScope getExportedScope() {
        return ToitScope.fromMap(getExportedScope(new HashSet<>()));
    }

    public Map<String, IToitPrimaryLanguageElement> getLocals() {
        return locals;
    }

    public ToitFile getImportedLibrary(String name) {
        return importedLibraries.get(name);
    }

    private @NotNull  Map<String, PsiElement> getExportedScope(Set<ToitFileScope> seen) {
        Map<String, PsiElement> result = new HashMap<>();
        if (seen.contains(this)) return result; // Cycle detection. If export are cycled, it is an error
        seen.add(this);

        if (exports.isEmpty()) result.putAll(locals);
        else {
            for (ToitFile projectImport : projectImports) {
                Map<String, PsiElement> pExported =  projectImport.getToitFileScope().getExportedScope(seen);
                pExported.entrySet().stream()
                        .filter(e -> exports.contains("*") || exports.contains(e.getKey()))
                        .forEach(e -> result.put(e.getKey(),e.getValue()));
            }
            locals.entrySet().stream()
                    .filter(e -> exports.contains("*") || exports.contains(e.getKey()))
                    .forEach(e -> result.put(e.getKey(),e.getValue()));
        }
        return result;
    }

    public void build(ToitFile toitFile) {
        toitFile.acceptChildren(new ToitVisitor() {
            public void visit(ToitImportDeclaration toitImportDeclaration) {
                int prefixDots = toitImportDeclaration.getPrefixDots();
                List<String> paths = new ArrayList<>();
                List<String> shows = new ArrayList<>();
                ToitIdentifier as = null;
                for (ToitIdentifier toitIdentifier : toitImportDeclaration.childrenOfType(ToitIdentifier.class)) {
                    if (toitIdentifier.isImport()) paths.add(toitIdentifier.getName());
                    if (toitIdentifier.isShow()) shows.add(toitIdentifier.getName());
                    if (toitIdentifier.isImportAs()) as = toitIdentifier;
                }

                if (toitImportDeclaration.isShowStar()) shows.add("*");

                if (paths.isEmpty()) return;

                ToitFile importedFile = ToitFileResolver.resolve(toitFile, prefixDots, paths);

                if (importedFile == null) return;

                importedLibraries.put("$"+prefixDots+"$"+String.join(".",paths), importedFile);

                if (as != null) {
                    imports.put(as.getName(), importedFile);
                } else if (!shows.isEmpty()) {
                    shows.forEach(s -> ToitFileScope.this.shows.computeIfAbsent(importedFile, (k) -> new ArrayList<>()).add(s));
                } else if (prefixDots == 0) {
                    imports.put(paths.get(paths.size()-1), importedFile);
                } else {
                    projectImports.add(importedFile);
                }
            }

            @Override
            public void visit(ToitStructure toitStructure) { locals.put(toitStructure.getName(), toitStructure); }

            @Override
            public void visit(ToitFunction toitFunction) { locals.put(toitFunction.getName(), toitFunction); }

            @Override
            public void visit(ToitVariableDeclaration toitVariableDeclaration) { locals.put(toitVariableDeclaration.getName(), toitVariableDeclaration); }

            @Override
            public void visit(ToitExportDeclaration toitExportDeclaration) {
                if (toitExportDeclaration.isStar()) exports.add("*");
                else exports.addAll(toitExportDeclaration.getExportedNames());
            }
        });
    }

    public Object[] dependencies(ToitFile toitFile) {
        HashSet<PsiElement> dependencies = new HashSet<>(imports.values());
        dependencies.add(toitFile);
        dependencies.addAll(importedLibraries.values());
        dependencies.addAll(shows.keySet());
        dependencies.addAll(projectImports);
        return dependencies.toArray();
    }

    @Override
    public String toString() {
        return "ToitFileScope{" +
                "imports=" + imports +
                ", locals=" + locals +
                ", importedLibraries=" + importedLibraries +
                ", shows=" + shows +
                ", projectImports=" + projectImports +
                ", exports=" + exports +
                '}';
    }
}
