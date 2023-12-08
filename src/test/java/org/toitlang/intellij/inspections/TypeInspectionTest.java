package org.toitlang.intellij.inspections;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class TypeInspectionTest extends BasePlatformTestCase {
    TypeInspection myInspection;
    public void setUp() throws Exception {
        super.setUp();
        myInspection = new TypeInspection();
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
        return "src/test/testData/inspections/type-inspection";
    }

    public void testMinusMinusNo() { doTestInspection(); }

}
