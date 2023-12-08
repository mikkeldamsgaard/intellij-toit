package org.toitlang.intellij.inspections;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class SemanticErrorInspectorTest extends BasePlatformTestCase {
    SemanticErrorInspector myInspection;
    public void setUp() throws Exception {
        super.setUp();
        myInspection = new SemanticErrorInspector();
        myFixture.enableInspections(myInspection);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            myFixture.disableInspections(myInspection);
        }
        catch (Throwable e) {
            addSuppressedException(e);
        }
        finally {
            myInspection = null;
            super.tearDown();
        }
    }

    private void doTestInspection() {
        myFixture.testHighlighting(getTestName(false) + ".toit");
    }

    protected String getTestDataPath() {
        return "src/test/testData/inspections/semantic-error-inspector";
    }

    public void testMissingImplementation1() { doTestInspection(); }
    public void testMissingImplementation2() { doTestInspection(); }

}
