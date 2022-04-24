package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

final class JdkBackedImmutableMap<K, V> extends ImmutableMap<K, V> {
    private final transient Map<K, V> delegateMap;
    private final transient ImmutableList<Map.Entry<K, V>> entries;

    static <K, V> ImmutableMap<K, V> create(
            int n, Entry<K, V>[] entryArray, boolean throwIfDuplicateKeys) {
        Map<K, V> delegateMap = Maps.newHashMapWithExpectedSize(n);
        Map<K, V> duplicates = null;
        int dupCount = 0;
        for (int i = 0; i < n; i++) {
            entryArray[i] = RegularImmutableMap.makeImmutable(requireNonNull(entryArray[i]));
            K key = entryArray[i].getKey();
            V value = entryArray[i].getValue();
            V oldValue = delegateMap.put(key, value);
            if (oldValue != null) {
                if (throwIfDuplicateKeys) {
                    throw conflictException("key", entryArray[i], entryArray[i].getKey() + "=" + oldValue);
                }
                if (duplicates == null) {
                    duplicates = new HashMap<>();
                }
                duplicates.put(key, value);
                dupCount++;
            }
        }
        if (duplicates != null) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Entry<K, V>[] newEntryArray = new Entry[n - dupCount];
            for (int inI = 0, outI = 0; inI < n; inI++) {
                Entry<K, V> entry = requireNonNull(entryArray[inI]);
                K key = entry.getKey();
                if (duplicates.containsKey(key)) {
                    V value = duplicates.get(key);
                    if (value == null) {
                        continue; // delete this duplicate
                    }
                    entry = new ImmutableMapEntry<>(key, value);
                    duplicates.put(key, null);
                }
                newEntryArray[outI++] = entry;
            }
            entryArray = newEntryArray;
        }
        return new JdkBackedImmutableMap<>(delegateMap, ImmutableList.asImmutableList(entryArray, n));
    }

    JdkBackedImmutableMap(Map<K, V> delegateMap, ImmutableList<Map.Entry<K, V>> entries) {
        this.delegateMap = delegateMap;
        this.entries = entries;
    }

    public int size() {
        return this.entries.size();
    }

    public V get(Object key) {
        return this.delegateMap.get(key);
    }

    ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        return new ImmutableMapEntrySet.RegularEntrySet(this, this.entries);
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        Preconditions.checkNotNull(action);
        this.entries.forEach((e) -> {
            action.accept(e.getKey(), e.getValue());
        });
    }

    ImmutableSet<K> createKeySet() {
        return new ImmutableMapKeySet(this);
    }

    ImmutableCollection<V> createValues() {
        return new ImmutableMapValues(this);
    }

    boolean isPartialView() {
        return false;
    }
}
