package org.toitlang.intellij.psi.scope;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.toitlang.intellij.model.IToitPrimaryLanguageElement;

import java.util.*;
import java.util.stream.Collectors;

public class ToitScope {
    private final static ToitScope ROOT = new ToitScope() {

        @Override
        public @NotNull List<PsiElement> resolve(String key) {
            return Collections.emptyList();
        }

        @Override
        public void add(String key, PsiElement value) {
        }

        @Override
        public void add(String key, List<PsiElement> value) {
        }

        @Override
        protected void addVariant(Set<PsiElement> variants) {
        }
    };
    private final static List<ToitScope> DEFAULT_PARENTS = Collections.singletonList(ROOT);
    private final List<ToitScope> parents;
    Map<String, List<PsiElement>> local = new HashMap<>();

    public ToitScope() {
        this.parents = DEFAULT_PARENTS;
    }

    private ToitScope(ToitScope parent) {
        this.parents = Collections.singletonList(parent);
    }

    private ToitScope(List<ToitScope> parents) {
        this.parents = parents;
    }

    public ToitScope derive() {
        return new ToitScope(this);
    }

    public void add(String key, PsiElement value) {
        if (key == null) return;
        local.computeIfAbsent(key, k -> new ArrayList<>(1)).add(value);
    }

    public void add(String key, List<PsiElement> value) {
        if (key == null) return;
        local.computeIfAbsent(key, k -> new ArrayList<>(1)).addAll(value);
    }

    public @NotNull List<PsiElement> resolve(String key) {
        List<PsiElement> result = new ArrayList<>();
        if (local.containsKey(key)) result.addAll(local.get(key));
        for (ToitScope parent : parents) {
            result.addAll(parent.resolve(key));
        }
        return result;
    }

    protected void addVariant(Set<PsiElement> variants) {
        variants.addAll(local.values().stream().flatMap(List::stream).collect(Collectors.toList()));
        for (ToitScope parent : parents) {
            parent.addVariant(variants);
        }
    }

    public Object[] asVariant() {
        Set<PsiElement> result = new HashSet<>();
        addVariant(result);
        return result.toArray();
    }

    private boolean isRedundant() {
        return local.isEmpty() && parents == DEFAULT_PARENTS;
    }

    @Override
    public String toString() {
        return "ToitScope{" +
                "parent=" + parents +
                ", local=" + local +
                '}';
    }

    public static ToitScope fromMap(Map<String, PsiElement> locals) {
        ToitScope toitScope = new ToitScope();
        locals.forEach(toitScope::add);
        return toitScope;
    }

    public static ToitScope chain(ToitScope... scopes) {
        return new ToitScope(Arrays.asList(scopes).stream().filter(t -> !t.isRedundant()).collect(Collectors.toList()));
    }

}
