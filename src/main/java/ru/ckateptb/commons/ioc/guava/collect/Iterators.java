package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Objects;
import ru.ckateptb.commons.ioc.guava.base.Preconditions;
import ru.ckateptb.commons.ioc.guava.base.Predicate;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class Iterators {
    static <T> UnmodifiableListIterator<T> emptyListIterator() {
        return (UnmodifiableListIterator<T>) Iterators.ArrayItr.EMPTY;
    }

    public static <T> UnmodifiableIterator<T> unmodifiableIterator(final Iterator<? extends T> iterator) {
        Preconditions.checkNotNull(iterator);
        if (iterator instanceof UnmodifiableIterator) {
            UnmodifiableIterator<T> result = (UnmodifiableIterator)iterator;
            return result;
        } else {
            return new UnmodifiableIterator<T>() {
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                public T next() {
                    return iterator.next();
                }
            };
        }
    }

    public static boolean contains(Iterator<?> iterator, Object element) {
        if (element == null) {
            while(iterator.hasNext()) {
                if (iterator.next() == null) {
                    return true;
                }
            }
        } else {
            while(iterator.hasNext()) {
                if (element.equals(iterator.next())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean elementsEqual(Iterator<?> iterator1, Iterator<?> iterator2) {
        while(true) {
            if (iterator1.hasNext()) {
                if (!iterator2.hasNext()) {
                    return false;
                }

                Object o1 = iterator1.next();
                Object o2 = iterator2.next();
                if (Objects.equal(o1, o2)) {
                    continue;
                }

                return false;
            }

            return !iterator2.hasNext();
        }
    }

    public static String toString(Iterator<?> iterator) {
        StringBuilder sb = (new StringBuilder()).append('[');
        boolean first = true;

        while(iterator.hasNext()) {
            if (!first) {
                sb.append(", ");
            }

            first = false;
            sb.append(iterator.next());
        }

        return sb.append(']').toString();
    }

    public static <T> T getOnlyElement(Iterator<T> iterator) {
        T first = iterator.next();
        if (!iterator.hasNext()) {
            return first;
        } else {
            StringBuilder sb = (new StringBuilder()).append("expected one element but was: <").append(first);

            for(int i = 0; i < 4 && iterator.hasNext(); ++i) {
                sb.append(", ").append(iterator.next());
            }

            if (iterator.hasNext()) {
                sb.append(", ...");
            }

            sb.append('>');
            throw new IllegalArgumentException(sb.toString());
        }
    }

    public static <T> boolean addAll(Collection<T> addTo, Iterator<? extends T> iterator) {
        Preconditions.checkNotNull(addTo);
        Preconditions.checkNotNull(iterator);

        boolean wasModified;
        for(wasModified = false; iterator.hasNext(); wasModified |= addTo.add(iterator.next())) {
        }

        return wasModified;
    }

    public static <T> UnmodifiableIterator<T> filter(final Iterator<T> unfiltered, final Predicate<? super T> retainIfTrue) {
        Preconditions.checkNotNull(unfiltered);
        Preconditions.checkNotNull(retainIfTrue);
        return new AbstractIterator<T>() {
            protected T computeNext() {
                while(true) {
                    if (unfiltered.hasNext()) {
                        T element = unfiltered.next();
                        if (!retainIfTrue.apply(element)) {
                            continue;
                        }

                        return element;
                    }

                    return this.endOfData();
                }
            }
        };
    }

    @SafeVarargs
    public static <T> UnmodifiableIterator<T> forArray(T... array) {
        return forArray(array, 0, array.length, 0);
    }

    static <T> UnmodifiableListIterator<T> forArray(T[] array, int offset, int length, int index) {
        Preconditions.checkArgument(length >= 0);
        int end = offset + length;
        Preconditions.checkPositionIndexes(offset, end, array.length);
        Preconditions.checkPositionIndex(index, length);
        return (UnmodifiableListIterator)(length == 0 ? emptyListIterator() : new ArrayItr(array, offset, length, index));
    }

    public static <T> UnmodifiableIterator<T> singletonIterator(final T value) {
        return new UnmodifiableIterator<T>() {
            boolean done;

            public boolean hasNext() {
                return !this.done;
            }

            public T next() {
                if (this.done) {
                    throw new NoSuchElementException();
                } else {
                    this.done = true;
                    return value;
                }
            }
        };
    }

    private static final class ArrayItr<T> extends AbstractIndexedListIterator<T> {
        static final UnmodifiableListIterator<Object> EMPTY = new Iterators.ArrayItr<>(new Object[0], 0, 0, 0);
        private final T[] array;
        private final int offset;

        ArrayItr(T[] array, int offset, int length, int index) {
            super(length, index);
            this.array = array;
            this.offset = offset;
        }

        protected T get(int index) {
            return this.array[this.offset + index];
        }
    }
}
