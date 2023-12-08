package org.toitlang.intellij.reference;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTypes;

public class ReferenceTest extends BasePlatformTestCase {
    /**
     * @return path to test data file directory relative to root of this module.
     */
    @Override
    protected @NotNull String getTestDataPath() {
        return "src/test/testData";
    }

    // Test that a reference to a variable in the same file is resolved
    public void testLocalVariable() {
        myFixture.configureByText("foo.toit", "x := 1\nmain: <caret>x\n");
        var reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
        assertNotNull(reference);
        var resolved = reference.resolve();
        assertNotNull(resolved);
        assertEquals("x := 1\n", resolved.getText());
    }

    public void testBlockFunctionVariable() {
        myFixture.configureByFile("block-function-variable.toit");
        var reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
        assertNotNull(reference);
        var resolved = reference.resolve();
        assertNotNull(resolved);
        assertEquals(ToitTypes.PARAMETER_NAME, resolved.getNode().getElementType());
        assertEquals("b", resolved.getText());
    }

    public void testSuperSetConstructor() {
        myFixture.configureByFile("super-set-constructor.toit");
        var reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
        assertNotNull(reference);
        var resolved = reference.resolve();
        assertNotNull(resolved);
    }

    public void testFactoryMethodIsNotStatic() {
        myFixture.configureByFile("factory-constructor-is-not-static.toit");
        var reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
        assertNotNull(reference);
        var resolved = reference.resolve();
        assertNull(resolved);
    }

    public void testImportAsDouble() {
        myFixture.configureByFiles("ref-import.toit", "i1.toit", "i2.toit");
        var reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
        assertNotNull(reference);
        var resolved = reference.resolve();
        assertNotNull(resolved);
    }

    public void testChainedDeref() {
        myFixture.configureByFile("chained-deref.toit");
        var reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
        assertNotNull(reference);
        var resolved = reference.resolve();
        assertNotNull(resolved);
    }
}
