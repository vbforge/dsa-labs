package com.vbforge.visual.patterns;

import com.vbforge.visual.Algorithm;
import com.vbforge.visual.Data;
import com.vbforge.visual.Frame;
import com.vbforge.visual.Meta;
import com.vbforge.visual.Meta.Field;
import com.vbforge.visual.Meta.Preset;
import com.vbforge.visual.Params;
import com.vbforge.visual.Viz;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Next Greater Element with a monotonic (decreasing) stack of indices.
 *
 * <p>Time O(n), space O(n). Renderer: {@code stack}.
 */
public final class MonotonicStackTrace implements Algorithm {

    private static final List<String> PSEUDO = List.of(
            "stack = []              // indices, values decreasing",
            "for i in 0 .. n-1:",
            "    while stack and a[stack.top] < a[i]:",
            "        res[stack.pop()] = a[i]   // a[i] is their next greater",
            "    stack.push(i)",
            "leftovers have no greater element → -1");

    @Override
    public Meta meta() {
        return new Meta("monotonic-stack", "Next Greater Element", "Monotonic Stack",
                "A stack of still-unanswered indices; a bigger value pops (and answers) everything smaller below it.",
                "O(n)", "O(n)",
                "Each index is pushed once and popped at most once, so the inner while-loop is amortised O(1). "
                        + "Four knobs define the whole family: what you store (values/indices), the comparator, "
                        + "where the answer is written, and when you push.",
                PSEUDO,
                List.of(Field.intList("nums", "Array", new int[]{2, 7, 3, 5, 4, 6, 8, 1},
                        "comma-separated integers", -99, 99, 1, 24)),
                List.of(
                        Preset.of("Mixed", "nums", new int[]{2, 7, 3, 5, 4, 6, 8, 1}),
                        Preset.of("Daily temperatures", "nums", new int[]{73, 74, 75, 71, 69, 72, 76, 73}),
                        Preset.of("Strictly decreasing", "nums", new int[]{9, 7, 5, 3, 1}),
                        Preset.of("Strictly increasing", "nums", new int[]{1, 2, 3, 4, 5})));
    }

    @Override
    public void trace(Params p, Consumer<Frame> emit) {
        int[] a = p.ints("nums", 1, 24, -99, 99);
        int n = a.length;
        Integer[] res = new Integer[n];
        Deque<Integer> stack = new ArrayDeque<>();

        frame(emit, a, -1, stack, res, -1, "Empty stack – it will hold indices whose next greater value is still unknown", 0);

        for (int i = 0; i < n; i++) {
            frame(emit, a, i, stack, res, -1, "i = " + i + ", value " + a[i], 1);

            while (!stack.isEmpty() && a[stack.peek()] < a[i]) {
                int j = stack.pop();
                res[j] = a[i];
                frame(emit, a, i, stack, res, j,
                        "a[" + j + "] = " + a[j] + " < " + a[i] + "  →  pop " + j + ";  next greater of " + a[j] + " is " + a[i], 3);
            }
            stack.push(i);
            frame(emit, a, i, stack, res, -1,
                    "push " + i + "  (stack stays decreasing: " + describe(a, stack) + ")", 4);
        }

        int leftover = stack.size();
        while (!stack.isEmpty()) res[stack.pop()] = -1;
        frame(emit, a, n, stack, res, -1,
                leftover == 0 ? "Done – every element found a greater neighbour"
                        : leftover + " index(es) never met a greater value  →  -1", 5);
    }

    private static String describe(int[] a, Deque<Integer> stack) {
        List<String> vals = new ArrayList<>();
        for (int idx : bottomUp(stack)) vals.add(String.valueOf(a[idx]));
        return String.join(" > ", vals);
    }

    /** Stack contents, bottom first. */
    private static List<Integer> bottomUp(Deque<Integer> stack) {
        List<Integer> out = new ArrayList<>();
        Iterator<Integer> it = stack.descendingIterator();
        while (it.hasNext()) out.add(it.next());
        return out;
    }

    private static void frame(Consumer<Frame> emit, int[] a, int i, Deque<Integer> stack, Integer[] res,
                              int popped, String note, int line) {
        char[] m = new char[a.length];
        Arrays.fill(m, '.');
        for (int idx : stack) m[idx] = 'w';
        if (popped >= 0) m[popped] = 'x';
        if (i >= 0 && i < a.length) m[i] = 'c';
        if (i >= a.length) Arrays.fill(m, 'f');

        emit.accept(Frame.of("stack", Data.of()
                .with("values", a)
                .with("cur", i)
                .with("stack", bottomUp(stack))
                .with("result", res)
                .with("marks", new String(m))
                .with("info", Viz.kv("i", i >= 0 && i < a.length ? i : null, "stack size", stack.size())), note, line));
    }
}
