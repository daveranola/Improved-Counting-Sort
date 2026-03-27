import java.util.Arrays;

public class Improved_CountingSort {
    private static final int C = 10;

    private static void sort(int[] arr) {
        quicksort_modified(arr, 0, arr.length-1, getMax(arr, arr.length), getMin(arr, arr.length));
        countingsort(arr, arr.length);
    }

    private static void quicksort_modified(int[] arr, int low, int high,
                            int maxValue, int minValue) {
        if (low >= high) return;

        int size = high - low + 1;

        if (maxValue - minValue + size <= C) return;

        int pivot = partition(arr, low, high);
        int mid = arr[pivot];

        quicksort_modified(arr, low, pivot-1, mid, minValue);
        quicksort_modified(arr, pivot+1, high, maxValue, mid);

    }

    private static int partition(int[] arr, int low, int high) {
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

    static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }



    private static int getMax(int[] arr, int n) {
        int max = arr[0];
        for (int i = 1; i < n; i++) {
            max = Math.max(max, arr[i]);
        }

        return max;
    }

    private static int getMin(int[] arr, int n) {
        int min = arr[0];
        for (int i = 1; i < n; i++) {
            min = Math.min(min, arr[i]);
        }

        return min;
    }

    private static void countingsort(int arr[], int n) {
        int[] output = new int[n];
        int r = getMax(arr, n);
        int[] count = new int[r+1];

        for (int i = 0; i <= r; i++) {
            count[i] = 0;
        }

        for (int i = 0; i < n; i++) {
            count[arr[i]]++;
        }

        for (int i = 1; i <= r; i++) {
            count[i] += count[i - 1];
        }

        for (int i = n - 1; i >= 0; i--) {
            output[count[arr[i]] - 1] = arr[i];
            count[arr[i]] -= 1;
        }

        for (int i = 0; i < n; i++) {
            arr[i] = output[i];
        }

    }

    public static void main(String[] args) {
        int[] arr = {8, 3, 6, 1, 7, 2, 5, 4};
//        sort(arr);
        quicksort_modified(arr, 0, arr.length-1, getMax(arr, arr.length), getMin(arr, arr.length));
        for (int i : arr) {
            System.out.println(i);
        }
    }
}
