package org.toitlang.intellij.reference;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.ast.ToitFunction;
import org.toitlang.intellij.psi.ast.ToitParameterName;
import org.toitlang.intellij.psi.ast.ToitReferenceTarget;
import org.toitlang.intellij.psi.ast.ToitVariableDeclaration;
import org.toitlang.intellij.psi.reference.ToitReference;

public class ReferenceTest extends BasePlatformTestCase {
    /**
     * @return path to test data file directory relative to root of this module.
     */
    @Override
    protected @NotNull String getTestDataPath() {
        return "src/test/testData";
    }


    private ToitReferenceTarget doResolvedTest(String filename, Class<? extends ToitReferenceTarget> expectedClass) {
        myFixture.configureByFile(filename);
        return doResolvedTestAfterConfigured(expectedClass);
    }

    private void doUnresolvedTest(String filename, boolean soft) {
        myFixture.configureByFile(filename);
        doUnresolvedTestAfterConfigured(soft);
    }

    private void doUnresolvedTestAfterConfigured(boolean soft) {
        var reference = getReference();
        var resolved = reference.resolve();
        assertNull(resolved);
        assertEquals(soft, reference.isSoft());
    }


    private ToitReferenceTarget doResolvedTestAfterConfigured(Class<? extends ToitReferenceTarget> expectedClass) {
        var reference = getReference();
        var resolved = reference.resolve();
        assertNotNull(resolved);
        assertEquals(expectedClass, resolved.getClass());
        return (ToitReferenceTarget) resolved;
    }

    @NotNull
    private ToitReference getReference() {
        var reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
        assertNotNull(reference);
        return (ToitReference) reference;
    }

    // Test that a reference to a variable in the same file is resolved
    public void testLocalVariable() {
        myFixture.configureByText("foo.toit", "x := 1\nmain: <caret>x\n");
        var referenceTarget = doResolvedTestAfterConfigured(ToitVariableDeclaration.class);
        assertEquals("x := 1\n", referenceTarget.getText());
    }

    public void testBlockFunctionVariable() {
        var referenceTarget = doResolvedTest("block-function-variable.toit", ToitParameterName.class);
        assertEquals(ToitTypes.PARAMETER_NAME, referenceTarget.getNode().getElementType());
        assertEquals("b", referenceTarget.getText());
    }

    public void testSuperSetConstructor() {
        doResolvedTest("super-set-constructor.toit", ToitFunction.class);
    }

    public void testFactoryMethodIsNotStatic() {
        doUnresolvedTest("factory-constructor-is-not-static.toit", false);
    }

    public void testImportAsDouble() {
        myFixture.configureByFiles("ref-import.toit", "i1.toit", "i2.toit");
        doResolvedTestAfterConfigured(ToitFunction.class);
    }

    public void testChainedDeref() {
        doResolvedTest("chained-deref.toit", ToitFunction.class);
    }

    public void testThis() {
        doResolvedTest("this.toit", ToitFunction.class);
    }

    public void testConstructorWithParams() {
        doResolvedTest("constructor-with-params.toit", ToitFunction.class);
    }

    public void testImportedConstructorWithParams() {
        myFixture.configureByFiles("imported-constructor-with-params.toit", "i1.toit");
        doResolvedTestAfterConfigured(ToitFunction.class);
    }

    public void testDotArguments() {
        doResolvedTest("dot-arguments.toit", ToitVariableDeclaration.class);
    }

    public void testDotArgumentResolveArgument() {
        doResolvedTest("dot-arguments-resolve-argument.toit", ToitVariableDeclaration.class);
    }

    public void testNoArgumentConstructor() {
        doResolvedTest("no-argument-constructor.toit", ToitVariableDeclaration.class);
    }

    public void testNoArgumentConstructor2() {
        doResolvedTest("no-argument-constructor-2.toit", ToitFunction.class);
    }

    public void testNoArgumentConstructor3() {
        myFixture.configureByFiles("no-argument-constructor-3.toit", "i1.toit");
        doResolvedTestAfterConfigured(ToitFunction.class);
    }

    public void testInterfaceType() {
        doResolvedTest("interface-type.toit", ToitFunction.class);
    }

    public void testTypeAs() {
        doResolvedTest("type-as.toit", ToitFunction.class);
    }

    public void testSoft() {
        doUnresolvedTest("soft.toit", true);
    }

    public void testPrimitiveSoft() {
        doUnresolvedTest("primitive-soft.toit", true);
    }

    public void testSoftIt() {
        doUnresolvedTest("soft-it.toit", true);
    }

    public void testSuper() {
        var func = doResolvedTest("super.toit", ToitFunction.class);
        assertEquals("b:", func.getText().trim());
    }
    public void testSuperParam() {
        var func = doResolvedTest("super-param.toit", ToitFunction.class);
        assertEquals("b a:", func.getText().trim());
    }

    public void testSuperOperator() {
        var func = doResolvedTest("super-operator.toit", ToitFunction.class);
        assertEquals("operator == other:", func.getText().trim());
    }
}
