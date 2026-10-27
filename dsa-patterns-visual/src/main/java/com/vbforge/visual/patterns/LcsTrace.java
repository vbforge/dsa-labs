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
 * Longest Common Subsequence – 2-D bottom-up dynamic programming.
 *
 * <p>Time O(m·n), space O(m·n). Renderer: {@code grid} (used as a DP table).
 */
public final class LcsTrace implements Algorithm {

    private static final List<String> PSEUDO = List.of(
            "dp[i][0] = dp[0][j] = 0          // empty prefix",
            "for i in 1..m, j in 1..n:",
            "    if a[i-1] == b[j-1]:  dp[i][j] = dp[i-1][j-1] + 1      // match: extend diagonal",
            "    else:                 dp[i][j] = max(dp[i-1][j], dp[i][j-1])",
            "walk back from dp[m][n] to rebuild the sequence");

    @Override
    public Meta meta() {
        return new Meta("lcs", "Longest Common Subsequence", "Dynamic Programming",
                "dp[i][j] = LCS length of the first i letters of A and the first j letters of B. Fill the table, then walk back.",
                "O(m·n)", "O(m·n)",
                "The four DP steps: state (prefix pair), transition (match → diagonal + 1, else best of up/left), "
                        + "base cases (empty prefix = 0) and computation order (row by row so dependencies exist). "
                        + "The table also lets you reconstruct the subsequence by backtracking.",
                PSEUDO,
                List.of(
                        Field.text("a", "String A", "AGGTAB", "up to 12 characters", 12, "ACGT"),
                        Field.text("b", "String B", "GXTXAYB", "up to 12 characters", 12, "ACGT")),
                List.of(
                        Preset.of("Textbook", "a", "AGGTAB", "b", "GXTXAYB"),
                        Preset.of("DNA", "a", "ACCGGTCGAGTG", "b", "GTCGTTCGGAAT"),
                        Preset.of("Identical", "a", "HELLO", "b", "HELLO"),
                        Preset.of("Nothing shared", "a", "ABC", "b", "XYZ")));
    }

    @Override
    public void trace(Params p, Consumer<Frame> emit) {
        String a = p.text("a", 1, 12);
        String b = p.text("b", 1, 12);
        int m = a.length(), n = b.length();

        int[][] dp = new int[m + 1][n + 1];
        char[][] mk = new char[m + 1][n + 1];
        String[][] text = new String[m + 1][n + 1];
        for (int i = 0; i <= m; i++)
            for (int j = 0; j <= n; j++) {
                mk[i][j] = '.';
                text[i][j] = (i == 0 || j == 0) ? "0" : "";
            }

        List<String> rowLabels = new ArrayList<>(), colLabels = new ArrayList<>();
        rowLabels.add("\u2205");
        colLabels.add("\u2205");
        for (char ch : a.toCharArray()) rowLabels.add(Viz.show(ch));
        for (char ch : b.toCharArray()) colLabels.add(Viz.show(ch));

        frame(emit, mk, text, rowLabels, colLabels, null, "Base cases: an empty prefix shares nothing  →  first row and column are 0", 0);

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                char x = a.charAt(i - 1), y = b.charAt(j - 1);
                boolean match = x == y;
                String note;
                if (match) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    mk[i - 1][j - 1] = 'd';
                    note = "'" + Viz.show(x) + "' == '" + Viz.show(y) + "'  →  match!  dp[" + i + "][" + j + "] = diagonal "
                            + dp[i - 1][j - 1] + " + 1 = " + dp[i][j];
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                    mk[i - 1][j] = 'd';
                    mk[i][j - 1] = 'd';
                    note = "'" + Viz.show(x) + "' != '" + Viz.show(y) + "'  →  max(up " + dp[i - 1][j] + ", left "
                            + dp[i][j - 1] + ") = " + dp[i][j];
                }
                text[i][j] = String.valueOf(dp[i][j]);
                mk[i][j] = match ? 'm' : 'c';
                frame(emit, mk, text, rowLabels, colLabels, dp[i][j], note, match ? 2 : 3);

                mk[i][j] = '.';                       // clear this step's highlights
                if (match) mk[i - 1][j - 1] = '.';
                else { mk[i - 1][j] = '.'; mk[i][j - 1] = '.'; }
            }
        }

        // ---- backtrack: rebuild one optimal subsequence from the table
        int i = m, j = n;
        StringBuilder picked = new StringBuilder();      // collected last-to-first
        mk[i][j] = 'p';
        frame(emit, mk, text, rowLabels, colLabels, dp[m][n],
                "Table complete: LCS length = " + dp[m][n] + ".  Walk back from the bottom-right corner", 4, "");
        while (i > 0 && j > 0) {
            String note;
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                picked.append(a.charAt(i - 1));
                mk[i][j] = 'm';
                note = "letters equal  →  take '" + Viz.show(a.charAt(i - 1)) + "' and move diagonally";
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                note = "up (" + dp[i - 1][j] + ") >= left (" + dp[i][j - 1] + ")  →  move up";
                i--;
            } else {
                note = "left (" + dp[i][j - 1] + ") > up (" + dp[i - 1][j] + ")  →  move left";
                j--;
            }
            mk[i][j] = 'p';
            frame(emit, mk, text, rowLabels, colLabels, dp[m][n], note, 4, new StringBuilder(picked).reverse().toString());
        }
        String lcs = new StringBuilder(picked).reverse().toString();
        frame(emit, mk, text, rowLabels, colLabels, dp[m][n],
                lcs.isEmpty() ? "No common subsequence" : "LCS = \"" + lcs + "\"  (length " + lcs.length() + ")", 4, lcs);
    }

    private static void frame(Consumer<Frame> emit, char[][] mk, String[][] text, List<String> rowLabels,
                              List<String> colLabels, Integer value, String note, int line) {
        frame(emit, mk, text, rowLabels, colLabels, value, note, line, null);
    }

    private static void frame(Consumer<Frame> emit, char[][] mk, String[][] text, List<String> rowLabels,
                              List<String> colLabels, Integer value, String note, int line, String lcs) {
        String[] rows = new String[mk.length];
        for (int r = 0; r < mk.length; r++) rows[r] = new String(mk[r]);
        Data d = Data.of()
                .with("rows", mk.length).with("cols", mk[0].length)
                .with("cells", rows)
                .with("text", text)
                .with("rowLabels", rowLabels)
                .with("colLabels", colLabels)
                .with("info", Viz.kv("cell", value));
        if (lcs != null && !lcs.isEmpty()) d.with("lists", List.of(Viz.list("LCS so far", Arrays.asList(lcs.split("")))));
        emit.accept(Frame.of("grid", d, note, line));
    }
}
