package ru.ckateptb.commons.ioc.guava.base;

import java.util.Iterator;

public final class Splitter {
    private final CharMatcher trimmer;
    private final boolean omitEmptyStrings;
    private final Strategy strategy;
    private final int limit;

    private Splitter(Strategy strategy) {
        this(strategy, false, CharMatcher.none(), Integer.MAX_VALUE);
    }

    private Splitter(Strategy strategy, boolean omitEmptyStrings, CharMatcher trimmer, int limit) {
        this.strategy = strategy;
        this.omitEmptyStrings = omitEmptyStrings;
        this.trimmer = trimmer;
        this.limit = limit;
    }

    public static Splitter on(char separator) {
        return on(CharMatcher.is(separator));
    }

    public static Splitter on(final CharMatcher separatorMatcher) {
        Preconditions.checkNotNull(separatorMatcher);
        return new Splitter(new Strategy() {
            public SplittingIterator iterator(Splitter splitter, CharSequence toSplit) {
                return new SplittingIterator(splitter, toSplit) {
                    int separatorStart(int start) {
                        return separatorMatcher.indexIn(this.toSplit, start);
                    }

                    int separatorEnd(int separatorPosition) {
                        return separatorPosition + 1;
                    }
                };
            }
        });
    }

    public static Splitter on(final String separator) {
        Preconditions.checkArgument(separator.length() != 0, "The separator may not be the empty string.");
        return separator.length() == 1 ? on(separator.charAt(0)) : new Splitter(new Strategy() {
            public SplittingIterator iterator(Splitter splitter, CharSequence toSplit) {
                return new SplittingIterator(splitter, toSplit) {
                    public int separatorStart(int start) {
                        int separatorLength = separator.length();
                        int p = start;

                        label24:
                        for(int last = this.toSplit.length() - separatorLength; p <= last; ++p) {
                            for(int i = 0; i < separatorLength; ++i) {
                                if (this.toSplit.charAt(i + p) != separator.charAt(i)) {
                                    continue label24;
                                }
                            }

                            return p;
                        }

                        return -1;
                    }

                    public int separatorEnd(int separatorPosition) {
                        return separatorPosition + separator.length();
                    }
                };
            }
        });
    }

    public Splitter omitEmptyStrings() {
        return new Splitter(this.strategy, true, this.trimmer, this.limit);
    }

    public Iterable<String> split(final CharSequence sequence) {
        Preconditions.checkNotNull(sequence);
        return new Iterable<String>() {
            public Iterator<String> iterator() {
                return Splitter.this.splittingIterator(sequence);
            }

            public String toString() {
                return Joiner.on(", ").appendTo((new StringBuilder()).append('['), this).append(']').toString();
            }
        };
    }

    private Iterator<String> splittingIterator(CharSequence sequence) {
        return this.strategy.iterator(this, sequence);
    }

    private abstract static class SplittingIterator extends AbstractIterator<String> {
        final CharSequence toSplit;
        final CharMatcher trimmer;
        final boolean omitEmptyStrings;
        int offset = 0;
        int limit;

        abstract int separatorStart(int var1);

        abstract int separatorEnd(int var1);

        protected SplittingIterator(Splitter splitter, CharSequence toSplit) {
            this.trimmer = splitter.trimmer;
            this.omitEmptyStrings = splitter.omitEmptyStrings;
            this.limit = splitter.limit;
            this.toSplit = toSplit;
        }

        protected String computeNext() {
            int nextStart = this.offset;

            while(true) {
                while(this.offset != -1) {
                    int start = nextStart;
                    int separatorPosition = this.separatorStart(this.offset);
                    int end;
                    if (separatorPosition == -1) {
                        end = this.toSplit.length();
                        this.offset = -1;
                    } else {
                        end = separatorPosition;
                        this.offset = this.separatorEnd(separatorPosition);
                    }

                    if (this.offset != nextStart) {
                        while(start < end && this.trimmer.matches(this.toSplit.charAt(start))) {
                            ++start;
                        }

                        while(end > start && this.trimmer.matches(this.toSplit.charAt(end - 1))) {
                            --end;
                        }

                        if (!this.omitEmptyStrings || start != end) {
                            if (this.limit == 1) {
                                end = this.toSplit.length();

                                for(this.offset = -1; end > start && this.trimmer.matches(this.toSplit.charAt(end - 1)); --end) {
                                }
                            } else {
                                --this.limit;
                            }

                            return this.toSplit.subSequence(start, end).toString();
                        }

                        nextStart = this.offset;
                    } else {
                        ++this.offset;
                        if (this.offset > this.toSplit.length()) {
                            this.offset = -1;
                        }
                    }
                }

                return (String)this.endOfData();
            }
        }
    }

    private interface Strategy {
        Iterator<String> iterator(Splitter var1, CharSequence var2);
    }
}