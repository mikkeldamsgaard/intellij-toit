package org.toitlang.intellij.files;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.util.List;

@Data
@AllArgsConstructor
public class FileInfo {
    List<String> path;
    String fileName;

    public String constructPath() {
        return String.format("%s%s%s", path, File.separator, fileName);
    }
}
