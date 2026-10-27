package com.vbforge.visual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Small helpers shared by every trace. */
public final class Viz {
    private Viz() {}

    /** Ordered map from alternating key/value arguments. */
    public static Map<String, Object> kv(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    /** A labelled chip row shown at the bottom of the canvas. */
    public static Map<String, Object> list(String label, Collection<?> items) {
        return kv("label", label, "items", new ArrayList<>(items));
    }

    public static List<Integer> boxed(int[] a) {
        return Arrays.stream(a).boxed().toList();
    }

    /** Printable form of a character (blank becomes a visible symbol). */
    public static String show(char c) {
        return c == ' ' ? "\u2423" : String.valueOf(c);
    }

    /**
     * One mark per cell of a 1-D structure: cells outside {@code [lo, hi]} are
     * dimmed ('d'), cells listed in {@code hot} get {@code hotMark}, the rest '.'.
     */
    public static String marks(int n, int lo, int hi, char hotMark, int... hot) {
        char[] m = new char[n];
        for (int i = 0; i < n; i++) m[i] = (i < lo || i > hi) ? 'd' : '.';
        for (int h : hot) if (h >= 0 && h < n) m[h] = hotMark;
        return new String(m);
    }
}
