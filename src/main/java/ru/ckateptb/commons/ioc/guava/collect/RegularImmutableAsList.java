package ru.ckateptb.commons.ioc.guava.collect;

import java.util.function.Consumer;

class RegularImmutableAsList<E> extends ImmutableAsList<E> {
    private final ImmutableCollection<E> delegate;
    private final ImmutableList<? extends E> delegateList;

    RegularImmutableAsList(ImmutableCollection<E> delegate, ImmutableList<? extends E> delegateList) {
        this.delegate = delegate;
        this.delegateList = delegateList;
    }

    RegularImmutableAsList(ImmutableCollection<E> delegate, Object[] array) {
        this(delegate, ImmutableList.asImmutableList(array));
    }

    ImmutableCollection<E> delegateCollection() {
        return this.delegate;
    }

    public UnmodifiableListIterator<E> listIterator(int index) {
        return (UnmodifiableListIterator<E>) this.delegateList.listIterator(index);
    }

    public void forEach(Consumer<? super E> action) {
        this.delegateList.forEach(action);
    }

    int copyIntoArray(Object[] dst, int offset) {
        return this.delegateList.copyIntoArray(dst, offset);
    }

    Object[] internalArray() {
        return this.delegateList.internalArray();
    }

    int internalArrayStart() {
        return this.delegateList.internalArrayStart();
    }

    int internalArrayEnd() {
        return this.delegateList.internalArrayEnd();
    }

    public E get(int index) {
        return this.delegateList.get(index);
    }
}
