package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Preconditions;

import java.util.EnumMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.BiConsumer;

final class ImmutableEnumMap<K extends Enum<K>, V> extends ImmutableMap.IteratorBasedImmutableMap<K, V> {
    private final transient EnumMap<K, V> delegate;

    static <K extends Enum<K>, V> ImmutableMap<K, V> asImmutable(EnumMap<K, V> map) {
        switch (map.size()) {
            case 0:
                return ImmutableMap.of();
            case 1:
                Entry<K, V> entry = Iterables.getOnlyElement(map.entrySet());
                return ImmutableMap.of(entry.getKey(), entry.getValue());
            default:
                return new ImmutableEnumMap<>(map);
        }
    }

    private ImmutableEnumMap(EnumMap<K, V> delegate) {
        this.delegate = delegate;
        Preconditions.checkArgument(!delegate.isEmpty());
    }

    UnmodifiableIterator<K> keyIterator() {
        return Iterators.unmodifiableIterator(this.delegate.keySet().iterator());
    }

    Spliterator<K> keySpliterator() {
        return this.delegate.keySet().spliterator();
    }

    public int size() {
        return this.delegate.size();
    }

    public boolean containsKey(Object key) {
        return this.delegate.containsKey(key);
    }

    public V get(Object key) {
        return this.delegate.get(key);
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else {
            if (object instanceof ImmutableEnumMap) {
                object = ((ImmutableEnumMap) object).delegate;
            }

            return this.delegate.equals(object);
        }
    }

    UnmodifiableIterator<Map.Entry<K, V>> entryIterator() {
        return Maps.unmodifiableEntryIterator(this.delegate.entrySet().iterator());
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        this.delegate.forEach(action);
    }

    boolean isPartialView() {
        return false;
    }
}