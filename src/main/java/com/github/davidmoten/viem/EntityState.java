package com.github.davidmoten.viem;

import java.util.Map;

public interface EntityState<K, V, M> {

    Map<K, V> identifiers();

    M metadata();

    public static <K, V, M> EntityState<K, V, M> create(Map<K, V> map, M metadata) {
        return new EntityStateDefault<K, V, M>(map, metadata);
    }

}
