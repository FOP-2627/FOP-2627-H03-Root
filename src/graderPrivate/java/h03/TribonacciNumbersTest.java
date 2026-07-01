package h03;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.annotation.SkipAfterFirstFailedTest;
import org.tudalgo.algoutils.tutor.general.assertions.Assertions2;
import org.tudalgo.algoutils.tutor.general.assertions.Assertions4;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSetTest;
import org.tudalgo.algoutils.tutor.general.reflections.BasicMethodLink;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLoop;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.contextBuilder;

@TestForSubmission
@Timeout(
    value = TestConstants.TEST_TIMEOUT_IN_SECONDS,
    unit = TimeUnit.SECONDS,
    threadMode = Timeout.ThreadMode.SEPARATE_THREAD
)
@SkipAfterFirstFailedTest
public class TribonacciNumbersTest {
    private static final int[] startingNumbers = {0, 1, 1};

    /**
     * @param mock the mock to register the reference implementation on
     */
    private static void registerPush(MockedStatic<TribonacciNumbers> mock) {
        mock.when(() -> TribonacciNumbers.push(Mockito.any(), Mockito.anyInt())).thenAnswer(invocation -> {
            final int[] array = invocation.getArgument(0);
            final int value = invocation.getArgument(1);
            final int[] newArray = Arrays.copyOf(array, array.length + 1);
            newArray[array.length] = value;
            return newArray;
        });
    }

    @Nested
    class GetStartingNumbers {
        private Integer[] boxIntArray(int[] a) {
            return Arrays.stream(a).boxed().toArray(Integer[]::new);
        }

        @Test
        public void check() {
            final Integer[] expected = boxIntArray(startingNumbers);
            final Integer[] actual = boxIntArray(TribonacciNumbers.getStartingNumbers());
            final var cb = contextBuilder()
                .add("expected", expected)
                .add("actual", actual);
            Assertions2.assertEquals(expected.length, actual.length, cb.build(), r -> "Array has incorrect length.");
            Assertions2.assertArrayEquals(expected, actual, cb.build(), r -> "");
        }
    }

    @Nested
    class Push {
        final String input = "generated/trib/push/randomNumbers.json";

        /**
         * Tests the {@link TribonacciNumbers#push(int[], int)} method.
         *
         * @param params             The {@link JsonParameterSet} to use for the test.
         * @param checkLastOnly      Whether to only test the last element.
         * @param checkUnchangedOnly Whether to only test that the input array is unchanged. Will not test the result.
         */
        private static void testPush(final JsonParameterSet params, final boolean checkLastOnly, final boolean checkUnchangedOnly) {
            final List<Integer> input = params.get("array");
            final int value = params.getInt("value");
            final List<Integer> expectedArray = params.get("expected result");
            final var ParamsContext = params.toContext();
            final var cb = contextBuilder()
                .add(ParamsContext)
                .add("Method", "push");
            final int[] inputArray = input.stream().mapToInt(i -> i).toArray();
            final int[] result = Assertions2.callObject(
                () -> TribonacciNumbers.push(
                    inputArray,
                    value
                ),
                cb.build(),
                r -> "An error occurred during execution."
            );
            final var actualArray = Arrays.stream(result).boxed().toList();
            cb.add("actual result", actualArray);
            if (checkUnchangedOnly) {
                // no need to assert length here since changing it is impossible
                Assertions2.assertIterableEquals(
                    input,
                    Arrays.stream(inputArray).boxed().toList(),
                    cb.build(),
                    r -> "The input array was changed."
                );
                return;
            }
            if (checkLastOnly) {
                Assertions2.assertEquals(
                    expectedArray.getLast(),
                    actualArray.getLast(),
                    cb.build(),
                    r -> "Invalid result."
                );
            } else {
                Assertions2.assertEquals(expectedArray.size(), actualArray.size(), cb.build(), r -> "Array has incorrect length");
                Assertions2.assertIterableEquals(
                    expectedArray,
                    actualArray,
                    cb.build(),
                    r -> "Invalid result."
                );
            }
        }

        @ParameterizedTest
        @JsonParameterSetTest(value = input)
        public void lastElement(final JsonParameterSet params) {
            testPush(params, true, false);
        }

        @ParameterizedTest
        @JsonParameterSetTest(value = input)
        public void allElements(final JsonParameterSet params) {
            testPush(params, false, false);
        }

        @ParameterizedTest
        @JsonParameterSetTest(value = input)
        public void inputUnchanged(final JsonParameterSet params) {
            testPush(params, false, true);
        }
    }

    @Nested
    class CalcNext {
        final String inputPositive = "generated/trib/calcNext/randomNumbersPositive.json";
        final String inputArbitrary = "generated/trib/calcNext/randomNumbers.json";

