package com.vbforge.slidingwindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h2>Sliding Window Pattern</h2>
 *
 * <p>Maintains a contiguous subarray (the <em>window</em>) between two pointers
 * {@code [left, right]} and slides it across the input. Every element enters
 * the window exactly once (when {@code right} advances) and leaves at most
 * once (when {@code left} advances), giving <b>amortised O(n)</b>.</p>
 *
 * <p>Two flavours:</p>
 * <ul>
 *   <li><b>Fixed-size</b> — window length is given; slide by moving both
 *       pointers together.</li>
 *   <li><b>Variable-size</b> — grow {@code right} until the window becomes
 *       invalid, then shrink {@code left} until it becomes valid again.</li>
 * </ul>
 *
 * <p>All methods are {@code static}; this is a pure utility class.</p>
 */
public final class SlidingWindow {

    private SlidingWindow() {
        // Utility class — do not instantiate.
    }

    // =====================================================================
    //  1. Fixed-size window
    // =====================================================================

    /**
     * <b>Maximum Sum Subarray of Size K</b>.
     *
     * <p>Compute the sum of the first window once, then slide by
     * {@code +nums[i] - nums[i - k]}.</p>
     *
     * <p>Time: O(n) — Space: O(1)</p>
     *
     * @throws IllegalArgumentException if {@code k <= 0} or {@code k > nums.length}
     */
    public static int maxSumSubarray(int[] nums, int k) {
        if (k <= 0 || k > nums.length) {
            throw new IllegalArgumentException("k must be in [1, nums.length]");
        }
        int sum = 0;
        for (int i = 0; i < k; i++) sum += nums[i];

        int max = sum;
        for (int i = k; i < nums.length; i++) {
            sum += nums[i] - nums[i - k];
            max = Math.max(max, sum);
        }
        return max;
    }

    /**
     * <b>Find All Anagrams in a String</b>.
     *
     * <p>Returns every start index {@code i} such that the substring
     * {@code s[i .. i + p.length() - 1]} is an anagram of {@code p}.</p>
     *
     * <p>Uses a fixed window of size {@code p.length()} plus two
     * {@code int[26]} frequency arrays that are compared via
     * {@link Arrays#equals(int[], int[]) Arrays.equals} (O(1) — fixed size).</p>
     *
     * <p>Time: O(n) — Space: O(1) (alphabet constant)</p>
     */
    public static List<Integer> findAnagrams(String s, String p) {
        List<Integer> result = new ArrayList<>();
        int k = p.length();
        if (k == 0 || k > s.length()) return result;

        int[] pCount      = new int[26];
        int[] windowCount = new int[26];
        for (char c : p.toCharArray()) pCount[c - 'a']++;

        for (int i = 0; i < s.length(); i++) {
            windowCount[s.charAt(i) - 'a']++;
            if (i >= k) {
                windowCount[s.charAt(i - k) - 'a']--;        // shrink
            }
            if (i >= k - 1 && Arrays.equals(pCount, windowCount)) {
                result.add(i - k + 1);
            }
        }
        return result;
    }

    // =====================================================================
    //  2. Variable-size window — grow/shrink on constraint
    // =====================================================================

