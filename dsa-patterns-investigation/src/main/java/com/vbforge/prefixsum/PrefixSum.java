package com.vbforge.prefixsum;

import java.util.HashMap;
import java.util.Map;

/**
 * <h2>Prefix Sum Pattern</h2>
 *
 * <p>Precomputes cumulative information so that any range query becomes an
 * O(1) difference of two precomputed values. The canonical arrays are:</p>
 * <ul>
 *   <li><b>Sum</b>:   {@code prefix[i+1] = prefix[i] + nums[i]} → range sum</li>
 *   <li><b>Xor</b>:   {@code prefix[i+1] = prefix[i] ^ nums[i]} → range xor</li>
 *   <li><b>Product</b>: two arrays, left-products and right-products</li>
 * </ul>
 *
 * <p>The <b>prefix-sum + HashMap</b> idiom is the single most reusable trick
 * in this pattern: whenever you need "how many subarrays have property X",
 * scan once accumulating the prefix and look up the needed prior prefix
 * in a hashmap.</p>
 */
public final class PrefixSum {

    private PrefixSum() { }

    // =====================================================================
    //  1. 1-D prefix sum
    // =====================================================================

    /**
     * Builds the {@code (n + 1)}-sized prefix array. The leading {@code 0}
     * is intentional — it makes {@link #rangeSum(int[], int, int)} uniform
     * for ranges starting at index 0.
     *
     * <p>Time: O(n) — Space: O(n)</p>
     */
    public static int[] buildPrefix(int[] nums) {
        int[] prefix = new int[nums.length + 1];
        for (int i = 0; i < nums.length; i++) {
            prefix[i + 1] = prefix[i] + nums[i];
        }
        return prefix;
    }

    /**
     * Sum of {@code nums[left..right]} inclusive. Requires a prefix array
     * built via {@link #buildPrefix(int[])}.
     *
     * <p>Time: O(1) — Space: O(1)</p>
     */
    public static int rangeSum(int[] prefix, int left, int right) {
        return prefix[right + 1] - prefix[left];
    }

    // =====================================================================
    //  2. 2-D prefix sum
    // =====================================================================