        private static void check(final JsonParameterSet params, final boolean checkRequirements) {
            try (final var mock = Mockito.mockStatic(TribonacciNumbers.class, Answers.CALLS_REAL_METHODS)) {
                final List<Integer> input = params.get("array");
                final List<Integer> expectedArray = params.get("expected result");
                final var ParamsContext = params.toContext();
                final var cb = contextBuilder()
                    .add(ParamsContext)
                    .add("Method", "calculateNextTribonacci");

                registerPush(mock);

                final int[] inputArray = input.stream().mapToInt(i -> i).toArray();
                final int[] result = Assertions2.callObject(
                    () -> TribonacciNumbers.calculateNextTribonacci(
                        inputArray
                    ),
                    cb.build(),
                    r -> "An error occurred during execution."
                );
                final var actualArray = Arrays.stream(result).boxed().toList();
                cb.add("actual result", actualArray);
                if (checkRequirements) {
                    // no need to assert length here since changing it is impossible
                    Assertions2.assertIterableEquals(
                        input,
                        Arrays.stream(inputArray).boxed().toList(),
                        cb.build(),
                        r -> "The input array was changed."
                    );
                    mock.verify(
                        () -> TribonacciNumbers.push(
                            inputArray,
                            expectedArray.getLast()
                        ),
                        Mockito.atLeastOnce()
                    );
                    return;
                }
                Assertions2.assertEquals(expectedArray.size(), actualArray.size(), cb.build(), r -> "Array has incorrect length");
                Assertions2.assertIterableEquals(
                    expectedArray,
                    actualArray,
                    cb.build(),
                    r -> "Invalid result."
                );

            }
        }

        @ParameterizedTest
        @JsonParameterSetTest(value = inputPositive)
        public void positiveOnly(final JsonParameterSet params) {
            check(params, false);
        }

        @ParameterizedTest
        @JsonParameterSetTest(value = inputArbitrary)
        public void arbitraryInput(final JsonParameterSet params) {
            check(params, false);
        }

        @ParameterizedTest
        @JsonParameterSetTest(value = inputArbitrary)
        public void requirements(final JsonParameterSet params) {
            check(params, true);
        }
    }

    @Nested
    class Tribonacci {
        final String inputSmall = "generated/trib/tribonacci/randomNumbersSmall.json";
        final String inputFull = "generated/trib/tribonacci/randomNumbers.json";

        private static void check(final JsonParameterSet params) {
            try (final var mock = Mockito.mockStatic(TribonacciNumbers.class, Answers.CALLS_REAL_METHODS)) {
                final Integer input = params.get("n");
                final int expectedResult = params.get("expected result");

                final var cb = contextBuilder()
                    .add(params.toContext("reference array"))
                    .add("Method", "tribonacci");

                mock.when(TribonacciNumbers::getStartingNumbers).thenReturn(startingNumbers);

                registerPush(mock);

                mock.when(() -> TribonacciNumbers.calculateNextTribonacci(Mockito.any())).thenAnswer(invocation -> {
                    final int[] array = invocation.getArgument(0);
                    return TribonacciNumbers.push(array, array[array.length - 1] + array[array.length - 2] + array[array.length - 3]);
                });

                final int result = Assertions2.callObject(
                    () -> TribonacciNumbers.tribonacci(
                        input
                    ),
                    cb.build(),
                    r -> "An error occurred during execution."
                );

                cb.add("actual result", result);
                Assertions2.assertEquals(
                    expectedResult,
                    result,
                    cb.build(),
                    r -> "Invalid result."
                );

            }
        }

        @ParameterizedTest
        @JsonParameterSetTest(value = inputSmall)
        public void smallerThanTwo(final JsonParameterSet params) {
            check(params);
        }

        @ParameterizedTest
        @JsonParameterSetTest(value = inputFull)
        public void full(final JsonParameterSet params) {
            check(params);
        }

        @Test
        public void requirements() throws NoSuchMethodException {
            // test exactly one for loop
            final var ml = BasicMethodLink.of(TribonacciNumbers.class.getDeclaredMethod("tribonacci", int.class));
            final var ctElement = ml.getCtElement();
            final var cb = contextBuilder()
                .add("Method", "tribonacci");

            Assertions4.assertIsNotRecursively(
                ctElement,
                cb.build(),
                r -> "The method should not have any recursive calls."
            );

            final var loops = ctElement.filterChildren(CtLoop.class::isInstance).list();
            Assertions2.assertEquals(
                1,
                loops.size(),
                cb.build(),
                r -> "The method should contain exactly one loop."
            );
            // assert correct method calls
            final Set<String> actualCalls = ctElement.filterChildren(c -> c instanceof CtInvocation<?>)
                .list(CtInvocation.class)
                .stream()
                .map(c -> c.getExecutable().getSimpleName())
                .collect(Collectors.toSet());
            cb.add("method calls", actualCalls);
            Assertions2.assertEquals(
                2,
                actualCalls.size(),
                cb.build(),
                r -> "The method should contain exactly two method calls."
            );
            final var expectedCalls = Set.of("calculateNextTribonacci", "getStartingNumbers");
            Assertions2.assertEquals(
                expectedCalls,
                actualCalls,
                cb.build(),
                r -> "The method should only call calculateNextTribonacci."
            );
        }
    }
}
