package ru.ckateptb.commons.ioc.guava.collect;

import java.util.Set;

final class JdkBackedImmutableSet<E> extends IndexedImmutableSet<E> {
    private final Set<?> delegate;
    private final ImmutableList<E> delegateList;

    JdkBackedImmutableSet(Set<?> delegate, ImmutableList<E> delegateList) {
        this.delegate = delegate;
        this.delegateList = delegateList;
    }

    E get(int index) {
        return this.delegateList.get(index);
    }

    public boolean contains(Object object) {
        return this.delegate.contains(object);
    }

    boolean isPartialView() {
        return false;
    }

    public int size() {
        return this.delegateList.size();
    }
}
