package ru.ckateptb.commons.ioc.guava.collect;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class ImmutableMap<K, V> implements Serializable, Map<K, V> {
    static final Map.Entry<?, ?>[] EMPTY_ENTRY_ARRAY = new Map.Entry[0];
    private transient ImmutableSet<Map.Entry<K, V>> entrySet;
    private transient ImmutableSet<K> keySet;
    private transient ImmutableCollection<V> values;

    public static <K, V> ImmutableMap<K, V> of() {
        return (ImmutableMap<K, V>)RegularImmutableMap.EMPTY;
    }

    public static <K, V> ImmutableMap<K, V> of(K k1, V v1) {
        return ImmutableBiMap.of(k1, v1);
    }

    static void checkNoConflict(boolean safe, String conflictDescription, Object entry1, Object entry2) {
        if (!safe) {
            throw conflictException(conflictDescription, entry1, entry2);
        }
    }

    static IllegalArgumentException conflictException(String conflictDescription, Object entry1, Object entry2) {
        String var3 = String.valueOf(entry1);
        String var4 = String.valueOf(entry2);
        return new IllegalArgumentException((new StringBuilder(34 + String.valueOf(conflictDescription).length() + String.valueOf(var3).length() + String.valueOf(var4).length())).append("Multiple entries with same ").append(conflictDescription).append(": ").append(var3).append(" and ").append(var4).toString());
    }

    public static <K, V> ImmutableMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        ImmutableMap kvMap;
        if (map instanceof ImmutableMap && !(map instanceof SortedMap)) {
            kvMap = (ImmutableMap)map;
            if (!kvMap.isPartialView()) {
                return kvMap;
            }
        } else if (map instanceof EnumMap) {
            kvMap = copyOfEnumMap((EnumMap)map);
            return kvMap;
        }

        return copyOf((Iterable)map.entrySet());
    }

    public static <K, V> ImmutableMap<K, V> copyOf(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        Map.Entry<K, V>[] entryArray = (Map.Entry[])Iterables.toArray(entries, EMPTY_ENTRY_ARRAY);
        switch (entryArray.length) {
            case 0:
                return of();
            case 1:
                Map.Entry<K, V> onlyEntry = (Map.Entry)Objects.requireNonNull(entryArray[0]);
                return of(onlyEntry.getKey(), onlyEntry.getValue());
            default:
                return RegularImmutableMap.fromEntries(entryArray);
        }
    }

    private static <K extends Enum<K>, V> ImmutableMap<K, V> copyOfEnumMap(EnumMap<K, ? extends V> original) {
        EnumMap<K, V> copy = new EnumMap(original);
        Iterator var2 = copy.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<K, V> entry = (Map.Entry)var2.next();
            CollectPreconditions.checkEntryNotNull(entry.getKey(), entry.getValue());
        }

        return ImmutableEnumMap.asImmutable(copy);
    }

    ImmutableMap() {
    }

    /** @deprecated */
    @Deprecated
    public final V put(K k, V v) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final V replace(K key, V value) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final void putAll(Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final V remove(Object o) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public boolean containsKey(Object key) {
        return this.get(key) != null;
    }

    public boolean containsValue(Object value) {
        return this.values().contains(value);
    }

    public abstract V get(Object var1);

    public final V getOrDefault(Object key, V defaultValue) {
        V result = this.get(key);
        return result != null ? result : defaultValue;
    }

    public ImmutableSet<Map.Entry<K, V>> entrySet() {
        ImmutableSet<Map.Entry<K, V>> result = this.entrySet;
        return result == null ? (this.entrySet = this.createEntrySet()) : result;
    }

    abstract ImmutableSet<Map.Entry<K, V>> createEntrySet();

    public ImmutableSet<K> keySet() {
        ImmutableSet<K> result = this.keySet;
        return result == null ? (this.keySet = this.createKeySet()) : result;
    }

    abstract ImmutableSet<K> createKeySet();

    UnmodifiableIterator<K> keyIterator() {
        final UnmodifiableIterator<Entry<K, V>> entryIterator = entrySet().iterator();
        return new UnmodifiableIterator<K>() {
            @Override
            public boolean hasNext() {
                return entryIterator.hasNext();
            }

            @Override
            public K next() {
                return entryIterator.next().getKey();
            }
        };
    }

    Spliterator<K> keySpliterator() {
        return CollectSpliterators.map(this.entrySet().spliterator(), Map.Entry::getKey);
    }

    public ImmutableCollection<V> values() {
        ImmutableCollection<V> result = this.values;
        return result == null ? (this.values = this.createValues()) : result;
    }

    abstract ImmutableCollection<V> createValues();

    public boolean equals(Object object) {
        return Maps.equalsImpl(this, object);
    }

    abstract boolean isPartialView();

    public int hashCode() {
        return Sets.hashCodeImpl(this.entrySet());
    }

    boolean isHashCodeFast() {
        return false;
    }

    public String toString() {
        return Maps.toStringImpl(this);
    }

    abstract static class IteratorBasedImmutableMap<K, V> extends ImmutableMap<K, V> {
        IteratorBasedImmutableMap() {
        }

        abstract UnmodifiableIterator<Map.Entry<K, V>> entryIterator();

        ImmutableSet<K> createKeySet() {
            return new ImmutableMapKeySet(this);
        }

        ImmutableSet<Map.Entry<K, V>> createEntrySet() {
            class EntrySetImpl extends ImmutableMapEntrySet<K, V> {
                EntrySetImpl() {
                }

                ImmutableMap<K, V> map() {
                    return IteratorBasedImmutableMap.this;
                }

                public UnmodifiableIterator<Map.Entry<K, V>> iterator() {
                    return IteratorBasedImmutableMap.this.entryIterator();
                }
            }

            return new EntrySetImpl();
        }

        ImmutableCollection<V> createValues() {
            return new ImmutableMapValues(this);
        }
    }
}
