package ru.ckateptb.commons.ioc.guava.collect;


public final class Collections2 {
    static StringBuilder newStringBuilderForCollection(int size) {
        CollectPreconditions.checkNonnegative(size, "size");
        return new StringBuilder((int)Math.min((long)size * 8L, 1073741824L));
    }
}
