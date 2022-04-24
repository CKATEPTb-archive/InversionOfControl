package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Preconditions;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.IntStream;

final class CollectSpliterators {
    static <T> Spliterator<T> indexed(int size, int extraCharacteristics, IntFunction<T> function) {
        return indexed(size, extraCharacteristics, function, (Comparator)null);
    }

    static <T> Spliterator<T> indexed(int size, final int extraCharacteristics, final IntFunction<T> function, final Comparator<? super T> comparator) {
        if (comparator != null) {
            Preconditions.checkArgument((extraCharacteristics & 4) != 0);
        }

        class WithCharacteristics implements Spliterator<T> {
            private final Spliterator.OfInt delegate;

            WithCharacteristics(Spliterator.OfInt delegate) {
                this.delegate = delegate;
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                return delegate.tryAdvance((IntConsumer) i -> action.accept(function.apply(i)));
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                delegate.forEachRemaining((IntConsumer) i -> action.accept(function.apply(i)));
            }

            public Spliterator<T> trySplit() {
                Spliterator.OfInt split = this.delegate.trySplit();
                return split == null ? null : new WithCharacteristics(split);
            }

            public long estimateSize() {
                return this.delegate.estimateSize();
            }

            public int characteristics() {
                return 16464 | extraCharacteristics;
            }

            public Comparator<? super T> getComparator() {
                if (this.hasCharacteristics(4)) {
                    return comparator;
                } else {
                    throw new IllegalStateException();
                }
            }
        }

        return new WithCharacteristics(IntStream.range(0, size).spliterator());
    }

    static <InElementT, OutElementT> Spliterator<OutElementT> map(final Spliterator<InElementT> fromSpliterator, final Function<? super InElementT, ? extends OutElementT> function) {
        Preconditions.checkNotNull(fromSpliterator);
        Preconditions.checkNotNull(function);
        return new Spliterator<OutElementT>() {
            public boolean tryAdvance(Consumer<? super OutElementT> action) {
                return fromSpliterator.tryAdvance((fromElement) -> {
                    action.accept(function.apply(fromElement));
                });
            }

            public void forEachRemaining(Consumer<? super OutElementT> action) {
                fromSpliterator.forEachRemaining((fromElement) -> {
                    action.accept(function.apply(fromElement));
                });
            }

            public Spliterator<OutElementT> trySplit() {
                Spliterator<InElementT> fromSplit = fromSpliterator.trySplit();
                return fromSplit != null ? CollectSpliterators.map(fromSplit, function) : null;
            }

            public long estimateSize() {
                return fromSpliterator.estimateSize();
            }

            public int characteristics() {
                return fromSpliterator.characteristics() & -262;
            }
        };
    }

    static <T> Spliterator<T> filter(final Spliterator<T> fromSpliterator, final Predicate<? super T> predicate) {
        Preconditions.checkNotNull(fromSpliterator);
        Preconditions.checkNotNull(predicate);

        class Splitr implements Spliterator<T>, Consumer<T> {
            T holder = null;

            Splitr() {
            }

            public void accept(T t) {
                this.holder = t;
            }

            public boolean tryAdvance(Consumer<? super T> action) {
                while(true) {
                    if (fromSpliterator.tryAdvance(this)) {
                        boolean var3;
                        try {
                            T next = NullnessCasts.uncheckedCastNullableTToT(this.holder);
                            if (!predicate.test(next)) {
                                continue;
                            }

                            action.accept(next);
                            var3 = true;
                        } finally {
                            this.holder = null;
                        }

                        return var3;
                    }

                    return false;
                }
            }

            public Spliterator<T> trySplit() {
                Spliterator<T> fromSplit = fromSpliterator.trySplit();
                return fromSplit == null ? null : CollectSpliterators.filter(fromSplit, predicate);
            }

            public long estimateSize() {
                return fromSpliterator.estimateSize() / 2L;
            }

            public Comparator<? super T> getComparator() {
                return fromSpliterator.getComparator();
            }

            public int characteristics() {
                return fromSpliterator.characteristics() & 277;
            }
        }

        return new Splitr();
    }

