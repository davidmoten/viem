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
        final int prime = 31;
        int result = 1;
        result = prime * result + identifiers.hashCode();
        result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EntityStateDefault<?, ?, ?> other = (EntityStateDefault<?, ?, ?>) obj;
        if (!identifiers.equals(other.identifiers))
            return false;
        if (metadata == null) {
            if (other.metadata != null)
                return false;
        } else if (!metadata.equals(other.metadata))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EntityStateDefault [ids=" + identifiers + ", metadata=" + metadata + "]";
    }
}
