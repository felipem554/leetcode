import java.util.Arrays;

public class RemoveDuplicatesSortedArray {

    public static void main(String[] args){

        int[] nums = {1,1,1,2,2,3};
        System.out.println(Arrays.toString(removeDuplicates(nums)));
    }

    public static int[] removeDuplicates(int[] nums) {

        int nonDuplicateArrayIndex = 0;
        for(int i = 0; i < nums.length; i++){
            if(nonDuplicateArrayIndex < 2 || nums[nonDuplicateArrayIndex-2] < nums[i]){
                nums[nonDuplicateArrayIndex] = nums[i];
                nonDuplicateArrayIndex++;
            }
        }
        return nums;
    }

    public static void junitTest(int[] nums){
        int[] expected = {1,1,2,2,3,3};
        for (int i = 0; i < nums.length; i++) {
            //assertEquals(num[i], expected[i], "mismacht at index = " + i);
        }
    }
}
