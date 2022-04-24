package ru.ckateptb.commons.ioc.guava.collect;

public abstract class ImmutableBiMap<K, V> extends ImmutableBiMapFauxverideShim<K, V> implements BiMap<K, V> {
    public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1) {
        return new SingletonImmutableBiMap(k1, v1);
    }

    ImmutableBiMap() {
    }

    public abstract ImmutableBiMap<V, K> inverse();

    public ImmutableSet<V> values() {
        return this.inverse().keySet();
    }

    final ImmutableSet<V> createValues() {
        throw new AssertionError("should never be called");
    }
}