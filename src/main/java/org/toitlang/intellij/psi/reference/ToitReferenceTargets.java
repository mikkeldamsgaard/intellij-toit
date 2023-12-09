package org.toitlang.intellij.psi.reference;

import lombok.Data;
import org.toitlang.intellij.psi.ast.ToitReferenceTarget;

import java.util.List;

@Data
public class ToitReferenceTargets {
  List<ToitExpressionReferenceTarget> targets;
  boolean soft;

  public ToitReferenceTargets(List<ToitExpressionReferenceTarget> targets, boolean soft) {
    this.targets = targets;
    this.soft = soft;
  }

  public ToitReferenceTargets(List<ToitExpressionReferenceTarget> targets) {
    this(targets, false);
  }

  public ToitReferenceTargets() {
    this(List.of(), false);
  }
}
