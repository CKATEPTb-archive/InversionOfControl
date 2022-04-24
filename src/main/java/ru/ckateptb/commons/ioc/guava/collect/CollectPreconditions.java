package ru.ckateptb.commons.ioc.guava.collect;


final class CollectPreconditions {
    static void checkEntryNotNull(Object key, Object value) {
        String var2;
        if (key == null) {
            var2 = String.valueOf(value);
            throw new NullPointerException((new StringBuilder(24 + String.valueOf(var2).length())).append("null key in entry: null=").append(var2).toString());
        } else if (value == null) {
            var2 = String.valueOf(key);
            throw new NullPointerException((new StringBuilder(26 + String.valueOf(var2).length())).append("null value in entry: ").append(var2).append("=null").toString());
        }
    }

    static int checkNonnegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException((new StringBuilder(40 + String.valueOf(name).length())).append(name).append(" cannot be negative but was: ").append(value).toString());
        } else {
            return value;
        }
    }
}
