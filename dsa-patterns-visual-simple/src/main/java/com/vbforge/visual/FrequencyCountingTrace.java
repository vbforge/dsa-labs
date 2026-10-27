package com.vbforge.visual;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class FrequencyCountingTrace {

    public static void trace(Consumer<Frame> emit) {
        char[] chars = {'a', 'a', 'b', 'b', 'c'};
        int n = chars.length;

        // ── Pass 1: count frequencies ────────────────────────────────
        Map<Character, Integer> freq = new LinkedHashMap<>();
        for (char c : chars) freq.merge(c, 1, Integer::sum);

        int[] counts = new int[n];
        for (int i = 0; i < n; i++) counts[i] = freq.get(chars[i]);

        // Frame 1 — initial (all counts zero)
        emit.accept(Frame.of("array+pointers",
                data(chars, new int[n], -1, List.of()),
                "Initialized — about to count occurrences"));

        // Frame 2 — after counting
        emit.accept(Frame.of("array+pointers",
                data(chars, counts, -1, List.of()),
                "Counted — now scan for the first character with count 1"));

        // ── Pass 2: scan for first unique ────────────────────────────
        for (int i = 0; i < n; i++) {
            if (counts[i] == 1) {
                emit.accept(Frame.of("array+pointers",
                        data(chars, counts, i, List.of(i)),
                        String.format("FOUND: '%c' at index %d  (count = 1)",
                                chars[i], i)));
                return;
            }
            emit.accept(Frame.of("array+pointers",
                    data(chars, counts, i, List.of()),
                    String.format("'%c' at index %d  →  count = %d  (not unique)",
                            chars[i], i, counts[i])));
        }

        emit.accept(Frame.of("array+pointers",
                data(chars, counts, -1, List.of()),
                "No unique character exists"));
    }

    private static Map<String, Object> data(char[] chars, int[] counts,
                                            int cursor, List<Integer> highlight) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("values",    chars);
        d.put("counts",    counts);
        d.put("left",      cursor);
        d.put("highlight", highlight);
        return d;
    }
}