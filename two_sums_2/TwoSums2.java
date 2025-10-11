package two_sums_2;

import java.util.Arrays;
import java.util.HashMap;

public class TwoSums2 {

    public static void main(String[] args) {

        System.out.println(Arrays.toString(twoSum(new int[] {-1, 0}, -1)));
    }

    public static int[] twoSum(int[] numbers, int target) {

        HashMap<Integer, Integer> hashmap = new HashMap<>();

        for(int i = 0; i < numbers.length; i++){ //

            if(hashmap.containsKey(numbers[i])){
                return new int[] {hashmap.get(numbers[i]), i};
            }

            int aux = target - numbers[i]; //
            hashmap.put(aux, i);
        }

        throw new IllegalArgumentException("the dream is over");
    }
}
