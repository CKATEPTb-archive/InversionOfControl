package ru.ckateptb.commons.ioc.guava.collect;

import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

abstract class ImmutableMapEntrySet<K, V> extends ImmutableSet.CachingAsList<Map.Entry<K, V>> {
    ImmutableMapEntrySet() {
    }

    abstract ImmutableMap<K, V> map();

    public int size() {
        return this.map().size();
    }

    public boolean contains(Object object) {
        if (!(object instanceof Map.Entry)) {
            return false;
        } else {
            Map.Entry<?, ?> entry = (Map.Entry)object;
            V value = this.map().get(entry.getKey());
            return value != null && value.equals(entry.getValue());
        }
    }

    boolean isPartialView() {
        return this.map().isPartialView();
    }

    boolean isHashCodeFast() {
        return this.map().isHashCodeFast();
    }

    public int hashCode() {
        return this.map().hashCode();
    }

    static final class RegularEntrySet<K, V> extends ImmutableMapEntrySet<K, V> {
        private final transient ImmutableMap<K, V> map;
        private final transient ImmutableList<Map.Entry<K, V>> entries;

        RegularEntrySet(ImmutableMap<K, V> map, Map.Entry<K, V>[] entries) {
            this(map, ImmutableList.asImmutableList(entries));
        }

        RegularEntrySet(ImmutableMap<K, V> map, ImmutableList<Map.Entry<K, V>> entries) {
            this.map = map;
            this.entries = entries;
        }

        ImmutableMap<K, V> map() {
            return this.map;
        }

        int copyIntoArray(Object[] dst, int offset) {
            return this.entries.copyIntoArray(dst, offset);
        }

        public UnmodifiableIterator<Map.Entry<K, V>> iterator() {
            return this.entries.iterator();
        }

        public Spliterator<Map.Entry<K, V>> spliterator() {
            return this.entries.spliterator();
        }

        public void forEach(Consumer<? super Map.Entry<K, V>> action) {
            this.entries.forEach(action);
        }

        ImmutableList<Map.Entry<K, V>> createAsList() {
            return new RegularImmutableAsList(this, this.entries);
        }
    }
}
