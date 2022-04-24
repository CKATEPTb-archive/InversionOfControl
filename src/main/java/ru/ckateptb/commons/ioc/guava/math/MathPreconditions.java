package ru.ckateptb.commons.ioc.guava.math;

final class MathPreconditions {
    static int checkPositive(String role, int x) {
        if (x <= 0) {
            throw new IllegalArgumentException((new StringBuilder(26 + String.valueOf(role).length())).append(role).append(" (").append(x).append(") must be > 0").toString());
        } else {
            return x;
        }
    }

    static int checkNonNegative(String role, int x) {
        if (x < 0) {
            throw new IllegalArgumentException((new StringBuilder(27 + String.valueOf(role).length())).append(role).append(" (").append(x).append(") must be >= 0").toString());
        } else {
            return x;
        }
    }

    static void checkRoundingUnnecessary(boolean condition) {
        if (!condition) {
            throw new ArithmeticException("mode was UNNECESSARY, but rounding was necessary");
        }
    }
}
