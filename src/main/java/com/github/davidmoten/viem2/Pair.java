package com.github.davidmoten.viem2;

final class Pair<T> {

    final T a;
    final T b;

    Pair(T a, T b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String toString() {
        return "Pair [a=" + a + ", b=" + b + "]";
    }
    
}
