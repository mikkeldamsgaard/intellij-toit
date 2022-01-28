package org.toitlang.intellij.indexer;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.IndexableSetContributor;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.ToitApplicationSettings;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public class ToitSdkIndexRoot extends IndexableSetContributor {
    @Override
    public @NotNull Set<VirtualFile> getAdditionalRootsToIndex() {
        return Collections.singleton(LocalFileSystem.getInstance().findFileByIoFile(new File(ToitApplicationSettings.SettingsState.getInstance().sdkPath)));
    }
}
