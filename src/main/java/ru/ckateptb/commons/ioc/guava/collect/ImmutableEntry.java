package ru.ckateptb.commons.ioc.guava.collect;

import java.io.Serializable;

class ImmutableEntry<K, V> extends AbstractMapEntry<K, V> implements Serializable {
    final K key;
    final V value;

    ImmutableEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public final K getKey() {
        return this.key;
    }

    public final V getValue() {
        return this.value;
    }

    public final V setValue(V value) {
        throw new UnsupportedOperationException();
    }
}
