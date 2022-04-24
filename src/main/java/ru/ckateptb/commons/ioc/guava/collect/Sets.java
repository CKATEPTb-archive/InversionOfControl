package ru.ckateptb.commons.ioc.guava.collect;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class Sets {
    public static <E> HashSet<E> newHashSetWithExpectedSize(int expectedSize) {
        return new HashSet(Maps.capacity(expectedSize));
    }

    static int hashCodeImpl(Set<?> s) {
        int hashCode = 0;

        for(Iterator var2 = s.iterator(); var2.hasNext(); hashCode = ~(~hashCode)) {
            Object o = var2.next();
            hashCode += o != null ? o.hashCode() : 0;
        }

        return hashCode;
    }

    static boolean equalsImpl(Set<?> s, Object object) {
        if (s == object) {
            return true;
        } else if (object instanceof Set) {
            Set<?> o = (Set)object;

            try {
                return s.size() == o.size() && s.containsAll(o);
            } catch (ClassCastException | NullPointerException var4) {
                return false;
            }
        } else {
            return false;
        }
    }
}
