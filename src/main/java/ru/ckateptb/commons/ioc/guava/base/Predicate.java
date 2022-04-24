package ru.ckateptb.commons.ioc.guava.base;

@FunctionalInterface
public interface Predicate<T> extends java.util.function.Predicate<T> {
    boolean apply(T var1);

    default boolean test(T input) {
        return this.apply(input);
    }
}
