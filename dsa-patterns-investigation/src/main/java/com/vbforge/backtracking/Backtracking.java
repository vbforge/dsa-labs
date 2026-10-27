package com.vbforge.backtracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <h2>Backtracking Pattern</h2>
 *
 * <p>All problems share one recursive skeleton: {@code choose → explore →
 * un-choose}. The base case records a solution; the loop enumerates
 * candidates; the prune step (a) rejects illegal candidates, (b) removes
 * duplicates at the same recursion level.</p>
 *
 * <p><b>Every method returns a fresh structure</b> and — where the input is
 * mutable (arrays, boards) — <b>leaves the input unchanged</b>. This is
 * verified by tests.</p>
 */
public final class Backtracking {

    private Backtracking() { }

    // =====================================================================
    //  1. Subsets  (LC 78)  — include / exclude
    // =====================================================================

    /**
     * <b>Subsets</b> — all {@code 2ⁿ} subsets of distinct {@code nums}.
     *
     * <p>Each index is either included or excluded. Prune step is empty
     * (every subset is valid). {@code start} ensures each subset is built
     * in ascending index order — no permutations of the same subset.</p>
     *
     * <p>Time: O(n · 2ⁿ) — Space: O(n) recursion depth</p>
     */
    public static List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        subsetsDfs(0, nums, new ArrayList<>(), result);
        return result;
    }

    private static void subsetsDfs(int start, int[] nums,
                                   List<Integer> path, List<List<Integer>> result) {
        result.add(new ArrayList<>(path));                    // every prefix is a valid subset
        for (int i = start; i < nums.length; i++) {
            path.add(nums[i]);                                // choose
            subsetsDfs(i + 1, nums, path, result);            // explore
            path.remove(path.size() - 1);                     // un-choose
        }
    }

    // =====================================================================
    //  2. Subsets II  (LC 90) — with duplicates
    // =====================================================================

    /**
     * <b>Subsets II</b> — subsets of {@code nums} which may contain
     * duplicates.
     *
     * <p><b>Sort first</b> so equal values are adjacent. Then in the loop,
     * if {@code i > start && nums[i] == nums[i-1]}, skip — it means this
     * branch is a duplicate of one already explored at the same depth.</p>
     *
     * <p>Time: O(n · 2ⁿ) worst case, much less with duplicates —
     * Space: O(n)</p>
     */
    public static List<List<Integer>> subsetsWithDup(int[] nums) {
        int[] sorted = nums.clone();
        Arrays.sort(sorted);
        List<List<Integer>> result = new ArrayList<>();
        subsetsWithDupDfs(0, sorted, new ArrayList<>(), result);
        return result;
    }

    private static void subsetsWithDupDfs(int start, int[] nums,
                                          List<Integer> path, List<List<Integer>> result) {
        result.add(new ArrayList<>(path));
        for (int i = start; i < nums.length; i++) {
            // skip same value at the same level of recursion
            if (i > start && nums[i] == nums[i - 1]) continue;
            path.add(nums[i]);
            subsetsWithDupDfs(i + 1, nums, path, result);
            path.remove(path.size() - 1);
        }
    }

    // =====================================================================
    //  3. Permutations  (LC 46) — used[] flag
    // =====================================================================

    /**
     * <b>Permutations</b> — all {@code n!} orderings of distinct
     * {@code nums}.
     *
     * <p>Unlike subsets (where {@code start} prevents revisiting indices),
     * permutations need a {@code used[]} flag — any unused element can be
     * the next in the sequence. Base case: {@code path.size() == n}.</p>
     *
     * <p>Time: O(n · n!) — Space: O(n)</p>
     */
    public static List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        boolean[] used = new boolean[nums.length];
        permuteDfs(nums, used, new ArrayList<>(), result);
        return result;
    }

    private static void permuteDfs(int[] nums, boolean[] used,
                                   List<Integer> path, List<List<Integer>> result) {
        if (path.size() == nums.length) {
            result.add(new ArrayList<>(path));
            return;
        }
        for (int i = 0; i < nums.length; i++) {
            if (used[i]) continue;
            used[i] = true;
            path.add(nums[i]);
            permuteDfs(nums, used, path, result);
            path.remove(path.size() - 1);
            used[i] = false;
        }
    }

    // =====================================================================
    //  4. Permutations II  (LC 47) — with duplicates
    // =====================================================================

    /**
     * <b>Permutations II</b> — distinct permutations of {@code nums} which
     * may contain duplicates.
     *
     * <p>Sort first, then combine two checks:</p>
     * <ul>
     *   <li>{@code used[i]} — skip already-used.</li>
     *   <li>{@code i > 0 && nums[i] == nums[i-1] && !used[i-1]} —
     *       skip duplicate at the same level. The {@code !used[i-1]} is
     *       crucial: it means the previous equal value has already been
     *       popped, so we're choosing it again at the same level.</li>
     * </ul>
     *
     * <p>Time: O(n · n!) worst — Space: O(n)</p>
     */
    public static List<List<Integer>> permuteUnique(int[] nums) {
        int[] sorted = nums.clone();
        Arrays.sort(sorted);
        List<List<Integer>> result = new ArrayList<>();
        boolean[] used = new boolean[sorted.length];
        permuteUniqueDfs(sorted, used, new ArrayList<>(), result);
        return result;
    }

    private static void permuteUniqueDfs(int[] nums, boolean[] used,
                                         List<Integer> path, List<List<Integer>> result) {
        if (path.size() == nums.length) {
            result.add(new ArrayList<>(path));
            return;
        }
        for (int i = 0; i < nums.length; i++) {
            if (used[i]) continue;
            if (i > 0 && nums[i] == nums[i - 1] && !used[i - 1]) continue;
            used[i] = true;
            path.add(nums[i]);
            permuteUniqueDfs(nums, used, path, result);
            path.remove(path.size() - 1);
            used[i] = false;
        }
    }

    // =====================================================================
    //  5. Combination Sum  (LC 39) — with reuse
    // =====================================================================

    /**
     * <b>Combination Sum</b> — all unique combinations of {@code candidates}
     * (distinct, positive) that sum to {@code target}. Each candidate may be
     * used <b>unlimited</b> times.
     *
     * <p>Prune step: if {@code sum + candidates[i] > target}, break — sorted
     * input guarantees all subsequent candidates are also too big.</p>
     *
     * <p>The recursive call uses {@code i} (not {@code i + 1}) — allowing
     * reuse. To avoid duplicate combinations, {@code start} still increments.</p>
     *
     * <p>Time: exponential in target/min — Space: O(target/min)</p>
     */
    public static List<List<Integer>> combinationSum(int[] candidates, int target) {
        int[] sorted = candidates.clone();
        Arrays.sort(sorted);
        List<List<Integer>> result = new ArrayList<>();
        combinationSumDfs(sorted, 0, target, 0, new ArrayList<>(), result);
        return result;
    }

    private static void combinationSumDfs(int[] nums, int start, int target, int sum,
                                          List<Integer> path, List<List<Integer>> result) {
        if (sum == target) {
            result.add(new ArrayList<>(path));
            return;
        }
        for (int i = start; i < nums.length; i++) {
            if (sum + nums[i] > target) break;              // sorted → all subsequent too big
            path.add(nums[i]);
            combinationSumDfs(nums, i, target, sum + nums[i], path, result);   // i, not i+1 → reuse
            path.remove(path.size() - 1);
        }
    }

    // =====================================================================
    //  6. Combination Sum II  (LC 40) — no reuse, with duplicates
    // =====================================================================

    /**
     * <b>Combination Sum II</b> — same as above but each candidate may be
     * used <b>at most once</b>, and candidates may contain duplicates.
     *
     * <p>Two changes from {@link #combinationSum(int[], int)}:
     * the recursive call uses {@code i + 1}, and we skip duplicates at
     * the same level via {@code i > start && nums[i] == nums[i-1]}.</p>
     *
     * <p>Time: exponential — Space: O(n)</p>
     */
    public static List<List<Integer>> combinationSum2(int[] candidates, int target) {
        int[] sorted = candidates.clone();
        Arrays.sort(sorted);
        List<List<Integer>> result = new ArrayList<>();
        combinationSum2Dfs(sorted, 0, target, 0, new ArrayList<>(), result);
        return result;
    }

    private static void combinationSum2Dfs(int[] nums, int start, int target, int sum,
                                           List<Integer> path, List<List<Integer>> result) {
        if (sum == target) {
            result.add(new ArrayList<>(path));
            return;
        }
        for (int i = start; i < nums.length; i++) {
            if (sum + nums[i] > target) break;
            if (i > start && nums[i] == nums[i - 1]) continue;   // skip same-level duplicates
            path.add(nums[i]);
            combinationSum2Dfs(nums, i + 1, target, sum + nums[i], path, result);
            path.remove(path.size() - 1);
        }
    }

    // =====================================================================
    //  7. Generate Parentheses  (LC 22)
    // =====================================================================

    /**
     * <b>Generate Parentheses</b> — all well-formed parentheses strings of
     * {@code n} pairs.
     *
     * <p>Three prune rules:</p>
     * <ol>
     *   <li>Open parens used {@code < n}.</li>
     *   <li>Close parens used {@code < } open parens used — never close
     *       before opening.</li>
     *   <li>Base case: {@code 2n} chars produced.</li>
     * </ol>
     *
     * <p>Time: O(4ⁿ / √n) — the n-th Catalan number — Space: O(n)</p>
     */
    public static List<String> generateParenthesis(int n) {
        List<String> result = new ArrayList<>();
        genParensDfs(n, 0, 0, new StringBuilder(), result);
        return result;
    }

    private static void genParensDfs(int n, int open, int close,
                                     StringBuilder path, List<String> result) {
        if (path.length() == 2 * n) {
            result.add(path.toString());
            return;
        }
        if (open < n) {
            path.append('(');
            genParensDfs(n, open + 1, close, path, result);
            path.deleteCharAt(path.length() - 1);
        }
        if (close < open) {
            path.append(')');
            genParensDfs(n, open, close + 1, path, result);
            path.deleteCharAt(path.length() - 1);
        }
    }

    // =====================================================================
    //  8. N-Queens  (LC 51)
    // =====================================================================

    /**
     * <b>N-Queens</b> — all ways to place {@code n} non-attacking queens on
     * an {@code n × n} board.
     *
     * <p>Place queens row by row ({@code r} = current row). Three O(1)-check
     * "attack sets":</p>
     * <ul>
     *   <li>{@code colUsed[c]} — column c occupied.</li>
     *   <li>{@code diagUsed[r - c + n - 1]} — main diagonal
     *       ({@code r - c} constant, offset by {@code n-1} to stay ≥ 0).</li>
     *   <li>{@code antiDiagUsed[r + c]} — anti-diagonal
     *       ({@code r + c} constant, ranges 0..2n-2).</li>
     * </ul>
     * Using three boolean arrays instead of scanning the board makes the
     * prune O(1) — the whole algorithm runs in O(n!) time.</p>
     */
    public static List<List<String>> solveNQueens(int n) {
        List<List<String>> result = new ArrayList<>();
        int[] queens = new int[n];                              // queens[r] = column of queen in row r
        boolean[] colUsed      = new boolean[n];
        boolean[] diagUsed     = new boolean[2 * n - 1];
        boolean[] antiDiagUsed = new boolean[2 * n - 1];
        solveNQueensDfs(0, n, queens, colUsed, diagUsed, antiDiagUsed, result);
        return result;
    }

    private static void solveNQueensDfs(int r, int n, int[] queens,
                                        boolean[] colUsed, boolean[] diagUsed,
                                        boolean[] antiDiagUsed,
                                        List<List<String>> result) {
        if (r == n) {
            List<String> board = new ArrayList<>();
            for (int row = 0; row < n; row++) {
                char[] line = new char[n];
                Arrays.fill(line, '.');
                line[queens[row]] = 'Q';
                board.add(new String(line));
            }
            result.add(board);
            return;
        }
        for (int c = 0; c < n; c++) {
            int di = r - c + n - 1;
            int ai = r + c;
            if (colUsed[c] || diagUsed[di] || antiDiagUsed[ai]) continue;
            queens[r] = c;
            colUsed[c] = diagUsed[di] = antiDiagUsed[ai] = true;
            solveNQueensDfs(r + 1, n, queens, colUsed, diagUsed, antiDiagUsed, result);
            colUsed[c] = diagUsed[di] = antiDiagUsed[ai] = false;
        }
    }

    // =====================================================================
    //  9. Word Search  (LC 79)
    // =====================================================================

    /**
     * <b>Word Search</b> — {@code true} iff {@code word} can be built from
     * adjacent (4-directional) cells of {@code board}, each used at most once.
     *
     * <p>Backtracking on the grid: mark the current cell, try all four
     * neighbours, un-mark on return. The board is mutated during the call
     * but <b>restored before return</b>.</p>
     *
     * <p>Time: O(m · n · 4ᴸ) where {@code L = |word|} — Space: O(L)</p>
     */
    public static boolean exist(char[][] board, String word) {
        if (board == null || board.length == 0 || board[0].length == 0 || word.isEmpty()) {
            return false;
        }
        int m = board.length, n = board[0].length;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (wordSearchDfs(board, word, 0, i, j, m, n)) return true;
            }
        }
        return false;
    }

    private static boolean wordSearchDfs(char[][] board, String word, int idx,
                                         int r, int c, int m, int n) {
        if (idx == word.length()) return true;
        if (r < 0 || r >= m || c < 0 || c >= n) return false;
        if (board[r][c] != word.charAt(idx)) return false;
        char saved = board[r][c];
        board[r][c] = '#';                                     // in-place visited marker
        boolean found =
                wordSearchDfs(board, word, idx + 1, r + 1, c, m, n) ||
                wordSearchDfs(board, word, idx + 1, r - 1, c, m, n) ||
                wordSearchDfs(board, word, idx + 1, r, c + 1, m, n) ||
                wordSearchDfs(board, word, idx + 1, r, c - 1, m, n);
        board[r][c] = saved;                                   // un-choose
        return found;
    }
}