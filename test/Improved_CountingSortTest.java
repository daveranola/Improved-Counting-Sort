import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Improved Counting Sort")
class Improved_CountingSortTest {
    private static final long BASE_SEED = 20260408L;
    // The paper reports this baseline but does not publish an insertion cutoff.
    private static final int INSERTION_SORT_CUTOFF = 32;
    private static final int BENCHMARK_WARMUP_RUNS = 2;
    private static final int BENCHMARK_MEASURED_RUNS = 3;

    @Nested
    @DisplayName("Accuracy")
    class AccuracyTests {
        @DisplayName("Hand-written examples")
        @ParameterizedTest(name = "{0}")
        @MethodSource("Improved_CountingSortTest#handWrittenExamples")
        void handWrittenExamples(String caseName, int[] input, int[] expected) {
            int[] actual = input.clone();

            Improved_CountingSort.sort(actual);

            assertAll(
                    caseName,
                    () -> assertArrayEquals(expected, actual),
                    () -> assertSorted(actual)
            );
        }

        @DisplayName("Table 1 cases")
        @ParameterizedTest(name = "{0}")
        @MethodSource("Improved_CountingSortTest#table1StyleInputs")
        void table1Cases(String caseName, int[] input) {
            assertMatchesJavaSort(caseName, input);
        }

        @DisplayName("Table 2 cases")
        @ParameterizedTest(name = "{0}")
        @MethodSource("Improved_CountingSortTest#table2StyleInputs")
        void table2Cases(String caseName, int[] input) {
            assertMatchesJavaSort(caseName, input);
        }

        @DisplayName("Table 2 staged pipeline")
        @ParameterizedTest(name = "{0}")
        @MethodSource("Improved_CountingSortTest#table2StyleInputs")
        void table2StagedPipeline(String caseName, int[] input) {
            assertStagedPipelineMatchesJavaSort(caseName, input);
        }

        @DisplayName("Table 3 cases")
        @ParameterizedTest(name = "{0}")
        @MethodSource("Improved_CountingSortTest#table3StyleInputs")
        void table3Cases(String caseName, int[] input) {
            assertMatchesJavaSort(caseName, input);
        }

        @DisplayName("Preprocessing respects the threshold invariant from the paper")
        @Test
        void preprocessingRespectsThresholdInvariant() {
            int threshold = 128;
            int[] input = randomNonNegativeArray(5_000, 4_999, BASE_SEED + 30);
            Improved_CountingSort.PartitionPlan partitionPlan = Improved_CountingSort.preprocess(input, threshold);

            for (int i = 0; i < partitionPlan.partitionCount(); i++) {
                Improved_CountingSort.Partition partition = partitionPlan.partitionAt(i);

                assertAll(
                        "partition " + i,
                        () -> assertTrue(partition.rangePlusSize() <= threshold, "Partition exceeds threshold"),
                        () -> assertTrue(partition.low() <= partition.high(), "Partition must be non-empty")
                );

                if (i > 0) {
                    Improved_CountingSort.Partition previous = partitionPlan.partitionAt(i - 1);
                    assertTrue(previous.high() < partition.low(), "Partitions must stay disjoint and ordered by index");
                    assertTrue(previous.maxValue() <= partition.minValue(), "Partitions are not ordered by value");
                }
            }

            assertTrue(partitionPlan.threshold() == threshold);
        }

        @DisplayName("Threshold overload matches Java sort")
        @Test
        void thresholdOverloadMatchesJavaSort() {
            int[] input = randomNonNegativeArray(10_000, 9_999, BASE_SEED + 31);
            int[] expected = input.clone();
            int[] actual = input.clone();

            Arrays.sort(expected);
            Improved_CountingSort.sort(actual, 256);

            assertArrayEquals(expected, actual);
        }
    }

