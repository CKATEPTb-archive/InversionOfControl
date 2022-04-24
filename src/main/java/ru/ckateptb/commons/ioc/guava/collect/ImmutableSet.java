package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Preconditions;
import ru.ckateptb.commons.ioc.guava.math.IntMath;

import java.math.RoundingMode;
import java.util.*;

import static ru.ckateptb.commons.ioc.guava.base.Preconditions.checkNotNull;


public abstract class ImmutableSet<E> extends ImmutableCollection<E> implements Set<E> {
    public static <E> ImmutableSet<E> of() {
        return (ImmutableSet<E>) RegularImmutableSet.EMPTY;
    }

    public static <E> ImmutableSet<E> of(E element) {
        return new SingletonImmutableSet(element);
    }

    private static <E> ImmutableSet<E> constructUnknownDuplication(int n, Object... elements) {
        return construct(n, Math.max(4, IntMath.sqrt(n, RoundingMode.CEILING)), elements);
    }

    private static <E> ImmutableSet<E> construct(int n, int expectedSize, Object... elements) {
        switch (n) {
            case 0:
                return of();
            case 1:
                @SuppressWarnings("unchecked") // safe; elements contains only E's
                E elem = (E) elements[0];
                return of(elem);
            default:
                ImmutableSet.SetBuilderImpl<E> builder = new ImmutableSet.RegularSetBuilderImpl<E>(expectedSize);
                for (int i = 0; i < n; i++) {
                    @SuppressWarnings("unchecked")
                    E e = (E) checkNotNull(elements[i]);
                    builder = builder.add(e);
                }
                return builder.review().build();
        }
    }

    public static <E> ImmutableSet<E> copyOf(Collection<? extends E> elements) {
        if (elements instanceof ImmutableSet && !(elements instanceof SortedSet)) {
            ImmutableSet<E> set = (ImmutableSet) elements;
            if (!set.isPartialView()) {
                return set;
            }
        } else if (elements instanceof EnumSet) {
            return copyOfEnumSet((EnumSet) elements);
        }

        Object[] array = elements.toArray();
        return elements instanceof Set ? construct(array.length, array.length, array) : constructUnknownDuplication(array.length, array);
    }

    public static <E> ImmutableSet<E> copyOf(Iterable<? extends E> elements) {
        return elements instanceof Collection ? copyOf((Collection) elements) : copyOf(elements.iterator());
    }

    public static <E> ImmutableSet<E> copyOf(Iterator<? extends E> elements) {
        if (!elements.hasNext()) {
            return of();
        } else {
            E first = elements.next();
            return !elements.hasNext() ? of(first) : (new Builder()).add(first).addAll(elements).build();
        }
    }

    private static ImmutableSet copyOfEnumSet(EnumSet enumSet) {
        return ImmutableEnumSet.asImmutable(EnumSet.copyOf(enumSet));
    }

    ImmutableSet() {
    }

    boolean isHashCodeFast() {
        return false;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else {
            return object instanceof ImmutableSet && this.isHashCodeFast() && ((ImmutableSet) object).isHashCodeFast() && this.hashCode() != object.hashCode() ? false : Sets.equalsImpl(this, object);
        }
    }

    public int hashCode() {
        return Sets.hashCodeImpl(this);
    }

    public abstract UnmodifiableIterator<E> iterator();

    public static <E> Builder<E> builder() {
        return new Builder();
    }

    static int chooseTableSize(int setSize) {
        setSize = Math.max(setSize, 2);
        if (setSize >= 751619276) {
            Preconditions.checkArgument(setSize < 1073741824, "collection too large");
            return 1073741824;
        } else {
            int tableSize;
            for (tableSize = Integer.highestOneBit(setSize - 1) << 1; (double) tableSize * 0.7 < (double) setSize; tableSize <<= 1) {
            }

            return tableSize;
        }
    }

    private static final class JdkBackedSetBuilderImpl<E> extends SetBuilderImpl<E> {
        private final Set<Object> delegate;

        JdkBackedSetBuilderImpl(SetBuilderImpl<E> toCopy) {
            super(toCopy);
            this.delegate = Sets.newHashSetWithExpectedSize(this.distinct);

            for (int i = 0; i < this.distinct; ++i) {
                this.delegate.add(Objects.requireNonNull(this.dedupedElements[i]));
            }

        }

        SetBuilderImpl<E> add(E e) {
            checkNotNull(e);
            if (this.delegate.add(e)) {
                this.addDedupedElement(e);
            }

            return this;
        }

        SetBuilderImpl<E> copy() {
            return new JdkBackedSetBuilderImpl(this);
        }

        ImmutableSet<E> build() {
            switch (this.distinct) {
                case 0:
                    return ImmutableSet.of();
                case 1:
                    return ImmutableSet.of(Objects.requireNonNull(this.dedupedElements[0]));
                default:
                    return new JdkBackedImmutableSet(this.delegate, ImmutableList.asImmutableList(this.dedupedElements, this.distinct));
            }
        }
    }

