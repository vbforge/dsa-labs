package com.vbforge.backtracking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Backtracking Pattern")
class BacktrackingTest {

    // ==================================================================
    //  Comparison helper — outer-collection order irrelevant; inner order
    //  preserved (it carries meaning: permutations need distinct order,
    //  subsets/combinations must be produced in canonical ascending order).
    // ==================================================================
    private static List<String> canon(List<? extends List<?>> lists) {
        return lists.stream().map(Object::toString).sorted().toList();
    }

    // ==================================================================
    //  Independent oracles. Deliberately built with different structure
    //  from the DFS implementation, so agreement is meaningful.
    // ==================================================================

    private static List<List<Integer>> subsetsOracle(int[] nums) {
        int n = nums.length;
        List<List<Integer>> out = new ArrayList<>();
        for (int mask = 0; mask < (1 << n); mask++) {
            List<Integer> sub = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) sub.add(nums[i]);
            }
            out.add(sub);
        }
        return out;
    }

    private static List<List<Integer>> subsetsWithDupOracle(int[] nums) {
        int n = nums.length;
        Set<String> seen = new HashSet<>();
        List<List<Integer>> out = new ArrayList<>();
        for (int mask = 0; mask < (1 << n); mask++) {
            List<Integer> sub = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) sub.add(nums[i]);
            }
            Collections.sort(sub);
            if (seen.add(sub.toString())) out.add(sub);
        }
        return out;
    }

    /** Independent permutation generator via next_permutation loop. */
    private static List<List<Integer>> permuteOracle(int[] nums) {
        int[] a = nums.clone();
        Arrays.sort(a);
        List<List<Integer>> out = new ArrayList<>();
        do {
            List<Integer> perm = new ArrayList<>();
            for (int v : a) perm.add(v);
            out.add(perm);
            a = nextPermutation(a);
        } while (a != null);
        return out;
    }

    private static int[] nextPermutation(int[] a) {
        int i = a.length - 2;
        while (i >= 0 && a[i] >= a[i + 1]) i--;
        if (i < 0) return null;
        int j = a.length - 1;
        while (a[j] <= a[i]) j--;
        int t = a[i]; a[i] = a[j]; a[j] = t;
        int lo = i + 1, hi = a.length - 1;
        while (lo < hi) { t = a[lo]; a[lo++] = a[hi]; a[hi--] = t; }
        return a;
    }

    /**
     * Combination-Sum oracle — iterates counts per index (0..⌊remaining/x⌋)
     * rather than a start-index that allows reuse. Structurally different.
     */
    private static List<List<Integer>> combinationSumOracle(int[] candidates, int target) {
        List<List<Integer>> out = new ArrayList<>();
        combSumHelper(candidates, 0, target, new ArrayList<>(), out);
        return out;
    }

    private static void combSumHelper(int[] nums, int idx, int remaining,
                                      List<Integer> path, List<List<Integer>> out) {
        if (remaining == 0) { out.add(new ArrayList<>(path)); return; }
        if (idx == nums.length) return;
        int maxCount = remaining / nums[idx];
        for (int count = 0; count <= maxCount; count++) {
            for (int i = 0; i < count; i++) path.add(nums[idx]);
            combSumHelper(nums, idx + 1, remaining - count * nums[idx], path, out);
            for (int i = 0; i < count; i++) path.remove(path.size() - 1);
        }
    }

    /** Combination-Sum-II oracle — bit-mask subset enumeration + Set dedupe. */
    private static List<List<Integer>> combinationSum2Oracle(int[] candidates, int target) {
        int n = candidates.length;
        Set<String> seen = new HashSet<>();
        List<List<Integer>> out = new ArrayList<>();
        for (int mask = 0; mask < (1 << n); mask++) {
            int sum = 0;
            List<Integer> sub = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) { sum += candidates[i]; sub.add(candidates[i]); }
            }
            if (sum != target) continue;
            Collections.sort(sub);
            if (seen.add(sub.toString())) out.add(sub);
        }
        return out;
    }

    /** Generate-Parentheses oracle — filter all 2^(2n) strings. */
    private static List<String> generateParenthesisOracle(int n) {
        List<String> out = new ArrayList<>();
        int total = 1 << (2 * n);
        for (int mask = 0; mask < total; mask++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 2 * n; i++) {
                sb.append((mask & (1 << i)) != 0 ? '(' : ')');
            }
            String s = sb.toString();
            if (isValidParens(s)) out.add(s);
        }
        return out;
    }

    private static boolean isValidParens(String s) {
        int depth = 0;
        for (char c : s.toCharArray()) {
            if (c == '(') depth++;
            else if (--depth < 0) return false;
        }
        return depth == 0;
    }

    // ------------------------------------------------------------------
    // 1. Subsets
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("subsets (LC 78)")
    class SubsetsTests {

        @Test
        void classicThreeElements() {
            // all 2^3 = 8 subsets
            assertThat(canon(Backtracking.subsets(new int[]{1, 2, 3})))
                    .containsExactlyElementsOf(canon(List.of(
                            List.of(), List.of(1), List.of(2), List.of(3),
                            List.of(1, 2), List.of(1, 3), List.of(2, 3),
                            List.of(1, 2, 3))));
        }

        @Test
        void emptyArray() {
            // only the empty subset
            assertThat(Backtracking.subsets(new int[0]))
                    .containsExactly(List.of());
        }

        @Test
        void singleElement() {
            assertThat(canon(Backtracking.subsets(new int[]{5})))
                    .containsExactlyElementsOf(canon(List.of(List.of(), List.of(5))));
        }

        @Test
        void matchesBitMaskOracle() {
            int[][] cases = {
                    {1, 2, 3},
                    {},
                    {5},
                    {1, 2},
                    {0, -1, 2, -3}
            };
            for (int[] nums : cases) {
                assertThat(canon(Backtracking.subsets(nums)))
                        .as("nums=%s", Arrays.toString(nums))
                        .containsExactlyElementsOf(canon(subsetsOracle(nums)));
            }
        }
    }

    // ------------------------------------------------------------------
    // 2. Subsets II (with duplicates)
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("subsetsWithDup (LC 90)")
    class SubsetsWithDupTests {

        @Test
        void withOneDuplicate() {
            // [1,2,2] → 6 unique subsets (2^3=8 minus 2 duplicate [2]s)
            assertThat(canon(Backtracking.subsetsWithDup(new int[]{1, 2, 2})))
                    .containsExactlyElementsOf(canon(List.of(
                            List.of(), List.of(1), List.of(2),
                            List.of(1, 2), List.of(2, 2), List.of(1, 2, 2))));
        }

        @Test
        void allEqual() {
            // [0,0,0] → 4 subsets: [], [0], [0,0], [0,0,0]
            assertThat(canon(Backtracking.subsetsWithDup(new int[]{0, 0, 0})))
                    .containsExactlyElementsOf(canon(List.of(
                            List.of(), List.of(0), List.of(0, 0), List.of(0, 0, 0))));
        }

        @Test
        void doesNotMutateInput() {
            int[] input = {4, 4, 4, 1, 4};
            int[] before = input.clone();
            Backtracking.subsetsWithDup(input);
            assertThat(input).containsExactly(before);
        }

        @Test
        void matchesBitMaskOracle() {
            int[][] cases = {
                    {1, 2, 2},
                    {0},
                    {0, 0, 0},
                    {4, 4, 4, 1, 4},
                    {1, 2, 3}
            };
            for (int[] nums : cases) {
                assertThat(canon(Backtracking.subsetsWithDup(nums)))
                        .as("nums=%s", Arrays.toString(nums))
                        .containsExactlyElementsOf(canon(subsetsWithDupOracle(nums)));
            }
        }
    }

    // ------------------------------------------------------------------
    // 3. Permutations
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("permute (LC 46)")
    class PermuteTests {

        @Test
        void classicThreeElements() {
            // 3! = 6 permutations
            assertThat(canon(Backtracking.permute(new int[]{1, 2, 3})))
                    .containsExactlyElementsOf(canon(List.of(
                            List.of(1, 2, 3), List.of(1, 3, 2),
                            List.of(2, 1, 3), List.of(2, 3, 1),
                            List.of(3, 1, 2), List.of(3, 2, 1))));
        }

        @Test
        void singleElement() {
            assertThat(Backtracking.permute(new int[]{1}))
                    .containsExactly(List.of(1));
        }

        @Test
        void twoElements() {
            assertThat(canon(Backtracking.permute(new int[]{1, 2})))
                    .containsExactlyElementsOf(canon(List.of(
                            List.of(1, 2), List.of(2, 1))));
        }

        @Test
        void doesNotMutateInput() {
            int[] input = {3, 1, 2};
            int[] before = input.clone();
            Backtracking.permute(input);
            assertThat(input).containsExactly(before);
        }

        @Test
        void matchesNextPermutationOracle() {
            int[][] cases = {
                    {1, 2, 3},
                    {1},
                    {1, 2},
                    {3, 1, 2},
                    {5, 4, 6, 7}
            };
            for (int[] nums : cases) {
                assertThat(canon(Backtracking.permute(nums)))
                        .as("nums=%s", Arrays.toString(nums))
                        .containsExactlyElementsOf(canon(permuteOracle(nums)));
            }
        }
    }

    // ------------------------------------------------------------------
    // 4. Permutations II (with duplicates)
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("permuteUnique (LC 47)")
    class PermuteUniqueTests {

        @Test
        void oneDuplicate() {
            // [1,1,2] → 3!/(2!1!) = 3 unique
            assertThat(canon(Backtracking.permuteUnique(new int[]{1, 1, 2})))
                    .containsExactlyElementsOf(canon(List.of(
                            List.of(1, 1, 2), List.of(1, 2, 1), List.of(2, 1, 1))));
        }

        @Test
        void allEqual() {
            assertThat(Backtracking.permuteUnique(new int[]{1, 1, 1}))
                    .containsExactly(List.of(1, 1, 1));
        }

        @Test
        void allDistinctBehavesLikePermute() {
            assertThat(canon(Backtracking.permuteUnique(new int[]{1, 2, 3})))
                    .containsExactlyElementsOf(canon(permuteOracle(new int[]{1, 2, 3})));
        }

        @Test
        void matchesNextPermutationOracle() {
            int[][] cases = {
                    {1, 1, 2},
                    {1, 1, 1},
                    {1, 2, 3},
                    {1, 1, 2, 2},
                    {2, 2, 1, 1}
            };
            for (int[] nums : cases) {
                assertThat(canon(Backtracking.permuteUnique(nums)))
                        .as("nums=%s", Arrays.toString(nums))
                        .containsExactlyElementsOf(canon(permuteOracle(nums)));
            }
        }
    }

    // ------------------------------------------------------------------
    // 5. Combination Sum (with reuse)
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("combinationSum (LC 39)")
    class CombinationSumTests {

        @Test
        void classic() {
            // [2,3,6,7], 7 → [[2,2,3],[7]]
            assertThat(canon(Backtracking.combinationSum(new int[]{2, 3, 6, 7}, 7)))
                    .containsExactlyElementsOf(canon(List.of(
                            List.of(2, 2, 3), List.of(7))));
        }

        @Test
        void threeCandidates() {
            // [2,3,5], 8 → [2,2,2,2], [2,3,3], [3,5]
            assertThat(canon(Backtracking.combinationSum(new int[]{2, 3, 5}, 8)))
                    .containsExactlyElementsOf(canon(List.of(
                            List.of(2, 2, 2, 2), List.of(2, 3, 3), List.of(3, 5))));
        }

        @Test
        void noCombination() {
            assertThat(Backtracking.combinationSum(new int[]{2}, 1)).isEmpty();
        }

        @Test
        void singleCandidate() {
            // [7], 7 → [[7]]
            assertThat(Backtracking.combinationSum(new int[]{7}, 7))
                    .containsExactly(List.of(7));
        }

        @Test
        void doesNotMutateInput() {
            int[] input = {3, 12, 9, 11, 6, 7, 8, 5};
            int[] before = input.clone();
            Backtracking.combinationSum(input, 15);
            assertThat(input).containsExactly(before);
        }

        @Test
        void matchesCountBasedOracle() {
            Object[][] cases = {
                    {new int[]{2, 3, 6, 7}, 7},
                    {new int[]{2, 3, 5}, 8},
                    {new int[]{2}, 1},
                    {new int[]{7}, 7},
                    {new int[]{2, 3}, 6}
            };
            for (Object[] c : cases) {
                int[] nums = (int[]) c[0];
                int target = (int) c[1];
                assertThat(canon(Backtracking.combinationSum(nums, target)))
                        .as("nums=%s, target=%d", Arrays.toString(nums), target)
                        .containsExactlyElementsOf(canon(combinationSumOracle(nums, target)));
            }
        }
    }

    // ------------------------------------------------------------------
    // 6. Combination Sum II (no reuse, with duplicates)
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("combinationSum2 (LC 40)")
    class CombinationSum2Tests {

        @Test
        void classic() {
            // [10,1,2,7,6,1,5], 8 → [1,1,6], [1,2,5], [1,7], [2,6]
            assertThat(canon(Backtracking.combinationSum2(
                    new int[]{10, 1, 2, 7, 6, 1, 5}, 8)))
                    .containsExactlyElementsOf(canon(List.of(
                            List.of(1, 1, 6),
                            List.of(1, 2, 5),
                            List.of(1, 7),
                            List.of(2, 6))));
        }

        @Test
        void secondExample() {
            // [2,5,2,1,2], 5 → [1,2,2], [5]
            assertThat(canon(Backtracking.combinationSum2(
                    new int[]{2, 5, 2, 1, 2}, 5)))
                    .containsExactlyElementsOf(canon(List.of(
                            List.of(1, 2, 2), List.of(5))));
        }

        @Test
        void noCombination() {
            assertThat(Backtracking.combinationSum2(new int[]{1, 2, 3}, 100)).isEmpty();
        }

        @Test
        void matchesBitMaskOracle() {
            Object[][] cases = {
                    {new int[]{10, 1, 2, 7, 6, 1, 5}, 8},
                    {new int[]{2, 5, 2, 1, 2}, 5},
                    {new int[]{1, 2, 3}, 3},
                    {new int[]{1, 1, 1}, 2},
                    {new int[]{3, 1, 3, 5, 1, 1}, 8}
            };
            for (Object[] c : cases) {
                int[] nums = (int[]) c[0];
                int target = (int) c[1];
                assertThat(canon(Backtracking.combinationSum2(nums, target)))
                        .as("nums=%s, target=%d", Arrays.toString(nums), target)
                        .containsExactlyElementsOf(canon(combinationSum2Oracle(nums, target)));
            }
        }
    }

    // ------------------------------------------------------------------
    // 7. Generate Parentheses
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("generateParenthesis (LC 22)")
    class GenerateParenthesisTests {

        @Test
        void nEqualsOne() {
            assertThat(Backtracking.generateParenthesis(1))
                    .containsExactlyInAnyOrder("()");
        }

        @Test
        void nEqualsTwo() {
            assertThat(Backtracking.generateParenthesis(2))
                    .containsExactlyInAnyOrder("(())", "()()");
        }

        @Test
        void nEqualsThree() {
            assertThat(Backtracking.generateParenthesis(3))
                    .containsExactlyInAnyOrder(
                            "((()))", "(()())", "(())()", "()(())", "()()()");
        }

        @Test
        void matchesFilterOracle() {
            for (int n = 1; n <= 5; n++) {
                assertThat(Backtracking.generateParenthesis(n))
                        .as("n=%d", n)
                        .containsExactlyInAnyOrderElementsOf(generateParenthesisOracle(n));
            }
        }
    }

    // ------------------------------------------------------------------
    // 8. N-Queens
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("solveNQueens (LC 51)")
    class NQueensTests {

        @Test
        void nEqualsOne() {
            assertThat(canon(Backtracking.solveNQueens(1)))
                    .containsExactlyElementsOf(canon(List.of(
                            List.of("Q"))));
        }

        @Test
        void nEqualsTwo() {
            // impossible — no solution on a 2x2 board
            assertThat(Backtracking.solveNQueens(2)).isEmpty();
        }

        @Test
        void nEqualsThree() {
            // impossible on 3x3
            assertThat(Backtracking.solveNQueens(3)).isEmpty();
        }

        @Test
        void nEqualsFour() {
            // exactly 2 solutions
            assertThat(canon(Backtracking.solveNQueens(4)))
                    .containsExactlyElementsOf(canon(List.of(
                            List.of(".Q..", "...Q", "Q...", "..Q."),
                            List.of("..Q.", "Q...", "...Q", ".Q.."))));
        }

        @Test
        void solutionCountsMatchKnownValues() {
            // known counts for n = 1..8: 1, 0, 0, 2, 10, 4, 40, 92
            int[] expected = {0, 1, 0, 0, 2, 10, 4, 40, 92};
            for (int n = 1; n <= 8; n++) {
                assertThat(Backtracking.solveNQueens(n))
                        .as("n=%d", n)
                        .hasSize(expected[n]);
            }
        }
    }

    // ------------------------------------------------------------------
    // 9. Word Search
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("exist (LC 79)")
    class WordSearchTests {

        private static final char[][] BOARD = {
                {'A', 'B', 'C', 'E'},
                {'S', 'F', 'C', 'S'},
                {'A', 'D', 'E', 'E'}
        };

        @ParameterizedTest(name = "\"{0}\" -> {1}")
        @MethodSource("cases")
        void exist(String word, boolean expected) {
            // board cloned per case so any accidental mutation can't leak
            char[][] copy = new char[BOARD.length][];
            for (int i = 0; i < BOARD.length; i++) copy[i] = BOARD[i].clone();
            assertThat(Backtracking.exist(copy, word)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // A(0,0)→B(0,1)→C(0,2)→C(1,2)→E(2,2)→D(2,1) ✓
                    Arguments.of("ABCCED", true),
                    // S(1,0)→E(2,0)→E(2,1) ✓
                    Arguments.of("SEE", true),
                    // A→B→C→B requires going back through B — cell reuse forbidden
                    Arguments.of("ABCB", false),
                    // trivial
                    Arguments.of("A", true),
                    // not on board at all
                    Arguments.of("Z", false),
                    // prefix-only — must not match
                    Arguments.of("ABCZ", false)
            );
        }

        @Test
        void doesNotMutateBoard() {
            char[][] input = {
                    {'A', 'B', 'C', 'E'},
                    {'S', 'F', 'C', 'S'},
                    {'A', 'D', 'E', 'E'}
            };
            char[][] before = {
                    {'A', 'B', 'C', 'E'},
                    {'S', 'F', 'C', 'S'},
                    {'A', 'D', 'E', 'E'}
            };
            Backtracking.exist(input, "SEE");
            assertThat(input).isDeepEqualTo(before);
        }

        @Test
        void repeatedCallsWorkAfterFailure() {
            // a failing search must not corrupt the board for the next call
            char[][] board = {
                    {'A', 'B'},
                    {'C', 'D'}
            };
            assertThat(Backtracking.exist(board, "ZZZ")).isFalse();
            assertThat(Backtracking.exist(board, "ABD")).isTrue();
        }

        @Test
        void emptyInputs() {
            assertThat(Backtracking.exist(new char[0][], "A")).isFalse();
            assertThat(Backtracking.exist(new char[][]{{}}, "A")).isFalse();
            assertThat(Backtracking.exist(new char[][]{{'A'}}, "")).isFalse();
        }
    }
}