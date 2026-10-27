package com.vbforge.visual;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Visualizes Spiral Matrix traversal (LC 54).
 *
 * Uses the "grid" renderer. Each frame shows one cell as the current position
 * (yellow + arrow), all previously-visited cells in green, and the rest white.
 */
public class MatrixTraversalTrace {

    private static final int[][] MATRIX = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
    };

    public static void trace(Consumer<Frame> emit) {
        int m = MATRIX.length, n = MATRIX[0].length;
        Set<String> visited = new HashSet<>();

        int top = 0, bottom = m - 1, left = 0, right = n - 1;

        while (top <= bottom && left <= right) {
            for (int j = left; j <= right; j++)  visit(visited, top, j, "right", emit);
            top++;

            for (int i = top; i <= bottom; i++)  visit(visited, i, right, "down", emit);
            right--;

            if (top <= bottom) {
                for (int j = right; j >= left; j--)  visit(visited, bottom, j, "left", emit);
                bottom--;
            }

            if (left <= right) {
                for (int i = bottom; i >= top; i--)  visit(visited, i, left, "up", emit);
                left++;
            }
        }

        Map<String, String> colors = new HashMap<>();
        for (String k : visited) colors.put(k, "#c8e6c9");
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("grid",        MATRIX);
        d.put("cellColors",  colors);
        emit.accept(Frame.of("grid", d, "Complete — 9 cells in spiral order"));
    }

    private static void visit(Set<String> visited, int r, int c,
                              String arrow, Consumer<Frame> emit) {
        String key = r + "," + c;
        Map<String, String> colors = new HashMap<>();
        for (String v : visited) colors.put(v, "#c8e6c9");
        colors.put(key, "#ffe082");

        Map<String, Object> d = new LinkedHashMap<>();
        d.put("grid",       MATRIX);
        d.put("cellColors", colors);
        d.put("current",    new int[]{r, c});
        d.put("arrow",      arrow);
        emit.accept(Frame.of("grid", d,
                String.format("cell (%d,%d) = %d  %s", r, c, MATRIX[r][c], glyph(arrow))));

        visited.add(key);
    }

    private static String glyph(String d) {
        return switch (d) {
            case "right" -> "→";
            case "down"  -> "↓";
            case "left"  -> "←";
            case "up"    -> "↑";
            default      -> "";
        };
    }
}