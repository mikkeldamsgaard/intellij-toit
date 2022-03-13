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
//        Collection<PsiElement> referencedValue = CachedValuesManager.getCachedValue(myElement, this).getReferencedValue();
//        if (referencedValue != null && referencedValue.size() > 0) return referencedValue.iterator().next();
        return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        var fileScope = getElement().getParentOfType(ToitFile.class).getScope();
        ReferenceCalculation calc = getElement().calculateReference(fileScope);
        return calc.getVariants();
        //return CachedValuesManager.getCachedValue(myElement, this).getVariants();
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        //if (element.textMatches(getElement()))
            return getElement().getManager().areElementsEquivalent(resolve(), element);
        //return false;
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
        var fileScope = getElement().getParentOfType(ToitFile.class).getScope();
        ReferenceCalculation calc = getElement().calculateReference(fileScope);
        Object[] dependencies;
        if (calc.getDependencies().isEmpty()) dependencies = NEVER_CHANGE;
        else dependencies=calc.getDependencies().toArray();
        return new Result<>(calc, dependencies);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        var fileScope = getElement().getParentOfType(ToitFile.class).getScope();
        ReferenceCalculation calc = getElement().calculateReference(fileScope);
        return calc.getResolveResult();
    }
}