package com.github.davidmoten.viem;

import java.util.Map;

final class EntityStateDefault<K, V, M> implements EntityState<K, V, M> {

    private final Map<K, V> identifiers;
    private final M metadata;

    EntityStateDefault(Map<K, V> identifiers, M metadata) {
        if (identifiers == null) {
            throw new NullPointerException("identifiers cannot be null");
        }
        this.identifiers = identifiers;
        this.metadata = metadata;
    }

    @Override
    public Map<K, V> identifiers() {
        return identifiers;
    }

    @Override
    public M metadata() {
        return metadata;
    }

    @Override
    public int hashCode() {
        return EntityState.hashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EntityState.equals(this, obj);
    }

    @Override
    public String toString() {
        return "EntityStateDefault [ids=" + identifiers + ", metadata=" + metadata + "]";
    }
}