    /**
     * Builds an {@code (m+1) × (n+1)} 2-D prefix using the
     * inclusion-exclusion identity:
     * <pre>{@code
     *   P[i+1][j+1] = M[i][j] + P[i][j+1] + P[i+1][j] - P[i][j]
     * }</pre>
     *
     * <p>Time: O(m·n) — Space: O(m·n)</p>
     */
    public static int[][] buildPrefix2D(int[][] matrix) {
        int m = matrix.length, n = matrix[0].length;
        int[][] prefix = new int[m + 1][n + 1];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                prefix[i + 1][j + 1] = matrix[i][j]
                        + prefix[i][j + 1]
                        + prefix[i + 1][j]
                        - prefix[i][j];
            }
        }
        return prefix;
    }

    /**
     * Sum of the rectangle {@code [r1..r2] × [c1..c2]} inclusive.
     * Uses inclusion-exclusion on the 2-D prefix:
     * <pre>{@code
     *   P[r2+1][c2+1] - P[r1][c2+1] - P[r2+1][c1] + P[r1][c1]
     * }</pre>
     *
     * <p>Time: O(1) — Space: O(1)</p>
     */
    public static int rangeSum2D(int[][] prefix, int r1, int c1, int r2, int c2) {
        return prefix[r2 + 1][c2 + 1]
             - prefix[r1][c2 + 1]
             - prefix[r2 + 1][c1]
             + prefix[r1][c1];
    }

    // =====================================================================
    //  3. Subarray Sum Equals K
    // =====================================================================

    /**
     * <b>Subarray Sum Equals K</b> — number of contiguous subarrays whose
     * sum is exactly {@code k}.
     *
     * <p>For each {@code j}, the subarray {@code (i, j]} has sum
     * {@code prefix[j] - prefix[i]}. We want that to equal {@code k}, i.e.
     * {@code prefix[i] = prefix[j] - k}. So the count is simply "how many
     * prior prefixes equal {@code runningSum - k}".</p>
     *
     * <p>Seeding the map with {@code {0: 1}} accounts for subarrays that
     * start at index 0 — the "prefix before the array" is 0.</p>
     *
     * <p>Time: O(n) — Space: O(n)</p>
     */
    public static int subarraySum(int[] nums, int k) {
        Map<Integer, Integer> prefixCount = new HashMap<>();
        prefixCount.put(0, 1);                       // empty prefix
        int runningSum = 0;
        int count = 0;

        for (int n : nums) {
            runningSum += n;
            count += prefixCount.getOrDefault(runningSum - k, 0);
            prefixCount.merge(runningSum, 1, Integer::sum);
        }
        return count;
    }

    // =====================================================================
    //  4. Product of Array Except Self
    // =====================================================================

    /**
     * <b>Product of Array Except Self</b> — {@code result[i]} = product of
     * all elements except {@code nums[i]}, <b>without division</b> and in
     * O(1) extra space (the output array is not counted).
     *
     * <p>Two passes:</p>
     * <ol>
     *   <li>Left-to-right: store prefix products.</li>
     *   <li>Right-to-left: multiply by suffix product on the fly.</li>
     * </ol>
     *
     * <p>Time: O(n) — Space: O(1) (output only)</p>
     */
    public static int[] productExceptSelf(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];

        // Pass 1 — result[i] = product of nums[0..i-1]
        result[0] = 1;
        for (int i = 1; i < n; i++) {
            result[i] = result[i - 1] * nums[i - 1];
        }

        // Pass 2 — multiply by product of nums[i+1..n-1]
        int right = 1;
        for (int i = n - 1; i >= 0; i--) {
            result[i] *= right;
            right *= nums[i];
        }
        return result;
    }

    // =====================================================================
    //  5. Pivot Index
    // =====================================================================

    /**
     * <b>Find Pivot Index</b> — smallest {@code i} such that
     * {@code sum(nums[0..i-1]) == sum(nums[i+1..n-1])}, or {@code -1}.
     *
     * <p>Left sum accumulates as we go; right sum is
     * {@code total - leftSum - nums[i]}.</p>
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static int pivotIndex(int[] nums) {
        int total = 0;
        for (int n : nums) total += n;

        int leftSum = 0;
        for (int i = 0; i < nums.length; i++) {
            int rightSum = total - leftSum - nums[i];
            if (leftSum == rightSum) return i;
            leftSum += nums[i];
        }
        return -1;
    }

    // =====================================================================
    //  6. Contiguous Array — longest subarray with equal 0s and 1s
    // =====================================================================

    /**
     * <b>Contiguous Array</b> — length of the longest subarray with an
     * equal number of 0s and 1s.
     *
     * <p>Transform: replace every {@code 0} with {@code -1}. Then "equal
     * zeros and ones" ⟺ "subarray sums to 0". The prefix-sum trick asks for
     * the <em>first</em> index at which each prefix sum appears — the
     * difference is the length of the balanced subarray between them.</p>
     *
     * <p>Time: O(n) — Space: O(n)</p>
     */
    public static int findMaxLength(int[] nums) {
        Map<Integer, Integer> firstIndex = new HashMap<>();
        firstIndex.put(0, -1);                       // empty prefix at index -1
        int runningSum = 0;
        int best = 0;

        for (int i = 0; i < nums.length; i++) {
            runningSum += (nums[i] == 0) ? -1 : 1;
            Integer prev = firstIndex.get(runningSum);
            if (prev != null) {
                best = Math.max(best, i - prev);
            } else {
                firstIndex.put(runningSum, i);
            }
        }
        return best;
    }

    // =====================================================================
    //  7. Subarray Sums Divisible by K
    // =====================================================================

    /**
     * <b>Subarray Sums Divisible by K</b> — number of contiguous subarrays
     * whose sum is a multiple of {@code k}.
     *
     * <p>Fact: {@code (prefix[j] - prefix[i]) % k == 0} ⟺
     * {@code prefix[i] % k == prefix[j] % k}. So count matching remainders.</p>
     *
     * <p><b>Negative-remainder trap:</b> in Java, {@code -7 % 5 == -2}, not
     * {@code 3}. Normalise with {@code ((sum % k) + k) % k}.</p>
     *
     * <p>Time: O(n) — Space: O(k)</p>
     */
    public static int subarraysDivByK(int[] nums, int k) {
        Map<Integer, Integer> remainderCount = new HashMap<>();
        remainderCount.put(0, 1);                    // empty prefix — remainder 0
        int runningSum = 0;
        int count = 0;

        for (int n : nums) {
            runningSum += n;
            int rem = ((runningSum % k) + k) % k;    // handle negatives
            count += remainderCount.getOrDefault(rem, 0);
            remainderCount.merge(rem, 1, Integer::sum);
        }
        return count;
    }

    // =====================================================================
    //  8. XOR Queries of a Subarray
    // =====================================================================

    /**
     * Builds the prefix-XOR array. XOR is self-inverse, so any range
     * {@code [l, r]} xor is {@code prefix[r+1] ^ prefix[l]} — same shape as
     * sum but with {@code ^} instead of {@code -}.
     *
     * <p>Time: O(n) — Space: O(n)</p>
     */
    public static int[] buildPrefixXor(int[] arr) {
        int[] prefix = new int[arr.length + 1];
        for (int i = 0; i < arr.length; i++) {
            prefix[i + 1] = prefix[i] ^ arr[i];
        }
        return prefix;
    }

    /**
     * XOR of {@code arr[left..right]} inclusive.
     *
     * <p>Time: O(1) — Space: O(1)</p>
     */
    public static int rangeXor(int[] prefixXor, int left, int right) {
        return prefixXor[right + 1] ^ prefixXor[left];
    }

    /**
     * <b>XOR Queries of a Subarray</b> — applies {@link #rangeXor(int[], int, int)}
     * to every query {@code [l, r]}.
     *
     * <p>Time: O(n + q) — Space: O(n + q)</p>
     */
    public static int[] xorQueries(int[] arr, int[][] queries) {
        int[] prefix = buildPrefixXor(arr);
        int[] result = new int[queries.length];
        for (int i = 0; i < queries.length; i++) {
            result[i] = rangeXor(prefix, queries[i][0], queries[i][1]);
        }
        return result;
    }
}