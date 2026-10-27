package com.vbforge.visual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Visualizes N-Queens (n = 4, first solution) with backtracking.
 * Uses the "path" renderer.
 *
 * <p>Emits a frame for every successful queen placement, every backtrack,
 * and the final solution. Skips the "column conflict" micro-steps — the
 * rejected columns are tinted red on the next emitted frame instead, so
 * the story is told visually rather than by frame count.</p>
 */
public class BacktrackingTrace {

    public static void trace(Consumer<Frame> emit) {
        int n = 4;
        int[] queens = new int[n];
        Arrays.fill(queens, -1);
        boolean[] colUsed      = new boolean[n];
        boolean[] diagUsed     = new boolean[2 * n - 1];
        boolean[] antiDiagUsed = new boolean[2 * n - 1];

        emit.accept(frame(queens, n, -1, -1, -1, List.of(),
                "Starting search — " + n + "-Queens"));

        solve(0, n, queens, colUsed, diagUsed, antiDiagUsed, emit);
    }

    /** Returns true when a solution is found, causing the whole recursion to unwind early. */
    private static boolean solve(int row, int n, int[] queens,
                                 boolean[] colUsed, boolean[] diagUsed, boolean[] antiDiagUsed,
                                 Consumer<Frame> emit) {
        if (row == n) {
            emit.accept(frame(queens, n, -1, -1, -1, List.of(),
                    "SOLUTION FOUND ✓"));
            return true;
        }

        List<Integer> rejected = new ArrayList<>();

        for (int col = 0; col < n; col++) {
            if (colUsed[col] || diagUsed[row - col + n - 1] || antiDiagUsed[row + col]) {
                rejected.add(col);
                continue;
            }

            queens[row] = col;
            colUsed[col] = diagUsed[row - col + n - 1] = antiDiagUsed[row + col] = true;

            emit.accept(frame(queens, n, row, col, row, new ArrayList<>(rejected),
                    "Placed queen at (" + row + ", " + col + ")"));

            if (solve(row + 1, n, queens, colUsed, diagUsed, antiDiagUsed, emit)) {
                return true;
            }

            queens[row] = -1;
            colUsed[col] = diagUsed[row - col + n - 1] = antiDiagUsed[row + col] = false;
            rejected.add(col);

            emit.accept(frame(queens, n, -1, -1, row, new ArrayList<>(rejected),
                    "Backtrack: removed queen from (" + row + ", " + col + ")"));
        }

        return false;
    }

    private static Frame frame(int[] queens, int n,
                               int currentRow, int currentCol,
                               int rejectedRow, List<Integer> rejected,
                               String note) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("n",           n);
        d.put("queens",      queens.clone());               // defensive copy
        d.put("currentRow",  currentRow);
        d.put("currentCol",  currentCol);
        d.put("rejectedRow", rejectedRow);
        d.put("rejected",    new ArrayList<>(rejected));    // defensive copy
        return Frame.of("path", d, note);
    }
}