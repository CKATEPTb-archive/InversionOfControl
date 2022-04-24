package ru.ckateptb.commons.ioc.guava.base;


import java.io.Serializable;

public abstract class Optional<T> implements Serializable {
    public static <T> Optional<T> absent() {
        return Absent.withType();
    }

    public static <T> Optional<T> of(T reference) {
        return new Present(Preconditions.checkNotNull(reference));
    }

    Optional() {
    }

    public abstract T or(T var1);
}