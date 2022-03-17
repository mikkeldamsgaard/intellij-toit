package org.toitlang.intellij.psi.scope;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.model.IToitPrimaryLanguageElement;

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

        @Override
        public Set<String> getAllKeys() {
            return new HashSet<>();
        }
    };

    private ToitScope parent = ROOT;
    Map<String, List<PsiElement>> local = new HashMap<>();

    public ToitScope() {
    }

    public ToitScope(Map<String, PsiElement> locals) {
        locals.forEach((k,v)->local.computeIfAbsent(k, k_-> new ArrayList<>()).add(v));
    }

    private ToitScope(ToitScope parent) {
        this.parent = parent;
    }

    private ToitScope(ToitScope parent, ToitScope locals) {
        this(parent);
        local = locals.local;
    }

    public ToitScope(ToitScope parent, String prefix, ToitScope scope) {
        this(parent);
        scope.getAllKeys().forEach(k -> local.put(prefix+"."+k, scope.resolve(k)));
    }

    public ToitScope derive() {
        return new ToitScope(this);
    }
    public ToitScope chain(ToitScope local) {
        return new ToitScope(this, local);
    }
    public ToitScope chainWithPrefix(String prefix, ToitScope scope) {
        return new ToitScope(this, prefix, scope);
    }

    public void add(String key, PsiElement value) {
        if (key == null) return;
        local.computeIfAbsent(key, k -> new ArrayList<>(1)).add(value);
    }

    public void add(String key, List<PsiElement> value) {
        if (key == null) return;
        local.computeIfAbsent(key, k -> new ArrayList<>(1)).addAll(value);
    }

    public boolean contains(String key) {
        return local.containsKey(key) || parent.contains(key);
    }

    public @NotNull List<PsiElement> resolve(String key) {
        List<PsiElement> result = new ArrayList<>(parent.resolve(key));
        if (local.containsKey(key)) result.addAll(local.get(key));
        return result;
    }

    public Set<String> getAllKeys() {
        Set<String> allKeys = parent.getAllKeys();
        allKeys.addAll(local.keySet());
        return allKeys;
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
