package org.toitlang.intellij.psi.reference;

import com.intellij.psi.PsiElement;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ast.ToitDerefExpression;
import org.toitlang.intellij.psi.ast.ToitExpression;
import org.toitlang.intellij.psi.ast.ToitReferenceIdentifier;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.scope.ToitLocalScopeCalculator;
import org.toitlang.intellij.psi.scope.ToitScope;

import java.util.HashSet;
import java.util.Set;

public class VariantsCalculator {
    private final ToitReferenceIdentifier source;
    private final Set<PsiElement> variants;
    private final EvaluationScope scope;
    private VariantsCalculator(ToitReferenceIdentifier source, EvaluationScope evaluationScope) {
        this.source = source;
        scope = evaluationScope;
        this.variants = new HashSet<>();
    }

    private VariantsCalculator build() {
        source.getExpressionParent().accept(new ToitExpressionVisitor<>() {
            @Override
            public Object visit(ToitDerefExpression toitDerefExpression) {
                var prevType = ((ToitExpression)toitDerefExpression.getPrevSibling()).getType(scope.getScope());
                if (prevType.getFile() != null) {
                    variants.addAll(prevType.getFile().getToitFileScope().getToitScope().asVariant());
                } else if (prevType.getStructure() != null) {
                    variants.addAll(prevType.getStructure().getScope(scope.getScope()).asVariant());
                }
                return null;
            }

            @Override
            public Object visitExpression(ToitExpression expression) {
                variants.addAll(scope.asVariant());
                return null;
            }
        });
        return this;
    }

    private Object[] getVariants() {
        return variants.toArray();
    }

    public static Object[] getVariants(ToitReferenceIdentifier source, EvaluationScope evaluationScope) {
        return new VariantsCalculator(source, evaluationScope).build().getVariants();
    }

}
