package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Preconditions;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;


public abstract class ImmutableList<E> extends ImmutableCollection<E> implements List<E>, RandomAccess {
    public static <E> ImmutableList<E> of() {
        return (ImmutableList<E>) RegularImmutableList.EMPTY;
    }

    public static <E> ImmutableList<E> of(E element) {
        return new SingletonImmutableList(element);
    }

    public static <E> ImmutableList<E> copyOf(E[] elements) {
        switch (elements.length) {
            case 0:
                return of();
            case 1:
                return of(elements[0]);
            default:
                return construct((Object[])elements.clone());
        }
    }

    private static <E> ImmutableList<E> construct(Object... elements) {
        return asImmutableList(ObjectArrays.checkElementsNotNull(elements));
    }

    static <E> ImmutableList<E> asImmutableList(Object[] elements) {
        return asImmutableList(elements, elements.length);
    }

    static <E> ImmutableList<E> asImmutableList( Object[] elements, int length) {
        switch (length) {
            case 0:
                return of();
            case 1:
                @SuppressWarnings("unchecked") // our callers put only E instances into the array
                E onlyElement = (E) requireNonNull(elements[0]);
                return of(onlyElement);
            default:
                @SuppressWarnings("nullness")
                Object[] elementsWithoutTrailingNulls =
                        length < elements.length ? Arrays.copyOf(elements, length) : elements;
                return new RegularImmutableList<E>(elementsWithoutTrailingNulls);
        }
    }

    ImmutableList() {
    }

    public UnmodifiableIterator<E> iterator() {
        return this.listIterator();
    }

    public UnmodifiableListIterator<E> listIterator() {
        return this.listIterator(0);
    }

    public UnmodifiableListIterator<E> listIterator(int index) {
        return new AbstractIndexedListIterator<E>(this.size(), index) {
            protected E get(int index) {
                return ImmutableList.this.get(index);
            }
        };
    }

    public void forEach(Consumer<? super E> consumer) {
        Preconditions.checkNotNull(consumer);
        int n = this.size();

        for(int i = 0; i < n; ++i) {
            consumer.accept(this.get(i));
        }

    }

    public int indexOf(Object object) {
        return object == null ? -1 : Lists.indexOfImpl(this, object);
    }

    public int lastIndexOf(Object object) {
        return object == null ? -1 : Lists.lastIndexOfImpl(this, object);
    }

    public boolean contains(Object object) {
        return this.indexOf(object) >= 0;
    }

    public ImmutableList<E> subList(int fromIndex, int toIndex) {
        Preconditions.checkPositionIndexes(fromIndex, toIndex, this.size());
        int length = toIndex - fromIndex;
        if (length == this.size()) {
            return this;
        } else if (length == 0) {
            return of();
        } else {
            return length == 1 ? of(this.get(fromIndex)) : this.subListUnchecked(fromIndex, toIndex);
        }
    }

    ImmutableList<E> subListUnchecked(int fromIndex, int toIndex) {
        return new SubList(fromIndex, toIndex - fromIndex);
    }

    /** @deprecated */
    @Deprecated
    public final boolean addAll(int index, Collection<? extends E> newElements) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final E remove(int index) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final void replaceAll(UnaryOperator<E> operator) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final void sort(Comparator<? super E> c) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final ImmutableList<E> asList() {
        return this;
    }

    public Spliterator<E> spliterator() {
        return CollectSpliterators.indexed(this.size(), 1296, this::get);
    }

    int copyIntoArray(Object[] dst, int offset) {
        int size = this.size();

        for(int i = 0; i < size; ++i) {
            dst[offset + i] = this.get(i);
        }

        return offset + size;
    }

    public boolean equals(Object obj) {
        return Lists.equalsImpl(this, obj);
    }

    public int hashCode() {
        int hashCode = 1;
        int n = this.size();

        for(int i = 0; i < n; ++i) {
            hashCode = 31 * hashCode + this.get(i).hashCode();
            hashCode = ~(~hashCode);
        }

        return hashCode;
    }

    public static <E> Builder<E> builder() {
        return new Builder();
    }

    public static final class Builder<E> extends ImmutableCollection.Builder<E> {
        Object[] contents;
        private int size;
        private boolean forceCopy;

        public Builder() {
            this(4);
        }

        Builder(int capacity) {
            this.contents = new Object[capacity];
            this.size = 0;
        }

        private void getReadyToExpandTo(int minCapacity) {
            if (this.contents.length < minCapacity) {
                this.contents = Arrays.copyOf(this.contents, expandedCapacity(this.contents.length, minCapacity));
                this.forceCopy = false;
            } else if (this.forceCopy) {
                this.contents = Arrays.copyOf(this.contents, this.contents.length);
                this.forceCopy = false;
            }

        }

        public Builder<E> add(E element) {
            Preconditions.checkNotNull(element);
            this.getReadyToExpandTo(this.size + 1);
            this.contents[this.size++] = element;
            return this;
        }

        public Builder<E> addAll(Iterable<? extends E> elements) {
            Preconditions.checkNotNull(elements);
            if (elements instanceof Collection) {
                Collection<?> collection = (Collection)elements;
                this.getReadyToExpandTo(this.size + collection.size());
                if (collection instanceof ImmutableCollection) {
                    ImmutableCollection<?> immutableCollection = (ImmutableCollection)collection;
                    this.size = immutableCollection.copyIntoArray(this.contents, this.size);
                    return this;
                }
            }

            super.addAll(elements);
            return this;
        }

        public Builder<E> addAll(Iterator<? extends E> elements) {
            super.addAll(elements);
            return this;
        }

        public ImmutableList<E> build() {
            this.forceCopy = true;
            return ImmutableList.asImmutableList(this.contents, this.size);
        }
    }

    class SubList extends ImmutableList<E> {
        final transient int offset;
        final transient int length;

        SubList(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }

        public int size() {
            return this.length;
        }

        public E get(int index) {
            Preconditions.checkElementIndex(index, this.length);
            return ImmutableList.this.get(index + this.offset);
        }

        public ImmutableList<E> subList(int fromIndex, int toIndex) {
            Preconditions.checkPositionIndexes(fromIndex, toIndex, this.length);
            return ImmutableList.this.subList(fromIndex + this.offset, toIndex + this.offset);
        }

        boolean isPartialView() {
            return true;
        }
    }
}