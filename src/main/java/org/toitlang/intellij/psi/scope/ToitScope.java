package org.toitlang.intellij.psi.scope;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.psi.ast.ToitReferenceTarget;

import java.util.*;
import java.util.stream.Collectors;

public class ToitScope {
    public final static ToitScope ROOT = new ToitScope("root", null) {
        @Override
        public @NotNull List<ToitReferenceTarget> resolve(String key) {
            return Collections.emptyList();
        }

        @Override
        public void add(String key, ToitReferenceTarget value) {
        }

        @Override
        public void add(String key, List<? extends ToitReferenceTarget> value) {
        }

        @Override
        protected void addVariant(Set<ToitReferenceTarget> variants) {
        }
    };

    private final ToitScope parent;

    Map<String, List<ToitReferenceTarget>> local = new HashMap<>();

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

    private List<ToitReferenceTarget> addToLocal(String key) {
        return local.computeIfAbsent(key, (k) -> new ArrayList<>());
    }

    public void add(String key, ToitReferenceTarget value) {
        if (key == null) return;
        addToLocal(key).add(value);
    }

    public void add(String key, List<? extends ToitReferenceTarget> value) {
        if (key == null) return;
        addToLocal(key).addAll(value);
    }

    public @NotNull List<ToitReferenceTarget> resolve(String key) {
        List<ToitReferenceTarget> result = new ArrayList<>();
        if (local.containsKey(key)) {
            if (finalScope) return local.get(key);
            result.addAll(local.get(key));
        }
        result.addAll(parent.resolve(key));
        return result;
    }

    protected void addVariant(Set<ToitReferenceTarget> variants) {
        variants.addAll(local.values().stream().flatMap(List::stream).collect(Collectors.toList()));
        parent.addVariant(variants);
    }

    public Collection<ToitReferenceTarget> asVariant() {
        Set<ToitReferenceTarget> result = new HashSet<>();
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

    public ToitScope subFromMap(String name, Map<String, List<? extends ToitReferenceTarget>> locals) {
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
