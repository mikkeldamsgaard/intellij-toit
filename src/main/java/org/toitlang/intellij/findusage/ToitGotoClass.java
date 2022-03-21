package org.toitlang.intellij.findusage;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.ChooseByNameContributorEx2;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.AbstractStubIndex;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ast.ToitStructure;
import org.toitlang.intellij.psi.stub.indecies.ToitStructureShortNameIndex;

public class ToitGotoClass implements ChooseByNameContributorEx {
    @Override
    public void processNames(@NotNull Processor<? super String> processor, @NotNull GlobalSearchScope scope, @Nullable IdFilter filter) {
        for (String key : ToitStructureShortNameIndex.INSTANCE.getAllKeys(scope.getProject())) {
            processor.process(key);
        }
    }

    @Override
    public void processElementsWithName(@NotNull String name, @NotNull Processor<? super NavigationItem> processor, @NotNull FindSymbolParameters parameters) {
        for (ToitStructure toitStructure : ToitStructureShortNameIndex.INSTANCE.get(name, parameters.getProject(), parameters.getSearchScope())) {
            processor.process(toitStructure);
        }
    }
}
