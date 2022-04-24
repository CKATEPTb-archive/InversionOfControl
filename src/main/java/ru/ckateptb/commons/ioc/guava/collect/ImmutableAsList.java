package ru.ckateptb.commons.ioc.guava.collect;

abstract class ImmutableAsList<E> extends ImmutableList<E> {
    ImmutableAsList() {
    }

    abstract ImmutableCollection<E> delegateCollection();

    public boolean contains(Object target) {
        return this.delegateCollection().contains(target);
    }

    public int size() {
        return this.delegateCollection().size();
    }

    public boolean isEmpty() {
        return this.delegateCollection().isEmpty();
    }

    boolean isPartialView() {
        return this.delegateCollection().isPartialView();
    }
}
