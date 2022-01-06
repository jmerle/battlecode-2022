package camel_case.util;

public class ArrayUtils {
    public static <T> T[] shuffle(T[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = RandomUtils.nextInt(i + 1);

            T temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }

        return array;
    }

    public static int[] shuffle(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = RandomUtils.nextInt(i + 1);

            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }

        return array;
    }
}
