package org.toitlang.intellij.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.highlighting.ToitSyntaxHighlighter;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ToitPsiHelper;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.ast.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class ToitCompletionContributor extends CompletionContributor {
    public ToitCompletionContributor() {

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
                            result.addElement(keywordElement("extends"));
                            result.addElement(keywordElement("implements"));
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
        completeAbstractClassInterfaceMonitor(toitElm, result);
        completeClassAfterAbstract(toitElm, result);
        completeBreakContinueReturnAndControls(toitElm, result);
        completeFinally(toitElm, result);
        completeConstructorAbstractStaticInStructureBlock(toitElm, result);
    }

    private void completeConstructorAbstractStaticInStructureBlock(IToitElement toitElm, CompletionResultSet result) {
        var toitStructure = toitElm.getParentChain(ToitStructure.class, List.of(ToitBlock.class, ToitFunction.class));
        if (toitStructure == null) return;

        if (toitStructure.isAbstract()) {
            result.addElement(keywordElement("abstract "));
        }

        result.addElement(keywordElement("static "));
        result.addElement(keywordElement("constructor"));
    }

    private void completeFinally(IToitElement toitElm, CompletionResultSet result) {
        if (!(toitElm instanceof ToitRecover)) return;
        if (!(toitElm.getParent() instanceof ToitBlock)) return;
        if (!(toitElm.getPrevSibling() instanceof ToitTry)) return;
        result.addElement(keywordElement("finally: "));
    }

    private void completeBreakContinueReturnAndControls(IToitElement toitElm, CompletionResultSet result) {
        var toitBlock = toitElm.getParentChain(ToitBlock.class, List.of(ToitTopLevelExpression.class, ToitPrimaryExpression.class));
        if (toitBlock == null) return;

        if (toitBlock.getParentOfType(ToitFor.class) != null || toitBlock.getParentOfType(ToitWhile.class) != null) {
            result.addElement(keywordElement("break"));
            result.addElement(keywordElement("continue"));
        }

        if (!(toitBlock.getParent() instanceof ToitStructure)) {
            result.addElement(keywordElement("return"));
            result.addElement(keywordElement("for "));
            result.addElement(keywordElement("if "));
            result.addElement(keywordElement("while "));
            result.addElement(keywordElement("try:"));
        }
    }

    private static final TokenSet CLAZZ = TokenSet.create(ToitTypes.CLASS);
    private void completeClassAfterAbstract(IToitElement toitElm, CompletionResultSet result) {
        if (!(toitElm instanceof ToitRecover)) return;
        var structure = toitElm.getPrevSiblingOfType(ToitStructure.class);
        if (structure == null) return;
        if (!structure.isAbstract() || structure.hasToken(CLAZZ)) return;
        result.addElement(keywordElement("class "));
    }

    private void completeAbstractClassInterfaceMonitor(IToitElement toitElm, CompletionResultSet result) {
        ToitFunction toitFunction = toitElm.getParentWithIntermediaries(ToitFunction.class, ToitNameableIdentifier.class);
        if (toitFunction == null) return;
        if (!toitFunction.isTopLevel()) return;
        PsiElement nextSibling = toitFunction.getNextSibling();
        if (nextSibling instanceof ToitStructure || nextSibling instanceof ToitFunction || nextSibling instanceof ToitVariableDeclaration) {
            result.addElement(keywordElement("class "));
            result.addElement(keywordElement("abstract "));
            result.addElement(keywordElement("interface "));
            result.addElement(keywordElement("monitor "));
        }
    }

    private void completeShowKeyword(IToitElement position, CompletionResultSet result) {
        if (position instanceof ToitRecover) {
            if (position.getPrevSibling() instanceof ToitImportDeclaration) {
                result.addElement(keywordElement("show "));
            }
        }
    }

    private void completeImportExportKeywords(IToitElement position, CompletionResultSet result) {
        var toitFunction = position.getParentWithIntermediaries(ToitFunction.class, ToitNameableIdentifier.class);
        if (toitFunction == null) return;
        if (position.getPrevSibling() != null) return;
        if (toitFunction.getPrevSibling() instanceof ToitImportDeclaration || toitFunction.getPrevSibling() instanceof ToitExportDeclaration) {
            result.addElement(keywordElement("import "));
            result.addElement(keywordElement("export "));
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



    private LookupElementBuilder keywordElement(String keyword) {
        return LookupElementBuilder.create(keyword)
                .withItemTextForeground(ToitSyntaxHighlighter.KEYWORD.getDefaultAttributes().getForegroundColor());
    }

}
