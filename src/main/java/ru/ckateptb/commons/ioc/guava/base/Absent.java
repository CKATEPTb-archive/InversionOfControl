package ru.ckateptb.commons.ioc.guava.base;

final class Absent<T> extends Optional<T> {
    static final Absent<Object> INSTANCE = new Absent<>();

    @SuppressWarnings("unchecked") // implementation is "fully variant"
    static <T> Optional<T> withType() {
        return (Optional<T>) INSTANCE;
    }

    private Absent() {
    }

    public T or(T defaultValue) {
        return Preconditions.checkNotNull(defaultValue, "use Optional.orNull() instead of Optional.or(null)");
    }

    public boolean equals(Object object) {
        return object == this;
    }

    public int hashCode() {
        return 2040732332;
    }

    public String toString() {
        return "Optional.absent()";
    }
}
