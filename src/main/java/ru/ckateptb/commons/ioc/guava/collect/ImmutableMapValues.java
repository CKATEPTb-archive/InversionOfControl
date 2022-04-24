package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Preconditions;

import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

final class ImmutableMapValues<K, V> extends ImmutableCollection<V> {
    private final ImmutableMap<K, V> map;

    ImmutableMapValues(ImmutableMap<K, V> map) {
        this.map = map;
    }

    public int size() {
        return this.map.size();
    }

    public UnmodifiableIterator<V> iterator() {
        return new UnmodifiableIterator<V>() {
            final UnmodifiableIterator<Map.Entry<K, V>> entryItr;

            {
                this.entryItr = ImmutableMapValues.this.map.entrySet().iterator();
            }

            public boolean hasNext() {
                return this.entryItr.hasNext();
            }

            public V next() {
                return this.entryItr.next().getValue();
            }
        };
    }

    public Spliterator<V> spliterator() {
        return CollectSpliterators.map(this.map.entrySet().spliterator(), Map.Entry::getValue);
    }

    public boolean contains(Object object) {
        return object != null && Iterators.contains(this.iterator(), object);
    }

    boolean isPartialView() {
        return true;
    }

    public ImmutableList<V> asList() {
        final ImmutableList<Map.Entry<K, V>> entryList = this.map.entrySet().asList();
        return new ImmutableAsList<V>() {
            public V get(int index) {
                return entryList.get(index).getValue();
            }

            ImmutableCollection<V> delegateCollection() {
                return ImmutableMapValues.this;
            }
        };
    }

    public void forEach(Consumer<? super V> action) {
        Preconditions.checkNotNull(action);
        this.map.forEach((k, v) -> {
            action.accept(v);
        });
    }
}