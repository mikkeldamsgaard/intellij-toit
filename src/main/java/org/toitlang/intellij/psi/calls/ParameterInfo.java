package org.toitlang.intellij.psi.calls;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.toitlang.intellij.psi.ast.ToitType;

import java.util.Objects;

@Data
@AllArgsConstructor
public class ParameterInfo {
    ToitType type;
    String name;
    boolean nullable;
    boolean hasDefaultValue;
    boolean isBlock;
    boolean isMemberInitializer;
}
