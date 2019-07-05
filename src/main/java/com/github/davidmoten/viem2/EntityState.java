package com.github.davidmoten.viem2;

import java.util.Map;

public interface EntityState<K, V, M> {

    Map<K, V> identifiers();

    M metadata();

}
