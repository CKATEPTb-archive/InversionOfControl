package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Preconditions;

import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

final class ImmutableMapKeySet<K, V> extends IndexedImmutableSet<K> {
    private final ImmutableMap<K, V> map;

    ImmutableMapKeySet(ImmutableMap<K, V> map) {
        this.map = map;
    }

    public int size() {
        return this.map.size();
    }

    public UnmodifiableIterator<K> iterator() {
        return this.map.keyIterator();
    }

    public Spliterator<K> spliterator() {
        return this.map.keySpliterator();
    }

    public boolean contains(Object object) {
        return this.map.containsKey(object);
    }

    K get(int index) {
        return map.entrySet().asList().get(index).getKey();
    }

    public void forEach(Consumer<? super K> action) {
        Preconditions.checkNotNull(action);
        this.map.forEach((k, v) -> {
            action.accept(k);
        });
    }

    boolean isPartialView() {
        return true;
    }
}
