package org.toitlang.intellij.psi.scope;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ToitScope {
    public final static ToitScope ROOT = new ToitScope("root", null) {
        @Override
        public @NotNull List<PsiElement> resolve(String key) {
            return Collections.emptyList();
        }

        @Override
        public void add(String key, PsiElement value) {
        }

        @Override
        public void add(String key, List<? extends PsiElement> value) {
        }

        @Override
        protected void addVariant(Set<PsiElement> variants) {
        }
    };

    private final ToitScope parent;

    Map<String, List<PsiElement>> local = new HashMap<>();

    private final String name;
    private final boolean finalScope;

    public ToitScope(String name) {
        this(name, ROOT);
    }

    private ToitScope(String name, ToitScope parent) {
        this(name, parent, false);
    }

    private ToitScope(String name, ToitScope parent, boolean finalScope) {
        this.name = name;
        this.parent = parent;
        this.finalScope = finalScope;
    }

    private List<PsiElement> addToLocal(String key) {
        return local.computeIfAbsent(key, (k) -> new ArrayList<>());
    }

    public void add(String key, PsiElement value) {
        if (key == null) return;
        addToLocal(key).add(value);
    }

    public void add(String key, List<? extends PsiElement> value) {
        if (key == null) return;
        addToLocal(key).addAll(value);
    }

    public @NotNull List<PsiElement> resolve(String key) {
        List<PsiElement> result = new ArrayList<>();
        if (local.containsKey(key)) {
            if (finalScope) return local.get(key);
            result.addAll(local.get(key));
        }
        result.addAll(parent.resolve(key));
        return result;
    }

    protected void addVariant(Set<PsiElement> variants) {
        variants.addAll(local.values().stream().flatMap(List::stream).collect(Collectors.toList()));
        parent.addVariant(variants);
    }

    public Collection<PsiElement> asVariant() {
        Set<PsiElement> result = new HashSet<>();
        addVariant(result);
        return result;
    }

    @Override
    public String toString() {
        return "ToitScope{" +
                "name=" + name +
                ", local=" + local +
                ", parent=" + parent +
                '}';
    }

    public ToitScope subFromMap(String name, Map<String, List<? extends PsiElement>> locals) {
        ToitScope toitScope = sub(name);
        locals.forEach(toitScope::add);
        return toitScope;
    }

    public ToitScope sub(String name) {
        return new ToitScope(name, this);
    }

    public ToitScope sub(String name, boolean finalScope) {
        return new ToitScope(name, this, finalScope);
    }
}
