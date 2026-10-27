package com.vbforge.visual.patterns;

import com.vbforge.visual.Algorithm;
import com.vbforge.visual.Data;
import com.vbforge.visual.Frame;
import com.vbforge.visual.Meta;
import com.vbforge.visual.Meta.Field;
import com.vbforge.visual.Meta.Preset;
import com.vbforge.visual.Params;
import com.vbforge.visual.Viz;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Top-K largest elements with a size-k <em>min</em>-heap.
 *
 * <p>Time O(n log k), space O(k). Renderer: {@code heap}.
 */
public final class TopKTrace implements Algorithm {

    private static final List<String> PSEUDO = List.of(
            "minHeap = []                  // keeps the k LARGEST seen so far",
            "for x in nums:",
            "    if size < k:        push(x); sift up",
            "    elif x > root:      replace root with x; sift down   // evict the smallest",
            "    else:               skip x",
            "return heap contents");

    @Override
    public Meta meta() {
        return new Meta("top-k", "Top-K Largest", "Top-K Elements",
                "Stream the values through a min-heap of size k – the root is always the weakest of the current top k.",
                "O(n log k)", "O(k)",
                "The inverted-heap rule: for the k LARGEST keep a MIN-heap (you must be able to evict the smallest "
                        + "of your best); for the k smallest keep a MAX-heap. Getting it backwards silently returns wrong "
                        + "answers, not exceptions.",
                PSEUDO,
                List.of(
                        Field.intList("nums", "Stream of values", new int[]{3, 1, 5, 12, 2, 11, 9, 7, 15, 4},
                                "comma-separated integers", -99, 99, 3, 20),
                        Field.integer("k", "k", 3, "heap capacity (1–15, ≤ number of values)", 1, 15, "upto:nums")),
                List.of(
                        Preset.of("k = 3", "nums", new int[]{3, 1, 5, 12, 2, 11, 9, 7, 15, 4}, "k", 3),
                        Preset.of("Ascending (worst case)", "nums", new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, "k", 4),
                        Preset.of("Descending (best case)", "nums", new int[]{10, 9, 8, 7, 6, 5, 4, 3, 2, 1}, "k", 4)));
    }

    @Override
    public void trace(Params p, Consumer<Frame> emit) {
        int[] a = p.ints("nums", 3, 20, -99, 99);
        int k = p.integer("k", 1, 15);
        if (k > a.length) throw new IllegalArgumentException("'k' must not exceed the number of values (" + a.length + ")");

        Run r = new Run(a, k, emit);
        r.show("Min-heap with capacity k = " + k + " – it will always hold the k largest values seen so far", 0);

        for (int i = 0; i < a.length; i++) {
            r.cur = i;
            int x = a[i];
            if (r.size < k) {
                r.heap[r.size++] = x;
                r.show("x = " + x + ": heap not full  →  push at index " + (r.size - 1), 2, r.size - 1);
                r.siftUp(r.size - 1);
            } else if (x > r.heap[0]) {
                int old = r.heap[0];
                r.heap[0] = x;
                r.show("x = " + x + " > root " + old + "  →  evict " + old + ", put " + x + " at the root", 3, 0);
                r.siftDown(0);
            } else {
                r.show("x = " + x + " <= root " + r.heap[0] + "  →  cannot beat the current top-" + k + ", skip", 4);
            }
        }

        r.cur = a.length;
        int[] top = Arrays.copyOf(r.heap, r.size);
        Integer[] desc = Arrays.stream(top).boxed().sorted(Comparator.reverseOrder()).toArray(Integer[]::new);
        char[] all = new char[r.size];
        Arrays.fill(all, 'f');
        emit.accept(Frame.of("heap", r.data(new String(all))
                .with("lists", List.of(Viz.list("top-" + k + " (desc)", Arrays.asList(desc)))),
                "Done: the " + k + " largest values are " + Arrays.toString(desc), 5));
    }

    /** Mutable heap state + the helpers that narrate every swap. */
    private static final class Run {
        final int[] a, heap;
        final int k;
        final Consumer<Frame> emit;
        int size, cur = -1;

        Run(int[] a, int k, Consumer<Frame> emit) {
            this.a = a;
            this.k = k;
            this.heap = new int[k];
            this.emit = emit;
        }

        Data data(String marks) {
            return Data.of()
                    .with("values", a)
                    .with("cur", cur)
                    .with("heap", Arrays.copyOf(heap, size))
                    .with("k", k)
                    .with("marks", marks)
                    .with("info", Viz.kv("k", k, "size", size, "root (min)", size > 0 ? heap[0] : null));
        }

        void show(String note, int line, int... hot) {
            char[] m = new char[size];
            Arrays.fill(m, '.');
            for (int h : hot) if (h >= 0 && h < size) m[h] = 'c';
            emit.accept(Frame.of("heap", data(new String(m)), note, line));
        }

        void siftUp(int i) {
            while (i > 0) {
                int par = (i - 1) / 2;
                if (heap[par] <= heap[i]) break;
                int child = heap[i], parent = heap[par];
                swap(par, i);
                show(child + " < parent " + parent + "  →  swap up", 2, par);
                i = par;
            }
        }

        void siftDown(int i) {
            while (true) {
                int l = 2 * i + 1, r = l + 1, m = i;
                if (l < size && heap[l] < heap[m]) m = l;
                if (r < size && heap[r] < heap[m]) m = r;
                if (m == i) break;
                int top = heap[i], child = heap[m];
                swap(i, m);
                show(top + " > smaller child " + child + "  →  swap down", 3, m);
                i = m;
            }
        }

        void swap(int i, int j) {
            int t = heap[i];
            heap[i] = heap[j];
            heap[j] = t;
        }
    }
}
