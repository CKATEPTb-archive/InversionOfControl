package ru.ckateptb.commons.ioc.guava.collect;


import ru.ckateptb.commons.ioc.guava.base.Preconditions;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;

public abstract class ImmutableCollection<E> extends AbstractCollection<E> implements Serializable {
    private static final Object[] EMPTY_ARRAY = new Object[0];

    ImmutableCollection() {
    }

    public abstract UnmodifiableIterator<E> iterator();

    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, 1296);
    }

    public final Object[] toArray() {
        return this.toArray(EMPTY_ARRAY);
    }

    public final <T> T[] toArray(T[] other) {
        Preconditions.checkNotNull(other);
        int size = this.size();
        if (other.length < size) {
            Object[] internal = this.internalArray();
            if (internal != null) {
                return Platform.copy(internal, this.internalArrayStart(), this.internalArrayEnd(), other);
            }

            other = ObjectArrays.newArray(other, size);
        } else if (other.length > size) {
            other[size] = null;
        }

        this.copyIntoArray(other, 0);
        return other;
    }

    Object[] internalArray() {
        return null;
    }

    int internalArrayStart() {
        throw new UnsupportedOperationException();
    }

    int internalArrayEnd() {
        throw new UnsupportedOperationException();
    }

    public abstract boolean contains(Object var1);

    /** @deprecated */
    @Deprecated
    public final boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final boolean remove(Object object) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final boolean addAll(Collection<? extends E> newElements) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final boolean removeAll(Collection<?> oldElements) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final boolean retainAll(Collection<?> elementsToKeep) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    public ImmutableList<E> asList() {
        switch (this.size()) {
            case 0:
                return ImmutableList.of();
            case 1:
                return ImmutableList.of(this.iterator().next());
            default:
                return new RegularImmutableAsList(this, this.toArray());
        }
    }

    abstract boolean isPartialView();

    int copyIntoArray(Object[] dst, int offset) {
        Object e;
        for(UnmodifiableIterator var3 = this.iterator(); var3.hasNext(); dst[offset++] = e) {
            e = var3.next();
        }

        return offset;
    }

    public abstract static class Builder<E> {
        static int expandedCapacity(int oldCapacity, int minCapacity) {
            if (minCapacity < 0) {
                throw new AssertionError("cannot store more than MAX_VALUE elements");
            } else {
                int newCapacity = oldCapacity + (oldCapacity >> 1) + 1;
                if (newCapacity < minCapacity) {
                    newCapacity = Integer.highestOneBit(minCapacity - 1) << 1;
                }

                if (newCapacity < 0) {
                    newCapacity = Integer.MAX_VALUE;
                }

                return newCapacity;
            }
        }

        Builder() {
        }

        public abstract Builder<E> add(E var1);

        public ImmutableCollection.Builder<E> addAll(Iterable<? extends E> elements) {
            for (E element : elements) {
                add(element);
            }
            return this;
        }

        public Builder<E> addAll(Iterator<? extends E> elements) {
            while(elements.hasNext()) {
                this.add(elements.next());
            }

            return this;
        }
    }
}
