package com.github.davidmoten.viem;

import java.util.Map;

final class EntityStateDefault<K, V, M> implements EntityState<K, V, M> {

    private final Map<K, V> map;
    private final M metadata;

    EntityStateDefault(Map<K, V> map, M metadata) {
        this.map = map;
        this.metadata = metadata;
    }

    @Override
    public Map<K, V> identifiers() {
        return map;
    }

    @Override
    public M metadata() {
        return metadata;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((map == null) ? 0 : map.hashCode());
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
        if (map == null) {
            if (other.map != null)
                return false;
        } else if (!map.equals(other.map))
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
        return "EntityStateDefault [ids=" + map + ", metadata=" + metadata + "]";
    }
}
