package org.toitlang.intellij.psi.calls;

import java.util.*;
import java.util.stream.Collectors;

public class ParametersInfo {
    private final Map<String, ParameterInfo> named;
    private final List<ParameterInfo> positionals;

    public ParametersInfo() {
        named = new HashMap<>();
        positionals = new ArrayList<>();
    }

    public void addNamed(String name, ParameterInfo info) {
        named.put(name,info);
    }

    public void addPositional(ParameterInfo info) {
        positionals.add(info);
    }

    public int getNumberOfNonDefaultPositionals() {
        int i = 0;
        while (i < positionals.size() && !positionals.get(i).hasDefaultValue) i++;
        return i;
    }

    public int getNumberOfPositionals() {
        return positionals.size();
    }

    public Set<String> getNonDefaultNamedParameters() {
        return named.entrySet().stream().filter(e -> !e.getValue().hasDefaultValue).map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    public boolean hasNamedParameter(String name) {
        return named.containsKey(name);
    }

    public ParameterInfo getPositional(int position) {
        return positionals.get(position);
    }

    public ParameterInfo getNamedParameter(String name) {
        return named.get(name);
    }
}
