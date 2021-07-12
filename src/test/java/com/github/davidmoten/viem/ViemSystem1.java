package com.github.davidmoten.viem;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * A ViemSystem for testing purposes only. Doesn't scale well because O(N)
 * lookups are used. A proper implementation would use O(1) lookups.
 *
 */
class ViemSystem1 implements ViemSystem<String, String, Long> {

    private EntityState<String, String, Long> es1;
    private EntityState<String, String, Long> es2;

    @Override
    public void comparing(EntityState<String, String, Long> a,
            EntityState<String, String, Long> b) {
       es1 = a;
       es2 = b;
    }

    // mutable
    BiPredicate<Long, Long> mergeable = (a, b) -> true;

    private final Set<EntityState<String, String, Long>> set;

    public ViemSystem1(Set<EntityState<String, String, Long>> set) {
        this.set = set;
    }

    @SafeVarargs
    static ViemSystem1 create(EntityState<String, String, Long>... states) {
        Set<EntityState<String, String, Long>> set = new HashSet<>();
        for (EntityState<String, String, Long> state : states) {
            set.add(state);
        }
        return new ViemSystem1(set);
    }

    @Override
    public Set<EntityState<String, String, Long>> matches(Map<String, String> identifiers) {
        return set.stream() //
                .filter(e -> e.identifiers() //
                        .entrySet() //
                        .stream() //
                        .filter(entry -> entry.getValue().equals(identifiers.get(entry.getKey()))) //
                        .findFirst().isPresent())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean keyGreaterThan(String a, String b) {
        return a.compareTo(b) < 0; // A > B
    }

    @Override
    public boolean metadataGreaterThan(Long a, Long b) {
        return a > b;
    }

    @Override
    public boolean mergeable(Long a, Long b) {
        return mergeable.test(a, b);
    }

    @Override
    public Long merge(Long a, Long b) {
        return Math.max(a, b);
    }

    @Override
    public Iterable<EntityState<String, String, Long>> entityStates() {
        return set;
    }

    @Override
    public ViemSystem<String, String, Long> update(List<EntityState<String, String, Long>> matches,
            Set<EntityState<String, String, Long>> newEntityStates) {
        set.removeAll(matches);
        set.addAll(newEntityStates);
        return this;
    }

}
