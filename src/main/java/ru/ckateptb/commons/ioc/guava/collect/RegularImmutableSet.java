package ru.ckateptb.commons.ioc.guava.collect;

import java.util.Spliterator;
import java.util.Spliterators;

final class RegularImmutableSet<E> extends ImmutableSet.CachingAsList<E> {
    private static final Object[] EMPTY_ARRAY = new Object[0];
    static final RegularImmutableSet<Object> EMPTY;
    private final transient Object[] elements;
    private final transient int hashCode;
    final transient Object[] table;
    private final transient int mask;

    RegularImmutableSet(Object[] elements, int hashCode, Object[] table, int mask) {
        this.elements = elements;
        this.hashCode = hashCode;
        this.table = table;
        this.mask = mask;
    }

    public boolean contains(Object target) {
        Object[] table = this.table;
        if (target != null && table.length != 0) {
            int i = Hashing.smearedHash(target);

            while(true) {
                i &= this.mask;
                Object candidate = table[i];
                if (candidate == null) {
                    return false;
                }

                if (candidate.equals(target)) {
                    return true;
                }

                ++i;
            }
        } else {
            return false;
        }
    }

    public int size() {
        return this.elements.length;
    }

    public UnmodifiableIterator<E> iterator() {
        return (UnmodifiableIterator<E>) Iterators.forArray(this.elements);
    }

    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this.elements, 1297);
    }

    Object[] internalArray() {
        return this.elements;
    }

    int internalArrayStart() {
        return 0;
    }

    int internalArrayEnd() {
        return this.elements.length;
    }

    int copyIntoArray(Object[] dst, int offset) {
        System.arraycopy(this.elements, 0, dst, offset, this.elements.length);
        return offset + this.elements.length;
    }

    ImmutableList<E> createAsList() {
        return (ImmutableList)(this.table.length == 0 ? ImmutableList.of() : new RegularImmutableAsList(this, this.elements));
    }

    boolean isPartialView() {
        return false;
    }

    public int hashCode() {
        return this.hashCode;
    }

    boolean isHashCodeFast() {
        return true;
    }

    static {
        EMPTY = new RegularImmutableSet(EMPTY_ARRAY, 0, EMPTY_ARRAY, 0);
    }
}
