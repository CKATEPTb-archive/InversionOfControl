package ru.ckateptb.commons.ioc.guava.collect;

import java.lang.reflect.Array;
import java.util.Arrays;

final class Platform {
    static <T> T[] newArray(T[] reference, int length) {
        Class<?> type = reference.getClass().getComponentType();
        T[] result = (T[])Array.newInstance(type, length);
        return result;
    }

    static <T> T[] copy(Object[] source, int from, int to, T[] arrayOfType) {
        return (T[]) Arrays.copyOfRange(source, from, to, arrayOfType.getClass());
    }

    private Platform() {
    }
}
