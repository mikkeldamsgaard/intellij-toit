// This is a generated file. Not intended for manual editing.
package org.toitlang.intellij.psi.ast;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.calls.ToitCallHelper;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.reference.ToitEvaluatedType;
import org.toitlang.intellij.psi.reference.ToitExpressionReferenceTarget;
import org.toitlang.intellij.psi.reference.ToitReferenceTargets;
import org.toitlang.intellij.psi.scope.ToitScope;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ToitDerefExpression extends ToitExpression {

  public ToitDerefExpression(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public <T> T accept(ToitExpressionVisitor<T> expressionVisitor) {
    return expressionVisitor.visit(this);
  }

  @Override
  public ToitReferenceTargets getReferenceTargets(ToitScope scope) {
    var previousSibling = getPrevSibling();
    var name = getName();
    if (!(previousSibling instanceof ToitExpression) || name == null) return new ToitReferenceTargets();
    var previousExpression = (ToitExpression) previousSibling;

    List<ToitExpressionReferenceTarget> result = new ArrayList<>();

    List<ToitEvaluatedType> possibleTypes = new ArrayList<>();

    ToitReferenceTargets previousTargets = previousExpression.getReferenceTargets(scope);
    for (ToitExpressionReferenceTarget referenceTarget :previousTargets.getTargets()) {
      ToitEvaluatedType evaluatedType = referenceTarget.getEvaluatedType();
      if (!evaluatedType.resolved()) continue;
      possibleTypes.add(evaluatedType);
    }

    if (possibleTypes.isEmpty()) {
      var type = previousExpression.getType(scope);
      if (type.resolved()) possibleTypes.add(type);
    }

    for (ToitEvaluatedType evaluatedType : possibleTypes) {
      if (evaluatedType.getFile() != null) {
          result.addAll(evaluatedType.getFile().getToitFileScope().getExportedScope()
            .resolve(getToitReferenceIdentifier().getName()).stream()
            .map(ToitExpressionReferenceTarget::new).collect(Collectors.toList()));
      } else if (evaluatedType.getStructure() != null) {
        // Special case for setters
        boolean isPotentialSetterCall = ToitCallHelper.isPotentialSetterCall(this);

        ToitScope structureScope;

        if (evaluatedType.isStatic()) {
          structureScope = evaluatedType.getStructure().getScope(ToitStructure.StaticScope.STATIC, ToitScope.ROOT);
          structureScope = evaluatedType.getStructure().getScope(ToitStructure.StaticScope.FACTORY, structureScope);
        } else {
          structureScope = evaluatedType.getStructure().getScope(ToitStructure.StaticScope.INSTANCE, ToitScope.ROOT);
          var functionParent = getParentOfType(ToitFunction.class);
          if (functionParent != null && functionParent.isConstructor()) {
            structureScope = evaluatedType.getStructure().getScope(ToitStructure.StaticScope.FACTORY, structureScope);
          }
        }

        for (ToitReferenceTarget resolvedTarget : structureScope.resolve(name)) {
          var expressionTarget = new ToitExpressionReferenceTarget(resolvedTarget);
          if (resolvedTarget instanceof ToitFunction) {
            if (!isPotentialSetterCall && ((ToitFunction) resolvedTarget).isSetter() ||
                isPotentialSetterCall && !((ToitFunction) resolvedTarget).isSetter()) {
              continue;
            }
          }
          result.add(expressionTarget);
        }
      }
    }

    // Check for constructor calls
    return
      new ToitReferenceTargets(
        result.stream().map(e -> ToitCallHelper.isDisguisedConstructorCall(e,this)).collect(Collectors.toList()),
        possibleTypes.isEmpty() || previousTargets.isSoft());
  }

  public ToitReferenceIdentifier getToitReferenceIdentifier() {
    for (ToitReferenceIdentifier toitReferenceIdentifier : getChildrenOfType(ToitReferenceIdentifier.class)) {
      return toitReferenceIdentifier;
    }
    return null;
  }

  @Override
  public String getName() {
    var ref = getToitReferenceIdentifier();
    if (ref != null) return ref.getName();
    return null;
  }
}
