package ru.ckateptb.commons.ioc.guava.base;

public final class Objects extends ExtraObjectsMethodsForWeb {
    public static boolean equal(Object a, Object b) {
        return a == b || a != null && a.equals(b);
    }
}

