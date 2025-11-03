import java.util.Arrays;
import java.util.Optional;

public class MergeSortedArray {
    public static void main(String[] args) {

        int[] nums1 = {1,2,3,0,0,0};
        int m = 3;
        int[] nums2 = {2,5,6};
        int n = 3;
        System.out.println(solution(nums1, m, nums2, n));
    }

    public static Integer solution(int[] nums1, int m, int[] nums2, int n) {

        int i = 0;
        int j = 0;
        int k = 0;
        int[] aux = new int[m+n];
        while(k < (m + n - 1)) {

            if (nums1[i] <= nums2[j]){
                aux[k] = nums1[i];
                i++;
            } else {
                aux[k] = nums2[j];
                j++;
            }
            k++;
        }
        return 0;
    }
}