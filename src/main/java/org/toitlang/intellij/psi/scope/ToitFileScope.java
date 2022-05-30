package org.toitlang.intellij.psi.scope;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.files.ToitFileResolver;
import org.toitlang.intellij.files.ToitSdkFiles;
import org.toitlang.intellij.model.IToitPrimaryLanguageElement;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

import java.util.*;

public class ToitFileScope {
    private final Map<String, List<ToitFile>> imports = new HashMap<>();
    private final Map<String, List<IToitPrimaryLanguageElement>> locals = new HashMap<>();
    private final Map<String, ToitFile> importedLibraries = new HashMap<>();
    private final Map<ToitFile, List<String>> shows = new HashMap<>();
    private final List<ToitFile> projectImports = new ArrayList<>();
    private final List<String> exports = new ArrayList<>();
    private final ToitFile toitFile;

    public ToitFileScope(ToitFile toitFile) {
        this.toitFile = toitFile;
    }

    public ToitScope getToitScope() {
        Map<String, List<? extends PsiElement>> scope = new HashMap<>(locals);
        scope.putAll(imports);
        shows.forEach((k,v)->{
            var exported = k.getToitFileScope().getExportedScope(new HashSet<>());
            if (v.contains("*")) scope.putAll(exported);
            else {
                for (String shown : v) {
                    if (exported.containsKey(shown)) scope.put(shown, exported.get(shown));
                }
            }
        });

        projectImports.forEach(p -> {
            Map<String, List<? extends PsiElement>> exportedScope = p.getToitFileScope().getExportedScope(new HashSet<>());
            for (String name : exportedScope.keySet()) {
                if (!locals.containsKey(name)) scope.put(name, exportedScope.get(name));
            }
        });

        return ToitScope.fromMap(toitFile.getName()+"-file", scope, true);
    }


    public ToitScope getExportedScope() {
        return ToitScope.fromMap(toitFile.getName()+"-exported", getExportedScope(new HashSet<>()),false);
    }

    public Map<String, List<IToitPrimaryLanguageElement>> getLocals() {
        return locals;
    }

    public ToitFile getImportedLibrary(String name) {
        return importedLibraries.get(name);
    }

    private @NotNull  Map<String, List<? extends PsiElement>> getExportedScope(Set<ToitFileScope> seen) {
        Map<String, List<? extends PsiElement>> result = new HashMap<>();
        if (seen.contains(this)) return result; // Cycle detection. If export are cycled, it is an error
        seen.add(this);
        result.putAll(locals);

        if (!exports.isEmpty()) {
            for (ToitFile projectImport : projectImports) {
                var pExported =  projectImport.getToitFileScope().getExportedScope(seen);
                pExported.entrySet().stream()
                        .filter(e -> exports.contains("*") || exports.contains(e.getKey()))
                        .forEach(e -> result.put(e.getKey(),e.getValue()));
            }

            for (Map.Entry<ToitFile, List<String>> e : shows.entrySet()) {
                exports.stream()
                        .filter(ex -> e.getValue().contains(ex))
                        .forEach(ex -> result.put(ex,e.getKey().getToitFileScope().getExportedScope().resolve(ex)));

            }
        }

        for (String export : exports) {
            if (!"*".equals(export) && !result.containsKey(export)) {
                result.put(export, ToitSdkFiles.getCoreScope(toitFile.getProject()).resolve(export));
            }
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
                for (ToitIdentifier toitIdentifier : toitImportDeclaration.getChildrenOfType(ToitIdentifier.class)) {
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
                    imports.computeIfAbsent(as.getName(), k -> new ArrayList<>(1)).add(importedFile);
                } else if (!shows.isEmpty()) {
                    shows.forEach(s -> ToitFileScope.this.shows.computeIfAbsent(importedFile, (k) -> new ArrayList<>()).add(s));
                } else if (prefixDots == 0) {
                    imports.computeIfAbsent(paths.get(paths.size()-1), k -> new ArrayList<>(1)).add(importedFile);
                } else {
                    projectImports.add(importedFile);
                }
            }

            @Override
            public void visit(ToitStructure toitStructure) { locals.computeIfAbsent(toitStructure.getName(), k -> new ArrayList<>(1)).add(toitStructure); }

            @Override
            public void visit(ToitFunction toitFunction) { locals.computeIfAbsent(toitFunction.getName(), k -> new ArrayList<>(1)).add(toitFunction); }

            @Override
            public void visit(ToitVariableDeclaration toitVariableDeclaration) { locals.computeIfAbsent(toitVariableDeclaration.getName(), k -> new ArrayList<>(1)).add( toitVariableDeclaration); }

            @Override
            public void visit(ToitExportDeclaration toitExportDeclaration) {
                if (toitExportDeclaration.isStar()) exports.add("*");
                else exports.addAll(toitExportDeclaration.getExportedNames());
            }
        });
    }

    public Object[] dependencies(ToitFile toitFile) {
        HashSet<PsiElement> dependencies = new HashSet<>();
        imports.values().forEach(dependencies::addAll);
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
