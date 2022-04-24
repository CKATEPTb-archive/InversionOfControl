package ru.ckateptb.commons.ioc.guava.collect;


class ImmutableMapEntry<K, V> extends ImmutableEntry<K, V> {
    static <K, V> ImmutableMapEntry<K, V>[] createEntryArray(int size) {
        return new ImmutableMapEntry[size];
    }

    ImmutableMapEntry(K key, V value) {
        super(key, value);
        CollectPreconditions.checkEntryNotNull(key, value);
    }

    ImmutableMapEntry<K, V> getNextInKeyBucket() {
        return null;
    }

    boolean isReusable() {
        return true;
    }

    static class NonTerminalImmutableMapEntry<K, V> extends ImmutableMapEntry<K, V> {
        private final transient ImmutableMapEntry<K, V> nextInKeyBucket;

        NonTerminalImmutableMapEntry(K key, V value, ImmutableMapEntry<K, V> nextInKeyBucket) {
            super(key, value);
            this.nextInKeyBucket = nextInKeyBucket;
        }

        final ImmutableMapEntry<K, V> getNextInKeyBucket() {
            return this.nextInKeyBucket;
        }

        final boolean isReusable() {
            return false;
        }
    }
}