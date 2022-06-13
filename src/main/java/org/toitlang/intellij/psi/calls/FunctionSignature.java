package org.toitlang.intellij.psi.calls;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@AllArgsConstructor
public class FunctionSignature {
    String functionName;
    List<ParameterInfo> positionalParameters;
    Map<String, ParameterInfo> namedParameters;
    Map<String, ParameterInfo> defaultedNamedParameters;

    public String getFunctionName() {
        return functionName;
    }

    public List<ParameterInfo> getPositionalParameters() {
        return positionalParameters;
    }

    public Map<String, ParameterInfo> getNamedParameters() {
        return namedParameters;
    }

    public Map<String, ParameterInfo> getDefaultedNamedParameters() {
        return defaultedNamedParameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionSignature that = (FunctionSignature) o;

        return Objects.equals(functionName, that.functionName) &&
                positionalParameters.size() == that.positionalParameters.size() &&
                Objects.equals(namedParameters.keySet(), that.namedParameters.keySet());
    }

    public boolean implements_(FunctionSignature signature) {
        if (!functionName.equals(signature.functionName)) return false;
        if (positionalParameters.size() != signature.positionalParameters.size()) return false;

        for (String nonDefaultParam : signature.namedParameters.keySet()) {
            if (!namedParameters.containsKey(nonDefaultParam) && !defaultedNamedParameters.containsKey(nonDefaultParam)) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionName, positionalParameters.size(), namedParameters.keySet());
    }

    public String render() {
        StringBuilder b = new StringBuilder();
        b.append(functionName);

        for (ParameterInfo positionalParameter : positionalParameters) {
            b.append(" ").append(positionalParameter.getName());
            if (positionalParameter.getType() != null) {
                b.append("/").append(positionalParameter.getType().getName());
            }
        }

        for (ParameterInfo namedParameter : getNamedParameters().values()) {
            b.append(" --").append(namedParameter.getName());
            if (namedParameter.getType() != null) {
                b.append("/").append(namedParameter.getType().getName());
            }
        }

        return b.toString();
    }

    public String asArguments() {
        StringBuilder b = new StringBuilder();
        for (ParameterInfo positionalParameter : positionalParameters) {
            b.append(" ").append(positionalParameter.getName());
        }
        for (ParameterInfo namedParameter : getNamedParameters().values()) {
            b.append(" --").append(namedParameter.getName()).append("=").append(namedParameter.getName());
        }
        return b.toString();
    }
}
