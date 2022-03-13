package org.toitlang.intellij.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ToitTypes;
import org.toitlang.intellij.psi.ast.ToitBlock;
import org.toitlang.intellij.psi.ast.ToitFunction;
import org.toitlang.intellij.psi.ast.ToitVariableDeclaration;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.not;
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
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                        result.addElement(LookupElementBuilder.create("return"));
                        result.addElement(LookupElementBuilder.create("continue"));
                        result.addElement(LookupElementBuilder.create("break"));
                    }
                });
    }
}
