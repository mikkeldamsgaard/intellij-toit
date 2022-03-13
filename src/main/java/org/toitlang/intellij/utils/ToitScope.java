package org.toitlang.intellij.utils;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ToitScope {
    private final static ToitScope ROOT = new ToitScope() {
        @Override
        public boolean contains(String key) {
            return false;
        }

        @Override
        public @NotNull List<PsiElement> resolve(String key) {
            return Collections.emptyList();
        }

        @Override
        public void add(String key, PsiElement value) { }

    };

    private ToitScope parent = ROOT;
    Map<String, List<PsiElement>> local = new HashMap<>();

    public ToitScope() {
    }

    private ToitScope(ToitScope parent) {
        this();
        this.parent = parent;
    }

    public ToitScope derive() {
        return new ToitScope(this);
    }

    public void add(String key, PsiElement value) {
        if (key == null) return;
        local.computeIfAbsent(key, k -> new ArrayList<>(1)).add(value);
    }

    public boolean contains(String key) {
        return local.containsKey(key) || parent.contains(key);
    }

    public @NotNull List<PsiElement> resolve(String key) {
        List<PsiElement> result = new ArrayList<>(parent.resolve(key));
        if (local.containsKey(key)) result.addAll(local.get(key));
        return result;
    }

    public Object[] asVariant() {
        Set<PsiElement> result = new HashSet<>();
        var iter = this;
        do {
            result.addAll(iter.local.values().stream().flatMap(List::stream).collect(Collectors.toList()));
            iter = iter.parent;
        } while (iter != ROOT);
        return  result.toArray();
    }
}
