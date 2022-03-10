package org.toitlang.intellij.parser;

import com.google.common.base.Charsets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class LibParseTest extends  ParserTest {
    void parseAllSub(File d) throws IOException {
        for (var c : d.listFiles()) {
            if (c.isDirectory() ) parseAllSub(c);
            else if (c.getName().endsWith("toit") ) {
                try {
//                    long t = System.currentTimeMillis();
                    String content = new String(new FileInputStream(c).readAllBytes(), Charsets.UTF_8);
                    var psi = parseFile(c.getName(), content);
//                    System.out.println(System.currentTimeMillis()-t);
                    checkError(psi, c.getPath());
//                    System.out.println(System.currentTimeMillis()-t);
                    System.out.println("Success: " + c);
                } catch (Throwable t) {
                    System.err.println("Failed to parse: " + c);
                    throw  t;
                }
            }
        }
    }

    public void testParsingTestData() throws IOException {
        File lib = new File("/Users/mikkel/proj/application/esp32/common/esp-toit/toit/lib");
        parseAllSub(lib);
    }


}
