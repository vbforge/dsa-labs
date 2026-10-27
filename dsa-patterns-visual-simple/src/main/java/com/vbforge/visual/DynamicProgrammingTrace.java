package com.vbforge.visual;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DynamicProgrammingTrace {

    public static void trace(Consumer<Frame> emit) {
        String word1 = "horse";
        String word2 = "ros";
        int m = word1.length();
        int n = word2.length();

        int[][] dp = new int[m + 1][n + 1];
        for (int[] row : dp) java.util.Arrays.fill(row, -1);

        // Base cases
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        emit.accept(frame(dp, -1, -1, "Initialized base cases (empty string to word1/word2)"));

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int diag = dp[i - 1][j - 1];
                int up   = dp[i - 1][j];
                int left = dp[i][j - 1];

                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = diag;
                    emit.accept(frame(dp, i, j, String.format(
                            "Match '%c': dp[%d][%d] = diag = %d",
                            word1.charAt(i - 1), i, j, dp[i][j])));
                } else {
                    dp[i][j] = 1 + Math.min(diag, Math.min(up, left));
                    emit.accept(frame(dp, i, j, String.format(
                            "Mismatch: dp[%d][%d] = 1 + min(diag:%d, up:%d, left:%d) = %d",
                            i, j, diag, up, left, dp[i][j])));
                }
            }
        }

        emit.accept(frame(dp, -1, -1,
                "Answer: Edit distance is " + dp[m][n]));
    }

    private static Frame frame(int[][] dp, int curR, int curC, String note) {
        Map<String, Object> d = new LinkedHashMap<>();
        
        // Build string representation for grid (blank for -1)
        String[][] displayGrid = new String[dp.length][dp[0].length];
        Map<String, String> colors = new HashMap<>();
        
        for (int r = 0; r < dp.length; r++) {
            for (int c = 0; c < dp[0].length; c++) {
                displayGrid[r][c] = dp[r][c] == -1 ? "" : String.valueOf(dp[r][c]);
                if (dp[r][c] == -1) colors.put(r + "," + c, "#fafafa"); // uncomputed
                else                colors.put(r + "," + c, "#ffffff");
            }
        }

        // Highlight dependencies
        if (curR > 0 && curC > 0) {
            colors.put((curR - 1) + "," + (curC - 1), "#e3f2fd"); // diag (light blue)
            colors.put((curR - 1) + "," + curC,       "#e3f2fd"); // up
            colors.put(curR + "," + (curC - 1),       "#e3f2fd"); // left
        }
        
        // Highlight current cell
        if (curR >= 0 && curC >= 0) {
            colors.put(curR + "," + curC, "#ffe082"); // current (yellow)
        }

        d.put("grid", displayGrid);
        d.put("cellColors", colors);
        if (curR >= 0 && curC >= 0) {
            d.put("current", new int[]{curR, curC});
        }
        
        return Frame.of("grid", d, note);
    }
}