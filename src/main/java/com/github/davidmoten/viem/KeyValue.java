package com.github.davidmoten.viem;

public interface KeyValue<K, V> {

    K key();

    V value();

    public static <K, V> KeyValue<K, V> create(K key, V value) {
        return new KeyValueDefault<K, V>(key, value);
    }

}
