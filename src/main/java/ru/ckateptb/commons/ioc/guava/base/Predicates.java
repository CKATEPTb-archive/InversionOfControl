package ru.ckateptb.commons.ioc.guava.base;


import java.io.Serializable;

public final class Predicates {
    public static <T> Predicate<T> instanceOf(Class<?> clazz) {
        return new InstanceOfPredicate(clazz);
    }

    private static class InstanceOfPredicate<T> implements Serializable, Predicate<T> {
        private final Class<?> clazz;

        private InstanceOfPredicate(Class<?> clazz) {
            this.clazz = (Class)Preconditions.checkNotNull(clazz);
        }

        public boolean apply(T o) {
            return this.clazz.isInstance(o);
        }

        public int hashCode() {
            return this.clazz.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof InstanceOfPredicate) {
                InstanceOfPredicate<?> that = (InstanceOfPredicate)obj;
                return this.clazz == that.clazz;
            } else {
                return false;
            }
        }

        public String toString() {
            String var1 = this.clazz.getName();
            return (new StringBuilder(23 + String.valueOf(var1).length())).append("Predicates.instanceOf(").append(var1).append(")").toString();
        }
    }
}