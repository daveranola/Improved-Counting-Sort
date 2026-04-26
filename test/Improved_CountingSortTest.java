import java.util.Random;

public class Improved_CountingSortTest {
    private static final long BASE_SEED = 20260408L;
    // The paper reports this baseline but does not publish an insertion cutoff.
    private static final int INSERTION_SORT_CUTOFF = 32;
    private static final int BENCHMARK_WARMUP_RUNS = 3;
    private static final int BENCHMARK_MEASURED_RUNS = 10;
    private static final int[] TABLE2_SIZES =
            new int[] {1_000, 2_000, 3_000, 4_000, 5_000, 10_000, 20_000, 30_000, 40_000, 50_000};

    public static void main(String[] args) {
        if (args.length == 0 || "all".equalsIgnoreCase(args[0])) {
            runAllBenchmarks();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "table1" -> runTable1Benchmark();
            case "table21" -> runTable21Benchmark();
            case "table22" -> runTable22Benchmark();
            case "table3" -> runTable3Benchmark();
            default -> {
                printUsage();
                throw new IllegalArgumentException("Unknown benchmark selection: " + args[0]);
            }
        }
    }

    static void runAllBenchmarks() {
        runTable1Benchmark();
        runTable21Benchmark();
        runTable22Benchmark();
        runTable3Benchmark();
    }

    static void runTable1Benchmark() {
        int[] sizes = new int[] {100_000, 200_000, 300_000, 500_000, 1_000_000, 2_000_000, 3_000_000, 5_000_000, 10_000_000};

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
        }

        System.out.println();
    }

    static void runTable21Benchmark() {
        runTable2Benchmark("Table 2.1", 1_000_000, TABLE2_SIZES);
    }

    static void runTable22Benchmark() {
        runTable2Benchmark("Table 2.2", 100_000_000, TABLE2_SIZES);
    }

    static void runTable3Benchmark() {
        int[] sizes = new int[] {
                1_000_000, 2_000_000, 3_000_000, 4_000_000, 5_000_000,
                10_000_000, 15_000_000, 20_000_000, 25_000_000
        };

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
        }

        System.out.println();
    }

    private static void runTable2Benchmark(String tableLabel, int range, int[] sizes) {
        printTableHeader(tableLabel + ": Running times in ms for counting sort with and without preprocessing");
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
            int[] input = randomNonNegativeArray(size, range - 1, BASE_SEED + 100 + size + range);
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
        }

        System.out.println();
    }

    private static void printUsage() {
        System.out.println("Usage: Improved_CountingSortTest [all|table1|table21|table22|table3]");
    }

    private static void printTableHeader(String title) {
        System.out.println();
        System.out.println(title);
    }

    private static void printDivider(int length) {
        System.out.println("-".repeat(length));
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

    private static PhaseTimings averagePreprocessAndCountingMillis(int[] source, int warmupRuns, int measuredRuns) {
        for (int i = 0; i < warmupRuns; i++) {
            int[] copy = source.clone();
            Improved_CountingSort.PartitionPlan partitionPlan = Improved_CountingSort.preprocess(copy);
            Improved_CountingSort.countingSortPartitions(copy, partitionPlan);
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
