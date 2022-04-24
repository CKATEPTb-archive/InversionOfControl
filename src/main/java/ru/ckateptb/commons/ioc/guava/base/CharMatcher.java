package ru.ckateptb.commons.ioc.guava.base;

public abstract class CharMatcher implements Predicate<Character> {
    public static CharMatcher any() {
        return CharMatcher.Any.INSTANCE;
    }

    public static CharMatcher none() {
        return CharMatcher.None.INSTANCE;
    }

    public static CharMatcher is(char match) {
        return new Is(match);
    }

    public static CharMatcher isNot(char match) {
        return new IsNot(match);
    }

    protected CharMatcher() {
    }

    public abstract boolean matches(char var1);

    public CharMatcher negate() {
        return new Negated(this);
    }

    public int indexIn(CharSequence sequence, int start) {
        int length = sequence.length();
        Preconditions.checkPositionIndex(start, length);

        for(int i = start; i < length; ++i) {
            if (this.matches(sequence.charAt(i))) {
                return i;
            }
        }

        return -1;
    }

    /** @deprecated */
    @Deprecated
    public boolean apply(Character character) {
        return this.matches(character);
    }

    public String toString() {
        return super.toString();
    }

    private static String showCharacter(char c) {
        String hex = "0123456789ABCDEF";
        char[] tmp = new char[]{'\\', 'u', '\u0000', '\u0000', '\u0000', '\u0000'};

        for(int i = 0; i < 4; ++i) {
            tmp[5 - i] = hex.charAt(c & 15);
            c = (char)(c >> 4);
        }

        return String.copyValueOf(tmp);
    }

    private static final class IsNot extends FastMatcher {
        private final char match;

        IsNot(char match) {
            this.match = match;
        }

        public boolean matches(char c) {
            return c != this.match;
        }

        public CharMatcher negate() {
            return is(this.match);
        }

        public String toString() {
            String var1 = CharMatcher.showCharacter(this.match);
            return (new StringBuilder(21 + String.valueOf(var1).length())).append("CharMatcher.isNot('").append(var1).append("')").toString();
        }
    }

    private static final class Is extends FastMatcher {
        private final char match;

        Is(char match) {
            this.match = match;
        }

        public boolean matches(char c) {
            return c == this.match;
        }

        public CharMatcher negate() {
            return isNot(this.match);
        }

        public String toString() {
            String var1 = CharMatcher.showCharacter(this.match);
            return (new StringBuilder(18 + String.valueOf(var1).length())).append("CharMatcher.is('").append(var1).append("')").toString();
        }
    }

    private static class Negated extends CharMatcher {
        final CharMatcher original;

        Negated(CharMatcher original) {
            this.original = (CharMatcher)Preconditions.checkNotNull(original);
        }

        public boolean matches(char c) {
            return !this.original.matches(c);
        }

        public CharMatcher negate() {
            return this.original;
        }

        public String toString() {
            String var1 = String.valueOf(this.original);
            return (new StringBuilder(9 + String.valueOf(var1).length())).append(var1).append(".negate()").toString();
        }
    }

    private static final class None extends NamedFastMatcher {
        static final None INSTANCE = new None();

        private None() {
            super("CharMatcher.none()");
        }

        public boolean matches(char c) {
            return false;
        }

        public int indexIn(CharSequence sequence, int start) {
            int length = sequence.length();
            Preconditions.checkPositionIndex(start, length);
            return -1;
        }

        public CharMatcher negate() {
            return any();
        }
    }

    private static final class Any extends NamedFastMatcher {
        static final Any INSTANCE = new Any();

        private Any() {
            super("CharMatcher.any()");
        }

        public boolean matches(char c) {
            return true;
        }

        public int indexIn(CharSequence sequence, int start) {
            int length = sequence.length();
            Preconditions.checkPositionIndex(start, length);
            return start == length ? -1 : start;
        }

        public CharMatcher negate() {
            return none();
        }
    }

    static class NegatedFastMatcher extends Negated {
        NegatedFastMatcher(CharMatcher original) {
            super(original);
        }
    }

    abstract static class NamedFastMatcher extends FastMatcher {
        private final String description;

        NamedFastMatcher(String description) {
            this.description = (String)Preconditions.checkNotNull(description);
        }

        public final String toString() {
            return this.description;
        }
    }

    abstract static class FastMatcher extends CharMatcher {
        FastMatcher() {
        }

        public CharMatcher negate() {
            return new NegatedFastMatcher(this);
        }
    }
}
