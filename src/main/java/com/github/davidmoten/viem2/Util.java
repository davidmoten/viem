package com.github.davidmoten.viem2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

final class Util {

    private Util() {
        // prevent instantiation
    }

    static <K, V, M> EntityState<K, V, M> min(System<K, V, M> system, EntityState<K, V, M> a, EntityState<K, V, M> b) {
        if (system.metadataGreaterThan(a.metadata(), b.metadata())) {
            return b;
        } else {
            return a;
        }
    }

    static <K, V, M> EntityState<K, V, M> max(System<K, V, M> system, EntityState<K, V, M> a, EntityState<K, V, M> b) {
        if (system.metadataGreaterThan(a.metadata(), b.metadata())) {
            return a;
        } else {
            return b;
        }
    }

    static <K, V, M> int compare(System<K, V, M> system, K a, K b) {
        return system.keyGreaterThan(a, b) ? 1 : -1;
    }

    static <K, V, M> Map<K, V> common(EntityState<K, V, M> a, EntityState<K, V, M> b) {
        Map<K, V> aIds = a.identifiers();
        Map<K, V> bIds = b.identifiers();
        return aIds //
                .entrySet() //
                .stream() //
                .filter(entry -> entry.getValue().equals(bIds.get(entry.getKey()))) //
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    static <K, V, M> Map<K, Pair<V>> conflicting(EntityState<K, V, M> a, EntityState<K, V, M> b) {
        Map<K, V> aIds = a.identifiers();
        Map<K, V> bIds = b.identifiers();
        Map<K, Pair<V>> map = new HashMap<>();
        for (K k : aIds.keySet()) {
            V bValue = bIds.get(k);
            if (bValue != null) {
                V aValue = aIds.get(k);
                if (!bValue.equals(aValue)) {
                    map.put(k, new Pair<V>(aValue, bValue));
                }
            }
        }
        return map;
    }

    static <K, V, M> Map<K, V> exclusive(EntityState<K, V, M> a, EntityState<K, V, M> b) {
        Map<K, V> aIds = a.identifiers();
        Map<K, V> bIds = b.identifiers();
        Map<K, V> map = new HashMap<>();
        for (K k : aIds.keySet()) {
            if (!bIds.containsKey(k)) {
                map.put(k, aIds.get(k));
            }
        }
        for (K k : bIds.keySet()) {
            if (!aIds.containsKey(k)) {
                map.put(k, bIds.get(k));
            }
        }
        return map;
    }

    static <K, V, M> boolean greaterThan(System<K, V, M> system, Set<K> a, Set<K> b) {
        if (b.isEmpty()) {
            return true;
        }
        if (a.isEmpty()) {
            return false;
        }
        K maxKeyA = Collections.max(a, (x, y) -> Util.compare(system, x, y));
        K maxKeyB = Collections.max(b, (x, y) -> Util.compare(system, x, y));
        return system.keyGreaterThan(maxKeyA, maxKeyB);
    }

    static <K, V, M> MergeResult<K, V, M> merge(System<K, V, M> system, EntityState<K, V, M> e) {
        Set<EntityState<K, V, M>> set = new HashSet<>();
        List<EntityState<K, V, M>> matches = new ArrayList<>(system.matches(e.identifiers()));
        Collections.sort(matches, //
                (a, b) -> {
                    K x = Collections.max(a.identifiers().keySet(), //
                            (r, s) -> Util.compare(system, r, s));
                    K y = Collections.max(b.identifiers().keySet(), //
                            (r, s) -> Util.compare(system, r, s));
                    return Util.compare(system, x, y);
                });
        EntityState<K, V, M> p = e;
        for (EntityState<K, V, M> f : matches) {
            if (p.identifiers().isEmpty()) {
                break;
            }
            Map<K, V> i1 = Util.common(p, f);
            Map<K, Pair<V>> i2 = Util.conflicting(p, f);
            Map<K, V> i3 = Util.exclusive(p, f);
            EntityState<K, V, M> min = Util.min(system, p, f);
            EntityState<K, V, M> max = Util.max(system, p, f);
            if (Util.greaterThan(system, i1.keySet(), i2.keySet()) && system.mergeable(p.metadata(), f.metadata())) {
                // TODO add I3 to max
                Map<K, V> ids = new HashMap<>();
                ids.putAll(max.identifiers());
                ids.putAll(i3);
                M metadata = system.merge(p.metadata(), f.metadata());
                EntityState<K, V, M> next = system.createEntityState(ids, metadata);
                p = next;
            } else {
                Map<K, V> ids = new HashMap<>();
                ids.putAll(min.identifiers());
                for (K k : i1.keySet()) {
                    ids.remove(k);
                }
                if (!min.identifiers().isEmpty()) {
                    EntityState<K, V, M> next = system.createEntityState(ids, min.metadata());
                    set.add(next);
                }
                p = max;
            }
        }
        if (!p.identifiers().isEmpty()) {
            set.add(p);
        }
        return new MergeResult<K, V, M>(matches, set);
    }

}
