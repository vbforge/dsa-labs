package com.vbforge.visual;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Visualizes Rotting Oranges (LC 994) with multi-source BFS.
 *
 * Uses the "grid" renderer. Each frame shows one "minute" of propagation:
 * just-rotted cells are yellow, previously-rotted cells are brown, still-fresh
 * cells are orange, and empty cells are grey.
 */
public class BFSTrace {

    private static final int[][] GRID = {
            {2, 1, 1},
            {1, 1, 0},
            {0, 1, 1}
    };
    private static final int[][] FOUR = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public static void trace(Consumer<Frame> emit) {
        int m = GRID.length, n = GRID[0].length;

        Set<String> fresh  = new HashSet<>();
        Set<String> rotten = new HashSet<>();
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                String k = r + "," + c;
                if      (GRID[r][c] == 1) fresh.add(k);
                else if (GRID[r][c] == 2) rotten.add(k);
            }
        }

        emit.accept(Frame.of("grid",
                data(rotten, fresh, null),
                String.format("t = 0:  %d fresh, %d rotten  (multi-source BFS)",
                        fresh.size(), rotten.size())));

        int minutes = 0;
        while (!fresh.isEmpty()) {
            Set<String> toRot = new HashSet<>();
            for (String rk : rotten) {
                String[] parts = rk.split(",");
                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                for (int[] d : FOUR) {
                    String nk = (r + d[0]) + "," + (c + d[1]);
                    if (fresh.contains(nk)) toRot.add(nk);
                }
            }
            if (toRot.isEmpty()) break;

            minutes++;
            fresh.removeAll(toRot);
            rotten.addAll(toRot);

            emit.accept(Frame.of("grid",
                    data(rotten, fresh, toRot),
                    String.format("t = %d:  %d fresh, %d rotten", minutes,
                            fresh.size(), rotten.size())));
        }

        int totalCells = m * n;
        int emptyCount = 0;
        for (int[] row : GRID) for (int v : row) if (v == 0) emptyCount++;
        int answerable = totalCells - emptyCount;

        Map<String, String> finalColors = new HashMap<>();
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                String k = r + "," + c;
                if (GRID[r][c] == 0) finalColors.put(k, "#e0e0e0");
                else                 finalColors.put(k, "#c8e6c9");
            }
        }
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("grid",       GRID);
        d.put("cellColors", finalColors);
        emit.accept(Frame.of("grid", d,
                String.format("All %d cells rotted in %d minute%s",
                        answerable, minutes, minutes == 1 ? "" : "s")));
    }

    private static Map<String, Object> data(Set<String> rotten, Set<String> fresh,
                                            Set<String> justRotted) {
        Map<String, String> colors = new HashMap<>();
        for (int r = 0; r < GRID.length; r++) {
            for (int c = 0; c < GRID[0].length; c++) {
                String k = r + "," + c;
                if (GRID[r][c] == 0) {
                    colors.put(k, "#e0e0e0");
                } else if (justRotted != null && justRotted.contains(k)) {
                    colors.put(k, "#ffeb3b");
                } else if (rotten.contains(k)) {
                    colors.put(k, "#a1887f");
                } else {
                    colors.put(k, "#ffcc80");
                }
            }
        }
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("grid",       GRID);
        d.put("cellColors", colors);
        return d;
    }
}