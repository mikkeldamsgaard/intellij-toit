package org.toitlang.intellij.psi.calls;

import java.util.*;
import java.util.stream.Collectors;

import static org.toitlang.intellij.psi.ast.ToitIdentifier.normalizeMinusUnderscore;

public class ParametersInfo {
    private final Map<String, String> nameTranslations;
    private final Map<String, ParameterInfo> named;
    private final List<ParameterInfo> positionalParameters;

    public ParametersInfo() {
        named = new HashMap<>();
        nameTranslations = new HashMap<>();
        positionalParameters = new ArrayList<>();
    }

    public synchronized void addNamed(String name, ParameterInfo info) {
        named.put(name,info);
        if (name.contains("_")) nameTranslations.put(normalizeMinusUnderscore(name), name);
        nameTranslations.put(name, name);
    }

    public synchronized void addPositional(ParameterInfo info) {
        positionalParameters.add(info);
    }

    public synchronized List<ParameterInfo> getNonDefaultPositionalParameters() {
        return new ArrayList<>(positionalParameters).stream().filter(p -> !p.hasDefaultValue).collect(Collectors.toList());
    }
    public synchronized int getNumberOfNonDefaultPositionalParameters() {
        return getNonDefaultPositionalParameters().size();
    }

    public synchronized int getNumberOfPositionalParameters() {
        return positionalParameters.size();
    }

    public synchronized Map<String, ParameterInfo> getNonDefaultNamedParameters() {
        Map<String, ParameterInfo> res = new HashMap<>();
        for (Map.Entry<String, ParameterInfo> e : new HashMap<>(named).entrySet()) {
            if (!e.getValue().hasDefaultValue) res.put(e.getKey(), e.getValue());
        }
        return res;
    }

    public synchronized Map<String, ParameterInfo> getDefaultNamedParameters() {
        Map<String, ParameterInfo> res = new HashMap<>();
        for (Map.Entry<String, ParameterInfo> e : new HashMap<>(named).entrySet()) {
            if (e.getValue().hasDefaultValue) res.put(e.getKey(), e.getValue());
        }
        return res;
    }

    String getTranslatedName(String name) {
        if (name.startsWith("no-") && !nameTranslations.containsKey(name))
            name = name.substring(3);
        return nameTranslations.get(name);
    }
    public synchronized boolean hasNamedParameter(String name) {
        String translated = getTranslatedName(name);
        if (translated == null) return false;
        return named.containsKey(translated);
    }

    public synchronized ParameterInfo getPositional(int position) {
        return positionalParameters.get(position);
    }

    public synchronized ParameterInfo getNamedParameter(String name) {
        String translated = getTranslatedName(name);
        return named.get(translated);
    }
}
