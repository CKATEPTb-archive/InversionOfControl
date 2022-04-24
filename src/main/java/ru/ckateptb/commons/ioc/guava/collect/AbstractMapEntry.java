package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Objects;

import java.util.Map;

abstract class AbstractMapEntry<K, V> implements Map.Entry<K, V> {
    AbstractMapEntry() {
    }

    public abstract K getKey();

    public abstract V getValue();

    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object object) {
        if (!(object instanceof Map.Entry)) {
            return false;
        } else {
            Map.Entry<?, ?> that = (Map.Entry)object;
            return Objects.equal(this.getKey(), that.getKey()) && Objects.equal(this.getValue(), that.getValue());
        }
    }

    public int hashCode() {
        K k = this.getKey();
        V v = this.getValue();
        return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
    }

    public String toString() {
        String var1 = String.valueOf(this.getKey());
        String var2 = String.valueOf(this.getValue());
        return (new StringBuilder(1 + String.valueOf(var1).length() + String.valueOf(var2).length())).append(var1).append("=").append(var2).toString();
    }
}