    private static final class RegularSetBuilderImpl<E> extends SetBuilderImpl<E> {
        private Object[] hashTable;
        private int maxRunBeforeFallback;
        private int expandTableThreshold;
        private int hashCode;

        RegularSetBuilderImpl(int expectedCapacity) {
            super(expectedCapacity);
            this.hashTable = null;
            this.maxRunBeforeFallback = 0;
            this.expandTableThreshold = 0;
        }

        RegularSetBuilderImpl(RegularSetBuilderImpl<E> toCopy) {
            super(toCopy);
            this.hashTable = toCopy.hashTable == null ? null : (Object[]) toCopy.hashTable.clone();
            this.maxRunBeforeFallback = toCopy.maxRunBeforeFallback;
            this.expandTableThreshold = toCopy.expandTableThreshold;
            this.hashCode = toCopy.hashCode;
        }

        SetBuilderImpl<E> add(E e) {
            checkNotNull(e);
            if (this.hashTable == null) {
                if (this.distinct == 0) {
                    this.addDedupedElement(e);
                    return this;
                } else {
                    this.ensureTableCapacity(this.dedupedElements.length);
                    E elem = this.dedupedElements[0];
                    --this.distinct;
                    return this.insertInHashTable(elem).add(e);
                }
            } else {
                return this.insertInHashTable(e);
            }
        }

        private SetBuilderImpl<E> insertInHashTable(E e) {
            Objects.requireNonNull(this.hashTable);
            int eHash = e.hashCode();
            int i0 = Hashing.smear(eHash);
            int mask = this.hashTable.length - 1;

            for (int i = i0; i - i0 < this.maxRunBeforeFallback; ++i) {
                int index = i & mask;
                Object tableEntry = this.hashTable[index];
                if (tableEntry == null) {
                    this.addDedupedElement(e);
                    this.hashTable[index] = e;
                    this.hashCode += eHash;
                    this.ensureTableCapacity(this.distinct);
                    return this;
                }

                if (tableEntry.equals(e)) {
                    return this;
                }
            }

            return (new JdkBackedSetBuilderImpl(this)).add(e);
        }

        SetBuilderImpl<E> copy() {
            return new RegularSetBuilderImpl(this);
        }

        SetBuilderImpl<E> review() {
            if (this.hashTable == null) {
                return this;
            } else {
                int targetTableSize = ImmutableSet.chooseTableSize(this.distinct);
                if (targetTableSize * 2 < this.hashTable.length) {
                    this.hashTable = rebuildHashTable(targetTableSize, this.dedupedElements, this.distinct);
                    this.maxRunBeforeFallback = maxRunBeforeFallback(targetTableSize);
                    this.expandTableThreshold = (int) (0.7 * (double) targetTableSize);
                }

                return (SetBuilderImpl) (hashFloodingDetected(this.hashTable) ? new JdkBackedSetBuilderImpl(this) : this);
            }
        }

        ImmutableSet<E> build() {
            switch (this.distinct) {
                case 0:
                    return ImmutableSet.of();
                case 1:
                    return ImmutableSet.of(Objects.requireNonNull(this.dedupedElements[0]));
                default:
                    Object[] elements = this.distinct == this.dedupedElements.length ? this.dedupedElements : Arrays.copyOf(this.dedupedElements, this.distinct);
                    return new RegularImmutableSet(elements, this.hashCode, (Object[]) Objects.requireNonNull(this.hashTable), this.hashTable.length - 1);
            }
        }

        static Object[] rebuildHashTable(int newTableSize, Object[] elements, int n) {
            Object[] hashTable = new Object[newTableSize];
            int mask = hashTable.length - 1;

            for (int i = 0; i < n; ++i) {
                Object e = Objects.requireNonNull(elements[i]);
                int j0 = Hashing.smear(e.hashCode());
                int j = j0;

                while (true) {
                    int index = j & mask;
                    if (hashTable[index] == null) {
                        hashTable[index] = e;
                        break;
                    }

                    ++j;
                }
            }

            return hashTable;
        }

        void ensureTableCapacity(int minCapacity) {
            int newTableSize;
            if (this.hashTable == null) {
                newTableSize = ImmutableSet.chooseTableSize(minCapacity);
                this.hashTable = new Object[newTableSize];
            } else {
                if (minCapacity <= this.expandTableThreshold || this.hashTable.length >= 1073741824) {
                    return;
                }

                newTableSize = this.hashTable.length * 2;
                this.hashTable = rebuildHashTable(newTableSize, this.dedupedElements, this.distinct);
            }

            this.maxRunBeforeFallback = maxRunBeforeFallback(newTableSize);
            this.expandTableThreshold = (int) (0.7 * (double) newTableSize);
        }

