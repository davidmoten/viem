package com.github.davidmoten.viem;

import java.util.List;
import java.util.Set;

final class MergeResult<K,V, M> {

    final List<EntityState<K, V, M>> matches;
    final Set<EntityState<K, V, M>> newEntityStates;

    MergeResult(List<EntityState<K, V, M>> matches, Set<EntityState<K, V, M>> newEntityStates) {
        this.matches = matches;
        this.newEntityStates = newEntityStates;
    }

}