    /**
     * <b>Longest Substring Without Repeating Characters</b>.
     *
     * <p>Track the last index of each character. On a repeat, jump
     * {@code left} forward to {@code lastSeen(c) + 1} — never move it
     * backwards.</p>
     *
     * <p>Time: O(n) — Space: O(min(n, Σ)) where Σ is the alphabet size</p>
     */
    public static int lengthOfLongestSubstring(String s) {
        Map<Character, Integer> lastSeen = new HashMap<>();
        int max = 0;
        int left = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            Integer prev = lastSeen.get(c);
            if (prev != null && prev >= left) {
                left = prev + 1;
            }
            lastSeen.put(c, right);
            max = Math.max(max, right - left + 1);
        }
        return max;
    }

    /**
     * <b>Minimum Size Subarray Sum</b> — smallest length of a contiguous
     * subarray whose sum is {@code >= target}, or {@code 0} if none exists.
     *
     * <p>Grow right; while {@code sum >= target}, record the length and
     * shrink from the left. All values in {@code nums} must be
     * <b>positive</b> (otherwise the shrink step is unsound).</p>
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static int minSubArrayLen(int target, int[] nums) {
        int left = 0;
        int sum = 0;
        int minLen = Integer.MAX_VALUE;

        for (int right = 0; right < nums.length; right++) {
            sum += nums[right];
            while (sum >= target) {
                minLen = Math.min(minLen, right - left + 1);
                sum -= nums[left++];
            }
        }
        return minLen == Integer.MAX_VALUE ? 0 : minLen;
    }

    // =====================================================================
    //  3. "At most K" variable window
    // =====================================================================

    /**
     * <b>Longest Repeating Character Replacement</b> — longest substring that
     * can be made all-same by replacing at most {@code k} characters.
     *
     * <p>Window is valid iff {@code windowSize - maxFrequency <= k}. The
     * "dominant" character fills the window; the rest are replaced.</p>
     *
     * <p>Time: O(n) — Space: O(Σ)</p>
     */
    public static int characterReplacement(String s, int k) {
        Map<Character, Integer> freq = new HashMap<>();
        int left = 0;
        int maxFreq = 0;
        int max = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            freq.merge(c, 1, Integer::sum);
            maxFreq = Math.max(maxFreq, freq.get(c));

            while ((right - left + 1) - maxFreq > k) {
                char lc = s.charAt(left++);
                freq.merge(lc, -1, Integer::sum);
            }
            max = Math.max(max, right - left + 1);
        }
        return max;
    }

    /**
     * <b>Max Consecutive Ones III</b> — longest subarray of 0/1 that can be
     * made all-1s by flipping at most {@code k} zeros.
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static int longestOnes(int[] nums, int k) {
        int left = 0;
        int zeros = 0;
        int max = 0;

        for (int right = 0; right < nums.length; right++) {
            if (nums[right] == 0) zeros++;
            while (zeros > k) {
                if (nums[left++] == 0) zeros--;
            }
            max = Math.max(max, right - left + 1);
        }
        return max;
    }

    /**
     * <b>Fruit Into Baskets</b> — longest subarray with at most <em>two</em>
     * distinct values.
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static int totalFruit(int[] fruits) {
        Map<Integer, Integer> count = new HashMap<>();
        int left = 0;
        int max = 0;

        for (int right = 0; right < fruits.length; right++) {
            count.merge(fruits[right], 1, Integer::sum);
            while (count.size() > 2) {
                int lf = fruits[left++];
                if (count.merge(lf, -1, Integer::sum) == 0) {
                    count.remove(lf);
                }
            }
            max = Math.max(max, right - left + 1);
        }
        return max;
    }

    // =====================================================================
    //  4. Advanced — Minimum Window Substring
    // =====================================================================

    /**
     * <b>Minimum Window Substring</b> — smallest substring of {@code s}
     * containing every character of {@code t} (with multiplicity).
     * Returns {@code ""} if no such window exists.
     *
     * <p>Uses a {@code int[128]} need-map and a {@code required} counter.
     * When {@code need[c] > 0} before decrementing, the character was still
     * needed, so {@code required--}. The mirror on shrinking works
     * symmetrically.</p>
     *
     * <p>Time: O(|s| + |t|) — Space: O(1) (ASCII alphabet)</p>
     */
    public static String minWindow(String s, String t) {
        if (t.isEmpty() || s.length() < t.length()) return "";

        int[] need = new int[128];
        for (char c : t.toCharArray()) need[c]++;

        int required = t.length();
        int left = 0;
        int minLen = Integer.MAX_VALUE;
        int minStart = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            if (need[c] > 0) required--;     // this occurrence was genuinely needed
            need[c]--;

            while (required == 0) {          // window is valid → try to shrink
                if (right - left + 1 < minLen) {
                    minLen = right - left + 1;
                    minStart = left;
                }
                char lc = s.charAt(left++);
                need[lc]++;
                if (need[lc] > 0) required++; // we now need this char again
            }
        }
        return minLen == Integer.MAX_VALUE ? "" : s.substring(minStart, minStart + minLen);
    }
}