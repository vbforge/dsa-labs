package com.vbforge.visual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PrefixSumTrace {

    public static void trace(Consumer<Frame> emit) {
        int[] nums = {1, 2, 3, 4, 5};
        int n = nums.length;
        int queryL = 1, queryR = 3;

        int[] prefix = new int[n + 1];
        Arrays.fill(prefix, -1);
        prefix[0] = 0;

        // Frame 1 — initial
        emit.accept(Frame.of("array+pointers",
                data(nums, prefix, List.of(), List.of()),
                "Initialize: prefix[0] = 0"));

        // Frames 2..(n+1) — build prefix one cell at a time
        for (int i = 1; i <= n; i++) {
            int prev = prefix[i - 1];
            int added = nums[i - 1];
            prefix[i] = prev + added;
            emit.accept(Frame.of("array+pointers",
                    data(nums, prefix, List.of(), List.of()),
                    String.format("prefix[%d] = prefix[%d] + nums[%d] = %d + %d = %d",
                            i, i - 1, i - 1, prev, added, prefix[i])));
        }

        // Final frame — range query [queryL..queryR]
        List<Integer> numsHighlight = new ArrayList<>();
        for (int i = queryL; i <= queryR; i++) numsHighlight.add(i);
        List<Integer> prefixHighlight = List.of(queryL, queryR + 1);

        int answer = prefix[queryR + 1] - prefix[queryL];
        emit.accept(Frame.of("array+pointers",
                data(nums, prefix, numsHighlight, prefixHighlight),
                String.format("sum(%d..%d) = prefix[%d] - prefix[%d] = %d - %d = %d",
                        queryL, queryR, queryR + 1, queryL,
                        prefix[queryR + 1], prefix[queryL], answer)));
    }

    private static Map<String, Object> data(int[] nums, int[] prefix,
                                            List<Integer> highlight,
                                            List<Integer> prefixHighlight) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("values",          nums);
        d.put("prefix",          prefix);
        d.put("highlight",       highlight);
        d.put("prefixHighlight", prefixHighlight);
        return d;
    }
}