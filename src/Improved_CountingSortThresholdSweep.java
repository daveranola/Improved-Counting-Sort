import java.util.Arrays;
import java.util.Random;

public class Improved_CountingSortThresholdSweep {
    private static final long BASE_SEED = 20260422L;
    private static final int WARMUP_RUNS = 1;
    private static final int MEASURED_RUNS = 3;
    // Candidate C values to try. Keeping the list explicit makes the sweep easy to adjust.
    private static final int[] THRESHOLDS = new int[] {512, 768, 1000, 1536, 2048, 3072, 4096, 6144, 8192, 12288, 16384};
    // Cases closest to the paper's published experiments.
    private static final SweepCase[] PAPER_CASES = new SweepCase[] {
            new SweepCase("table2 n=1k r=1M", randomNonNegativeArray(1_000, 999_999, BASE_SEED + 1)),
            new SweepCase("table2 n=2k r=1M", randomNonNegativeArray(2_000, 999_999, BASE_SEED + 2)),
            new SweepCase("table2 n=3k r=1M", randomNonNegativeArray(3_000, 999_999, BASE_SEED + 3)),
            new SweepCase("table3 n=r=1M", randomNonNegativeArray(1_000_000, 999_999, BASE_SEED + 4)),
            new SweepCase("table3 n=r=2M", randomNonNegativeArray(2_000_000, 1_999_999, BASE_SEED + 5))
    };
    // Larger in-repo cases if you want to bias the threshold toward bigger workloads.
    private static final SweepCase[] EXTENDED_CASES = new SweepCase[] {
            new SweepCase("table2 n=50k r=1M", randomNonNegativeArray(50_000, 999_999, BASE_SEED + 1)),
            new SweepCase("table3 n=r=1M", randomNonNegativeArray(1_000_000, 999_999, BASE_SEED + 2)),
            new SweepCase("table3 n=r=2M", randomNonNegativeArray(2_000_000, 1_999_999, BASE_SEED + 3))
    };

    public static void main(String[] args) {
        String profileName = profileName(args);
        SweepCase[] cases = selectCases(args);
        int dividerLength = 10 + (cases.length * 19) + 14;

        System.out.printf("Threshold sweep for Improved_CountingSort on %s (%s profile)%n",
                System.getProperty("os.name"),
                profileName);
        System.out.printf("Default threshold in source: %d%n%n", Improved_CountingSort.defaultThreshold());

        System.out.printf("%10s", "C");
        for (SweepCase sweepCase : cases) {
            System.out.printf(" %18s", sweepCase.name);
        }
        System.out.printf(" %12s%n", "total ms");
        System.out.println("-".repeat(dividerLength));

        int bestThreshold = THRESHOLDS[0];
        double bestTotalMillis = Double.MAX_VALUE;

        // The simplest ranking rule: sum the runtimes across the chosen cases and keep
        // the threshold with the smallest overall total.
        for (int threshold : THRESHOLDS) {
            double totalMillis = printThresholdRow(threshold, cases);
            if (totalMillis < bestTotalMillis) {
                bestTotalMillis = totalMillis;
                bestThreshold = threshold;
            }
        }

        System.out.printf("%nRecommended C for this device: %d%n", bestThreshold);
    }

    private static double printThresholdRow(int threshold, SweepCase[] cases) {
        double total = 0.0;

        System.out.printf("%10d", threshold);
        for (SweepCase sweepCase : cases) {
            double millis = averageMillis(sweepCase.source, threshold);
            total += millis;
            System.out.printf(" %18.3f", millis);
        }
        System.out.printf(" %12.3f%n", total);

        return total;
    }

    private static double averageMillis(int[] source, int threshold) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            int[] copy = source.clone();
            Improved_CountingSort.sort(copy, threshold);
            assertSorted(copy);
        }

        double[] samples = new double[MEASURED_RUNS];
        for (int i = 0; i < MEASURED_RUNS; i++) {
            int[] copy = source.clone();
            long start = System.nanoTime();
            Improved_CountingSort.sort(copy, threshold);
            long end = System.nanoTime();
            assertSorted(copy);
            samples[i] = (end - start) / 1_000_000.0;
        }

        // Use the median measured run so one noisy sample does not dominate the result.
        Arrays.sort(samples);
        return samples[samples.length / 2];
    }

    private static void assertSorted(int[] values) {
        for (int i = 1; i < values.length; i++) {
            if (values[i - 1] > values[i]) {
                throw new IllegalStateException("Array is not sorted at index " + i);
            }
        }
    }

    private static SweepCase[] selectCases(String[] args) {
        if (args.length == 0 || "paper".equalsIgnoreCase(args[0])) {
            return PAPER_CASES;
        }

        if ("extended".equalsIgnoreCase(args[0])) {
            return EXTENDED_CASES;
        }

        throw new IllegalArgumentException("Usage: Improved_CountingSortThresholdSweep [paper|extended]");
    }

    private static String profileName(String[] args) {
        if (args.length == 0) {
            return "paper";
        }

        if ("paper".equalsIgnoreCase(args[0]) || "extended".equalsIgnoreCase(args[0])) {
            return args[0].toLowerCase();
        }

        throw new IllegalArgumentException("Usage: Improved_CountingSortThresholdSweep [paper|extended]");
    }

    private static int[] randomNonNegativeArray(int length, int maxValueInclusive, long seed) {
        Random random = new Random(seed);
        int[] values = new int[length];

        for (int i = 0; i < length; i++) {
            values[i] = random.nextInt(maxValueInclusive + 1);
        }

        return values;
    }

    private static final class SweepCase {
        private final String name;
        private final int[] source;

        private SweepCase(String name, int[] source) {
            this.name = name;
            this.source = source;
        }
    }
}
