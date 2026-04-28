package h03;

import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

/**
 * This class serves as a container for the methods that are to be implemented by the students for exercise H3.1.1.
 * <p>
 * <strong>Tribonacci Sequence:</strong> A mathematical sequence where each number is the sum of the previous three numbers.
 * It's similar to the Fibonacci sequence (which sums the previous 2 numbers) but more complex.
 * <p>
 * Sequence: 0, 1, 1, 2, 4, 7, 13, 24, 44, 81, 149, ...
 * <ul>
 *   <li>Position 0: 0</li>
 *   <li>Position 1: 1</li>
 *   <li>Position 2: 1</li>
 *   <li>Position 3: 2 (= 0 + 1 + 1)</li>
 *   <li>Position 4: 4 (= 1 + 1 + 2)</li>
 *   <li>Position 5: 7 (= 1 + 2 + 4)</li>
 * </ul>
 */
@SuppressWarnings("ManualArrayCopy")
public class TribonacciNumbers {

    /**
     * Prevent instantiation of this utility class.
     */
    private TribonacciNumbers() {
        throw new IllegalStateException("This class is not meant to be instantiated.");
    }

    /**
     * Creates a new array by copying the input array and appending a new value at the end.
     * <p>
     * Example: {@code push([1, 2, 3], 4)} returns {@code [1, 2, 3, 4]} (original array unchanged)
     * <p>
     * Note: This is a "functional" approach: the original array is not modified. Instead, a new array is created
     * with all the original elements plus the new value. This is common in functional programming.
     *
     * @param array the input array to copy (will not be modified)
     * @param value the value to append to the end
     * @return a new array containing all elements from the input array plus the value at the end
     */
    @StudentImplementationRequired("H3.1.1")
    public static int[] push(final int[] array, final int value) {
        final int[] newArray = new int[array.length + 1];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        newArray[array.length] = value;
        return newArray;
    }

    /**
     * Calculates the next Tribonacci number and appends it to the array.
     * <p>
     * <strong>Tribonacci Formula:</strong> Each new number is the sum of the previous three numbers:
     * {@code nextNumber = last3rdElement + last2ndElement + lastElement}
     * <p>
     * The method takes the three most recent numbers from the array, adds them together, and returns
     * a new array with this sum appended using the {@code push()} method.
     * <p>
     * Example: {@code calculateNextTribonacci([0, 1, 1, 2])} returns {@code [0, 1, 1, 2, 4]}
     * (because 1 + 1 + 2 = 4)
     *
     * @param array the input array containing Tribonacci numbers. <strong>Must contain at least 3 elements</strong>
     * @return a new array with the next Tribonacci number appended at the end
     * @throws IndexOutOfBoundsException if the input array has less than 3 elements (accessing invalid indices)
     */
    @StudentImplementationRequired("H3.1.1")
    public static int[] calculateNextTribonacci(final int[] array) {
        int n_3 = array[array.length - 3];
        int n_2 = array[array.length - 2];
        int n_1 = array[array.length - 1];
        return push(array, n_3 + n_2 + n_1);
    }

    /**
     * Returns the n-th Tribonacci number (0-indexed).
     * <p>
     * This method generates the Tribonacci sequence up to the n-th position and returns the last number.
     * It starts with the initial sequence [0, 1, 1] and repeatedly calculates the next Tribonacci number
     * using {@link TribonacciNumbers#calculateNextTribonacci(int[])} until it reaches the desired position.
     * <p>
     * <strong>Important:</strong> This uses 0-indexing, meaning:
     * <ul>
     *   <li>{@code tribonacci(0)} returns 0 (first number)</li>
     *   <li>{@code tribonacci(1)} returns 1 (second number)</li>
     *   <li>{@code tribonacci(2)} returns 1 (third number)</li>
     *   <li>{@code tribonacci(3)} returns 2 (fourth number)</li>
     *   <li>{@code tribonacci(5)} returns 7</li>
     *   <li>{@code tribonacci(10)} returns 149</li>
     * </ul>
     *
     * @param n the position in the Tribonacci sequence (0-indexed).
     * @return the Tribonacci number at position {@code n}
     */
    @StudentImplementationRequired("H3.1.1")
    public static int tribonacci(final int n) {
        int[] array = {0, 1, 1};
        for (int i = 3; i <= n; i++) {
            array = calculateNextTribonacci(array);
        }
        return array[array.length - 1];
    }
}
