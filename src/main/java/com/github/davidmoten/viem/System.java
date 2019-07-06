package com.github.davidmoten.viem;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface System<K, V, M> {

    Iterable<EntityState<K, V, M>> entityStates();

    Set<EntityState<K, V, M>> matches(Map<K, V> identifiers);

    boolean keyGreaterThan(K a, K b);

    boolean metadataGreaterThan(M a, M b);

    boolean mergeable(M a, M b);

    M merge(M a, M b);

    System<K, V, M> update(List<EntityState<K, V, M>> matches,
            Set<EntityState<K, V, M>> newEntityStates);

    default EntityState<K, V, M> createEntityState(Map<K, V> identifiers, M metadata) {
        return EntityState.create(identifiers, metadata);
    }

    default System<K, V, M> merge(EntityState<K, V, M> entity) {
        MergeResult<K, V, M> r = Util.merge(this, entity);
        return update(r.matches, r.newEntityStates);
    }

    default Set<EntityState<K, V, M>> toSet() {
        Set<EntityState<K, V, M>> set = new HashSet<>();
        for (EntityState<K, V, M> es : entityStates()) {
            set.add(es);
        }
        return set;
    }

}
