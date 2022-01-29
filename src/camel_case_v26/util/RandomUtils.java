package camel_case_v26.util;

public class RandomUtils {
    public static int nextInt(int maxExclusive) {
        return (int) Math.floor(Math.random() * maxExclusive);
    }

    public static int nextInt(int minInclusive, int maxExclusive) {
        return nextInt(maxExclusive - minInclusive) + minInclusive;
    }

    public static boolean chance(double percentage) {
        return nextInt(1000) < percentage * 1000;
    }
}
