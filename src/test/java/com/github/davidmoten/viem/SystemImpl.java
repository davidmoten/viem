package com.github.davidmoten.viem;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.davidmoten.viem.EntityState;
import com.github.davidmoten.viem.System;

final class SystemImpl implements System<String, String, Long> {

    // mutable
    boolean mergeable = true;

    private final Set<EntityState<String, String, Long>> set;

    public SystemImpl(Set<EntityState<String, String, Long>> set) {
        this.set = set;
    }

    @SafeVarargs
    static SystemImpl create(EntityState<String, String, Long>... states) {
        Set<EntityState<String, String, Long>> set = new HashSet<>();
        for (EntityState<String, String, Long> state : states) {
            set.add(state);
        }
        return new SystemImpl(set);
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
        return mergeable;
    }

    @Override
    public Long merge(Long a, Long b) {
        return Math.max(a, b);
    }

    @Override
    public EntityState<String, String, Long> createEntityState(Map<String, String> identifiers, Long metadata) {
        return new EntityStateImpl(identifiers, metadata);
    }

    @Override
    public Iterable<EntityState<String, String, Long>> entityStates() {
        return set;
    }

    @Override
    public System<String, String, Long> update(List<EntityState<String, String, Long>> matches,
            Set<EntityState<String, String, Long>> newEntityStates) {
        set.removeAll(matches);
        set.addAll(newEntityStates);
        return this;
    }

}