    @Nested
    @DisplayName("Benchmarks")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ResultsReplicationTests {
        @Order(1)
        @DisplayName("Table 1 benchmark")
        @Test
        void table1Benchmark() {
            int[] sizes = new int[] {1_000_000, 2_000_000, 3_000_000, 4_000_000, 5_000_000, 10_000_000, 15_000_000, 20_000_000, 25_000_000};

            printTableHeader("Table 1: Performance comparison in ms over random and sorted inputs");
            System.out.printf("%12s %12s %16s %16s%n", "n", "r", "random (T1)", "sorted (T2)");
            printDivider(62);

            for (int size : sizes) {
                int[] randomInput = randomNonNegativeArray(size, size - 1, BASE_SEED + size);
                int[] sortedInput = alreadySortedArray(size);

                double randomMs = averageMillis(
                        randomInput,
                        BENCHMARK_WARMUP_RUNS,
                        BENCHMARK_MEASURED_RUNS,
                        Improved_CountingSortTest::classicCountingSort
                );
                double sortedMs = averageMillis(
                        sortedInput,
                        BENCHMARK_WARMUP_RUNS,
                        BENCHMARK_MEASURED_RUNS,
                        Improved_CountingSortTest::classicCountingSort
                );

                System.out.printf("%,12d %,12d %,16.3f %,16.3f%n", size, size, randomMs, sortedMs);

                assertTrue(randomMs >= 0.0);
                assertTrue(sortedMs >= 0.0);
            }

            System.out.println();
        }

        @Order(2)
        @DisplayName("Table 2 benchmark")
        @Test
        void table2Benchmark() {
            int[] sizes = new int[] {1_000, 2_000, 3_000, 4_000, 5_000, 10_000, 20_000, 30_000, 40_000, 50_000};
            int range = 1_000_000;

            printTableHeader("Table 2: Running times in ms for counting sort with and without preprocessing");
            System.out.printf(
                    "%12s %12s %16s %16s %16s %16s%n",
                    "n",
                    "r",
                    "preprocess",
                    "partitioned count",
                    "T1 total",
                    "classic (T2)"
            );
            printDivider(94);

            for (int size : sizes) {
                int[] input = randomNonNegativeArray(size, range - 1, BASE_SEED + 100 + size);
                PhaseTimings improvedTimings = averagePreprocessAndCountingMillis(
                        input,
                        BENCHMARK_WARMUP_RUNS,
                        BENCHMARK_MEASURED_RUNS
                );
                double classicMs = averageMillis(
                        input,
                        BENCHMARK_WARMUP_RUNS,
                        BENCHMARK_MEASURED_RUNS,
                        Improved_CountingSortTest::classicCountingSort
                );

                double improvedTotalMs = improvedTimings.preprocessMs + improvedTimings.countingMs;

                System.out.printf("%,12d %,12d %,16.3f %,16.3f %,16.3f %,16.3f%n",
                        size,
                        range,
                        improvedTimings.preprocessMs,
                        improvedTimings.countingMs,
                        improvedTotalMs,
                        classicMs);

                assertTrue(improvedTimings.preprocessMs >= 0.0);
                assertTrue(improvedTimings.countingMs >= 0.0);
                assertTrue(classicMs >= 0.0);
            }

            System.out.println();
        }

        @Order(3)
        @DisplayName("Table 3 benchmark")
        @Test
        void table3Benchmark() {
            int[] sizes = new int[] {1_000_000, 2_000_000, 3_000_000, 4_000_000, 5_000_000, 10_000_000, 15_000_000, 20_000_000, 25_000_000};

            printTableHeader("Table 3: Running times in ms for quicksort, quicksort with insertion sort, and quicksort with counting sort");
            System.out.printf("%14s %16s %24s %24s%n",
                    "n = r",
                    "quicksort (T1)",
                    "quick+insertion (T2)",
                    "quick+counting (T3)");
            printDivider(84);

            for (int size : sizes) {
                int[] input = randomNonNegativeArray(size, size - 1, BASE_SEED + 200 + size);

                double quickSortMs = averageMillis(
                        input,
                        BENCHMARK_WARMUP_RUNS,
                        BENCHMARK_MEASURED_RUNS,
                        Improved_CountingSortTest::classicQuickSort
                );
                double quickSortInsertionMs = averageMillis(
                        input,
                        BENCHMARK_WARMUP_RUNS,
                        BENCHMARK_MEASURED_RUNS,
                        Improved_CountingSortTest::quickSortWithInsertionSort
                );
                double proposedMs = averageMillis(
                        input,
                        BENCHMARK_WARMUP_RUNS,
                        BENCHMARK_MEASURED_RUNS,
                        Improved_CountingSort::sort
                );

                System.out.printf("%,14d %,16.3f %,24.3f %,24.3f%n",
                        size,
                        quickSortMs,
                        quickSortInsertionMs,
                        proposedMs);

                assertTrue(quickSortMs >= 0.0);
                assertTrue(quickSortInsertionMs >= 0.0);
                assertTrue(proposedMs >= 0.0);
            }

            System.out.println();
        }
    }

