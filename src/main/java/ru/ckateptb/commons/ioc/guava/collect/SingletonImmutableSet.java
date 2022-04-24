package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Preconditions;

final class SingletonImmutableSet<E> extends ImmutableSet<E> {
    final transient E element;

    SingletonImmutableSet(E element) {
        this.element = Preconditions.checkNotNull(element);
    }

    public int size() {
        return 1;
    }

    public boolean contains(Object target) {
        return this.element.equals(target);
    }

    public UnmodifiableIterator<E> iterator() {
        return Iterators.singletonIterator(this.element);
    }

    public ImmutableList<E> asList() {
        return ImmutableList.of(this.element);
    }

    boolean isPartialView() {
        return false;
    }

    int copyIntoArray(Object[] dst, int offset) {
        dst[offset] = this.element;
        return offset + 1;
    }

    public final int hashCode() {
        return this.element.hashCode();
    }

    public String toString() {
        String var1 = this.element.toString();
        return (new StringBuilder(2 + String.valueOf(var1).length())).append('[').append(var1).append(']').toString();
    }
}
