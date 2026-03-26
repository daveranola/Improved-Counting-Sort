public class Improved_CountingSort {
    private static final int C = 1000;

    private static void quicksort_modified(int arr[], int low, int high,
                            int maxValue, int minValue) {
        while ((low < high) &&
                (maxValue - minValue +
                        high - low > C)) {
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
        return 0;
    }

    private static int getMax(int[]arr, int n) {
        return 0;
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
}
