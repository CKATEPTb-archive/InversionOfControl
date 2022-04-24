package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Predicate;
import ru.ckateptb.commons.ioc.guava.base.Predicates;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import static ru.ckateptb.commons.ioc.guava.base.Preconditions.checkNotNull;


public final class Iterables {
    public static String toString(Iterable<?> iterable) {
        return Iterators.toString(iterable.iterator());
    }

    public static <T> T getOnlyElement(Iterable<T> iterable) {
        return Iterators.getOnlyElement(iterable.iterator());
    }

    static <T> T[] toArray(Iterable<? extends T> iterable, T[] array) {
        Collection<? extends T> collection = castOrCopyToCollection(iterable);
        return collection.toArray(array);
    }

    private static <E> Collection<E> castOrCopyToCollection(Iterable<E> iterable) {
        return iterable instanceof Collection ? (Collection)iterable : Lists.newArrayList(iterable.iterator());
    }

    public static <T extends Object> Iterable<T> filter(
            final Iterable<T> unfiltered, final Predicate<? super T> retainIfTrue) {
        checkNotNull(unfiltered);
        checkNotNull(retainIfTrue);
        return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return Iterators.filter(unfiltered.iterator(), retainIfTrue);
            }

            @Override
            public void forEach(Consumer<? super T> action) {
                checkNotNull(action);
                unfiltered.forEach(
                        ( T a) -> {
                            if (retainIfTrue.test(a)) {
                                action.accept(a);
                            }
                        });
            }

            @Override
            public Spliterator<T> spliterator() {
                return CollectSpliterators.filter(unfiltered.spliterator(), retainIfTrue);
            }
        };
    }

    public static <T> Iterable<T> filter(final Iterable<?> unfiltered, final Class<T> desiredType) {
        checkNotNull(unfiltered);
        checkNotNull(desiredType);
        return (Iterable<T>) filter(unfiltered, Predicates.instanceOf(desiredType));
    }
}
