package org.toitlang.intellij.psi.reference;

import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ast.ToitReferenceIdentifier;

public class ToitReferenceBase extends PsiReferenceBase<ToitReferenceIdentifier> implements CachedValueProvider<ReferenceCalculation>, PsiPolyVariantReference {
    public ToitReferenceBase(@NotNull ToitReferenceIdentifier element) {
        super(element, new TextRange(0, element.getTextLength()), false);
    }

    @Override
    public @Nullable PsiElement resolve() {
        var res = multiResolve(false);
        if (res.length > 0) return res[0].getElement();
        return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        ToitFile file = getElement().getParentOfType(ToitFile.class);
        file = (ToitFile) file.getOriginalFile();
        var fileScope = file.getToitFileScope();
        System.out.println(fileScope);
        ReferenceCalculation calc = getElement().calculateReference(fileScope);
        return calc.getVariants();
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        if (element.textMatches(getElement()))
            return getElement().getManager().areElementsEquivalent(resolve(), element);
        return false;
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        return new TextRange(0,getElement().getTextLength());
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        return getElement().setName(newElementName);
    }

    private static final Object[] NEVER_CHANGE = { ModificationTracker.NEVER_CHANGED };

    @Override
    public @Nullable Result<ReferenceCalculation> compute() {
        var fileScope = getElement().getParentOfType(ToitFile.class).getToitFileScope();
        ReferenceCalculation calc = getElement().calculateReference(fileScope);
        Object[] dependencies;
        if (calc.getDependencies().isEmpty()) dependencies = NEVER_CHANGE;
        else dependencies=calc.getDependencies().toArray();
        return new Result<>(calc, dependencies);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        ToitFile toitFile = getElement().getParentOfType(ToitFile.class);
        toitFile = (ToitFile) toitFile.getOriginalFile();
        var fileScope = toitFile.getToitFileScope();
        ReferenceCalculation calc = getElement().calculateReference(fileScope);
        mySoft = calc.isSoft();
        return calc.getResolveResult();
    }
}
