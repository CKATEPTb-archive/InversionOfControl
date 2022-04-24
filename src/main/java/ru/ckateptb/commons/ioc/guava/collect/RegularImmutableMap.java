package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Preconditions;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;
import static ru.ckateptb.commons.ioc.guava.base.Preconditions.checkPositionIndex;
import static ru.ckateptb.commons.ioc.guava.collect.CollectPreconditions.checkEntryNotNull;
import static ru.ckateptb.commons.ioc.guava.collect.ImmutableMapEntry.createEntryArray;

final class RegularImmutableMap<K, V> extends ImmutableMap<K, V> {
    static final ImmutableMap<Object, Object> EMPTY;
    final transient Map.Entry<K, V>[] entries;
    private final transient ImmutableMapEntry<K, V>[] table;
    private final transient int mask;

    static <K, V> ImmutableMap<K, V> fromEntries(Map.Entry<K, V>... entries) {
        return fromEntryArray(entries.length, entries, true);
    }

    static <K, V> ImmutableMap<K, V> fromEntryArray(
            int n, Entry<K, V>[] entryArray, boolean throwIfDuplicateKeys) {
        checkPositionIndex(n, entryArray.length);
        if (n == 0) {
            @SuppressWarnings("unchecked") // it has no entries so the type variables don't matter
            ImmutableMap<K, V> empty = (ImmutableMap<K, V>) EMPTY;
            return empty;
        }
        try {
            return fromEntryArrayCheckingBucketOverflow(n, entryArray, throwIfDuplicateKeys);
        } catch (RegularImmutableMap.BucketOverflowException e) {
            return JdkBackedImmutableMap.create(n, entryArray, throwIfDuplicateKeys);
        }
    }

    private static <K, V> ImmutableMap<K, V> fromEntryArrayCheckingBucketOverflow(
            int n, Entry<K, V>[] entryArray, boolean throwIfDuplicateKeys)
            throws RegularImmutableMap.BucketOverflowException {
        /*
         * The cast is safe: n==entryArray.length means that we have filled the whole array with Entry
         * instances, in which case it is safe to cast it from an array of nullable entries to an array
         * of non-null entries.
         */
        @SuppressWarnings("nullness")
        Entry<K, V>[] entries =
                (n == entryArray.length) ? (Entry<K, V>[]) entryArray : createEntryArray(n);
        int tableSize = Hashing.closedTableSize(n, 1.2);
        ImmutableMapEntry<K, V>[] table = createEntryArray(tableSize);
        int mask = tableSize - 1;
        // If duplicates are allowed, this IdentityHashMap will record the final Entry for each
        // duplicated key. We will use this final Entry to overwrite earlier slots in the entries array
        // that have the same key. Then a second pass will remove all but the first of the slots that
        // have this Entry. The value in the map becomes false when this first entry has been copied, so
        // we know not to copy the remaining ones.
        IdentityHashMap<Entry<K, V>, Boolean> duplicates = null;
        int dupCount = 0;
        for (int entryIndex = n - 1; entryIndex >= 0; entryIndex--) {
            // requireNonNull is safe because the first `n` elements have been filled in.
            Entry<K, V> entry = requireNonNull(entryArray[entryIndex]);
            K key = entry.getKey();
            V value = entry.getValue();
            checkEntryNotNull(key, value);
            int tableIndex = Hashing.smear(key.hashCode()) & mask;
            ImmutableMapEntry<K, V> keyBucketHead = table[tableIndex];
            ImmutableMapEntry<K, V> effectiveEntry =
                    checkNoConflictInKeyBucket(key, value, keyBucketHead, throwIfDuplicateKeys);
            if (effectiveEntry == null) {
                // prepend, not append, so the entries can be immutable
                effectiveEntry =
                        (keyBucketHead == null)
                                ? makeImmutable(entry, key, value)
                                : new ImmutableMapEntry.NonTerminalImmutableMapEntry<K, V>(key, value, keyBucketHead);
                table[tableIndex] = effectiveEntry;
            } else {
                // We already saw this key, and the first value we saw (going backwards) is the one we are
                // keeping. So we won't touch table[], but we do still want to add the existing entry that
                // we found to entries[] so that we will see this key in the right place when iterating.
                if (duplicates == null) {
                    duplicates = new IdentityHashMap<>();
                }
                duplicates.put(effectiveEntry, true);
                dupCount++;
                // Make sure we are not overwriting the original entries array, in case we later do
                // buildOrThrow(). We would want an exception to include two values for the duplicate key.
                if (entries == entryArray) {
                    entries = entries.clone();
                }
            }
            entries[entryIndex] = effectiveEntry;
        }
        if (duplicates != null) {
            // Explicit type parameters needed here to avoid a problem with nullness inference.
            entries = RegularImmutableMap.<K, V>removeDuplicates(entries, n, n - dupCount, duplicates);
            int newTableSize = Hashing.closedTableSize(entries.length, 1.2);
            if (newTableSize != tableSize) {
                return fromEntryArrayCheckingBucketOverflow(
                        entries.length, entries, /* throwIfDuplicateKeys= */ true);
            }
        }
        return new RegularImmutableMap<>(entries, table, mask);
    }

