package ru.ckateptb.commons.ioc.guava.base;

final class NullnessCasts {
    static <T> T uncheckedCastNullableTToT(T t) {
        return t;
    }
}