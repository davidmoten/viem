package com.github.davidmoten.viem;

import com.github.davidmoten.viem.KeyValue;

final class KeyValueImpl implements KeyValue<String, String> {

    private final String key;
    private final String value;

    KeyValueImpl(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return "KeyValueImpl [key=" + key + ", value=" + value + "]";
    }
    
}
