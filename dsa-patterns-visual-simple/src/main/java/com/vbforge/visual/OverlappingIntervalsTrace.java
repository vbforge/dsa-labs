package com.vbforge.visual;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Visualizes Merge Intervals (LC 56).
 * Uses the "intervals" renderer.
 */
public class OverlappingIntervalsTrace {

    public static void trace(Consumer<Frame> emit) {
        int[][] input = {{1, 3}, {2, 6}, {8, 10}, {15, 18}};
        int n = input.length;

        int maxEnd = 0;
        for (int[] iv : input) maxEnd = Math.max(maxEnd, iv[1]);

        String[] statuses = new String[n];
        for (int i = 0; i < n; i++) statuses[i] = "pending";
        statuses[0] = "current";

        List<int[]> merged = new ArrayList<>();
        int[] cur = input[0].clone();

        emit.accept(frame(input, statuses, merged, cur, maxEnd,
                "Sorted by start. cur = [" + cur[0] + "," + cur[1] + "]"));

        for (int i = 1; i < n; i++) {
            int[] next = input[i];
            statuses[i] = "next";

            if (next[0] <= cur[1]) {
                cur[1] = Math.max(cur[1], next[1]);
                statuses[i] = "merged";
                emit.accept(frame(input, statuses, merged, cur, maxEnd,
                        String.format("next=[%d,%d] overlaps cur (start %d ≤ end %d) → merge, cur=[%d,%d]",
                                next[0], next[1], next[0], cur[1], cur[0], cur[1])));
            } else {
                int[] oldCur = cur;
                merged.add(cur.clone());
                cur = next.clone();
                for (int j = 0; j < i; j++) {
                    if (!statuses[j].equals("merged")) statuses[j] = "merged";
                }
                statuses[i] = "current";
                emit.accept(frame(input, statuses, merged, cur, maxEnd,
                        String.format("next=[%d,%d] does NOT overlap → finalize [%d,%d], cur=[%d,%d]",
                                next[0], next[1], oldCur[0], oldCur[1], cur[0], cur[1])));
            }
        }

        merged.add(cur.clone());
        for (int j = 0; j < n; j++) statuses[j] = "merged";
        emit.accept(frame(input, statuses, merged, null, maxEnd,
                "Done. Result: " + fmt(merged)));
    }

    private static Frame frame(int[][] input, String[] statuses, List<int[]> merged,
                               int[] cur, int maxCoord, String note) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("input",    input);
        d.put("statuses", statuses);
        d.put("merged",   merged.stream().map(int[]::clone).toArray(int[][]::new));
        if (cur != null) d.put("running", new int[]{cur[0], cur[1]});
        d.put("maxCoord", maxCoord);
        return Frame.of("intervals", d, note);
    }

    private static String fmt(List<int[]> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("[").append(list.get(i)[0]).append(",").append(list.get(i)[1]).append("]");
        }
        return sb.append("]").toString();
    }
}