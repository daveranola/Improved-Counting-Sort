import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Improved Counting Sort")
class Improved_CountingSortTest {
    private static final long BASE_SEED = 20260408L;
    // TODO: Replace with the seed your team wants to use for reproducible experiments.

    @Nested
    @DisplayName("Accuracy of implementation")
    class AccuracyTests {
        @ParameterizedTest(name = "{0}")
        @MethodSource("Improved_CountingSortTest#starterCases")
        void sortsStarterCases(String caseName, int[] input, int[] expected) {
            int[] actual = input.clone();

            Improved_CountingSort.sort(actual);

            assertAll(
                    () -> assertArrayEquals(expected, actual),
                    () -> assertSorted(actual)
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("Improved_CountingSortTest#oracleCases")
        void matchesJavaSortOnOracleCases(String caseName, int[] input) {
            assertMatchesJavaSort(input);
        }
    }

    @Nested
    @DisplayName("Paper-aligned scenarios")
    class PaperScenarioTests {
        @ParameterizedTest(name = "{0}")
        @MethodSource("Improved_CountingSortTest#paperCases")
        void matchesJavaSortOnPaperStyleDatasets(String caseName, int[] input) {
            assertMatchesJavaSort(input);
        }
    }

    @Nested
    @DisplayName("Contract decisions to finalize")
    class ContractTemplateTests {
        // TODO: Decide and document whether empty input should be supported or rejected.
        @Disabled("Enable after you decide how the implementation should handle empty input.")
        @Test
        void emptyArrayBehavior() {
            int[] values = {};

            Improved_CountingSort.sort(values);

            assertArrayEquals(new int[0], values);
        }

        // TODO: Decide and document whether negative integers should be rejected explicitly.
        @Disabled("Enable after you decide whether to reject negative integers explicitly.")
        @Test
        void negativeInputBehavior() {
            assertThrows(IllegalArgumentException.class, () ->
                    Improved_CountingSort.sort(new int[] {4, -1, 2, 0})
            );
        }
    }

    @Nested
    @Disabled("Enable when collecting timing data for the report tables.")
    @DisplayName("Results replication scaffolding")
    class ResultReplicationTemplate {
        // TODO: Adjust dataset sizes, warmup runs, and measured runs to match your final report setup.
        @Test
        void compareAgainstClassicCountingSortWhenNAndRangeMatch() {
            int[] source = randomNonNegativeArray(100_000, 99_999, BASE_SEED);

            double improvedMs = averageMillis(source, 3, 5, Improved_CountingSort::sort);
            double baselineMs = averageMillis(source, 3, 5, Improved_CountingSortTest::classicCountingSort);

            System.out.printf(
                    "n~=r -> improved=%.3f ms, classicCounting=%.3f ms%n",
                    improvedMs,
                    baselineMs
            );

            assertTrue(improvedMs >= 0.0 && baselineMs >= 0.0);
        }

        // TODO: Tune this case to mirror the paper's non-ideal r >> n scenario on your machine.
        @Test
        void compareAgainstClassicCountingSortWhenRangeIsMuchLargerThanN() {
            int[] source = randomNonNegativeArray(3_000, 1_000_000, BASE_SEED + 1);

            double improvedMs = averageMillis(source, 3, 5, Improved_CountingSort::sort);
            double baselineMs = averageMillis(source, 3, 5, Improved_CountingSortTest::classicCountingSort);

            System.out.printf(
                    "r>>n -> improved=%.3f ms, classicCounting=%.3f ms%n",
                    improvedMs,
                    baselineMs
            );

            assertTrue(improvedMs >= 0.0 && baselineMs >= 0.0);
        }

        // TODO: Replace Arrays.sort with your actual quicksort baseline once that code exists in the project.
        @Disabled("Replace Arrays.sort with your quicksort baseline when you add it to the project.")
        @Test
        void templateForTable3StyleComparison() {
            int[] source = randomNonNegativeArray(100_000, 99_999, BASE_SEED + 2);

            double improvedMs = averageMillis(source, 3, 5, Improved_CountingSort::sort);
            double quicksortBaselineMs = averageMillis(source, 3, 5, Arrays::sort);

            System.out.printf(
                    "table3-template -> improved=%.3f ms, quicksortBaseline=%.3f ms%n",
                    improvedMs,
                    quicksortBaselineMs
            );

            assertTrue(improvedMs >= 0.0 && quicksortBaselineMs >= 0.0);
        }
    }

    private static Stream<Arguments> starterCases() {
        return Stream.of(
                Arguments.of(
                        "main-method example",
                        new int[] {8, 3, 6, 1, 7, 2, 5, 4},
                        new int[] {1, 2, 3, 4, 5, 6, 7, 8}
                ),
                Arguments.of(
                        "threshold-boundary case where counting sort should be enough",
                        new int[] {4, 1, 3, 0, 2},
                        new int[] {0, 1, 2, 3, 4}
                ),
                Arguments.of(
                        "duplicate-heavy values",
                        new int[] {4, 2, 2, 4, 1, 3, 3, 0},
                        new int[] {0, 1, 2, 2, 3, 3, 4, 4}
                )
                // TODO: Add small hand-written examples you want to cite in the report.
                // , Arguments.of(
                //         "your custom case name",
                //         new int[] { /* TODO */ },
                //         new int[] { /* TODO */ }
                // )
        );
    }

    private static Stream<Arguments> oracleCases() {
        return Stream.of(
                Arguments.of("single element", new int[] {42}),
                Arguments.of("already sorted", new int[] {0, 1, 2, 3, 4, 5}),
                Arguments.of("reverse sorted", new int[] {5, 4, 3, 2, 1, 0}),
                Arguments.of("all equal values", new int[] {7, 7, 7, 7, 7, 7}),
                Arguments.of("small random, seed 1", randomNonNegativeArray(20, 20, BASE_SEED)),
                Arguments.of("small random, seed 2", randomNonNegativeArray(20, 20, BASE_SEED + 1)),
                Arguments.of("duplicate-heavy random", duplicateHeavyArray(50, 6, BASE_SEED + 2))
                // TODO: Add more oracle cases for boundary sizes or value distributions if needed.
        );
    }

    private static Stream<Arguments> paperCases() {
        return Stream.of(
                Arguments.of(
                        "paper favorable regime: n and range are the same order",
                        randomNonNegativeArray(1_000, 999, BASE_SEED + 10)
                ),
                Arguments.of(
                        "paper non-ideal regime: range is much larger than n",
                        randomNonNegativeArray(1_000, 1_000_000, BASE_SEED + 11)
                ),
                Arguments.of(
                        "locality best case: already sorted input",
                        alreadySortedArray(1_000)
                ),
                Arguments.of(
                        "locality comparison case: random input with the same range size",
                        randomNonNegativeArray(1_000, 999, BASE_SEED + 12)
                )
                // TODO: Add exact n/r combinations from your report tables once you finalize them.
        );
    }

    private static void assertMatchesJavaSort(int[] input) {
        int[] expected = input.clone();
        int[] actual = input.clone();

        Arrays.sort(expected);
        Improved_CountingSort.sort(actual);

        assertAll(
                () -> assertArrayEquals(expected, actual),
                () -> assertSorted(actual)
        );
    }

    private static void assertSorted(int[] values) {
        for (int i = 1; i < values.length; i++) {
            assertTrue(values[i - 1] <= values[i], "Array is not sorted at index " + i);
        }
    }

    private static int[] randomNonNegativeArray(int length, int maxValueInclusive, long seed) {
        Random random = new Random(seed);
        int[] values = new int[length];

        for (int i = 0; i < length; i++) {
            values[i] = random.nextInt(maxValueInclusive + 1);
        }

        return values;
    }

    private static int[] duplicateHeavyArray(int length, int distinctValues, long seed) {
        Random random = new Random(seed);
        int[] values = new int[length];

        for (int i = 0; i < length; i++) {
            values[i] = random.nextInt(distinctValues);
        }

        return values;
    }

    private static int[] alreadySortedArray(int length) {
        int[] values = new int[length];

        for (int i = 0; i < length; i++) {
            values[i] = i;
        }

        return values;
    }

    private static double averageMillis(int[] source, int warmupRuns, int measuredRuns, IntArraySorter sorter) {
        for (int i = 0; i < warmupRuns; i++) {
            int[] copy = source.clone();
            sorter.sort(copy);
        }

        long totalNanos = 0L;
        for (int i = 0; i < measuredRuns; i++) {
            int[] copy = source.clone();
            long start = System.nanoTime();
            sorter.sort(copy);
            long end = System.nanoTime();
            totalNanos += end - start;
        }

        return totalNanos / 1_000_000.0 / measuredRuns;
    }

    private static void classicCountingSort(int[] input) {
        if (input.length == 0) {
            return;
        }

        int max = input[0];
        for (int i = 1; i < input.length; i++) {
            max = Math.max(max, input[i]);
        }

        int[] count = new int[max + 1];
        int[] output = new int[input.length];

        for (int value : input) {
            count[value]++;
        }

        for (int i = 1; i < count.length; i++) {
            count[i] += count[i - 1];
        }

        for (int i = input.length - 1; i >= 0; i--) {
            output[count[input[i]] - 1] = input[i];
            count[input[i]]--;
        }

        System.arraycopy(output, 0, input, 0, input.length);
    }

    @FunctionalInterface
    private interface IntArraySorter {
        void sort(int[] values);
    }
}
