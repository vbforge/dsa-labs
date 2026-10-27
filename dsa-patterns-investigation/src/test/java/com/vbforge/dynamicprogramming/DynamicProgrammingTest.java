package com.vbforge.dynamicprogramming;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Dynamic Programming Pattern")
class DynamicProgrammingTest {

    // ------------------------------------------------------------------
    // 1. Climbing Stairs
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("climbStairs (LC 70)")
    class ClimbStairsTests {

        @ParameterizedTest(name = "n={0} -> {1}")
        @CsvSource({
                "0,   1",    // empty sequence = 1 way
                "1,   1",
                "2,   2",    // [1,1],[2]
                "3,   3",    // [1,1,1],[1,2],[2,1]
                "4,   5",
                "5,   8",
                "6,   13",
                "10,  89",
                "15,  987"
        })
        void climbStairs(int n, int expected) {
            assertThat(DynamicProgramming.climbStairs(n)).isEqualTo(expected);
        }

        @Test
        void rejectsNegative() {
            assertThatThrownBy(() -> DynamicProgramming.climbStairs(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void matchesMemoizedOracle() {
            for (int n = 0; n <= 30; n++) {
                assertThat(DynamicProgramming.climbStairs(n))
                        .as("n=%d", n)
                        .isEqualTo(DynamicProgramming.climbStairsMemo(n));
            }
        }
    }

    // ------------------------------------------------------------------
    // 2. House Robber
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("rob (LC 198)")
    class RobTests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void rob(int[] nums, int expected) {
            assertThat(DynamicProgramming.rob(nums)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // Trace [1,2,3,1]: dp[0]=1, dp[1]=2, dp[2]=max(2,1+3)=4, dp[3]=max(4,2+1)=4
                    Arguments.of(new int[]{1, 2, 3, 1},       4),
                    // Trace [2,7,9,3,1]: dp=[2,7,11,11,12] → 12
                    Arguments.of(new int[]{2, 7, 9, 3, 1},    12),
                    // Trace [2,1,1,2]: dp=[2,2,3,4] → 4
                    Arguments.of(new int[]{2, 1, 1, 2},       4),
                    Arguments.of(new int[]{},                 0),
                    Arguments.of(new int[]{5},                5),
                    Arguments.of(new int[]{2, 7},             7)
            );
        }

        @Test
        void matchesMemoizedOracle() {
            int[][] cases = {
                    {1, 2, 3, 1}, {2, 7, 9, 3, 1}, {2, 1, 1, 2},
                    {}, {5}, {2, 7}, {10, 1, 1, 10}, {1, 3, 1, 3, 100}
            };
            for (int[] nums : cases) {
                assertThat(DynamicProgramming.rob(nums))
                        .as("nums=%s", Arrays.toString(nums))
                        .isEqualTo(DynamicProgramming.robMemo(nums));
            }
        }
    }

    // ------------------------------------------------------------------
    // 3. Coin Change
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("coinChange (LC 322)")
    class CoinChangeTests {

        @ParameterizedTest(name = "coins={0}, amount={1} -> {2}")
        @MethodSource("cases")
        void coinChange(int[] coins, int amount, int expected) {
            assertThat(DynamicProgramming.coinChange(coins, amount)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // Trace [1,2,5] @ 11: dp[11]=3 (5+5+1)
                    Arguments.of(new int[]{1, 2, 5},      11, 3),
                    // [2] @ 3 → impossible
                    Arguments.of(new int[]{2},            3, -1),
                    // amount 0 always needs 0 coins
                    Arguments.of(new int[]{1},            0, 0),
                    Arguments.of(new int[]{1},            2, 2),
                    // [2,5] @ 11 → 4 (5+2+2+2)
                    Arguments.of(new int[]{2, 5},         11, 4),
                    // order doesn't matter
                    Arguments.of(new int[]{5, 2, 1},      11, 3),
                    // [2] @ 0 → 0
                    Arguments.of(new int[]{2},            0, 0),
                    // large amount, one coin type
                    Arguments.of(new int[]{1},            100, 100)
            );
        }

        @Test
        void rejectsNegativeAmount() {
            assertThatThrownBy(() -> DynamicProgramming.coinChange(new int[]{1}, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void matchesMemoizedOracle() {
            int[][] coinSets = {
                    {1, 2, 5},
                    {2},
                    {1},
                    {2, 5},
                    {5, 2, 1},
                    {3, 7},
                    {1, 5, 10, 25}
            };
            for (int[] coins : coinSets) {
                for (int amount = 0; amount <= 30; amount++) {
                    assertThat(DynamicProgramming.coinChange(coins, amount))
                            .as("coins=%s, amount=%d", Arrays.toString(coins), amount)
                            .isEqualTo(DynamicProgramming.coinChangeMemo(coins, amount));
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // 4. LIS
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("lengthOfLIS (LC 300)")
    class LISTests {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void lengthOfLIS(int[] nums, int expected) {
            assertThat(DynamicProgramming.lengthOfLIS(nums)).isEqualTo(expected);
            assertThat(DynamicProgramming.lengthOfLISOptimal(nums)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // Trace [10,9,2,5,3,7,101,18]: dp = [1,1,1,2,2,3,4,4] → 4
                    Arguments.of(new int[]{10, 9, 2, 5, 3, 7, 101, 18}, 4),
                    Arguments.of(new int[]{0, 1, 0, 3, 2, 3},           4),
                    Arguments.of(new int[]{7, 7, 7, 7, 7},              1),
                    Arguments.of(new int[]{},                           0),
                    Arguments.of(new int[]{1},                          1),
                    Arguments.of(new int[]{1, 2, 3, 4, 5},              5),
                    Arguments.of(new int[]{5, 4, 3, 2, 1},              1)
            );
        }

        /**
         * The three implementations must agree on every input.
         * They use three structurally different algorithms:
         *   - lengthOfLIS: O(n²) DP
         *   - lengthOfLISOptimal: O(n log n) patience sort
         *   - lengthOfLISMemo: top-down memoised DP
         */
        @Test
        void threeImplementationsAgree() {
            int[][] cases = {
                    {10, 9, 2, 5, 3, 7, 101, 18},
                    {0, 1, 0, 3, 2, 3},
                    {7, 7, 7, 7, 7},
                    {},
                    {1},
                    {1, 2, 3, 4, 5},
                    {5, 4, 3, 2, 1},
                    {3, 1, 4, 1, 5, 9, 2, 6},
                    {-1, 0, 1, 2, -2, -1}
            };
            for (int[] nums : cases) {
                int dp    = DynamicProgramming.lengthOfLIS(nums);
                int optim = DynamicProgramming.lengthOfLISOptimal(nums);
                int memo  = DynamicProgramming.lengthOfLISMemo(nums);
                assertThat(dp).as("dp for %s", Arrays.toString(nums)).isEqualTo(memo);
                assertThat(optim).as("optim for %s", Arrays.toString(nums)).isEqualTo(memo);
            }
        }
    }

    // ------------------------------------------------------------------
    // 5. Word Break
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("wordBreak (LC 139)")
    class WordBreakTests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void wordBreak(String s, List<String> dict, boolean expected) {
            assertThat(DynamicProgramming.wordBreak(s, dict)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of("leetcode", List.of("leet", "code"), true),
                    Arguments.of("applepenapple", List.of("apple", "pen"), true),
                    Arguments.of("catsandog",
                            List.of("cats", "dog", "sand", "and", "cat"), false),
                    Arguments.of("", List.of(), true),
                    Arguments.of("a", List.of(), false),
                    Arguments.of("a", List.of("a"), true),
                    Arguments.of("ab", List.of("a", "b"), true),
                    Arguments.of("ab", List.of("ab"), true),
                    Arguments.of("ab", List.of("a"), false),
                    Arguments.of("aaaaaaa", List.of("aaaa", "aaa"), true)
            );
        }

        @Test
        void matchesMemoizedOracle() {
            Object[][] cases = {
                    {"leetcode", List.of("leet", "code")},
                    {"applepenapple", List.of("apple", "pen")},
                    {"catsandog", List.of("cats", "dog", "sand", "and", "cat")},
                    {"", List.of()},
                    {"a", List.of()},
                    {"a", List.of("a")},
                    {"ab", List.of("a", "b")},
                    {"ab", List.of("ab")},
                    {"ab", List.of("a")},
                    {"aaaaaaa", List.of("aaaa", "aaa")},
                    {"bb", List.of("a", "b", "bbb", "bbbb")}
            };
            for (Object[] c : cases) {
                String s = (String) c[0];
                @SuppressWarnings("unchecked")
                List<String> dict = (List<String>) c[1];
                assertThat(DynamicProgramming.wordBreak(s, dict))
                        .as("s=%s, dict=%s", s, dict)
                        .isEqualTo(DynamicProgramming.wordBreakMemo(s, dict));
            }
        }
    }

    // ------------------------------------------------------------------
    // 6. Unique Paths
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("uniquePaths (LC 62)")
    class UniquePathsTests {

        @ParameterizedTest(name = "{0}x{1} -> {2}")
        @CsvSource({
                "1, 1, 1",
                "1, 5, 1",
                "5, 1, 1",
                "3, 2, 3",     // DDR, DRD, RDD
                "3, 7, 28",    // LC example
                "7, 3, 28",
                "10, 10, 48620"  // C(18,9)
        })
        void uniquePaths(int m, int n, int expected) {
            assertThat(DynamicProgramming.uniquePaths(m, n)).isEqualTo(expected);
            assertThat(DynamicProgramming.uniquePaths1D(m, n)).isEqualTo(expected);
        }

        @Test
        void rejectsNonPositive() {
            assertThatThrownBy(() -> DynamicProgramming.uniquePaths(0, 1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> DynamicProgramming.uniquePaths(1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void threeImplementationsAgree() {
            for (int m = 1; m <= 10; m++) {
                for (int n = 1; n <= 10; n++) {
                    int a = DynamicProgramming.uniquePaths(m, n);
                    int b = DynamicProgramming.uniquePaths1D(m, n);
                    int c = DynamicProgramming.uniquePathsMemo(m, n);
                    assertThat(a).as("%dx%d 2D vs 1D", m, n).isEqualTo(b);
                    assertThat(b).as("%dx%d 1D vs memo", m, n).isEqualTo(c);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // 7. Edit Distance
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("minDistance (LC 72)")
    class EditDistanceTests {

        @ParameterizedTest(name = "\"{0}\" -> \"{1}\" costs {2}")
        @CsvSource({
                "'',          '',          0",
                "'a',         '',          1",
                "'',          'a',         1",
                "'abc',       'abc',       0",
                "'horse',     'ros',       3",
                "'intention', 'execution', 5",
                "'a',         'b',         1",
                "'ab',        'ba',        2",
                "'abc',       'yabd',      2"
        })
        void minDistance(String w1, String w2, int expected) {
            assertThat(DynamicProgramming.minDistance(w1, w2)).isEqualTo(expected);
        }

        @Test
        void matchesMemoizedOracle() {
            String[][] cases = {
                    {"", ""}, {"a", ""}, {"", "a"}, {"abc", "abc"},
                    {"horse", "ros"}, {"intention", "execution"},
                    {"a", "b"}, {"ab", "ba"}, {"abc", "yabd"},
                    {"kitten", "sitting"}
            };
            for (String[] c : cases) {
                assertThat(DynamicProgramming.minDistance(c[0], c[1]))
                        .as("\"%s\" -> \"%s\"", c[0], c[1])
                        .isEqualTo(DynamicProgramming.minDistanceMemo(c[0], c[1]));
            }
        }

        @Test
        void randomizedOracleCrossCheck() {
            Random rnd = new Random(20_260_102L);
            for (int t = 0; t < 200; t++) {
                String a = randomString(rnd, rnd.nextInt(8));
                String b = randomString(rnd, rnd.nextInt(8));
                assertThat(DynamicProgramming.minDistance(a, b))
                        .as("t=%d, a=\"%s\", b=\"%s\"", t, a, b)
                        .isEqualTo(DynamicProgramming.minDistanceMemo(a, b));
            }
        }

        private static String randomString(Random rnd, int len) {
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++) sb.append((char) ('a' + rnd.nextInt(4)));
            return sb.toString();
        }
    }

    // ------------------------------------------------------------------
    // 8. LCS
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("longestCommonSubsequence (LC 1143)")
    class LCSTests {

        @ParameterizedTest(name = "(\"{0}\", \"{1}\") -> {2}")
        @CsvSource({
                "'',      '',      0",
                "'abc',   'abc',   3",
                "'abcde', 'ace',   3",
                "'abc',   'def',   0",
                "'abc',   '',      0",
                "'abcba', 'abcba', 5",
                "'bsbininm', 'jmjkbkjkv', 1"
        })
        void lcs(String a, String b, int expected) {
            assertThat(DynamicProgramming.longestCommonSubsequence(a, b)).isEqualTo(expected);
        }

        @Test
        void matchesMemoizedOracle() {
            String[][] cases = {
                    {"", ""}, {"abc", "abc"}, {"abcde", "ace"}, {"abc", "def"},
                    {"abc", ""}, {"abcba", "abcba"}, {"bsbininm", "jmjkbkjkv"}
            };
            for (String[] c : cases) {
                assertThat(DynamicProgramming.longestCommonSubsequence(c[0], c[1]))
                        .as("(\"%s\", \"%s\")", c[0], c[1])
                        .isEqualTo(DynamicProgramming.longestCommonSubsequenceMemo(c[0], c[1]));
            }
        }

        @Test
        void randomizedOracleCrossCheck() {
            Random rnd = new Random(20_260_103L);
            for (int t = 0; t < 200; t++) {
                String a = randomString(rnd, rnd.nextInt(10));
                String b = randomString(rnd, rnd.nextInt(10));
                assertThat(DynamicProgramming.longestCommonSubsequence(a, b))
                        .as("t=%d, a=\"%s\", b=\"%s\"", t, a, b)
                        .isEqualTo(DynamicProgramming.longestCommonSubsequenceMemo(a, b));
            }
        }

        private static String randomString(Random rnd, int len) {
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++) sb.append((char) ('a' + rnd.nextInt(3)));
            return sb.toString();
        }
    }

    // ------------------------------------------------------------------
    // 9. 0/1 Knapsack
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("knapsack01")
    class KnapsackTests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void knapsack(int[] weights, int[] values, int capacity, int expected) {
            assertThat(DynamicProgramming.knapsack01(weights, values, capacity))
                    .isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // Trace: items (w,v) = (1,1),(3,4),(4,5),(5,7), cap=7
                    //   1+3+4 = w8 (too heavy)
                    //   3+4 = w7, v9   ✓
                    //   1+5 = w6, v8
                    //   Best = 9
                    Arguments.of(new int[]{1, 3, 4, 5}, new int[]{1, 4, 5, 7}, 7, 9),
                    // (2,3),(3,4),(4,5),(5,6), cap=5
                    //   2+3 = w5, v7  ✓
                    //   5 = w5, v6
                    //   Best = 7
                    Arguments.of(new int[]{2, 3, 4, 5}, new int[]{3, 4, 5, 6}, 5, 7),
                    // no items → 0
                    Arguments.of(new int[]{}, new int[]{}, 10, 0),
                    // too heavy → 0
                    Arguments.of(new int[]{5}, new int[]{10}, 3, 0),
                    // exactly fits → 10
                    Arguments.of(new int[]{5}, new int[]{10}, 5, 10),
                    // classic 3-item example
                    // (1,60),(2,100),(3,120), cap=5 → 3+2=w5, v220
                    Arguments.of(new int[]{1, 2, 3}, new int[]{60, 100, 120}, 5, 220)
            );
        }

        @Test
        void rejectsMismatchedArrays() {
            assertThatThrownBy(() -> DynamicProgramming.knapsack01(
                    new int[]{1, 2}, new int[]{1}, 5))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void matchesMemoizedOracle() {
            Object[][] cases = {
                    {new int[]{1, 3, 4, 5}, new int[]{1, 4, 5, 7}, 7},
                    {new int[]{2, 3, 4, 5}, new int[]{3, 4, 5, 6}, 5},
                    {new int[]{}, new int[]{}, 10},
                    {new int[]{5}, new int[]{10}, 3},
                    {new int[]{5}, new int[]{10}, 5},
                    {new int[]{1, 2, 3}, new int[]{60, 100, 120}, 5},
                    {new int[]{2, 2, 3}, new int[]{3, 4, 5}, 4}
            };
            for (Object[] c : cases) {
                int[] w = (int[]) c[0];
                int[] v = (int[]) c[1];
                int cap = (int) c[2];
                assertThat(DynamicProgramming.knapsack01(w, v, cap))
                        .as("w=%s, v=%s, cap=%d", Arrays.toString(w), Arrays.toString(v), cap)
                        .isEqualTo(DynamicProgramming.knapsack01Memo(w, v, cap));
            }
        }
    }

    // ------------------------------------------------------------------
    // 10. Burst Balloons
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("maxCoins (LC 312)")
    class BurstBalloonsTests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void maxCoins(int[] nums, int expected) {
            assertThat(DynamicProgramming.maxCoins(nums)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // Trace [3,1,5,8]: burst order 1,5,3,8
                    //   burst 1: 3*1*5 = 15
                    //   burst 5: 3*5*8 = 120
                    //   burst 3: 1*3*8 = 24
                    //   burst 8: 1*8*1 = 8
                    //   total = 167
                    Arguments.of(new int[]{3, 1, 5, 8},  167),
                    // Trace [1,5]:
                    //   burst 1 → 5
                    //   burst 5 → 5 (with boundaries 1,1)
                    //   total = 10
                    Arguments.of(new int[]{1, 5},        10),
                    Arguments.of(new int[]{5},           5),
                    Arguments.of(new int[]{1},           1),
                    Arguments.of(new int[]{},            0),
                    // All-equal — every pop costs a*a*a
                    // Trace [2,2]: burst either first (cost 1*2*2 = 4), then the other (cost 1*2*1 = 2).
                    // Total = 6. Both orders symmetric.
                    Arguments.of(new int[]{2, 2},        6)
            );
        }

        @Test
        void matchesMemoizedOracle() {
            int[][] cases = {
                    {3, 1, 5, 8},
                    {1, 5},
                    {5},
                    {1},
                    {},
                    {2, 2},
                    {1, 2, 3, 4},
                    {9, 1, 9},
                    {1, 1, 1, 1}
            };
            for (int[] nums : cases) {
                assertThat(DynamicProgramming.maxCoins(nums))
                        .as("nums=%s", Arrays.toString(nums))
                        .isEqualTo(DynamicProgramming.maxCoinsMemo(nums));
            }
        }
    }
}