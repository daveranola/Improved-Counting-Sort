import java.util.ArrayList;
import java.util.List;

public class Improved_CountingSort {
    // Paper 2 treats C as a machine-dependent limit for when a partition is small enough
    // for localized counting sort to be worthwhile without further quicksort splitting.
    // The paper's experiments used C = 1000, so that stays as the repository default.
    private static final int DEFAULT_THRESHOLD = 1000;

    public static void sort(int[] arr) {
        sort(arr, DEFAULT_THRESHOLD);
    }

    public static void sort(int[] arr, int threshold) {
        // Validate once at the public entry point so invalid C values fail fast before any work.
        validateThreshold(threshold);
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
        validateThreshold(threshold);
        if (arr.length == 0) {
            return new PartitionPlan(threshold, new Partition[0]);
        }

        // Keep the partition model uniform for later stages: even a single value is represented
        // as one partition with fully known index and value bounds.
        if (arr.length == 1) {
            return new PartitionPlan(threshold, new Partition[] {
                    new Partition(0, 0, arr[0], arr[0])
            });
        }

        List<Partition> partitions = new ArrayList<>();
        // The initial bounds cover the whole input. Recursive calls narrow these bounds as the
        // modified quicksort discovers smaller value ranges for each partition.
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

        // The paper's partition condition is (value range + partition size) <= C.
        if (size < 2 || maxValue - minValue + size <= threshold) {
            partitions.add(new Partition(low, high, minValue, maxValue));
            return;
        }

        PartitionSplit split = partitionMedianOfThreeWithBounds(arr, low, high);

        if (split.hasLeftPartition()) {
            quicksortModified(arr, low, split.pivotIndex() - 1, split.leftMaxValue(), minValue, threshold, partitions);
        }

        // Keep pivot positions as singleton partitions so the plan stays contiguous and covers
        // the whole array, which matches the paper's partition description.
        partitions.add(new Partition(split.pivotIndex(), split.pivotIndex(), split.pivotValue(), split.pivotValue()));

        if (split.hasRightPartition()) {
            quicksortModified(arr, split.pivotIndex() + 1, high, maxValue, split.rightMinValue(), threshold, partitions);
        }
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

    private static PartitionSplit partitionMedianOfThreeWithBounds(int[] arr, int low, int high) {
        int mid = (low + high) / 2;

        // Reuse the same median-of-three ordering as the public quicksort helper so benchmark
        // comparisons stay consistent across the repository.
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
        boolean hasLeftPartition = false;
        int leftMaxValue = Integer.MIN_VALUE;
        boolean hasRightPartition = false;
        int rightMinValue = Integer.MAX_VALUE;

        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                hasLeftPartition = true;
                // Track the actual maximum seen on the left so recursive calls receive tighter
                // bounds than the pivot value when duplicates are present.
                leftMaxValue = Math.max(leftMaxValue, arr[j]);
                swap(arr, i, j);
                i++;
            } else {
                hasRightPartition = true;
                // Values equal to the pivot stay on the right side in this partition scheme, so
                // the next recursive call needs the true minimum from that side rather than pivot.
                rightMinValue = Math.min(rightMinValue, arr[j]);
            }
        }

        swap(arr, i, high);
        return new PartitionSplit(i, pivot, hasLeftPartition, leftMaxValue, hasRightPartition, rightMinValue);
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
        // Each partition is sorted relative to its own minimum value, which keeps the count array
        // as small as the paper's range bound allows.
        int[] count = new int[maxValue - minValue + 1];

        for (int i = low; i <= high; i++) {
            count[arr[i] - minValue]++;
        }

        for (int i = 1; i < count.length; i++) {
            count[i] += count[i - 1];
        }

        for (int i = high; i >= low; i--) {
            // Normalize values into the local [0, range] bucket space for this partition.
            int normalizedValue = arr[i] - minValue;
            output[count[normalizedValue] - 1] = arr[i];
            count[normalizedValue]--;
        }

        System.arraycopy(output, 0, arr, low, size);
    }

    private static void validateThreshold(int threshold) {
        if (threshold < 1) {
            // The paper only requires C > 0; smaller values make the partition rule meaningless.
            throw new IllegalArgumentException("Threshold C must be at least 1.");
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

    private static final class PartitionSplit {
        private final int pivotIndex;
        private final int pivotValue;
        private final boolean hasLeftPartition;
        private final int leftMaxValue;
        private final boolean hasRightPartition;
        private final int rightMinValue;

        private PartitionSplit(int pivotIndex,
                               int pivotValue,
                               boolean hasLeftPartition,
                               int leftMaxValue,
                               boolean hasRightPartition,
                               int rightMinValue) {
            this.pivotIndex = pivotIndex;
            this.pivotValue = pivotValue;
            this.hasLeftPartition = hasLeftPartition;
            this.leftMaxValue = leftMaxValue;
            this.hasRightPartition = hasRightPartition;
            this.rightMinValue = rightMinValue;
        }

        int pivotIndex() {
            return pivotIndex;
        }

        int pivotValue() {
            return pivotValue;
        }

        boolean hasLeftPartition() {
            return hasLeftPartition;
        }

        int leftMaxValue() {
            return leftMaxValue;
        }

        boolean hasRightPartition() {
            return hasRightPartition;
        }

        int rightMinValue() {
            return rightMinValue;
        }
    }
}
