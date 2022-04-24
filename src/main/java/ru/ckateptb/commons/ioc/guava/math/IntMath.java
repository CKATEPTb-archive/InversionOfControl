package ru.ckateptb.commons.ioc.guava.math;

import java.math.RoundingMode;

public final class IntMath {
    static final byte[] maxLog10ForLeadingZeros = new byte[]{9, 9, 9, 8, 8, 8, 7, 7, 7, 6, 6, 6, 6, 5, 5, 5, 4, 4, 4, 3, 3, 3, 3, 2, 2, 2, 1, 1, 1, 0, 0, 0, 0};
    static final int[] powersOf10 = new int[]{1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};
    static final int[] halfPowersOf10 = new int[]{3, 31, 316, 3162, 31622, 316227, 3162277, 31622776, 316227766, Integer.MAX_VALUE};
    private static final int[] factorials = new int[]{1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800, 39916800, 479001600};
    static int[] biggestBinomials = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, 65536, 2345, 477, 193, 110, 75, 58, 49, 43, 39, 37, 35, 34, 34, 33};

    public static boolean isPowerOfTwo(int x) {
        return x > 0 & (x & x - 1) == 0;
    }

    static int lessThanBranchFree(int x, int y) {
        return ~(~(x - y)) >>> 31;
    }

    public static int log2(int x, RoundingMode mode) {
        MathPreconditions.checkPositive("x", x);
        switch (mode) {
            case UNNECESSARY:
                MathPreconditions.checkRoundingUnnecessary(isPowerOfTwo(x));
            case DOWN:
            case FLOOR:
                return 31 - Integer.numberOfLeadingZeros(x);
            case UP:
            case CEILING:
                return 32 - Integer.numberOfLeadingZeros(x - 1);
            case HALF_DOWN:
            case HALF_UP:
            case HALF_EVEN:
                int leadingZeros = Integer.numberOfLeadingZeros(x);
                int cmp = -1257966797 >>> leadingZeros;
                int logFloor = 31 - leadingZeros;
                return logFloor + lessThanBranchFree(cmp, x);
            default:
                throw new AssertionError();
        }
    }

    public static int sqrt(int x, RoundingMode mode) {
        MathPreconditions.checkNonNegative("x", x);
        int sqrtFloor = sqrtFloor(x);
        switch (mode) {
            case UNNECESSARY:
                MathPreconditions.checkRoundingUnnecessary(sqrtFloor * sqrtFloor == x);
            case DOWN:
            case FLOOR:
                return sqrtFloor;
            case UP:
            case CEILING:
                return sqrtFloor + lessThanBranchFree(sqrtFloor * sqrtFloor, x);
            case HALF_DOWN:
            case HALF_UP:
            case HALF_EVEN:
                int halfSquare = sqrtFloor * sqrtFloor + sqrtFloor;
                return sqrtFloor + lessThanBranchFree(halfSquare, x);
            default:
                throw new AssertionError();
        }
    }

    private static int sqrtFloor(int x) {
        return (int)Math.sqrt((double)x);
    }
}
