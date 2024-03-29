package org.toitlang.intellij.psi.calls;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.toitlang.intellij.psi.ast.ToitParameterName;
import org.toitlang.intellij.psi.ast.ToitType;

@Data
@AllArgsConstructor
public class ParameterInfo {
    ToitType type;
    ToitParameterName parameterName;
    boolean nullable;
    boolean hasDefaultValue;
    boolean isBlock;
    boolean isMemberInitializer;

    public String getName() {
        return parameterName.getName();
    }
}