    static <InElementT, OutElementT> Spliterator<OutElementT> flatMap(Spliterator<InElementT> fromSpliterator, Function<? super InElementT, Spliterator<OutElementT>> function, int topCharacteristics, long topSize) {
        Preconditions.checkArgument((topCharacteristics & 16384) == 0, "flatMap does not support SUBSIZED characteristic");
        Preconditions.checkArgument((topCharacteristics & 4) == 0, "flatMap does not support SORTED characteristic");
        Preconditions.checkNotNull(fromSpliterator);
        Preconditions.checkNotNull(function);
        return new FlatMapSpliteratorOfObject((Spliterator)null, fromSpliterator, function, topCharacteristics, topSize);
    }

    static final class FlatMapSpliteratorOfObject<InElementT, OutElementT> extends FlatMapSpliterator<InElementT, OutElementT, Spliterator<OutElementT>> {
        FlatMapSpliteratorOfObject(Spliterator<OutElementT> prefix, Spliterator<InElementT> from, Function<? super InElementT, Spliterator<OutElementT>> function, int characteristics, long estimatedSize) {
            super(prefix, from, function, FlatMapSpliteratorOfObject::new, characteristics, estimatedSize);
        }
    }

    abstract static class FlatMapSpliterator<InElementT, OutElementT, OutSpliteratorT extends Spliterator<OutElementT>> implements Spliterator<OutElementT> {
        OutSpliteratorT prefix;
        final Spliterator<InElementT> from;
        final Function<? super InElementT, OutSpliteratorT> function;
        final Factory<InElementT, OutSpliteratorT> factory;
        int characteristics;
        long estimatedSize;

        FlatMapSpliterator(OutSpliteratorT prefix, Spliterator<InElementT> from, Function<? super InElementT, OutSpliteratorT> function, Factory<InElementT, OutSpliteratorT> factory, int characteristics, long estimatedSize) {
            this.prefix = prefix;
            this.from = from;
            this.function = function;
            this.factory = factory;
            this.characteristics = characteristics;
            this.estimatedSize = estimatedSize;
        }

        @Override
        public final boolean tryAdvance(Consumer<? super OutElementT> action) {
            while (true) {
                if (prefix != null && prefix.tryAdvance(action)) {
                    if (estimatedSize != Long.MAX_VALUE) {
                        estimatedSize--;
                    }
                    return true;
                } else {
                    prefix = null;
                }
                if (!from.tryAdvance(fromElement -> prefix = function.apply(fromElement))) {
                    return false;
                }
            }
        }

        public final void forEachRemaining(Consumer<? super OutElementT> action) {
            if (this.prefix != null) {
                this.prefix.forEachRemaining(action);
                this.prefix = null;
            }

            this.from.forEachRemaining((fromElement) -> {
                Spliterator<OutElementT> elements = (Spliterator)this.function.apply(fromElement);
                if (elements != null) {
                    elements.forEachRemaining(action);
                }

            });
            this.estimatedSize = 0L;
        }

        public final OutSpliteratorT trySplit() {
            Spliterator<InElementT> fromSplit = this.from.trySplit();
            if (fromSplit != null) {
                int splitCharacteristics = this.characteristics & -65;
                long estSplitSize = this.estimateSize();
                if (estSplitSize < Long.MAX_VALUE) {
                    estSplitSize /= 2L;
                    this.estimatedSize -= estSplitSize;
                    this.characteristics = splitCharacteristics;
                }

                OutSpliteratorT result = this.factory.newFlatMapSpliterator(this.prefix, fromSplit, this.function, splitCharacteristics, estSplitSize);
                this.prefix = null;
                return result;
            } else if (this.prefix != null) {
                OutSpliteratorT result = this.prefix;
                this.prefix = null;
                return result;
            } else {
                return null;
            }
        }

        public final long estimateSize() {
            if (this.prefix != null) {
                this.estimatedSize = Math.max(this.estimatedSize, this.prefix.estimateSize());
            }

            return Math.max(this.estimatedSize, 0L);
        }

        public final int characteristics() {
            return this.characteristics;
        }

        @FunctionalInterface
        interface Factory<InElementT, OutSpliteratorT extends Spliterator<?>> {
            OutSpliteratorT newFlatMapSpliterator(OutSpliteratorT var1, Spliterator<InElementT> var2, Function<? super InElementT, OutSpliteratorT> var3, int var4, long var5);
        }
    }
}
