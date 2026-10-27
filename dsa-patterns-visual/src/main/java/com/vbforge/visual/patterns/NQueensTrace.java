package com.vbforge.visual.patterns;

import com.vbforge.visual.Algorithm;
import com.vbforge.visual.Data;
import com.vbforge.visual.Frame;
import com.vbforge.visual.Meta;
import com.vbforge.visual.Meta.Field;
import com.vbforge.visual.Meta.Preset;
import com.vbforge.visual.Params;
import com.vbforge.visual.Viz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * N-Queens – the canonical backtracking skeleton (choose · explore · un-choose).
 *
 * <p>Finds the first solution. Time O(n!) worst case. Renderer: {@code grid}.
 */
public final class NQueensTrace implements Algorithm {

    private static final List<String> PSEUDO = List.of(
            "place(row):",
            "    if row == n: record solution; return true       // goal",
            "    for col in 0 .. n-1:",
            "        if attacked(row, col): continue               // prune",
            "        board[row] = col                              // choose",
            "        if place(row + 1): return true                // explore",
            "        board[row] = none                             // un-choose (backtrack)",
            "    return false");

    @Override
    public Meta meta() {
        return new Meta("n-queens", "N-Queens", "Backtracking",
                "Place one queen per row; when a row has no safe column, undo the previous queen and try its next column.",
                "O(n!)", "O(n)",
                "Every backtracking problem is one skeleton: goal check → for each candidate: prune invalid, choose, "
                        + "recurse, un-choose. Only three things change per problem: what a candidate is, what the goal is, "
                        + "and what 'valid' means.",
                PSEUDO,
                List.of(Field.integer("n", "Board size n", 6, "4 – 8 (a first solution is searched)", 4, 8, null)),
                List.of(
                        Preset.of("n = 4", "n", 4),
                        Preset.of("n = 6", "n", 6),
                        Preset.of("n = 8 (classic)", "n", 8)));
    }

    @Override
    public void trace(Params p, Consumer<Frame> emit) {
        int n = p.integer("n", 4, 8);
        Run r = new Run(n, emit);
        r.show(null, -1, -1, "Empty " + n + "×" + n + " board – we fill it row by row", 0);
        if (!r.place(0)) throw new IllegalStateException("no solution found for n = " + n);

        List<String> where = new ArrayList<>();
        for (int row = 0; row < n; row++) where.add("(" + row + "," + r.queen[row] + ")");
        char[][] cells = r.cells();
        for (int row = 0; row < n; row++) cells[row][r.queen[row]] = 'm';
        emit.accept(Frame.of("grid", r.payload(cells).with("lists", List.of(Viz.list("queens", where))),
                "SOLUTION: columns " + Arrays.toString(r.queen) + "  after " + r.attempts + " attempts and "
                        + r.backtracks + " backtracks", 1));
    }

    private static final class Run {
        final int n;
        final int[] queen;
        final Consumer<Frame> emit;
        int attempts, backtracks;

        Run(int n, Consumer<Frame> emit) {
            this.n = n;
            this.emit = emit;
            this.queen = new int[n];
            Arrays.fill(queen, -1);
        }

        boolean place(int row) {
            if (row == n) return true;
            for (int col = 0; col < n; col++) {
                attempts++;
                int[] attacker = attackedBy(row, col);
                if (attacker != null) {
                    show(new int[]{row, col}, attacker[0], attacker[1],
                            "(" + row + "," + col + ") is attacked by the queen at (" + attacker[0] + "," + attacker[1]
                                    + ") – " + (attacker[2] == 0 ? "same column" : "same diagonal") + "  →  prune", 3);
                    continue;
                }
                queen[row] = col;
                show(new int[]{row, col}, -1, -1, "safe  →  place a queen at (" + row + "," + col + "), then solve row " + (row + 1), 4);
                if (place(row + 1)) return true;
                queen[row] = -1;
                backtracks++;
                show(new int[]{row, col}, -1, -1,
                        "row " + (row + 1) + " has no safe square  →  backtrack: remove (" + row + "," + col + ") and try the next column", 6);
            }
            return false;
        }

        /** @return {row, col, reasonCode-as-index} of an attacking queen, or null if (row, col) is safe */
        int[] attackedBy(int row, int col) {
            for (int r = 0; r < row; r++) {
                if (queen[r] == col) return new int[]{r, col, 0};
                if (Math.abs(queen[r] - col) == row - r) return new int[]{r, queen[r], 1};
            }
            return null;
        }

        char[][] cells() {
            char[][] c = new char[n][n];
            for (char[] line : c) Arrays.fill(line, '.');
            for (int r = 0; r < n; r++) if (queen[r] >= 0) c[r][queen[r]] = 'q';
            return c;
        }

        Data payload(char[][] cells) {
            String[] rows = new String[n];
            String[][] text = new String[n][n];
            List<String> labels = new ArrayList<>();
            for (int r = 0; r < n; r++) {
                rows[r] = new String(cells[r]);
                labels.add(String.valueOf(r));
                for (int c = 0; c < n; c++) text[r][c] = queen[r] == c ? "Q" : (cells[r][c] == 'x' ? "\u00d7" : "");
            }
            return Data.of().with("rows", n).with("cols", n).with("cells", rows).with("text", text)
                    .with("rowLabels", labels).with("colLabels", labels).with("checker", true)
                    .with("info", Viz.kv("row", currentRow(), "attempts", attempts, "backtracks", backtracks));
        }

        int currentRow() {
            int r = 0;
            while (r < n && queen[r] >= 0) r++;
            return Math.min(r, n - 1);
        }

        /** @param tried the square being examined (nullable); (ar, ac) the attacker to highlight (or -1) */
        void show(int[] tried, int ar, int ac, String note, int line) {
            char[][] c = cells();
            if (tried != null && (ar >= 0 || line == 6)) c[tried[0]][tried[1]] = 'x';   // pruned square / removed queen
            if (ar >= 0) c[ar][ac] = 'd';                                              // the attacking queen
            emit.accept(Frame.of("grid", payload(c), note, line));
        }
    }
}
