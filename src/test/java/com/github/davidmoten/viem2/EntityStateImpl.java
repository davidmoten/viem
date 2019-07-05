package com.github.davidmoten.viem2;

import java.util.HashMap;
import java.util.Map;

final class EntityStateImpl implements EntityState<String, String, Long> {

    private final Map<String, String> identifiers;
    private final long metadata;

    public EntityStateImpl(Map<String, String> identifiers, long metadata) {
        this.identifiers = identifiers;
        this.metadata = metadata;
    }

    public static EntityStateImpl create(long metadata, String... arr) {
        Map<String, String> map = new HashMap<>();
        for (String s : arr) {
            map.put(s.substring(0, 1), s.substring(1, 2));
        }
        return new EntityStateImpl(map, metadata);
    }

    @Override
    public Map<String, String> identifiers() {
        return identifiers;
    }

    @Override
    public Long metadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "ES[ids=" + identifiers + ", md=" + metadata + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifiers == null) ? 0 : identifiers.hashCode());
        result = prime * result + (int) (metadata ^ (metadata >>> 32));
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
        EntityStateImpl other = (EntityStateImpl) obj;
        if (identifiers == null) {
            if (other.identifiers != null)
                return false;
        } else if (!identifiers.equals(other.identifiers))
            return false;
        if (metadata != other.metadata)
            return false;
        return true;
    }
}
