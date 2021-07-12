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
public interface ViemSystem<K, V, M> {

    /**
     * Returns an Iterable of the current entity-states in the system.
     * 
     * @return the current entity states in the system
     */
    Iterable<EntityState<K, V, M>> entityStates();

    /**
     * Returns all EntityStates that match one or more of the identifiers (both in
     * key and value). Note that equals and hashCode must be implemented in
     * EntityState based solely on the identifiers. This fact is also mentioned in
     * the EntityState javadoc.
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

    /**
     * Returns true if and only if a is more reliable than b. That is a > b means
     * that we would choose the a version of any properties over the b version. When
     * time is included in the metadata and we seek that the System holds the latest
     * information then we would consider the metadata with the later timestamp more
     * reliable than the metadata with the earlier timestamp.
     * 
     * @param a metadata
     * @param b metadata
     * @return true if and only if a is more "reliable" than b
     */
    boolean metadataGreaterThan(M a, M b);

    /**
     * Returns true if and only if there is no inherent important conflict between
     * the reports that would prevent the merging of metadata. For example, in the
     * case where metadata contains positional and time information and the reports
     * are for timestamped vessel positions then we might reject two sets of
     * metadata if the calculated effective speed was beyond a probable maximum.
     * 
     * <p>
     * Note that normally only the metadata is required for mergeability (for
     * example via an effective speed check) and that identifiers are included (via
     * EntityState) for logging purposes and as a lesser purpose for some unusual
     * mergeability criteria (like don't ever merge vessel 13579135 with a vessel
     * outside of Sydney Harbour because it shouldn't be anywhere else).
     * 
     * @param a first entity state
     * @param b second entity state
     * @return true if and only if the reports with given metadata can be merged
     */
    boolean mergeable(EntityState<K, V, M> a, EntityState<K, V, M> b);

    M merge(M a, M b);

    ViemSystem<K, V, M> update(List<EntityState<K, V, M>> matches,
            Set<EntityState<K, V, M>> newEntityStates);

    default EntityState<K, V, M> createEntityState(Map<K, V> identifiers, M metadata) {
        return EntityState.create(identifiers, metadata);
    }

    default ViemSystem<K, V, M> merge(EntityState<K, V, M> entity) {
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
