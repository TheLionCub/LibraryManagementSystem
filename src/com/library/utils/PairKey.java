package com.library.utils;

public class PairKey<T, V> {
    private final T publicKey;
    private final V key;

    public PairKey(T publicKey, V key) {
        this.publicKey = publicKey;
        this.key = key;
    }

    @Override
    public String toString() {
        return publicKey.toString();
    }

    public T getPublicKey() {
        return publicKey;
    }

    public V getKey() {
        return key;
    }
}
