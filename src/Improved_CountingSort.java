import java.util.ArrayList;
import java.util.List;

public class Improved_CountingSort {
    // Paper 2 treats C as a machine-dependent limit for when a partition is small enough
    // for localized counting sort to be worthwhile without further quicksort splitting.
    private static final int DEFAULT_THRESHOLD = 1000;

    public static void sort(int[] arr) {
        sort(arr, DEFAULT_THRESHOLD);
    }

    public static void sort(int[] arr, int threshold) {
        if (arr.length < 2) {
            return;
        }

        // First use modified quicksort to carve the array into threshold-bounded partitions,
        // then run counting sort independently inside each partition.
        PartitionPlan partitionPlan = preprocess(arr, threshold);
        countingSortPartitions(arr, partitionPlan);
    }

    static PartitionPlan preprocess(int[] arr) {
        return preprocess(arr, DEFAULT_THRESHOLD);
    }

    static PartitionPlan preprocess(int[] arr, int threshold) {
        if (arr.length < 2) {
            return new PartitionPlan(threshold, new Partition[0]);
        }

        validateThreshold(threshold);
        List<Partition> partitions = new ArrayList<>();
        quicksortModified(arr, 0, arr.length - 1, getMax(arr), getMin(arr), threshold, partitions);
        return new PartitionPlan(threshold, partitions.toArray(new Partition[0]));
    }

    static void countingSortPartitions(int[] arr, PartitionPlan partitionPlan) {
        for (Partition partition : partitionPlan.partitions) {
            countingSortPartition(arr, partition.low, partition.high, partition.minValue, partition.maxValue);
        }
    }

    static int defaultThreshold() {
        return DEFAULT_THRESHOLD;
    }

    private static void quicksortModified(int[] arr, int low, int high,
                                          int maxValue, int minValue, int threshold, List<Partition> partitions) {
        if (low > high) {
            return;
        }

        int size = high - low + 1;

        // This is the paper's stopping rule: stop recursing once
        // (partition value range + partition size - 1) <= C.
        if (size < 2 || maxValue - minValue + size - 1 <= threshold) {
            partitions.add(new Partition(low, high, minValue, maxValue));
            return;
        }

        int pivot = partitionMedianOfThree(arr, low, high);
        int midValue = arr[pivot];

        quicksortModified(arr, low, pivot - 1, midValue, minValue, threshold, partitions);
        quicksortModified(arr, pivot + 1, high, maxValue, midValue, threshold, partitions);
    }

    public static int partitionMedianOfThree(int[] arr, int low, int high) {
        int mid = (low + high) / 2;

        // median of three
        if (arr[low] > arr[mid]) {
            swap(arr, low, mid);
        }

        if (arr[low] > arr[high]) {
            swap(arr, low, high);
        }

        if (arr[mid] > arr[high]) {
            swap(arr, mid, high);
        }

        swap(arr, mid, high);
        int pivot = arr[high];
        int i = low;

        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                swap(arr, i, j);
                i++;
            }
        }

        swap(arr, i, high);
        return i;

    }

    public static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public static int getMax(int[] arr) {
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            max = Math.max(max, arr[i]);
        }

        return max;
    }

    public static int getMin(int[] arr) {
        int min = arr[0];
        for (int i = 1; i < arr.length; i++) {
            min = Math.min(min, arr[i]);
        }

        return min;
    }

    private static void countingSortPartition(int[] arr, int low, int high, int minValue, int maxValue) {
        if (low >= high) {
            return;
        }

        int size = high - low + 1;
        int[] output = new int[size];
        int[] count = new int[maxValue - minValue + 1];

        for (int i = low; i <= high; i++) {
            count[arr[i] - minValue]++;
        }

        for (int i = 1; i < count.length; i++) {
            count[i] += count[i - 1];
        }

        for (int i = high; i >= low; i--) {
            int normalizedValue = arr[i] - minValue;
            output[count[normalizedValue] - 1] = arr[i];
            count[normalizedValue]--;
        }

        System.arraycopy(output, 0, arr, low, size);
    }

    private static void validateThreshold(int threshold) {
        if (threshold < 2) {
            throw new IllegalArgumentException("Threshold C must be at least 2.");
        }
    }

    static final class PartitionPlan {
        private final int threshold;
        private final Partition[] partitions;

        private PartitionPlan(int threshold, Partition[] partitions) {
            this.threshold = threshold;
            this.partitions = partitions;
        }

        int partitionCount() {
            return partitions.length;
        }

        int threshold() {
            return threshold;
        }

        Partition partitionAt(int index) {
            return partitions[index];
        }
    }

    static final class Partition {
        private final int low;
        private final int high;
        private final int minValue;
        private final int maxValue;

        private Partition(int low, int high, int minValue, int maxValue) {
            this.low = low;
            this.high = high;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        int low() {
            return low;
        }

        int high() {
            return high;
        }

        int minValue() {
            return minValue;
        }

        int maxValue() {
            return maxValue;
        }

        int size() {
            return high - low + 1;
        }

        int rangeWidth() {
            return maxValue - minValue;
        }

        int rangePlusSize() {
            // Helper for the paper's threshold check: (max - min) + partition length.
            return rangeWidth() + size();
        }
    }
}
