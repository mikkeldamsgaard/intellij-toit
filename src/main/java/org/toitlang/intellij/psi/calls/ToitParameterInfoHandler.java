package org.toitlang.intellij.psi.calls;

import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandler;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.intellij.ui.JBColor;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toitlang.intellij.psi.ast.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ToitParameterInfoHandler implements ParameterInfoHandler<ToitExpression, ToitFunction> {

    @Override
    public @Nullable ToitExpression findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
        CallResolve callResolve = resolve(context.getFile(), context.getOffset());
        if (callResolve == null) return null;
        context.setItemsToShow(callResolve.getFunctions().toArray());
        return callResolve.getCall();
    }

    @Override
    public void showParameterInfo(@NotNull ToitExpression element, @NotNull CreateParameterInfoContext context) {
        context.showHint(element, element.getTextOffset() + 2, this);
    }

    @Override
    public @Nullable ToitExpression findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
        CallResolve callResolve = resolve(context.getFile(), context.getOffset());
        if (callResolve == null) return null;
        return callResolve.getCall();
    }

    @Override
    public void updateParameterInfo(@NotNull ToitExpression toitExpression, @NotNull UpdateParameterInfoContext context) {
        context.setParameterOwner(toitExpression);
        context.setCurrentParameter(1);
    }

    @Override
    public void updateUI(ToitFunction toitFunction, @NotNull ParameterInfoUIContext context) {
        java.util.List<String> parameters = new ArrayList<>();
        var children = toitFunction.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof ToitParameterName) {
                String p = children[i].getText();
                if (i + 1 < children.length && children[i + 1] instanceof ToitType) {
                    p += "/" + children[i + 1].getText();
                    i += 1;
                }
                parameters.add(p);
            }
        }
        if (parameters.isEmpty()) {
            context.setupUIComponentPresentation("No parameters for " + toitFunction.getName(), 0, 0, false, false, false, context.getDefaultParameterColor());
        } else {
            context.setupUIComponentPresentation(String.join(" ", parameters), 0, 0, false, false, false, context.getDefaultParameterColor());
        }
    }

    @Override
    public boolean isWhitespaceSensitive() {
        return true;
    }

    @Data
    @AllArgsConstructor
    public static class CallResolve {
        private ToitExpression call;
        private java.util.List<ToitFunction> functions;
    }


    private static CallResolve resolve(PsiFile file, int offset) {
        var e = file.findElementAt(offset);
        while (e != null && !(e instanceof ToitExpression)) e = e.getParent();
        if (e == null) return null;

        ToitExpression call;
        ToitReferenceIdentifier functionName = null;
        if (e instanceof ToitDerefExpression && e.getParent().getParent() instanceof ToitTopLevelExpression) {
            call = (ToitExpression) e;
            functionName = call.lastChildOfType(ToitReferenceIdentifier.class);
        } else if (e instanceof ToitPrimaryExpression && e.getParent() instanceof ToitTopLevelExpression) {
            call = (ToitExpression) e;
            functionName = call.firstChildOfType(ToitReferenceIdentifier.class);
        } else {
            call = ((ToitExpression) e).getParentOfType(ToitCallExpression.class);
            if (call == null) return null;
            ToitExpression functionalExpression = call.firstChildOfType(ToitExpression.class);
            if (functionalExpression != null) {
                var refs = functionalExpression.getDescendentsOfType(ToitReferenceIdentifier.class);
                if (!refs.isEmpty()) functionName = refs.get(refs.size()-1);
            }
        }


        if (functionName == null) return null;

        var ref = functionName.getReference().multiResolve(false);
        var functions =
                Arrays.stream(ref).map(ResolveResult::getElement)
                        .filter(element -> element instanceof ToitFunction)
                        .map(ToitFunction.class::cast)
                        .collect(Collectors.toList());

        if (functions.isEmpty()) {
            // Search for default constructors
            var structs = Arrays.stream(ref).map(ResolveResult::getElement)
                    .filter(element -> element instanceof ToitStructure)
                    .map(ToitStructure.class::cast)
                    .collect(Collectors.toList());

            for (ToitStructure struct : structs) {
                var defaultConstructor = struct.getDefaultConstructor();
                if (defaultConstructor != null) functions.add(defaultConstructor);
            }
        }
        return new CallResolve(call, functions);

    }
}
