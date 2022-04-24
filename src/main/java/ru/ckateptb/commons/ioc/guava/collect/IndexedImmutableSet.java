package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Preconditions;

import java.util.Spliterator;
import java.util.function.Consumer;

abstract class IndexedImmutableSet<E> extends ImmutableSet.CachingAsList<E> {
    IndexedImmutableSet() {
    }

    abstract E get(int var1);

    public UnmodifiableIterator<E> iterator() {
        return this.asList().iterator();
    }

    public Spliterator<E> spliterator() {
        return CollectSpliterators.indexed(this.size(), 1297, this::get);
    }

    public void forEach(Consumer<? super E> consumer) {
        Preconditions.checkNotNull(consumer);
        int n = this.size();

        for(int i = 0; i < n; ++i) {
            consumer.accept(this.get(i));
        }

    }

    int copyIntoArray(Object[] dst, int offset) {
        return this.asList().copyIntoArray(dst, offset);
    }

    ImmutableList<E> createAsList() {
        return new ImmutableAsList<E>() {
            public E get(int index) {
                return IndexedImmutableSet.this.get(index);
            }

            boolean isPartialView() {
                return IndexedImmutableSet.this.isPartialView();
            }

            public int size() {
                return IndexedImmutableSet.this.size();
            }

            ImmutableCollection<E> delegateCollection() {
                return IndexedImmutableSet.this;
            }
        };
    }
}
