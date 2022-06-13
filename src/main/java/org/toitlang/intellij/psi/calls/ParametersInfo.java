package org.toitlang.intellij.psi.calls;

import java.util.*;
import java.util.stream.Collectors;

public class ParametersInfo {
    private final Map<String, ParameterInfo> named;
    private final List<ParameterInfo> positionalParameters;

    public ParametersInfo() {
        named = new HashMap<>();
        positionalParameters = new ArrayList<>();
    }

    public void addNamed(String name, ParameterInfo info) {
        named.put(name,info);
    }

    public void addPositional(ParameterInfo info) {
        positionalParameters.add(info);
    }

    public List<ParameterInfo> getNonDefaultPositionalParameters() {
        return positionalParameters.stream().filter(p -> !p.hasDefaultValue).collect(Collectors.toList());
    }
    public int getNumberOfNonDefaultPositionalParameters() {
        return getNonDefaultPositionalParameters().size();
    }

    public int getNumberOfPositionalParameters() {
        return positionalParameters.size();
    }

    public Map<String, ParameterInfo> getNonDefaultNamedParameters() {
        Map<String, ParameterInfo> res = new HashMap<>();
        for (Map.Entry<String, ParameterInfo> e : new HashMap<>(named).entrySet()) {
            if (!e.getValue().hasDefaultValue) res.put(e.getKey(), e.getValue());
        }
        return res;
    }

    public Map<String, ParameterInfo> getDefaultNamedParameters() {
        Map<String, ParameterInfo> res = new HashMap<>();
        for (Map.Entry<String, ParameterInfo> e : new HashMap<>(named).entrySet()) {
            if (e.getValue().hasDefaultValue) res.put(e.getKey(), e.getValue());
        }
        return res;
    }

    public boolean hasNamedParameter(String name) {
        return named.containsKey(name);
    }

    public ParameterInfo getPositional(int position) {
        return positionalParameters.get(position);
    }

    public ParameterInfo getNamedParameter(String name) {
        return named.get(name);
    }
}
