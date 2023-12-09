// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast

import com.intellij.lang.ASTNode
import org.toitlang.intellij.psi.calls.ToitCallHelper
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor
import org.toitlang.intellij.psi.reference.ToitEvaluatedType
import org.toitlang.intellij.psi.reference.ToitExpressionReferenceTarget
import org.toitlang.intellij.psi.reference.ToitReferenceTargets
import org.toitlang.intellij.psi.scope.ToitScope

class ToitPrimaryExpression(node: ASTNode) : ToitExpression(node) {
    // Possible children (Always only one child)
    // ToitReferenceIdentifier
    // ToitTopLevelExpression
    // Literals
    // ToitBlock (block or lambda)

    override fun <T> accept(expressionVisitor: ToitExpressionVisitor<T>): T {
        return expressionVisitor.visit(this)
    }

    override fun getReferenceTargets(scope: ToitScope): ToitReferenceTargets {
        val child = singleToitElementChild()
        when (child) {
            is ToitReferenceIdentifier -> {
                val result = ArrayList<ToitExpressionReferenceTarget>(1)
                val name = child.getName()
                for (referenceTarget in scope.resolve(name)) {
                    var expressionReferenceTarget = ToitExpressionReferenceTarget(referenceTarget)
                    if (ToitEvaluatedType.SUPER == name || ToitEvaluatedType.THIS == name) {
                        expressionReferenceTarget = expressionReferenceTarget.toInstance()
                    }
                    expressionReferenceTarget = ToitCallHelper.isDisguisedConstructorCall(expressionReferenceTarget, this)
                    result.add(expressionReferenceTarget)
                }
                return ToitReferenceTargets(result)
            }
            is ToitTopLevelExpression -> return child.getReferenceTargets(scope)
        }
        return super.getReferenceTargets(scope)
    }
}
