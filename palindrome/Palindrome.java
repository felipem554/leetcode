public class Palindrome {

    public static void main(String[] args){

        //non letter caracters are ignored
        String palindromo1 = "panama 333^R%*&%";
        String palindromo2 = "A man, a plan, a canal: Panama";
        System.out.println(isPalindrome(palindromo1));
        System.out.println(isPalindrome(palindromo2));
    }

    public static boolean isPalindrome(String s) {
        int left = 0;
        int right = s.length() - 1;

        // Loop until the two pointers meet or cross
        while (left < right) {

            // Move the left pointer forward if current character is not alphanumeric
            while (left < right && !Character.isLetterOrDigit(s.charAt(left))) {
                left++;
            }

            // Move the right pointer backward if current character is not alphanumeric
            while (left < right && !Character.isLetterOrDigit(s.charAt(right))) {
                right--;
            }

            // Compare the characters at left and right pointers (ignoring case)
            if (Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) {
                return false;
            }

            // Move both pointers towards the center
            left++;
            right--;
        }

        return true;
    }
}


