package ru.ckateptb.commons.ioc.guava.collect;

import ru.ckateptb.commons.ioc.guava.base.Objects;
import ru.ckateptb.commons.ioc.guava.base.Preconditions;

import java.util.*;

public final class Lists {
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList();
    }

    public static <E> ArrayList<E> newArrayList(Iterator<? extends E> elements) {
        ArrayList<E> list = newArrayList();
        Iterators.addAll(list, elements);
        return list;
    }

    static boolean equalsImpl(List<?> thisList, Object other) {
        if (other == Preconditions.checkNotNull(thisList)) {
            return true;
        } else if (!(other instanceof List)) {
            return false;
        } else {
            List<?> otherList = (List)other;
            int size = thisList.size();
            if (size != otherList.size()) {
                return false;
            } else if (thisList instanceof RandomAccess && otherList instanceof RandomAccess) {
                for(int i = 0; i < size; ++i) {
                    if (!Objects.equal(thisList.get(i), otherList.get(i))) {
                        return false;
                    }
                }

                return true;
            } else {
                return Iterators.elementsEqual(thisList.iterator(), otherList.iterator());
            }
        }
    }

    static int indexOfImpl(List<?> list, Object element) {
        if (list instanceof RandomAccess) {
            return indexOfRandomAccess(list, element);
        } else {
            ListIterator<?> listIterator = list.listIterator();

            do {
                if (!listIterator.hasNext()) {
                    return -1;
                }
            } while(!Objects.equal(element, listIterator.next()));

            return listIterator.previousIndex();
        }
    }

    private static int indexOfRandomAccess(List<?> list, Object element) {
        int size = list.size();
        int i;
        if (element == null) {
            for(i = 0; i < size; ++i) {
                if (list.get(i) == null) {
                    return i;
                }
            }
        } else {
            for(i = 0; i < size; ++i) {
                if (element.equals(list.get(i))) {
                    return i;
                }
            }
        }

        return -1;
    }

    static int lastIndexOfImpl(List<?> list, Object element) {
        if (list instanceof RandomAccess) {
            return lastIndexOfRandomAccess(list, element);
        } else {
            ListIterator<?> listIterator = list.listIterator(list.size());

            do {
                if (!listIterator.hasPrevious()) {
                    return -1;
                }
            } while(!Objects.equal(element, listIterator.previous()));

            return listIterator.nextIndex();
        }
    }

    private static int lastIndexOfRandomAccess(List<?> list, Object element) {
        int i;
        if (element == null) {
            for(i = list.size() - 1; i >= 0; --i) {
                if (list.get(i) == null) {
                    return i;
                }
            }
        } else {
            for(i = list.size() - 1; i >= 0; --i) {
                if (element.equals(list.get(i))) {
                    return i;
                }
            }
        }

        return -1;
    }
}