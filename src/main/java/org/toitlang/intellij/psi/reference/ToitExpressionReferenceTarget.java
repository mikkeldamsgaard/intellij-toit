package org.toitlang.intellij.psi.reference;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.toitlang.intellij.psi.ast.ToitReferenceTarget;

@Data
public class ToitExpressionReferenceTarget {
  boolean instanceTarget;
  ToitReferenceTarget target;

  public ToitExpressionReferenceTarget(ToitReferenceTarget target) {
    this(false,target);
  }

  private ToitExpressionReferenceTarget(boolean instanceTarget, ToitReferenceTarget target) {
    this.instanceTarget = instanceTarget;
    this.target = target;
  }

  public ToitExpressionReferenceTarget toInstance() {
    return new ToitExpressionReferenceTarget(true, target);
  }

  public ToitEvaluatedType getEvaluatedType() {
    var type = target.getEvaluatedType();
    if (instanceTarget) type = type.nonStatic();
    return type;
  }
}
