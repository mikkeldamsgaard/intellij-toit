package org.toitlang.intellij.highlighting;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.visitor.ToitVisitor;

public class ToitAnnotationHighlighter implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        element.accept(new ToitVisitor() {
            @Override
            public void visit(ToitNameableIdentifier toitNameableIdentifier) {
                if (toitNameableIdentifier.isFunctionName()) {
                    applyHighlight(holder, toitNameableIdentifier, ToitSyntaxHighlighter.FUNCTION_DECLARATION);
                } else if (toitNameableIdentifier.isVariableName()) {
                    toitNameableIdentifier.getParent().accept(annotateVariable(toitNameableIdentifier, holder));
                    applyHighlight(holder, toitNameableIdentifier, ToitSyntaxHighlighter.INSTANCE_FIELD);
                } else if (toitNameableIdentifier.isStructureName()) {
                    toitNameableIdentifier.getParent().accept(new ToitVisitor() {
                        @Override
                        public void visit(ToitStructure toitStructure) {
                            if (toitStructure.isInterface())
                                applyHighlight(holder, toitNameableIdentifier, ToitSyntaxHighlighter.INTERFACE_NAME);
                            else
                                applyHighlight(holder, toitNameableIdentifier, ToitSyntaxHighlighter.CLASS_NAME);
                        }
                    });
                    applyHighlight(holder, toitNameableIdentifier, ToitSyntaxHighlighter.CLASS_NAME);
                }
            }

            @Override
            public void visit(ToitReferenceIdentifier toitReferenceIdentifier) {
                if (toitReferenceIdentifier.isTypeName()) {
                    applyHighlight(holder, toitReferenceIdentifier, ToitSyntaxHighlighter.CLASS_REFERENCE);
                } else if (toitReferenceIdentifier.isReference()) {
                    PsiElement ref = toitReferenceIdentifier.getReference().resolve();
                    if (ref == null) return;
                    ref.accept(annotateVariable(toitReferenceIdentifier, holder));
                }
            }

            @Override
            public void visit(ToitPseudoKeyword toitPseudoKeyword) {
                applyHighlight(holder, toitPseudoKeyword, ToitSyntaxHighlighter.KEYWORD);
            }
        });
    }

    @NotNull
    private ToitVisitor annotateVariable(PsiElement element, @NotNull AnnotationHolder holder) {
        return new ToitVisitor() {
            @Override
            public void visit(ToitVariableDeclaration toitVariableDeclaration) {
                if (toitVariableDeclaration.isConstant()) {
                    applyHighlight(holder, element, ToitSyntaxHighlighter.CONSTANT);
                } else if (toitVariableDeclaration.isGlobal()) {
                    applyHighlight(holder, element, ToitSyntaxHighlighter.GLOBAL_VARIABLE);
                } else if (toitVariableDeclaration.isField()) {
                    applyHighlight(holder, element, ToitSyntaxHighlighter.INSTANCE_FIELD);
                } else {
                    applyHighlight(holder,element,ToitSyntaxHighlighter.LOCAL_VARIABLE);
                }
            }
        };
    }

    private void applyHighlight(@NotNull AnnotationHolder holder, PsiElement element, TextAttributesKey key) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element.getTextRange())
                .textAttributes(key)
                .create();
    }
}
