package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Preconditions;

import java.util.Collection;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

public interface Multiset<E> extends Collection<E> {
    int size();

    Set<Entry<E>> entrySet();

    default void forEach(Consumer<? super E> action) {
        Preconditions.checkNotNull(action);
        this.entrySet().forEach((entry) -> {
            E elem = entry.getElement();
            int count = entry.getCount();

            for(int i = 0; i < count; ++i) {
                action.accept(elem);
            }

        });
    }

    default Spliterator<E> spliterator() {
        return Multisets.spliteratorImpl(this);
    }

    public interface Entry<E> {
        E getElement();

        int getCount();
    }
}