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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Longest Substring Without Repeating Characters – variable-size sliding window.
 *
 * <p>Time O(n), space O(min(n, alphabet)). Renderer: {@code array}.
 */
public final class SlidingWindowTrace implements Algorithm {

    private static final List<String> PSEUDO = List.of(
            "left = 0; window = {}",
            "for right in 0 .. n-1:",
            "    while s[right] in window:        // window invalid",
            "        window.remove(s[left]); left++   // shrink",
            "    window.add(s[right])             // expand",
            "    best = max(best, right - left + 1)");

    @Override
    public Meta meta() {
        return new Meta("sliding-window", "Longest Unique Substring", "Sliding Window",
                "Grow the window on the right, shrink it from the left whenever it contains a repeated character.",
                "O(n)", "O(min(n, k))",
                "Every sliding-window problem fits one skeleton: expand right → while invalid, shrink left → "
                        + "record the answer. Each index enters and leaves the window at most once, so the "
                        + "nested-looking loop is still linear.",
                PSEUDO,
                List.of(Field.text("text", "String", "abcabcbb", "up to 28 characters", 28, "abcde")),
                List.of(
                        Preset.of("abcabcbb", "text", "abcabcbb"),
                        Preset.of("pwwkew", "text", "pwwkew"),
                        Preset.of("All unique", "text", "abcdefgh"),
                        Preset.of("All same", "text", "aaaaaa")));
    }

    @Override
    public void trace(Params p, Consumer<Frame> emit) {
        String s = p.text("text", 1, 28);
        int n = s.length();
        List<String> values = new ArrayList<>();
        for (char ch : s.toCharArray()) values.add(Viz.show(ch));

        Set<Character> window = new LinkedHashSet<>();
        int left = 0, best = 0, bestStart = 0;

        frame(emit, values, window, left, -1, best, marks(n, 0, -1, -1, '.'),
                "Start with an empty window", 0);

        for (int right = 0; right < n; right++) {
            char c = s.charAt(right);
            boolean dup = window.contains(c);
            frame(emit, values, window, left, right, best, marks(n, left, right - 1, right, dup ? 'x' : 'c'),
                    dup ? "'" + Viz.show(c) + "' is already in the window  →  shrink from the left"
                        : "'" + Viz.show(c) + "' is new  →  the window can grow", dup ? 2 : 1);

            while (window.contains(c)) {
                char out = s.charAt(left);
                window.remove(out);
                left++;
                boolean still = window.contains(c);
                frame(emit, values, window, left, right, best, marks(n, left, right - 1, right, still ? 'x' : 'c'),
                        "drop '" + Viz.show(out) + "' from the left"
                                + (still ? "  –  '" + Viz.show(c) + "' is still inside" : "  –  conflict resolved"), 3);
            }

            window.add(c);
            int len = right - left + 1;
            if (len > best) {
                best = len;
                bestStart = left;
            }
            frame(emit, values, window, left, right, best, marks(n, left, right, -1, '.'),
                    "window \"" + s.substring(left, right + 1) + "\" has length " + len + "   (best " + best + ")", 5);
        }

        int bestEnd = bestStart + best - 1;
        frame(emit, values, window, bestStart, bestEnd, best, marks(n, bestStart, bestEnd, -1, 'f'),
                "Longest substring without repeats: \"" + s.substring(bestStart, bestEnd + 1) + "\"  (length " + best + ")", 5);
    }

    /** Cells in [from, to] are the window ('w', or 'f' for the final answer); {@code focus} gets {@code focusMark}. */
    private static String marks(int n, int from, int to, int focus, char focusMark) {
        char[] m = new char[n];
        Arrays.fill(m, '.');
        char inside = focusMark == 'f' ? 'f' : 'w';
        for (int i = Math.max(0, from); i <= to && i < n; i++) m[i] = inside;
        if (focus >= 0 && focus < n && (focusMark == 'c' || focusMark == 'x')) m[focus] = focusMark;
        return new String(m);
    }

    private static void frame(Consumer<Frame> emit, List<String> values, Set<Character> window,
                              int left, int right, int best, String marks, String note, int line) {
        List<String> inWindow = new ArrayList<>();
        window.forEach(ch -> inWindow.add(Viz.show(ch)));

        // the highlighted window = every cell marked w/f
        int from = Math.max(marks.indexOf('w'), -1), to = marks.lastIndexOf('w');
        if (from < 0) { from = marks.indexOf('f'); to = marks.lastIndexOf('f'); }

        Data d = Data.of()
                .with("values", values)
                .with("marks", marks)
                .with("pointers", Viz.kv("left", left, "right", right))
                .with("info", Viz.kv("best", best, "window", window.size()))
                .with("lists", List.of(Viz.list("window set", inWindow)));
        if (from >= 0) d.with("window", Viz.kv("from", from, "to", to));
        emit.accept(Frame.of("array", d, note, line));
    }
}
