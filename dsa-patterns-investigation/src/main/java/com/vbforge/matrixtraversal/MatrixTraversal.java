package com.vbforge.matrixtraversal;

import java.util.ArrayList;
import java.util.List;

/**
 * <h2>Matrix Traversal Pattern</h2>
 *
 * <p>Traversing a 2-D array in a non-row-major order. Almost every method
 * here either (a) needs a <b>wall-shrink</b> skeleton, or (b) exploits an
 * <b>index identity</b> like {@code (i, j) ↔ (j, i)} for transpose.</p>
 *
 * <p>Common pitfalls:</p>
 * <ul>
 *   <li><b>Double-visiting the last row/column</b> in spiral traversal
 *       when the layer is 1 row or 1 column wide — always guard the
 *       {@code left} and {@code up} passes.</li>
 *   <li><b>Transpose is not symmetric</b> — iterate only {@code j > i},
 *       otherwise you swap each cell twice and land back where you started.</li>
 *   <li><b>In-place transpose requires a square matrix.</b> A rectangular
 *       matrix needs an auxiliary allocation.</li>
 * </ul>
 */
public final class MatrixTraversal {

    private MatrixTraversal() { }

    // =====================================================================
    //  1. Spiral order (read)
    // =====================================================================

    /**
     * <b>Spiral Matrix</b> — traverses the matrix clockwise from the
     * top-left corner. Returns a flat list.
     *
     * <p>Time: O(m·n) — Space: O(m·n) output, O(1) working memory</p>
     */
    public static List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> result = new ArrayList<>();
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            return result;
        }

        int top = 0;
        int bottom = matrix.length - 1;
        int left = 0;
        int right = matrix[0].length - 1;

        while (top <= bottom && left <= right) {
            // → right
            for (int j = left; j <= right; j++) result.add(matrix[top][j]);
            top++;
            // ↓ down
            for (int i = top; i <= bottom; i++) result.add(matrix[i][right]);
            right--;
            // ← left (only if there's still a row to walk)
            if (top <= bottom) {
                for (int j = right; j >= left; j--) result.add(matrix[bottom][j]);
                bottom--;
            }
            // ↑ up (only if there's still a column to walk)
            if (left <= right) {
                for (int i = bottom; i >= top; i--) result.add(matrix[i][left]);
                left++;
            }
        }
        return result;
    }

    // =====================================================================
    //  2. Spiral fill (write)
    // =====================================================================

    /**
     * <b>Generate Spiral Matrix II</b> — fills an {@code n × n} matrix with
     * {@code 1..n²} in spiral order.
     *
     * <p>Same wall skeleton as {@link #spiralOrder(int[][])}, but the
     * <em>write index</em> replaces {@code result.add}. The guards on the
     * {@code left} and {@code up} passes are identical.</p>
     *
     * <p>Time: O(n²) — Space: O(n²) output only</p>
     *
     * @throws IllegalArgumentException if {@code n <= 0}
     */
    public static int[][] generateMatrix(int n) {
        if (n <= 0) throw new IllegalArgumentException("n must be positive");

        int[][] matrix = new int[n][n];
        int top = 0, bottom = n - 1, left = 0, right = n - 1;
        int value = 1;

        while (top <= bottom && left <= right) {
            for (int j = left; j <= right; j++) matrix[top][j] = value++;
            top++;
            for (int i = top; i <= bottom; i++) matrix[i][right] = value++;
            right--;
            if (top <= bottom) {
                for (int j = right; j >= left; j--) matrix[bottom][j] = value++;
                bottom--;
            }
            if (left <= right) {
                for (int i = bottom; i >= top; i--) matrix[i][left] = value++;
                left++;
            }
        }
        return matrix;
    }

    // =====================================================================
    //  3. Diagonal zigzag
    // =====================================================================

    /**
     * <b>Diagonal Traverse</b> (LeetCode 498) — walks the matrix in
     * alternating up-right / down-left diagonals.
     *
     * <p>The direction is decided by {@code (r + c) % 2}:</p>
     * <ul>
     *   <li><b>Even</b> → moving <b>up-right</b>. Priority at walls:
     *       right wall → drop down; top wall → step right; else
     *       {@code (r-1, c+1)}.</li>
     *   <li><b>Odd</b> → moving <b>down-left</b>. Priority at walls:
     *       bottom wall → step right; left wall → drop down; else
     *       {@code (r+1, c-1)}.</li>
     * </ul>
     *
     * <p>Time: O(m·n) — Space: O(m·n) output</p>
     */
    public static int[] diagonalTraverse(int[][] matrix) {
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            return new int[0];
        }
        int m = matrix.length, n = matrix[0].length;
        int[] result = new int[m * n];
        int r = 0, c = 0;

        for (int i = 0; i < m * n; i++) {
            result[i] = matrix[r][c];

            if ((r + c) % 2 == 0) {                       // ↗ up-right
                if (c == n - 1)      r++;                 // hit right wall → down
                else if (r == 0)     c++;                 // hit top wall   → right
                else { r--; c++; }
            } else {                                      // ↙ down-left
                if (r == m - 1)      c++;                 // hit bottom wall → right
                else if (c == 0)     r++;                 // hit left wall   → down
                else { r++; c--; }
            }
        }
        return result;
    }

    // =====================================================================
    //  4. Transpose
    // =====================================================================

    /**
     * <b>Transpose in place</b> — works only on square matrices.
     * Swaps {@code a[i][j] ↔ a[j][i]} for {@code j > i} (the upper triangle
     * only; the lower triangle is the mirror and would undo the swap).
     *
     * <p>Time: O(n²) — Space: O(1)</p>
     *
     * @throws IllegalArgumentException if {@code matrix} is not square
     */
    public static void transpose(int[][] matrix) {
        if (matrix == null || matrix.length != matrix[0].length) {
            throw new IllegalArgumentException("transpose() requires a square matrix");
        }
        int n = matrix.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int tmp = matrix[i][j];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = tmp;
            }
        }
    }

    /**
     * <b>Transpose, rectangular version</b> — allocates a new
     * {@code n × m} matrix. Works for any dimensions.
     *
     * <p>Time: O(m·n) — Space: O(m·n)</p>
     */
    public static int[][] transposeCopy(int[][] matrix) {
        int m = matrix.length, n = matrix[0].length;
        int[][] result = new int[n][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    // =====================================================================
    //  5. 90° clockwise rotation
    // =====================================================================

    /**
     * <b>Rotate Image 90° clockwise, in place</b>.
     *
     * <p>Two-step recipe:</p>
     * <ol>
     *   <li><b>Transpose</b> the matrix — {@code a[i][j] ↔ a[j][i]}.</li>
     *   <li><b>Reverse each row</b> — swap columns {@code j} and {@code n-1-j}.</li>
     * </ol>
     * Together these compose to the 90° rotation. Equivalent to the classic
     * four-way swap, but far easier to reason about and debug.</p>
     *
     * <p>Time: O(n²) — Space: O(1)</p>
     *
     * @throws IllegalArgumentException if {@code matrix} is not square
     */
    public static void rotate90(int[][] matrix) {
        transpose(matrix);
        for (int[] row : matrix) {
            int lo = 0, hi = row.length - 1;
            while (lo < hi) {
                int tmp = row[lo];
                row[lo++] = row[hi];
                row[hi--] = tmp;
            }
        }
    }

    // =====================================================================
    //  6. Set Matrix Zeroes — O(1) extra space
    // =====================================================================

    /**
     * <b>Set Matrix Zeroes</b> — if any cell is {@code 0}, its entire row
     * and entire column become {@code 0}.
     *
     * <p>Uses the matrix itself as the marker store:</p>
     * <ul>
     *   <li>{@code matrix[i][0] == 0} means <em>row i must be zeroed</em>.</li>
     *   <li>{@code matrix[0][j] == 0} means <em>column j must be zeroed</em>.</li>
     * </ul>
     *
     * <p>Problem: cell {@code (0, 0)} is overloaded — it can mean either
     * "row 0 zeroed" or "col 0 zeroed" (or both). Resolve by tracking
     * {@code firstRowZero} and {@code firstColZero} <b>flags</b> that are
     * captured before the marker pass, then applied <em>last</em>.</p>
     *
     * <p>Time: O(m·n) — Space: O(1)</p>
     */
    public static void setZeroes(int[][] matrix) {
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) return;
        int m = matrix.length, n = matrix[0].length;

        boolean firstRowZero = false;
        boolean firstColZero = false;
        for (int j = 0; j < n; j++) if (matrix[0][j] == 0) firstRowZero = true;
        for (int i = 0; i < m; i++) if (matrix[i][0] == 0) firstColZero = true;

        // Pass 1 — plant markers in row 0 and column 0
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                if (matrix[i][j] == 0) {
                    matrix[i][0] = 0;
                    matrix[0][j] = 0;
                }
            }
        }

        // Pass 2 — apply markers to the interior
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                if (matrix[i][0] == 0 || matrix[0][j] == 0) {
                    matrix[i][j] = 0;
                }
            }
        }

        // Pass 3 — apply the captured flags to row 0 / column 0
        if (firstRowZero) for (int j = 0; j < n; j++) matrix[0][j] = 0;
        if (firstColZero) for (int i = 0; i < m; i++) matrix[i][0] = 0;
    }
}