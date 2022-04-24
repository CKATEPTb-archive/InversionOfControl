package ru.ckateptb.commons.ioc.guava.collect;

final class NullnessCasts {
    static <T> T uncheckedCastNullableTToT(T t) {
        return t;
    }
}
