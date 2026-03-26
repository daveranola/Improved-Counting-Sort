import java.util.Arrays;

public class Improved_CountingSort {
    private static final int C = 1000;

    private static void quicksort_modified(int[] arr, int low, int high,
                            int maxValue, int minValue) {
        while ((low < high) && (maxValue - minValue + high - low > C)) {
            int pivot = partition(arr, low, high);
            int midValue = arr[pivot];
            quicksort_modified(arr, low,
                    pivot - 1, midValue,
                    minValue);
            quicksort_modified(arr, pivot + 1,
                    high, maxValue,
                    midValue);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int[] arr1 = Arrays.copyOf(arr,arr.length);
        int pivot = arr[high];

        int i = low - 1;

        for (int j = low; j <= high - 1; j++) {
            if (arr1[j] < pivot) {
                i++;
                swap(arr1, i, j);
            }
        }

        swap(arr1, i + 1, high);
        return i + 1;
    }

    static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }



    private static int getMax(int[]arr, int n) {
        int max = arr[0];
        for (int i = 1; i < n; i++) {
            max = Math.max(max, arr[i]);
        }

        return max;
    }

    private static void countingsort(int arr[], int n) {
        int[] output = new int[n+1];
        // getmax() returns the greatest value from $arr$
        int r = getMax(arr, n);
        int[] count = new int[r+1];
        for (int i = 0; i <= r; i++)
            count[i] = 0;
        for (int i = 0; i < n; i++)
            count[arr[i]]++;
        for (int i = 1; i <= r; i++)
            count[i] += count[i - 1];
        for (int i = n - 1; i >= 0; i--) {
            output[count[arr[i]]] = arr[i];
            count[arr[i]] -= 1;
        }
        for (int i = 0; i < n; i++)
            arr[i] = output[i];
    }

    public static void main(String[] args) {
        int[] arr = new int[]{1, 10000000, 3, 2, 4};
        quicksort_modified(arr, 0, 4, getMax(arr, arr.length), 1);

        for (int i : arr) {
            System.out.println(i);
        }
    }
}