    static <K, V> Map.Entry<K, V>[] removeDuplicates(Map.Entry<K, V>[] entries, int n, int newN, IdentityHashMap<Map.Entry<K, V>, Boolean> duplicates) {
        Map.Entry<K, V>[] newEntries = createEntryArray(newN);
        int in = 0;

        for(int out = 0; in < n; ++in) {
            Map.Entry<K, V> entry = entries[in];
            Boolean status = (Boolean)duplicates.get(entry);
            if (status != null) {
                if (!status) {
                    continue;
                }

                duplicates.put(entry, false);
            }

            newEntries[out++] = entry;
        }

        return newEntries;
    }

    static <K, V> ImmutableMapEntry<K, V> makeImmutable(Map.Entry<K, V> entry, K key, V value) {
        boolean reusable = entry instanceof ImmutableMapEntry && ((ImmutableMapEntry)entry).isReusable();
        return reusable ? (ImmutableMapEntry)entry : new ImmutableMapEntry(key, value);
    }

    static <K, V> ImmutableMapEntry<K, V> makeImmutable(Map.Entry<K, V> entry) {
        return makeImmutable(entry, entry.getKey(), entry.getValue());
    }

    private RegularImmutableMap(Map.Entry<K, V>[] entries, ImmutableMapEntry<K, V>[] table, int mask) {
        this.entries = entries;
        this.table = table;
        this.mask = mask;
    }

    static <K, V> ImmutableMapEntry<K, V> checkNoConflictInKeyBucket(Object key, Object newValue, ImmutableMapEntry<K, V> keyBucketHead, boolean throwIfDuplicateKeys) throws BucketOverflowException {
        for(int bucketSize = 0; keyBucketHead != null; keyBucketHead = keyBucketHead.getNextInKeyBucket()) {
            if (keyBucketHead.getKey().equals(key)) {
                if (!throwIfDuplicateKeys) {
                    return keyBucketHead;
                }

                String var5 = String.valueOf(key);
                String var6 = String.valueOf(newValue);
                checkNoConflict(false, "key", keyBucketHead, (new StringBuilder(1 + String.valueOf(var5).length() + String.valueOf(var6).length())).append(var5).append("=").append(var6).toString());
            }

            ++bucketSize;
            if (bucketSize > 8) {
                throw new BucketOverflowException();
            }
        }

        return null;
    }

    public V get(Object key) {
        return get(key, this.table, this.mask);
    }

    static <V> V get(Object key, ImmutableMapEntry<?, V>[] keyTable, int mask) {
        if (key != null && keyTable != null) {
            int index = Hashing.smear(key.hashCode()) & mask;

            for(ImmutableMapEntry<?, V> entry = keyTable[index]; entry != null; entry = entry.getNextInKeyBucket()) {
                Object candidateKey = entry.getKey();
                if (key.equals(candidateKey)) {
                    return entry.getValue();
                }
            }

            return null;
        } else {
            return null;
        }
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        Preconditions.checkNotNull(action);
        Map.Entry[] var2 = this.entries;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Map.Entry<K, V> entry = var2[var4];
            action.accept(entry.getKey(), entry.getValue());
        }

    }

    public int size() {
        return this.entries.length;
    }

    boolean isPartialView() {
        return false;
    }

    ImmutableSet<Map.Entry<K, V>> createEntrySet() {
        return new ImmutableMapEntrySet.RegularEntrySet(this, this.entries);
    }

    ImmutableSet<K> createKeySet() {
        return new KeySet(this);
    }

    ImmutableCollection<V> createValues() {
        return new Values(this);
    }

    static {
        EMPTY = new RegularImmutableMap(ImmutableMap.EMPTY_ENTRY_ARRAY, (ImmutableMapEntry[])null, 0);
    }

    private static final class Values<K, V> extends ImmutableList<V> {
        final RegularImmutableMap<K, V> map;

        Values(RegularImmutableMap<K, V> map) {
            this.map = map;
        }

        public V get(int index) {
            return this.map.entries[index].getValue();
        }

        public int size() {
            return this.map.size();
        }

        boolean isPartialView() {
            return true;
        }
    }

    private static final class KeySet<K> extends IndexedImmutableSet<K> {
        private final RegularImmutableMap<K, ?> map;

        KeySet(RegularImmutableMap<K, ?> map) {
            this.map = map;
        }

        K get(int index) {
            return this.map.entries[index].getKey();
        }

        public boolean contains(Object object) {
            return this.map.containsKey(object);
        }

        boolean isPartialView() {
            return true;
        }

        public int size() {
            return this.map.size();
        }
    }

    static class BucketOverflowException extends Exception {
        BucketOverflowException() {
        }
    }
}
