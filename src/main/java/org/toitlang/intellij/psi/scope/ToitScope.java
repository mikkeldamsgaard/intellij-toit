package org.toitlang.intellij.psi.scope;

import com.intellij.psi.PsiElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static org.toitlang.intellij.psi.ast.ToitIdentifier.normalizeMinusUnderscore;

public class ToitScope {
    private final static ToitScope ROOT = new ToitScope("root",true) {

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
    private final static List<ToitScope> DEFAULT_PARENTS = Collections.singletonList(ROOT);
    private final List<ToitScope> parents;
    private final boolean finalResolver;
    Map<String, List<PsiElement>> local = new HashMap<>();

    private final String name;

    public ToitScope(String name, boolean finalResolver) {
        this.name = name;
        this.finalResolver = finalResolver;
        this.parents = DEFAULT_PARENTS;
    }

    private ToitScope(String name, List<ToitScope> parents, boolean finalResolver) {
        this.name = name;
        this.finalResolver = finalResolver;
        this.parents = parents;
    }

    private List<PsiElement> addToLocal(String key) {
        var result = local.get(key);
        if (result == null) {
            result = new ArrayList<>();
            if (key.contains("_")) {
                local.put(normalizeMinusUnderscore(key), result);
            }
            local.put(key, result);
        }
        return result;
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
        return doResolve(key).result;
    }

    private ScopeResolveResult doResolve(String key) {
        List<PsiElement> result = new ArrayList<>();
        if (local.containsKey(key)) result.addAll(local.get(key));
        if (finalResolver && !result.isEmpty()) return new ScopeResolveResult(result, true);

        if (parents != null) {
            for (ToitScope parent : parents) {
                var parentResult = parent.doResolve(key);
                if (parentResult.isFinal) return parentResult;

                result.addAll(parent.resolve(key));
            }
        }

        return new ScopeResolveResult(result,false);
    }

    protected void addVariant(Set<PsiElement> variants) {
        variants.addAll(local.values().stream().flatMap(List::stream).collect(Collectors.toList()));
        for (ToitScope parent : parents) {
            parent.addVariant(variants);
        }
    }

    public Collection<PsiElement> asVariant() {
        Set<PsiElement> result = new HashSet<>();
        addVariant(result);
        return result;
    }

    private boolean isRedundant() {
        return local.isEmpty() && parents == DEFAULT_PARENTS;
    }

    @Override
    public String toString() {
        return "ToitScope{" +
                "name="+name+
                ", parent=" + parents +
                ", local=" + local +
                '}';
    }

    public static ToitScope fromMap(String name, Map<String, List<? extends PsiElement>> locals, boolean finalResolver) {
        ToitScope toitScope = new ToitScope(name, finalResolver);
        locals.forEach(toitScope::add);
        return toitScope;
    }

    public static ToitScope chain(String name, ToitScope... scopes) {
        return new ToitScope(name, Arrays.stream(scopes).filter(t -> !t.isRedundant()).collect(Collectors.toList()), false);
    }

    @Data
    @AllArgsConstructor
    static class ScopeResolveResult {
        List<PsiElement> result;
        boolean isFinal;
    }
}
