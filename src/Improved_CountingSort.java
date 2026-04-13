import java.util.ArrayList;
import java.util.List;

public class Improved_CountingSort {
    private static final int C = 1000;

    public static void sort(int[] arr) {
        if (arr.length < 2) {
            return;
        }

        PartitionPlan partitionPlan = preprocess(arr);
        countingSortPartitions(arr, partitionPlan);
    }

    static PartitionPlan preprocess(int[] arr) {
        if (arr.length < 2) {
            return new PartitionPlan(new Partition[0]);
        }

        List<Partition> partitions = new ArrayList<>();
        quicksortModified(arr, 0, arr.length - 1, getMax(arr), getMin(arr), partitions);
        return new PartitionPlan(partitions.toArray(new Partition[0]));
    }

    static void countingSortPartitions(int[] arr, PartitionPlan partitionPlan) {
        for (Partition partition : partitionPlan.partitions) {
            countingSortPartition(arr, partition.low, partition.high, partition.minValue, partition.maxValue);
        }
    }

    private static void quicksortModified(int[] arr, int low, int high,
                                          int maxValue, int minValue, List<Partition> partitions) {
        if (low > high) {
            return;
        }

        int size = high - low + 1;

        if (size < 2 || maxValue - minValue + size - 1 <= C) {
            partitions.add(new Partition(low, high, minValue, maxValue));
            return;
        }

        int pivot = partitionMedianOfThree(arr, low, high);
        int midValue = arr[pivot];

        quicksortModified(arr, low, pivot - 1, midValue, minValue, partitions);
        quicksortModified(arr, pivot + 1, high, maxValue, midValue, partitions);
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

    static final class PartitionPlan {
        private final Partition[] partitions;

        private PartitionPlan(Partition[] partitions) {
            this.partitions = partitions;
        }

        int partitionCount() {
            return partitions.length;
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
    }
}