        static boolean hashFloodingDetected(Object[] hashTable) {
            int maxRunBeforeFallback = maxRunBeforeFallback(hashTable.length);
            int mask = hashTable.length - 1;
            int knownRunStart = 0;
            int knownRunEnd = 0;

            while (true) {
                label33:
                while (knownRunStart < hashTable.length) {
                    if (knownRunStart == knownRunEnd && hashTable[knownRunStart] == null) {
                        if (hashTable[knownRunStart + maxRunBeforeFallback - 1 & mask] == null) {
                            knownRunStart += maxRunBeforeFallback;
                        } else {
                            ++knownRunStart;
                        }

                        knownRunEnd = knownRunStart;
                    } else {
                        for (int j = knownRunStart + maxRunBeforeFallback - 1; j >= knownRunEnd; --j) {
                            if (hashTable[j & mask] == null) {
                                knownRunEnd = knownRunStart + maxRunBeforeFallback;
                                knownRunStart = j + 1;
                                continue label33;
                            }
                        }

                        return true;
                    }
                }

                return false;
            }
        }

        static int maxRunBeforeFallback(int tableSize) {
            return 13 * IntMath.log2(tableSize, RoundingMode.UNNECESSARY);
        }
    }

    private static final class EmptySetBuilderImpl<E> extends SetBuilderImpl<E> {
        private static final EmptySetBuilderImpl<Object> INSTANCE = new EmptySetBuilderImpl();

        static <E> SetBuilderImpl<E> instance() {
            return (SetBuilderImpl<E>) INSTANCE;
        }

        private EmptySetBuilderImpl() {
            super(0);
        }

        SetBuilderImpl<E> add(E e) {
            return (new RegularSetBuilderImpl(4)).add(e);
        }

        SetBuilderImpl<E> copy() {
            return this;
        }

        ImmutableSet<E> build() {
            return ImmutableSet.of();
        }
    }

    private abstract static class SetBuilderImpl<E> {
        E[] dedupedElements;
        int distinct;

        SetBuilderImpl(int expectedCapacity) {
            this.dedupedElements = (E[]) new Object[expectedCapacity];
            this.distinct = 0;
        }

        SetBuilderImpl(SetBuilderImpl<E> toCopy) {
            this.dedupedElements = Arrays.copyOf(toCopy.dedupedElements, toCopy.dedupedElements.length);
            this.distinct = toCopy.distinct;
        }

        private void ensureCapacity(int minCapacity) {
            if (minCapacity > this.dedupedElements.length) {
                int newCapacity = ImmutableCollection.Builder.expandedCapacity(this.dedupedElements.length, minCapacity);
                this.dedupedElements = Arrays.copyOf(this.dedupedElements, newCapacity);
            }

        }

        final void addDedupedElement(E e) {
            this.ensureCapacity(this.distinct + 1);
            this.dedupedElements[this.distinct++] = e;
        }

        abstract SetBuilderImpl<E> add(E var1);

        abstract SetBuilderImpl<E> copy();

        SetBuilderImpl<E> review() {
            return this;
        }

        abstract ImmutableSet<E> build();
    }

    public static class Builder<E> extends ImmutableCollection.Builder<E> {
        private SetBuilderImpl<E> impl;
        boolean forceCopy;

        public Builder() {
            this(0);
        }

        Builder(int capacity) {
            if (capacity > 0) {
                this.impl = new RegularSetBuilderImpl(capacity);
            } else {
                this.impl = ImmutableSet.EmptySetBuilderImpl.instance();
            }

        }

        final void copyIfNecessary() {
            if (this.forceCopy) {
                this.copy();
                this.forceCopy = false;
            }

        }

        void copy() {
            Objects.requireNonNull(this.impl);
            this.impl = this.impl.copy();
        }

        public Builder<E> add(E element) {
            Objects.requireNonNull(this.impl);
            checkNotNull(element);
            this.copyIfNecessary();
            this.impl = this.impl.add(element);
            return this;
        }

        public Builder<E> addAll(Iterable<? extends E> elements) {
            super.addAll(elements);
            return this;
        }

        public Builder<E> addAll(Iterator<? extends E> elements) {
            super.addAll(elements);
            return this;
        }

        public ImmutableSet<E> build() {
            Objects.requireNonNull(this.impl);
            this.forceCopy = true;
            this.impl = this.impl.review();
            return this.impl.build();
        }
    }

    abstract static class CachingAsList<E> extends ImmutableSet<E> {
        private transient ImmutableList<E> asList;

        CachingAsList() {
        }

        public ImmutableList<E> asList() {
            ImmutableList<E> result = this.asList;
            return result == null ? (this.asList = this.createAsList()) : result;
        }

        ImmutableList<E> createAsList() {
            return new RegularImmutableAsList(this, this.toArray());
        }
    }
}
