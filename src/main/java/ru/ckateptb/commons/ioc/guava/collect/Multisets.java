package ru.ckateptb.commons.ioc.guava.collect;

import java.util.Collections;
import java.util.Spliterator;

public final class Multisets {
    static <E> Spliterator<E> spliteratorImpl(Multiset<E> multiset) {
        Spliterator<Multiset.Entry<E>> entrySpliterator = multiset.entrySet().spliterator();
        return CollectSpliterators.flatMap(entrySpliterator, (entry) -> {
            return Collections.nCopies(entry.getCount(), entry.getElement()).spliterator();
        }, 64 | entrySpliterator.characteristics() & 1296, (long)multiset.size());
    }
}
