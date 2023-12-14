package org.toitlang.intellij.findusage;

import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.usageView.UsageInfo;
import org.toitlang.intellij.psi.ToitElementFactory;
import org.toitlang.intellij.psi.ast.ToitFunction;
import org.toitlang.intellij.psi.ast.ToitParameterName;

import java.util.Collection;
import java.util.List;

public class FindUsageTest extends BasePlatformTestCase {
    String textUnderscore =
            "method a_b: \n" +
                    "  print a_b\n";

    String textMinus =
            "method a-b: \n" +
                    "  print a-b\n";

    String textMinusUnderscore =
            "method a-b: \n" +
                    "  print a_b\n";

    String getTextUnderscoreMinus =
            "method a_b: \n" +
                    "  print a-b\n";

    private void doTest(String source) {
        ToitFunction methodFromText = ToitElementFactory.createFunctionFromText(getProject(), source);
        List<ToitParameterName> parameters = methodFromText.getParameters();
        PsiReference[] references =
                ReferencesSearch.search(parameters.get(0), new LocalSearchScope(methodFromText), false)
                        .toArray(PsiReference.EMPTY_ARRAY);

        assertEquals(2, references.length);
    }
    public void testUnderscoreFind() {
        doTest(textUnderscore);
    }

    public void testMinusFind() {
        doTest(textMinus);
    }

    public void testMinusUnderscoreFind() {
        doTest(textMinusUnderscore);
    }

    public void testUnderscoreMinusFind() {
        doTest(getTextUnderscoreMinus);
    }

    protected String getTestDataPath() {
        return "src/test/testData/findUsages";
    }

    public void testRemote() {
        Collection<UsageInfo> usageInfos =  myFixture.testFindUsages("remote.toit","remote-include.toit");
        assertEquals(3, usageInfos.size());
    }
}
