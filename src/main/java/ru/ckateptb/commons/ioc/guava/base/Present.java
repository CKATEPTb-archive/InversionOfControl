package ru.ckateptb.commons.ioc.guava.base;

final class Present<T> extends Optional<T> {
    private final T reference;

    Present(T reference) {
        this.reference = reference;
    }

    public T or(T defaultValue) {
        Preconditions.checkNotNull(defaultValue, "use Optional.orNull() instead of Optional.or(null)");
        return this.reference;
    }

    public boolean equals(Object object) {
        if (object instanceof Present) {
            Present<?> other = (Present)object;
            return this.reference.equals(other.reference);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return 1502476572 + this.reference.hashCode();
    }

    public String toString() {
        String var1 = String.valueOf(this.reference);
        return (new StringBuilder(13 + String.valueOf(var1).length())).append("Optional.of(").append(var1).append(")").toString();
    }
}