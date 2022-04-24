package ru.ckateptb.commons.ioc.guava.collect;

final class Hashing {
    static int smear(int hashCode) {
        return (int)(461845907L * (long)Integer.rotateLeft((int)((long)hashCode * -862048943L), 15));
    }

    static int smearedHash(Object o) {
        return smear(o == null ? 0 : o.hashCode());
    }

    static int closedTableSize(int expectedEntries, double loadFactor) {
        expectedEntries = Math.max(expectedEntries, 2);
        int tableSize = Integer.highestOneBit(expectedEntries);
        if (expectedEntries > (int)(loadFactor * (double)tableSize)) {
            tableSize <<= 1;
            return tableSize > 0 ? tableSize : 1073741824;
        } else {
            return tableSize;
        }
    }
}
