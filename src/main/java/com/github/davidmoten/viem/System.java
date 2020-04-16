package com.github.davidmoten.viem;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A collection of EntityStates and some rules for merging them.
 * 
 * @param <K> identifier key type
 * @param <V> identifier value type
 * @param <M> metadata type
 */
public interface System<K, V, M> {

    /**
     * Returns an Iterable of the current entity-states in the system.
     * 
     * @return the current entity states in the system
     */
    Iterable<EntityState<K, V, M>> entityStates();

    /**
     * Returns all EntityStates that match one or more of the identifiers (both in
     * key and value).
     * 
     * @param identifiers identifiers to match on
     * @return all EntityStates that match one or more of the identifiers (both in
     *         key and value)
     */
    Set<EntityState<K, V, M>> matches(Map<K, V> identifiers);

    /**
     * Returns true if and only if a is greater than b. For identifier keys this
     * carries the sense that a is greater than b if and only if the identifier key
     * of a is considered more reliable than the identifier key of b in representing
     * the underlying entity.
     * 
     * @param a the first key
     * @param b the second key
     * @return true if and only if a is more reliable than b
     */
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
        MergeResult<K, V, M> r = Algorithm.merge(this, entity);
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
