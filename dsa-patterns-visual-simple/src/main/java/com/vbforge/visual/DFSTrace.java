package com.vbforge.visual;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Visualizes Number of Islands (LC 200) with DFS (LC 200).
 *
 * Uses the "grid" renderer. Cells from different islands are coloured with a
 * rotating palette so you can visually trace the DFS order.
 */
public class DFSTrace {

    private static final int[][] GRID = {
            {1, 0, 0, 1},
            {1, 1, 0, 1},
            {0, 0, 1, 0}
    };
    private static final String[] PALETTE = {
            "#a5d6a7",   // island 1 — green
            "#90caf9",   // island 2 — blue
            "#ce93d8",   // island 3 — purple
            "#ffab91",   // island 4 — orange
            "#fff59d"    // island 5 — yellow
    };
    private static final int[][] FOUR = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public static void trace(Consumer<Frame> emit) {
        int m = GRID.length, n = GRID[0].length;
        Map<String, Integer> islandOf = new HashMap<>();

        emit.accept(Frame.of("grid", data(islandOf, null),
                "Initial — scanning for the first island"));

        int islandCount = 0;
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                String k = r + "," + c;
                if (GRID[r][c] == 1 && !islandOf.containsKey(k)) {
                    dfs(r, c, islandCount, islandOf, emit);
                    islandCount++;
                }
            }
        }

        emit.accept(Frame.of("grid", data(islandOf, null),
                String.format("Total: %d island%s", islandCount,
                        islandCount == 1 ? "" : "s")));
    }

    private static void dfs(int r, int c, int islandIdx,
                            Map<String, Integer> islandOf, Consumer<Frame> emit) {
        int m = GRID.length, n = GRID[0].length;
        if (r < 0 || r >= m || c < 0 || c >= n) return;
        String k = r + "," + c;
        if (GRID[r][c] != 1 || islandOf.containsKey(k)) return;

        islandOf.put(k, islandIdx);
        emit.accept(Frame.of("grid", data(islandOf, new int[]{r, c}),
                String.format("Island #%d:  visited (%d,%d)", islandIdx + 1, r, c)));

        for (int[] d : FOUR) dfs(r + d[0], c + d[1], islandIdx, islandOf, emit);
    }

    private static Map<String, Object> data(Map<String, Integer> islandOf, int[] current) {
        Map<String, String> colors = new HashMap<>();
        for (int r = 0; r < GRID.length; r++) {
            for (int c = 0; c < GRID[0].length; c++) {
                String k = r + "," + c;
                if (GRID[r][c] == 0)              colors.put(k, "#e0e0e0");
                else if (islandOf.containsKey(k)) colors.put(k, PALETTE[islandOf.get(k) % PALETTE.length]);
                else                              colors.put(k, "#fff");
            }
        }
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("grid",       GRID);
        d.put("cellColors", colors);
        if (current != null) d.put("current", current);
        return d;
    }
}