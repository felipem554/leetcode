
import java.util.Arrays;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {

        int[] nums1 = {72, 15, 92, 53, 47, 8, 66, 39, 85, 10, 28, 99, 4, 61, 35};
        int target = 19;
        try
        {
            System.out.println(Arrays.toString(solution(nums1, target)));
        } catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
        }
    }

    public static int[] solution(int[] nums1, int target) {

        HashMap<Integer, Integer> hashMap = new HashMap<>();

        // {11, 15, 7, 5, 17, 2};
        //{2: 0,
        // 6: 1,
        // -2: 2
        // 4: 3
        // 8: 4,
        // 7: 5} return

        for (int i = 0; i < nums1.length; i++) {

            if (hashMap.containsKey(nums1[i])){
                return new int[] {hashMap.get(nums1[i]), i};
            }
            int complement = target - nums1[i];
            hashMap.put(complement, i);
        }

        throw new IllegalArgumentException("two sums not found in the array!");
    }
}