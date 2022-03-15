package org.toitlang.intellij.psi.scope;

import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.utils.ToitScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToitFileScope {
    private ToitFile toitFile;

    // The library/package files imported
    // import lib
    // import lib show xxx
    // import .lib
    // both would add lib to this map.
    final Map<String, ToitFile> importedLibPackageFiles = new HashMap<>();

    // The imported scope from library and packages
    // import lib => adds lib
    // import lib as xxx => adds xxx
    // import .lib as xxx => adds xxx
    Map<String, ToitFileScope> importedWithPrefix = new HashMap<>();

    // For all locally defined names, they end in this scope (top level structures, functions and variables)
    final ToitScope locals = new ToitScope();

    // Everything that is imported with a "." and not an as.
    // import .lib => Add all top level names from lib to this scope
    // import lib show xxx => Add xxx to this scope
    // import .lib show xxx => Add xxx to this scope
    final List<ToitFileScope> importedLocals = new ArrayList<>();

    // The list of exported names from this file. Can be a singleton "*" or empty
    final List<String> exported = new ArrayList<>();

    public ToitFileScope(ToitFile toitFile) {
        this.toitFile = toitFile;
    }

    public ToitFile getToitFile() {
        return toitFile;
    }

    public ToitScope getExportedScope() {
        // locals and exported names from imported locals
        if (exported.isEmpty()) return locals;

        ToitScope locallyImportedScope = getLocallyImportedScope();

        if (exported.contains("*")) {
            return locals.chain(locallyImportedScope);
        } else {
            var exportedScope = locals.derive();
            for (String export : exported) {
                var importedLocal = locallyImportedScope.resolve(export);
                if (!importedLocal.isEmpty()) exportedScope.add(export, importedLocal);
            }
            return exportedScope;
        }
    }

    private ToitScope getLocallyImportedScope() {
        ToitScope locallyImportedScope = new ToitScope();
        for (ToitFileScope importedLocal : importedLocals) {
            locallyImportedScope = locallyImportedScope.chain(importedLocal.getExportedScope());
        }
        return locallyImportedScope;
    }

    // Returns the file used to resolve references after import in each of these:
    public ToitFile getImportedFile(String name) {
        return importedLibPackageFiles.get(name);
    }

    // Get this scope used to resolve any reference in toitFile itself
    public ToitScope getScopeForElementsInFile(ToitScope core) {
        var scope = core.chain(locals).chain(getLocallyImportedScope());
        for (String prefix : importedWithPrefix.keySet()) {
//            var exportedScope = importedWithPrefix.get(prefix).getExportedScope();
//            scope = scope.chainWithPrefix(prefix,exportedScope);
            scope.add(prefix, importedWithPrefix.get(prefix).toitFile);
        }
        return scope;
    }
}
