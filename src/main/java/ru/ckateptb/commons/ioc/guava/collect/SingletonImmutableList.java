package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Preconditions;

import java.util.Collections;
import java.util.Spliterator;

final class SingletonImmutableList<E> extends ImmutableList<E> {
    final transient E element;

    SingletonImmutableList(E element) {
        this.element = Preconditions.checkNotNull(element);
    }

    public E get(int index) {
        Preconditions.checkElementIndex(index, 1);
        return this.element;
    }

    public UnmodifiableIterator<E> iterator() {
        return Iterators.singletonIterator(this.element);
    }

    public Spliterator<E> spliterator() {
        return Collections.singleton(this.element).spliterator();
    }

    public int size() {
        return 1;
    }

    public ImmutableList<E> subList(int fromIndex, int toIndex) {
        Preconditions.checkPositionIndexes(fromIndex, toIndex, 1);
        return (ImmutableList)(fromIndex == toIndex ? ImmutableList.of() : this);
    }

    public String toString() {
        String var1 = this.element.toString();
        return (new StringBuilder(2 + String.valueOf(var1).length())).append('[').append(var1).append(']').toString();
    }

    boolean isPartialView() {
        return false;
    }
}
