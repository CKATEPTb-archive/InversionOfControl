package ru.ckateptb.commons.ioc.guava.collect;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Spliterator;
import java.util.function.Consumer;

final class ImmutableEnumSet<E extends Enum<E>> extends ImmutableSet<E> {
    private final transient EnumSet<E> delegate;
    private transient int hashCode;

    static ImmutableSet asImmutable(EnumSet set) {
        switch (set.size()) {
            case 0:
                return ImmutableSet.of();
            case 1:
                return ImmutableSet.of(Iterables.getOnlyElement(set));
            default:
                return new ImmutableEnumSet(set);
        }
    }

    private ImmutableEnumSet(EnumSet<E> delegate) {
        this.delegate = delegate;
    }

    boolean isPartialView() {
        return false;
    }

    public UnmodifiableIterator<E> iterator() {
        return Iterators.unmodifiableIterator(this.delegate.iterator());
    }

    public Spliterator<E> spliterator() {
        return this.delegate.spliterator();
    }

    public void forEach(Consumer<? super E> action) {
        this.delegate.forEach(action);
    }

    public int size() {
        return this.delegate.size();
    }

    public boolean contains(Object object) {
        return this.delegate.contains(object);
    }

    public boolean containsAll(Collection<?> collection) {
        if (collection instanceof ImmutableEnumSet) {
            collection = ((ImmutableEnumSet)collection).delegate;
        }

        return this.delegate.containsAll((Collection)collection);
    }

    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else {
            if (object instanceof ImmutableEnumSet) {
                object = ((ImmutableEnumSet)object).delegate;
            }

            return this.delegate.equals(object);
        }
    }

    boolean isHashCodeFast() {
        return true;
    }

    public int hashCode() {
        int result = this.hashCode;
        return result == 0 ? (this.hashCode = this.delegate.hashCode()) : result;
    }

    public String toString() {
        return this.delegate.toString();
    }
}