    private static void printTableHeader(String title) {
        System.out.println();
        System.out.println(title);
    }

    private static void printDivider(int length) {
        System.out.println("-".repeat(length));
    }

    private static Arguments[] handWrittenExamples() {
        return new Arguments[] {
                Arguments.of(
                        "main-method example",
                        new int[] {8, 3, 6, 1, 7, 2, 5, 4},
                        new int[] {1, 2, 3, 4, 5, 6, 7, 8}
                ),
                Arguments.of(
                        "threshold-boundary style example",
                        new int[] {4, 1, 3, 0, 2},
                        new int[] {0, 1, 2, 3, 4}
                ),
                Arguments.of(
                        "duplicate-heavy values",
                        new int[] {4, 2, 2, 4, 1, 3, 3, 0},
                        new int[] {0, 1, 2, 2, 3, 3, 4, 4}
                )
        };
    }

    private static Arguments[] table1StyleInputs() {
        return new Arguments[] {
                Arguments.of(
                        "table 1 style: random input where n and r are the same order",
                        randomNonNegativeArray(25_000, 24_999, BASE_SEED)
                ),
                Arguments.of(
                        "table 1 style: already sorted input where n and r are the same order",
                        alreadySortedArray(25_000)
                ),
                Arguments.of(
                        "table 1 style: reverse-sorted input where n and r are the same order",
                        reverseSortedArray(25_000)
                )
        };
    }

    private static Arguments[] table2StyleInputs() {
        return new Arguments[] {
                Arguments.of("table 2 row 1: n=1000, r=1000000", randomNonNegativeArray(1_000, 999_999, BASE_SEED + 10)),
                Arguments.of("table 2 row 2: n=2000, r=1000000", randomNonNegativeArray(2_000, 999_999, BASE_SEED + 11)),
                Arguments.of("table 2 row 3: n=3000, r=1000000", randomNonNegativeArray(3_000, 999_999, BASE_SEED + 12))
        };
    }

    private static Arguments[] table3StyleInputs() {
        return new Arguments[] {
                Arguments.of("table 3 style: n=r=100000", randomNonNegativeArray(100_000, 99_999, BASE_SEED + 20)),
                Arguments.of("table 3 style: n=r=200000", randomNonNegativeArray(200_000, 199_999, BASE_SEED + 21))
        };
    }

    private static void assertMatchesJavaSort(String caseName, int[] input) {
        int[] expected = input.clone();
        int[] actual = input.clone();

        Arrays.sort(expected);
        Improved_CountingSort.sort(actual);

        assertAll(
                caseName,
                () -> assertArrayEquals(expected, actual),
                () -> assertSorted(actual)
        );
    }

