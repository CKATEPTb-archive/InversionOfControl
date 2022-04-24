package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Preconditions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Maps {
    public static <K, V> HashMap<K, V> newHashMapWithExpectedSize(int expectedSize) {
        return new HashMap(capacity(expectedSize));
    }

    static int capacity(int expectedSize) {
        if (expectedSize < 3) {
            CollectPreconditions.checkNonnegative(expectedSize, "expectedSize");
            return expectedSize + 1;
        } else {
            return expectedSize < 1073741824 ? (int)((float)expectedSize / 0.75F + 1.0F) : Integer.MAX_VALUE;
        }
    }

    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap();
    }

    public static <K, V> Map.Entry<K, V> immutableEntry(K key, V value) {
        return new ImmutableEntry(key, value);
    }

    static <K, V> Map.Entry<K, V> unmodifiableEntry(final Map.Entry<? extends K, ? extends V> entry) {
        Preconditions.checkNotNull(entry);
        return new AbstractMapEntry<K, V>() {
            public K getKey() {
                return entry.getKey();
            }

            public V getValue() {
                return entry.getValue();
            }
        };
    }

    static <K, V> UnmodifiableIterator<Map.Entry<K, V>> unmodifiableEntryIterator(final Iterator<Map.Entry<K, V>> entryIterator) {
        return new UnmodifiableIterator<Map.Entry<K, V>>() {
            public boolean hasNext() {
                return entryIterator.hasNext();
            }

            public Map.Entry<K, V> next() {
                return Maps.unmodifiableEntry((Map.Entry)entryIterator.next());
            }
        };
    }

    static boolean equalsImpl(Map<?, ?> map, Object object) {
        if (map == object) {
            return true;
        } else if (object instanceof Map) {
            Map<?, ?> o = (Map)object;
            return map.entrySet().equals(o.entrySet());
        } else {
            return false;
        }
    }

    static String toStringImpl(Map<?, ?> map) {
        StringBuilder sb = Collections2.newStringBuilderForCollection(map.size()).append('{');
        boolean first = true;
        Iterator var3 = map.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<?, ?> entry = (Map.Entry)var3.next();
            if (!first) {
                sb.append(", ");
            }

            first = false;
            sb.append(entry.getKey()).append('=').append(entry.getValue());
        }

        return sb.append('}').toString();
    }
}
