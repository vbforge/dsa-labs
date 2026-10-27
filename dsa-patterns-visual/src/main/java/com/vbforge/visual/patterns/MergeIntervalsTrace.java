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
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Merge Intervals – sort by start, then sweep and extend the last group.
 *
 * <p>Time O(n log n), space O(n). Renderer: {@code intervals}.
 */
public final class MergeIntervalsTrace implements Algorithm {

    private static final List<String> PSEUDO = List.of(
            "sort intervals by start",
            "merged = [ first ]",
            "for next in the rest:",
            "    if next.start <= last.end:  last.end = max(last.end, next.end)   // overlap",
            "    else:                       merged.add(next)                      // gap",
            "return merged");

    @Override
    public Meta meta() {
        return new Meta("merge-intervals", "Merge Intervals", "Overlapping Intervals",
                "Sort by start, then sweep: overlapping intervals extend the current group, a gap starts a new one.",
                "O(n log n)", "O(n)",
                "Sorting by start guarantees that only the last merged interval can ever overlap the next one. "
                        + "Touching semantics are problem-specific: here [1,2] and [2,3] merge (closed intervals). "
                        + "Greedy selection problems (erase-overlap, min-arrows) sort by end instead.",
                PSEUDO,
                List.of(Field.intervals("intervals", "Intervals", new int[][]{{1, 3}, {8, 10}, {2, 6}, {15, 18}, {17, 20}},
                        "pairs like [1,3], [2,6]", 0, 60, 12)),
                List.of(
                        Preset.of("Classic", "intervals", new int[][]{{1, 3}, {8, 10}, {2, 6}, {15, 18}, {17, 20}}),
                        Preset.of("Chain reaction", "intervals", new int[][]{{1, 4}, {4, 5}, {5, 9}, {20, 24}, {8, 12}}),
                        Preset.of("Nothing overlaps", "intervals", new int[][]{{1, 2}, {4, 5}, {7, 8}, {10, 11}}),
                        Preset.of("One big cover", "intervals", new int[][]{{1, 40}, {3, 5}, {10, 12}, {20, 22}})));
    }

    @Override
    public void trace(Params p, Consumer<Frame> emit) {
        int[][] given = p.intervals("intervals", 1, 12, 0, 60);
        int n = given.length;

        int[][] iv = Arrays.stream(given).map(int[]::clone).toArray(int[][]::new);
        char[] st = new char[n];
        Arrays.fill(st, 'p');

        frame(emit, iv, st, new ArrayList<>(), "Input as given – unsorted", -1);
        Arrays.sort(iv, Comparator.comparingInt((int[] a) -> a[0]).thenComparingInt(a -> a[1]));
        frame(emit, iv, st, new ArrayList<>(), "Sorted by start – now only the LAST group can overlap the next interval", 0);

        List<int[]> merged = new ArrayList<>();
        merged.add(iv[0].clone());
        st[0] = 'k';
        frame(emit, iv, st, merged, "Seed the result with the first interval " + fmt(iv[0]), 1);

        for (int i = 1; i < n; i++) {
            int[] last = merged.get(merged.size() - 1);
            st[i] = 'c';
            frame(emit, iv, st, merged,
                    String.format("next %s  vs  last %s :  %d %s %d", fmt(iv[i]), fmt(last),
                            iv[i][0], iv[i][0] <= last[1] ? "<=" : ">", last[1]), 2);

            if (iv[i][0] <= last[1]) {
                int newEnd = Math.max(last[1], iv[i][1]);
                String note = newEnd == last[1]
                        ? "overlap, fully inside  →  last stays " + fmt(last)
                        : "overlap  →  extend last end " + last[1] + " → " + newEnd;
                last[1] = newEnd;
                st[i] = 'a';
                frame(emit, iv, st, merged, note, 3);
            } else {
                merged.add(iv[i].clone());
                st[i] = 'k';
                frame(emit, iv, st, merged, "gap  →  start a new group " + fmt(iv[i]), 4);
            }
        }

        List<String> shown = new ArrayList<>();
        merged.forEach(m -> shown.add(fmt(m)));
        frame(emit, iv, st, merged, "Result: " + String.join(" ", shown) + "   (" + n + " → " + merged.size() + " intervals)", 5);
    }

    private static String fmt(int[] a) {
        return "[" + a[0] + "," + a[1] + "]";
    }

    private static void frame(Consumer<Frame> emit, int[][] iv, char[] st, List<int[]> merged, String note, int line) {
        // st: p pending · c comparing · a absorbed into the previous group · k starts a group
        List<Object> items = new ArrayList<>();
        for (int i = 0; i < iv.length; i++) items.add(Viz.kv("s", iv[i][0], "e", iv[i][1], "st", String.valueOf(st[i])));
        List<int[]> copy = new ArrayList<>();
        merged.forEach(m -> copy.add(m.clone()));
        Data d = Data.of()
                .with("items", items)
                .with("merged", copy)
                .with("info", Viz.kv("groups", merged.size(), "intervals", iv.length));
        emit.accept(line < 0 ? Frame.of("intervals", d, note) : Frame.of("intervals", d, note, line));
    }
}
