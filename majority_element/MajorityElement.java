import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MajorityElement {

    public static void main(String[] args){
        Integer result = majorityElement(new int[]{1,2,1,2,2});
        System.out.println(result);
    }

    public static int majorityElement(int[] nums) {

        int candidate = 0, count = 0;

        for (int num : nums) {
            if(count == 0){
                candidate = num;
            }
            count += (candidate == num) ? 1 : -1;
        }
        return candidate;
    }
}
