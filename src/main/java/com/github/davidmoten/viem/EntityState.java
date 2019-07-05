package com.github.davidmoten.viem;

import java.util.Map;

public interface EntityState<K, V, M> {

    Map<K, V> identifiers();

    M metadata();

}
