package com.github.davidmoten.viem;

import java.util.Map;

/**
 * EntityState must implement hashCode and equals based on equality of the
 * identifiers only (ignoring metadata). it is advised to use the static methods
 * on EntityState to implement hashCode and equals. See
 * {@link EntityStateDefault} for example.
 *
 * @param <K> identifier key type
 * @param <V> identifier value type
 * @param <M> metadata type
 */
public interface EntityState<K, V, M> {

    Map<K, V> identifiers();

    M metadata();

    public static <K, V, M> EntityState<K, V, M> create(Map<K, V> ids, M metadata) {
        return new EntityStateDefault<K, V, M>(ids, metadata);
    }

    static int hashCode(EntityState<?, ?, ?> es) {
        return es.identifiers().hashCode();
    }

    static boolean equals(EntityState<?, ?, ?> a, Object obj) {
        if (a == obj)
            return true;
        if (obj == null)
            return false;
        if (a.getClass() != obj.getClass())
            return false;
        EntityStateDefault<?, ?, ?> other = (EntityStateDefault<?, ?, ?>) obj;
        if (!a.identifiers().equals(other.identifiers()))
            return false;
        return true;
    }
}
