package com.vbforge.dynamicprogramming;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h2>Dynamic Programming Pattern</h2>
 *
 * <p>All methods here have both a <b>bottom-up iterative</b> implementation
 * (production) and a <b>top-down memoised recursive</b> implementation
 * (oracle). The two follow opposite control flow — one ascends from base
 * cases, the other descends from the answer — so agreement between them is
 * a genuinely strong correctness signal.</p>
 *
 * <p>All methods are pure — inputs are never mutated.</p>
 */
public final class DynamicProgramming {

    private DynamicProgramming() { }

    // =====================================================================
    //  1. Climbing Stairs  (LC 70) — dp[n] = dp[n-1] + dp[n-2]
    // =====================================================================

    /**
     * <b>Climbing Stairs</b> — number of distinct ways to climb {@code n}
     * steps, taking 1 or 2 steps at a time. Fibonacci with offset.
     *
     * <p>Time: O(n) — Space: O(1)</p>
     *
     * @throws IllegalArgumentException if {@code n < 0}
     */
    public static int climbStairs(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative: " + n);
        if (n <= 1) return 1;
        int prev2 = 1, prev1 = 1;                             // dp[0], dp[1]
        for (int i = 2; i <= n; i++) {
            int cur = prev1 + prev2;
            prev2 = prev1;
            prev1 = cur;
        }
        return prev1;
    }

    /** Top-down memoised. Oracle. */
    public static int climbStairsMemo(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative: " + n);
        int[] memo = new int[n + 1];
        Arrays.fill(memo, -1);
        return climbStairsRec(n, memo);
    }

    private static int climbStairsRec(int n, int[] memo) {
        if (n <= 1) return 1;
        if (memo[n] != -1) return memo[n];
        return memo[n] = climbStairsRec(n - 1, memo) + climbStairsRec(n - 2, memo);
    }

    // =====================================================================
    //  2. House Robber  (LC 198) — dp[i] = max(dp[i-1], dp[i-2] + nums[i])
    // =====================================================================

    /**
     * <b>House Robber</b> — max amount robbed without hitting two adjacent
     * houses.
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static int rob(int[] nums) {
        if (nums.length == 0) return 0;
        if (nums.length == 1) return nums[0];
        int prev2 = 0;                                        // dp[i-2]
        int prev1 = 0;                                        // dp[i-1]
        for (int n : nums) {
            int cur = Math.max(prev1, prev2 + n);
            prev2 = prev1;
            prev1 = cur;
        }
        return prev1;
    }

    /** Top-down memoised. Oracle. */
    public static int robMemo(int[] nums) {
        int[] memo = new int[nums.length];
        Arrays.fill(memo, -1);
        return robRec(nums, nums.length - 1, memo);
    }

    private static int robRec(int[] nums, int i, int[] memo) {
        if (i < 0)   return 0;
        if (i == 0)  return nums[0];
        if (memo[i] != -1) return memo[i];
        return memo[i] = Math.max(robRec(nums, i - 1, memo),
                                  robRec(nums, i - 2, memo) + nums[i]);
    }

    // =====================================================================
    //  3. Coin Change  (LC 322) — dp[a] = min coins to make amount a
    // =====================================================================

