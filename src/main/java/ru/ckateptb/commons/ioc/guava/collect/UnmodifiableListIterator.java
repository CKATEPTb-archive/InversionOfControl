package ru.ckateptb.commons.ioc.guava.collect;

import java.util.ListIterator;

public abstract class UnmodifiableListIterator<E> extends UnmodifiableIterator<E> implements ListIterator<E> {
    protected UnmodifiableListIterator() {
    }

    /** @deprecated */
    @Deprecated
    public final void add(E e) {
        throw new UnsupportedOperationException();
    }

    /** @deprecated */
    @Deprecated
    public final void set(E e) {
        throw new UnsupportedOperationException();
    }
}
