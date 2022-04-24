package ru.ckateptb.commons.ioc.guava.collect;

public final class ObjectArrays {
    public static <T> T[] newArray(T[] reference, int length) {
        return Platform.newArray(reference, length);
    }

    static Object[] checkElementsNotNull(Object... array) {
        return checkElementsNotNull(array, array.length);
    }

    static Object[] checkElementsNotNull(Object[] array, int length) {
        for(int i = 0; i < length; ++i) {
            checkElementNotNull(array[i], i);
        }

        return array;
    }

    static Object checkElementNotNull(Object element, int index) {
        if (element == null) {
            throw new NullPointerException((new StringBuilder(20)).append("at index ").append(index).toString());
        } else {
            return element;
        }
    }
}
