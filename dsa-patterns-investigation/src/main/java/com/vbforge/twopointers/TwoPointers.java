package com.vbforge.twopointers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <h2>Two Pointers Pattern</h2>
 *
 * <p>Uses two indices that traverse the input to reduce a naive <b>O(n²)</b> scan
 * down to <b>O(n)</b>. Three main flavours are demonstrated here:</p>
 *
 * <ol>
 *   <li><b>Opposite direction</b> — pointers start at both ends and converge.
 *       Typically requires a <em>sorted</em> input.</li>
 *   <li><b>Same direction (fast/slow)</b> — usually for in-place
 *       compaction / filtering.</li>
 *   <li><b>Two arrays</b> — one pointer per array; walking from the back
 *       avoids extra memory.</li>
 * </ol>
 *
 * <p>All methods are <code>static</code>; this is a pure utility class.</p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Two-pointer_technique">Two-pointer technique</a>
 */
public final class TwoPointers {

    private TwoPointers() {
        // Utility class — do not instantiate.
    }

    // =====================================================================
    //  1. Opposite-direction pointers
    // =====================================================================

    /**
     * <b>Two Sum II</b> — input array is sorted.
     *
     * <p>Return 1-based indices {@code [i, j]} such that
     * {@code nums[i-1] + nums[j-1] == target}; return an empty array if none.</p>
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static int[] twoSumSorted(int[] nums, int target) {
        int left = 0;
        int right = nums.length - 1;

        while (left < right) {
            int sum = nums[left] + nums[right];
            if (sum == target) {
                return new int[]{left + 1, right + 1};
            } else if (sum < target) {
                left++;          // need a bigger sum → grow from the left
            } else {
                right--;         // need a smaller sum → shrink from the right
            }
        }
        return new int[0];
    }

    /**
     * <b>Valid Palindrome</b> — ignores case and non-alphanumeric characters.
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static boolean isPalindrome(String s) {
        int left = 0;
        int right = s.length() - 1;

        while (left < right) {
            while (left < right && !Character.isLetterOrDigit(s.charAt(left)))  left++;
            while (left < right && !Character.isLetterOrDigit(s.charAt(right))) right--;

            if (Character.toLowerCase(s.charAt(left))
                    != Character.toLowerCase(s.charAt(right))) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    /**
     * <b>Container With Most Water</b> — max area between two vertical lines.
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static int maxArea(int[] height) {
        int left = 0;
        int right = height.length - 1;
        int best = 0;

        while (left < right) {
            int h = Math.min(height[left], height[right]);
            int w = right - left;
            best = Math.max(best, h * w);

            // Move the shorter side inward — the taller one is the bottleneck.
            if (height[left] < height[right]) {
                left++;
            } else {
                right--;
            }
        }
        return best;
    }

    // =====================================================================
    //  2. Same-direction (fast/slow) pointers
    // =====================================================================

    /**
     * <b>Remove Duplicates from Sorted Array</b> — in place.
     *
     * <p>Returns {@code k}, the number of unique elements. The first {@code k}
     * elements of {@code nums} contain the unique values.</p>
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static int removeDuplicates(int[] nums) {
        if (nums.length == 0) return 0;

        int slow = 0;
        for (int fast = 1; fast < nums.length; fast++) {
            if (nums[fast] != nums[slow]) {
                slow++;
                nums[slow] = nums[fast];
            }
        }
        return slow + 1;
    }

    /**
     * <b>Move Zeroes</b> — moves all zeros to the end while preserving the
     * relative order of non-zero elements. In place.
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static void moveZeroes(int[] nums) {
        int slow = 0;
        for (int fast = 0; fast < nums.length; fast++) {
            if (nums[fast] != 0) {
                int tmp = nums[slow];
                nums[slow] = nums[fast];
                nums[fast] = tmp;
                slow++;
            }
        }
    }

    // =====================================================================
    //  3. Reversal (opposite-direction pointers)
    // =====================================================================

    /**
     * <b>Reverse String</b> — in place.
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static void reverseString(char[] s) {
        int left = 0;
        int right = s.length - 1;
        while (left < right) {
            char tmp = s[left];
            s[left++]  = s[right];
            s[right--] = tmp;
        }
    }

    // =====================================================================
    //  4. Two arrays — merge from the back
    // =====================================================================

    /**
     * <b>Merge Sorted Array</b> — merges {@code nums2} into {@code nums1}.
     * {@code nums1} has length {@code m + n}, with the first {@code m} cells
     * holding valid data and the rest zero-padding.
     *
     * <p>Iterating <em>from the back</em> avoids overwriting un-merged values.</p>
     *
     * <p>Time: O(m + n) — Space: O(1)</p>
     */
    public static void mergeSorted(int[] nums1, int m, int[] nums2, int n) {
        int i = m - 1;       // last valid element in nums1
        int j = n - 1;       // last element in nums2
        int k = m + n - 1;   // last cell of nums1

        while (j >= 0) {
            if (i >= 0 && nums1[i] > nums2[j]) {
                nums1[k--] = nums1[i--];
            } else {
                nums1[k--] = nums2[j--];
            }
        }
    }

    // =====================================================================
    //  5. Combination problems built on the two-pointer skeleton
    // =====================================================================

    /**
     * <b>3Sum</b> — all unique triplets that sum to zero.
     *
     * <p>Sort first, then for each anchor {@code i} use opposite-direction
     * two pointers over the remaining slice. Skip duplicates to keep the
     * triplets unique.</p>
     *
     * <p>Time: O(n²) — Space: O(1) (ignoring output)</p>
     */
    public static List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(nums);
        int n = nums.length;

        for (int i = 0; i < n - 2; i++) {
            if (nums[i] > 0) break;                              // no way to reach 0
            if (i > 0 && nums[i] == nums[i - 1]) continue;       // skip duplicate anchor

            int left = i + 1;
            int right = n - 1;
            int target = -nums[i];

            while (left < right) {
                int sum = nums[left] + nums[right];
                if (sum == target) {
                    result.add(List.of(nums[i], nums[left], nums[right]));
                    while (left < right && nums[left]  == nums[left + 1])  left++;
                    while (left < right && nums[right] == nums[right - 1]) right--;
                    left++;
                    right--;
                } else if (sum < target) {
                    left++;
                } else {
                    right--;
                }
            }
        }
        return result;
    }

    /**
     * <b>Sort Colors</b> — Dutch National Flag. Sorts an array of 0/1/2 in place
     * using <em>three</em> pointers ({@code low}, {@code mid}, {@code high}).
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static void sortColors(int[] nums) {
        int low = 0;
        int mid = 0;
        int high = nums.length - 1;

        while (mid <= high) {
            int v = nums[mid];
            if (v == 0) {
                swap(nums, low++, mid++);
            } else if (v == 1) {
                mid++;
            } else {
                swap(nums, mid, high--);
            }
        }
    }

    private static void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}