package org.toitlang.intellij.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ToitPsiHelper;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.ast.*;

import java.util.HashSet;
import java.util.Set;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.or;

public class ToitCompletionContributor extends CompletionContributor {
    public ToitCompletionContributor() {

        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement()
                        .inside(ToitBlock.class)
                        .and(or(
                                psiElement().inside(ToitFunction.class),
                                psiElement().inside(ToitVariableDeclaration.class)))
                        .and(psiElement().withParent(ToitBlock.class)),
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                        result.addElement(LookupElementBuilder.create("return"));
                        result.addElement(LookupElementBuilder.create("continue"));
                        result.addElement(LookupElementBuilder.create("break"));
                    }
                });

        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withAncestor(2, StandardPatterns.instanceOf(ToitNamedArgument.class)),
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                        var pos = (ToitNameableIdentifier) parameters.getPosition().getParent();
                        var call = pos.getParentOfType(ToitCallExpression.class);
                        var function = call.getFunction();
                        if (function == null) return;
                        for (ToitParameterName toitParameterName : function.childrenOfType(ToitParameterName.class)) {
                            result.addElement(LookupElementBuilder.create(toitParameterName));
                        }
                    }
                }
        );

        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement()
                        .withParent(ToitNameableIdentifier.class)
                        .withSuperParent(2, ToitFunction.class),
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                        if (parameters.getPosition().getParent().getPrevSibling() == null) {
                            ToitStructure structure = ((ToitNameableIdentifier) parameters.getPosition().getParent()).getParentOfType(ToitStructure.class);
                            if (structure != null && !structure.isInterface())
                                result.addElement(LookupElementBuilder.create("constructor"));
                        }
                    }
                }
        );

        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement()
                        .withParent(ToitRecover.class)
                        .withSuperParent(2, ToitFile.class)
                ,
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                        ToitRecover recover = (ToitRecover) parameters.getPosition().getParent();
                        if (recover.getPrevSibling() instanceof ToitStructure) {
                            ToitStructure toitStructure = (ToitStructure)recover.getPrevSibling();
                            if (!(toitStructure.isClass() || toitStructure.isInterface() || toitStructure.isMonitor())) return;
                            if (toitStructure.getNameIdentifier() == null) return;
                            result.addElement(LookupElementBuilder.create("extends"));
                            result.addElement(LookupElementBuilder.create("implements"));
                        }
                    }
                }
        );
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        super.fillCompletionVariants(parameters, result);

        IToitElement toitElm = ToitPsiHelper.findClosestIToitElement(parameters.getPosition().getParent());
        if (toitElm == null) return;

        completeConstructionParameters(toitElm, result);
        completeImportExportKeywords(toitElm, result);
        completeShowKeyword(toitElm, result);
        completeAbstractClassInterfaceMontiro(toitElm, result);
        completeAbstractClass(toitElm, result);
    }

    private static final TokenSet CLAZZ = TokenSet.create(ToitTypes.CLASS);
    private void completeAbstractClass(IToitElement toitElm, CompletionResultSet result) {
        if (!(toitElm instanceof ToitRecover)) return;
        var structure = toitElm.getPrevSiblingOfType(ToitStructure.class);
        if (structure == null) return;
        if (!structure.isAbstract() || structure.hasToken(CLAZZ)) return;
        result.addElement(LookupElementBuilder.create("class "));
    }

    private void completeAbstractClassInterfaceMontiro(IToitElement toitElm, CompletionResultSet result) {
        ToitFunction toitFunction = toitElm.getParentWithIntermediaries(ToitFunction.class, ToitNameableIdentifier.class);
        if (toitFunction == null) return;
        PsiElement nextSibling = toitFunction.getNextSibling();
        if (nextSibling instanceof ToitStructure || nextSibling instanceof ToitFunction || nextSibling instanceof ToitVariableDeclaration) {
            result.addElement(LookupElementBuilder.create("class "));
            result.addElement(LookupElementBuilder.create("abstract "));
            result.addElement(LookupElementBuilder.create("interface "));
            result.addElement(LookupElementBuilder.create("monitor "));
        }
    }

    private void completeShowKeyword(IToitElement position, CompletionResultSet result) {
        if (position instanceof ToitRecover) {
            if (position.getPrevSibling() instanceof ToitImportDeclaration) {
                result.addElement(LookupElementBuilder.create("show "));
            }
        }
    }

    private void completeImportExportKeywords(IToitElement position, CompletionResultSet result) {
        var toitFunction = position.getParentWithIntermediaries(ToitFunction.class, ToitNameableIdentifier.class);
        if (toitFunction == null) return;
        if (position.getPrevSibling() != null) return;
        if (toitFunction.getPrevSibling() instanceof ToitImportDeclaration || toitFunction.getPrevSibling() instanceof ToitExportDeclaration) {
            result.addElement(LookupElementBuilder.create("import "));
            result.addElement(LookupElementBuilder.create("export "));
        }
    }

    private void completeConstructionParameters(@NotNull IToitElement position, @NotNull CompletionResultSet result) {
        var toitFunction = position.getParentWithIntermediaries(ToitFunction.class, ToitParameterName.class, ToitReferenceIdentifier.class);
        if (toitFunction == null) return;
        Set<String> existingParameters = new HashSet<>();

        for (ToitParameterName toitParameterName : toitFunction.childrenOfType(ToitParameterName.class)) {
            var ref = toitParameterName.firstChildOfType(ToitReferenceIdentifier.class);
            if (ref != null) existingParameters.add(ref.getName());
        }
        toitFunction.getParentOfType(ToitBlock.class).childrenOfType(ToitVariableDeclaration.class)
                .stream()
                .filter(v -> !existingParameters.contains(v.getName()))
                .forEach(v -> result.addElement(LookupElementBuilder.create(v)));
    }
}
