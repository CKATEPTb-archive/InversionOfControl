package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Preconditions;

import java.util.Map;
import java.util.function.BiConsumer;

final class SingletonImmutableBiMap<K, V> extends ImmutableBiMap<K, V> {
    final transient K singleKey;
    final transient V singleValue;
    private final transient ImmutableBiMap<V, K> inverse;
    private transient ImmutableBiMap<V, K> lazyInverse;

    SingletonImmutableBiMap(K singleKey, V singleValue) {
        CollectPreconditions.checkEntryNotNull(singleKey, singleValue);
        this.singleKey = singleKey;
        this.singleValue = singleValue;
        this.inverse = null;
    }

    private SingletonImmutableBiMap(K singleKey, V singleValue, ImmutableBiMap<V, K> inverse) {
        this.singleKey = singleKey;
        this.singleValue = singleValue;
        this.inverse = inverse;
    }

    public V get(Object key) {
        return this.singleKey.equals(key) ? this.singleValue : null;
    }

    public int size() {
        return 1;
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        ((BiConsumer) Preconditions.checkNotNull(action)).accept(this.singleKey, this.singleValue);
    }

    public boolean containsKey(Object key) {
        return this.singleKey.equals(key);
    }

    public boolean containsValue(Object value) {
        return this.singleValue.equals(value);
    }

    boolean isPartialView() {
        return false;
    }

    ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        return ImmutableSet.of(Maps.immutableEntry(this.singleKey, this.singleValue));
    }

    ImmutableSet<K> createKeySet() {
        return ImmutableSet.of(this.singleKey);
    }

    public ImmutableBiMap<V, K> inverse() {
        if (this.inverse != null) {
            return this.inverse;
        } else {
            ImmutableBiMap<V, K> result = this.lazyInverse;
            return result == null ? (this.lazyInverse = new SingletonImmutableBiMap(this.singleValue, this.singleKey, this)) : result;
        }
    }
}
