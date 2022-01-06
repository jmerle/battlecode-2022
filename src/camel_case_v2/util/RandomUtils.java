package camel_case_v2.util;

public class RandomUtils {
    public static int nextInt(int maxExclusive) {
        return (int) Math.floor(Math.random() * maxExclusive);
    }

    public static int nextInt(int minInclusive, int maxExclusive) {
        return nextInt(maxExclusive - minInclusive) + minInclusive;
    }
}
