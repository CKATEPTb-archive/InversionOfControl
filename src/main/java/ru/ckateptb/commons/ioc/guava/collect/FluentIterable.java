package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Optional;
import ru.ckateptb.commons.ioc.guava.base.Predicate;

import java.util.Iterator;

public abstract class FluentIterable<E> implements Iterable<E> {
    private final Optional<Iterable<E>> iterableDelegate;

    protected FluentIterable() {
        this.iterableDelegate = Optional.absent();
    }

    FluentIterable(Iterable<E> iterable) {
        this.iterableDelegate = Optional.of(iterable);
    }

    private Iterable<E> getDelegate() {
        return iterableDelegate.or(this);
    }

    public static <E extends Object> FluentIterable<E> from(final Iterable<E> iterable) {
        return (iterable instanceof FluentIterable)
                ? (FluentIterable<E>) iterable
                : new FluentIterable<E>(iterable) {
            @Override
            public Iterator<E> iterator() {
                return iterable.iterator();
            }
        };
    }

    public String toString() {
        return Iterables.toString(this.getDelegate());
    }

    public final FluentIterable<E> filter(Predicate<? super E> predicate) {
        return from(Iterables.filter(getDelegate(), predicate));
    }

    public final <T> FluentIterable<T> filter(Class<T> type) {
        return from(Iterables.filter(this.getDelegate(), type));
    }

    public final ImmutableSet<E> toSet() {
        return ImmutableSet.copyOf(this.getDelegate());
    }
}