    private static void assertStagedPipelineMatchesJavaSort(String caseName, int[] input) {
        int[] expected = input.clone();
        int[] actual = input.clone();

        Arrays.sort(expected);

        Improved_CountingSort.PartitionPlan partitionPlan = Improved_CountingSort.preprocess(actual);
        Improved_CountingSort.countingSortPartitions(actual, partitionPlan);

        assertAll(
                caseName,
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

    private static int[] alreadySortedArray(int length) {
        int[] values = new int[length];

        for (int i = 0; i < length; i++) {
            values[i] = i;
        }

        return values;
    }

    private static int[] reverseSortedArray(int length) {
        int[] values = new int[length];

        for (int i = 0; i < length; i++) {
            values[i] = length - i - 1;
        }

        return values;
    }

    private static double averageMillis(int[] source, int warmupRuns, int measuredRuns, IntArraySorter sorter) {
        for (int i = 0; i < warmupRuns; i++) {
            int[] copy = source.clone();
            sorter.sort(copy);
            assertSorted(copy);
        }

        long totalNanos = 0L;
        for (int i = 0; i < measuredRuns; i++) {
            int[] copy = source.clone();
            long start = System.nanoTime();
            sorter.sort(copy);
            long end = System.nanoTime();
            totalNanos += end - start;
            assertSorted(copy);
        }

        return totalNanos / 1_000_000.0 / measuredRuns;
    }

    private static PhaseTimings averagePreprocessAndCountingMillis(int[] source, int warmupRuns, int measuredRuns) {
        for (int i = 0; i < warmupRuns; i++) {
            int[] copy = source.clone();
            Improved_CountingSort.PartitionPlan partitionPlan = Improved_CountingSort.preprocess(copy);
            Improved_CountingSort.countingSortPartitions(copy, partitionPlan);
            assertSorted(copy);
        }

        long preprocessNanos = 0L;
        long countingNanos = 0L;

        for (int i = 0; i < measuredRuns; i++) {
            int[] copy = source.clone();

            long preprocessStart = System.nanoTime();
            Improved_CountingSort.PartitionPlan partitionPlan = Improved_CountingSort.preprocess(copy);
            long preprocessEnd = System.nanoTime();

            long countingStart = System.nanoTime();
            Improved_CountingSort.countingSortPartitions(copy, partitionPlan);
            long countingEnd = System.nanoTime();

            preprocessNanos += preprocessEnd - preprocessStart;
            countingNanos += countingEnd - countingStart;
            assertSorted(copy);
        }

        return new PhaseTimings(
                preprocessNanos / 1_000_000.0 / measuredRuns,
                countingNanos / 1_000_000.0 / measuredRuns
        );
    }

    private static void classicQuickSort(int[] input) {
        if (input.length < 2) {
            return;
        }

        classicQuickSort(input, 0, input.length - 1);
    }

    private static void classicQuickSort(int[] input, int low, int high) {
        if (low >= high) {
            return;
        }

        int pivot = Improved_CountingSort.partitionMedianOfThree(input, low, high);

        classicQuickSort(input, low, pivot - 1);
        classicQuickSort(input, pivot + 1, high);
    }

    private static void quickSortWithInsertionSort(int[] input) {
        if (input.length < 2) {
            return;
        }

        quickSortWithInsertionSort(input, 0, input.length - 1);
    }

    private static void quickSortWithInsertionSort(int[] input, int low, int high) {
        if (low >= high) {
            return;
        }

        if (high - low + 1 <= INSERTION_SORT_CUTOFF) {
            insertionSortRange(input, low, high);
            return;
        }

        int pivot = Improved_CountingSort.partitionMedianOfThree(input, low, high);

        quickSortWithInsertionSort(input, low, pivot - 1);
        quickSortWithInsertionSort(input, pivot + 1, high);
    }

    private static void insertionSortRange(int[] input, int low, int high) {
        for (int i = low + 1; i <= high; i++) {
            int value = input[i];
            int j = i - 1;

            while (j >= low && input[j] > value) {
                input[j + 1] = input[j];
                j--;
            }

            input[j + 1] = value;
        }
    }

    private static void classicCountingSort(int[] input) {
        if (input.length == 0) {
            return;
        }

        int max = Improved_CountingSort.getMax(input);

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

    private interface IntArraySorter {
        void sort(int[] values);
    }

    private static final class PhaseTimings {
        private final double preprocessMs;
        private final double countingMs;

        private PhaseTimings(double preprocessMs, double countingMs) {
            this.preprocessMs = preprocessMs;
            this.countingMs = countingMs;
        }
    }
}