    /**
     * <b>Coin Change</b> — minimum number of coins to make {@code amount},
     * or {@code -1} if impossible. Coins may be used unlimited times.
     *
     * <p>Time: O(amount · |coins|) — Space: O(amount)</p>
     *
     * @throws IllegalArgumentException if {@code amount < 0}
     */
    public static int coinChange(int[] coins, int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be non-negative");
        if (amount == 0) return 0;
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1);                          // sentinel: "infinity"
        dp[0] = 0;
        for (int a = 1; a <= amount; a++) {
            for (int c : coins) {
                if (c <= a && dp[a - c] + 1 < dp[a]) {
                    dp[a] = dp[a - c] + 1;
                }
            }
        }
        return dp[amount] > amount ? -1 : dp[amount];
    }

    /** Top-down memoised. Oracle. */
    public static int coinChangeMemo(int[] coins, int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be non-negative");
        int[] memo = new int[amount + 1];
        Arrays.fill(memo, -2);                                // -2 = not computed, -1 = impossible
        return coinChangeRec(coins, amount, memo);
    }

    private static int coinChangeRec(int[] coins, int amount, int[] memo) {
        if (amount == 0) return 0;
        if (amount < 0)  return -1;
        if (memo[amount] != -2) return memo[amount];
        int best = Integer.MAX_VALUE;
        for (int c : coins) {
            int sub = coinChangeRec(coins, amount - c, memo);
            if (sub >= 0 && sub + 1 < best) best = sub + 1;
        }
        return memo[amount] = (best == Integer.MAX_VALUE ? -1 : best);
    }

    // =====================================================================
    //  4. Longest Increasing Subsequence  (LC 300)
    // =====================================================================

    /**
     * <b>LIS — O(n²) bottom-up</b>. Simple and easy to reason about.
     *
     * <p>Time: O(n²) — Space: O(n)</p>
     */
    public static int lengthOfLIS(int[] nums) {
        if (nums.length == 0) return 0;
        int[] dp = new int[nums.length];
        Arrays.fill(dp, 1);
        int best = 1;
        for (int i = 1; i < nums.length; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[j] < nums[i] && dp[j] + 1 > dp[i]) {
                    dp[i] = dp[j] + 1;
                }
            }
            best = Math.max(best, dp[i]);
        }
        return best;
    }

    /**
     * <b>LIS — O(n log n)</b> patience-sorting variant. Structurally
     * different from the O(n²) version — good cross-check.
     *
     * <p>{@code tails[k]} = smallest possible tail of an increasing
     * subsequence of length {@code k+1}. Binary-search the first tail
     * {@code ≥ num}, replace it.</p>
     *
     * <p>Time: O(n log n) — Space: O(n)</p>
     */
    public static int lengthOfLISOptimal(int[] nums) {
        if (nums.length == 0) return 0;
        int[] tails = new int[nums.length];
        int len = 0;
        for (int num : nums) {
            int lo = 0, hi = len;
            while (lo < hi) {
                int mid = (lo + hi) >>> 1;
                if (tails[mid] < num) lo = mid + 1;
                else                  hi = mid;
            }
            tails[lo] = num;
            if (lo == len) len++;
        }
        return len;
    }

    /** Top-down memoised O(n²). Oracle. */
    public static int lengthOfLISMemo(int[] nums) {
        if (nums.length == 0) return 0;
        Integer[] memo = new Integer[nums.length];
        int best = 0;
        for (int i = 0; i < nums.length; i++) {
            best = Math.max(best, lisEndingAt(nums, i, memo));
        }
        return best;
    }

    private static int lisEndingAt(int[] nums, int i, Integer[] memo) {
        if (memo[i] != null) return memo[i];
        int best = 1;
        for (int j = 0; j < i; j++) {
            if (nums[j] < nums[i]) {
                best = Math.max(best, 1 + lisEndingAt(nums, j, memo));
            }
        }
        return memo[i] = best;
    }

    // =====================================================================
    //  5. Word Break  (LC 139) — dp[i] = s[0..i) breakable?
    // =====================================================================

    /**
     * <b>Word Break</b> — can {@code s} be segmented into dictionary words?
     *
     * <p>Time: O(n² · L) where {@code L = max word length} — Space: O(n)</p>
     */
    public static boolean wordBreak(String s, List<String> wordDict) {
        int n = s.length();
        boolean[] dp = new boolean[n + 1];
        dp[0] = true;
        for (int i = 1; i <= n; i++) {
            for (String w : wordDict) {
                int start = i - w.length();
                if (start >= 0 && dp[start] && s.startsWith(w, start)) {
                    dp[i] = true;
                    break;
                }
            }
        }
        return dp[n];
    }

    /** Top-down memoised. Oracle. */
    public static boolean wordBreakMemo(String s, List<String> wordDict) {
        Boolean[] memo = new Boolean[s.length() + 1];
        return wordBreakRec(s, wordDict, 0, memo);
    }

    private static boolean wordBreakRec(String s, List<String> dict, int start, Boolean[] memo) {
        if (start == s.length()) return true;
        if (memo[start] != null) return memo[start];
        for (String w : dict) {
            if (s.startsWith(w, start) && wordBreakRec(s, dict, start + w.length(), memo)) {
                return memo[start] = true;
            }
        }
        return memo[start] = false;
    }

    // =====================================================================
    //  6. Unique Paths  (LC 62) — dp[i][j] = dp[i-1][j] + dp[i][j-1]
    // =====================================================================

    /**
     * <b>Unique Paths</b> — 2-D DP, O(m·n) space. Reference implementation.
     *
     * @throws IllegalArgumentException if {@code m < 1 || n < 1}
     */
    public static int uniquePaths(int m, int n) {
        if (m < 1 || n < 1) {
            throw new IllegalArgumentException("m and n must be positive");
        }
        int[][] dp = new int[m][n];
        for (int[] row : dp) row[0] = 1;
        Arrays.fill(dp[0], 1);
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[i][j] = dp[i - 1][j] + dp[i][j - 1];
            }
        }
        return dp[m - 1][n - 1];
    }

    /**
     * <b>Unique Paths — O(min(m,n)) space</b>. Rolling-row optimisation;
     * same recurrence, one array.
     */
    public static int uniquePaths1D(int m, int n) {
        if (m < 1 || n < 1) {
            throw new IllegalArgumentException("m and n must be positive");
        }
        if (m < n) { int tmp = m; m = n; n = tmp; }           // ensure n is the smaller
        int[] dp = new int[n];
        Arrays.fill(dp, 1);                                   // first row: 1 path per cell
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[j] += dp[j - 1];                           // dp[j] = above + left
            }
        }
        return dp[n - 1];
    }

    /** Top-down memoised. Oracle. */
    public static int uniquePathsMemo(int m, int n) {
        if (m < 1 || n < 1) {
            throw new IllegalArgumentException("m and n must be positive");
        }
        int[][] memo = new int[m][n];
        for (int[] row : memo) Arrays.fill(row, -1);
        return uniquePathsRec(m - 1, n - 1, memo);
    }

    private static int uniquePathsRec(int r, int c, int[][] memo) {
        if (r == 0 || c == 0) return 1;
        if (memo[r][c] != -1) return memo[r][c];
        return memo[r][c] = uniquePathsRec(r - 1, c, memo) + uniquePathsRec(r, c - 1, memo);
    }

    // =====================================================================
    //  7. Edit Distance  (LC 72)
    // =====================================================================

    /**
     * <b>Edit Distance</b> — minimum insertions / deletions / substitutions
     * to transform {@code word1} into {@code word2}.
     *
     * <p>Time: O(m·n) — Space: O(m·n)</p>
     */
    public static int minDistance(String word1, String word2) {
        int m = word1.length(), n = word2.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                            dp[i - 1][j - 1],                 // substitute
                            Math.min(dp[i - 1][j],            // delete
                                     dp[i][j - 1]));          // insert
                }
            }
        }
        return dp[m][n];
    }

    /** Top-down memoised. Oracle. */
    public static int minDistanceMemo(String word1, String word2) {
        int[][] memo = new int[word1.length() + 1][word2.length() + 1];
        for (int[] row : memo) Arrays.fill(row, -1);
        return editRec(word1, word2, word1.length(), word2.length(), memo);
    }

    private static int editRec(String a, String b, int i, int j, int[][] memo) {
        if (i == 0) return j;
        if (j == 0) return i;
        if (memo[i][j] != -1) return memo[i][j];
        if (a.charAt(i - 1) == b.charAt(j - 1)) {
            return memo[i][j] = editRec(a, b, i - 1, j - 1, memo);
        }
        return memo[i][j] = 1 + Math.min(
                editRec(a, b, i - 1, j - 1, memo),
                Math.min(editRec(a, b, i - 1, j, memo),
                         editRec(a, b, i, j - 1, memo)));
    }

    // =====================================================================
    //  8. Longest Common Subsequence  (LC 1143)
    // =====================================================================

    /**
     * <b>LCS</b> — length of the longest common subsequence.
     *
     * <p>Time: O(m·n) — Space: O(m·n)</p>
     */
    public static int longestCommonSubsequence(String text1, String text2) {
        int m = text1.length(), n = text2.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[m][n];
    }

    /** Top-down memoised. Oracle. */
    public static int longestCommonSubsequenceMemo(String text1, String text2) {
        int[][] memo = new int[text1.length() + 1][text2.length() + 1];
        for (int[] row : memo) Arrays.fill(row, -1);
        return lcsRec(text1, text2, text1.length(), text2.length(), memo);
    }

    private static int lcsRec(String a, String b, int i, int j, int[][] memo) {
        if (i == 0 || j == 0) return 0;
        if (memo[i][j] != -1) return memo[i][j];
        if (a.charAt(i - 1) == b.charAt(j - 1)) {
            return memo[i][j] = lcsRec(a, b, i - 1, j - 1, memo) + 1;
        }
        return memo[i][j] = Math.max(
                lcsRec(a, b, i - 1, j, memo),
                lcsRec(a, b, i, j - 1, memo));
    }

    // =====================================================================
    //  9. 0/1 Knapsack — each item used at most once
    // =====================================================================

    /**
     * <b>0/1 Knapsack</b> — max value fitting within {@code capacity},
     * each item used at most once.
     *
     * <p><b>Critical:</b> the inner loop iterates capacity <b>downward</b>.
     * Iterating upward would allow reuse of the current item — turning this
     * into the *unbounded* knapsack problem, a silent-wrong-answer bug.</p>
     *
     * <p>Time: O(n · capacity) — Space: O(capacity)</p>
     *
     * @throws IllegalArgumentException if arrays differ in length
     */
    public static int knapsack01(int[] weights, int[] values, int capacity) {
        if (weights.length != values.length) {
            throw new IllegalArgumentException("weights/values length mismatch");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be non-negative");
        }
        int[] dp = new int[capacity + 1];
        for (int i = 0; i < weights.length; i++) {
            for (int w = capacity; w >= weights[i]; w--) {    // DOWNWARD
                dp[w] = Math.max(dp[w], dp[w - weights[i]] + values[i]);
            }
        }
        return dp[capacity];
    }

    /** Top-down memoised. Oracle. */
    public static int knapsack01Memo(int[] weights, int[] values, int capacity) {
        if (weights.length != values.length) {
            throw new IllegalArgumentException("weights/values length mismatch");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be non-negative");
        }
        Integer[][] memo = new Integer[weights.length][capacity + 1];
        return knapsackRec(weights, values, 0, capacity, memo);
    }

    private static int knapsackRec(int[] w, int[] v, int i, int cap, Integer[][] memo) {
        if (i == w.length) return 0;
        if (memo[i][cap] != null) return memo[i][cap];
        int skip = knapsackRec(w, v, i + 1, cap, memo);
        int take = (w[i] <= cap)
                ? v[i] + knapsackRec(w, v, i + 1, cap - w[i], memo)
                : 0;
        return memo[i][cap] = Math.max(skip, take);
    }

    // =====================================================================
    //  10. Burst Balloons  (LC 312) — interval DP
    // =====================================================================

    /**
     * <b>Burst Balloons</b> — max coins from bursting balloons. Bursting
     * balloon {@code i} yields {@code left * nums[i] * right}, where
     * {@code left} / {@code right} are the nearest un-burst neighbours
     * (out-of-bounds counts as 1).
     *
     * <p>Interval DP over the padded array {@code [1, nums..., 1]}.
     * {@code dp[i][j]} = max coins from bursting all balloons strictly
     * between {@code i} and {@code j} (exclusive). Recurrence picks the
     * <b>last</b> balloon to burst in the interval — which is the trick
     * that makes the subproblems independent.</p>
     *
     * <p>Time: O(n³) — Space: O(n²)</p>
     */
    public static int maxCoins(int[] nums) {
        int n = nums.length;
        int[] padded = new int[n + 2];
        padded[0] = 1;
        padded[n + 1] = 1;
        System.arraycopy(nums, 0, padded, 1, n);

        int[][] dp = new int[n + 2][n + 2];
        for (int len = 1; len <= n; len++) {
            for (int i = 0; i + len + 1 <= n + 1; i++) {
                int j = i + len + 1;
                for (int k = i + 1; k < j; k++) {
                    dp[i][j] = Math.max(dp[i][j],
                            padded[i] * padded[k] * padded[j]
                                    + dp[i][k]
                                    + dp[k][j]);
                }
            }
        }
        return dp[0][n + 1];
    }

    /** Top-down memoised interval DP. Oracle. */
    public static int maxCoinsMemo(int[] nums) {
        int n = nums.length;
        int[] padded = new int[n + 2];
        padded[0] = 1;
        padded[n + 1] = 1;
        System.arraycopy(nums, 0, padded, 1, n);
        int[][] memo = new int[n + 2][n + 2];
        for (int[] row : memo) Arrays.fill(row, -1);
        return burstRec(padded, 0, n + 1, memo);
    }

    private static int burstRec(int[] p, int i, int j, int[][] memo) {
        if (i + 1 >= j) return 0;
        if (memo[i][j] != -1) return memo[i][j];
        int best = 0;
        for (int k = i + 1; k < j; k++) {
            int coins = p[i] * p[k] * p[j]
                    + burstRec(p, i, k, memo)
                    + burstRec(p, k, j, memo);
            best = Math.max(best, coins);
        }
        return memo[i][j] = best;
    }
}