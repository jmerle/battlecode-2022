package camel_case_v2.util;

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
}
