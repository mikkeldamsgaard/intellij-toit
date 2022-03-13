package org.toitlang.intellij.structureview;

import com.intellij.navigation.ItemPresentation;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public interface IStructureViewable extends Navigatable, ItemPresentation {
    default List<IStructureViewable> getStructureChildren() {
        return Collections.emptyList();
    }

    @Override
    default @Nullable Icon getIcon(boolean unused) {
        return null;
    }


}
