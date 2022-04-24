package ru.ckateptb.commons.ioc.guava.collect;

import java.util.Iterator;

public abstract class UnmodifiableIterator<E> implements Iterator<E> {
    protected UnmodifiableIterator() {
    }

    /** @deprecated */
    @Deprecated
    public final void remove() {
        throw new UnsupportedOperationException();
    }
}
