package com.vbforge.visual;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Visualizes Top K Frequent Elements (LC 347) — min-heap of size k.
 * Uses the "heap" renderer.
 */
public class TopKElementsTrace {

    public static void trace(Consumer<Frame> emit) {
        int[] nums = {1, 1, 1, 2, 2, 3};
        int k = 2;

        // Count frequencies, preserving first-seen order
        Map<Integer, Integer> freq = new LinkedHashMap<>();
        for (int n : nums) freq.merge(n, 1, Integer::sum);

        List<Integer> unique = new ArrayList<>(freq.keySet());
        int[] values = unique.stream().mapToInt(Integer::intValue).toArray();
        int[] freqs  = unique.stream().mapToInt(freq::get).toArray();

        emit.accept(frame(values, freqs, -1, List.of(), k,
                "Counted frequencies. Now examine each with a min-heap of size k=" + k));

        // Min-heap ordered by frequency ascending — kept as a list for visualization
        Deque<int[]> heap = new ArrayDeque<>();
        for (int i = 0; i < unique.size(); i++) {
            heap.add(new int[]{values[i], freqs[i]});
            List<int[]> sorted = new ArrayList<>(heap);
            sorted.sort((a, b) -> Integer.compare(a[1], b[1]));   // min-freq first
            heap = new ArrayDeque<>(sorted);

            String note;
            if (heap.size() > k) {
                int[] popped = heap.pollFirst();
                note = String.format(
                        "i=%d: elem=%d (freq=%d) → push, size=%d > k=%d → pop (%d,%d).",
                        i, values[i], freqs[i], heap.size() + 1, k, popped[0], popped[1]);
            } else {
                note = String.format("i=%d: elem=%d (freq=%d) → push. heap size=%d.",
                        i, values[i], freqs[i], heap.size());
            }
            emit.accept(frame(values, freqs, i, new ArrayList<>(heap), k, note));
        }

        List<Integer> answer = new ArrayList<>();
        for (int[] e : heap) answer.add(e[0]);
        emit.accept(frame(values, freqs, -1, new ArrayList<>(heap), k,
                "Answer (top " + k + " frequent): " + answer));
    }

    private static Frame frame(int[] values, int[] freqs, int current,
                               List<int[]> heap, int k, String note) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("values",      values);
        d.put("frequencies", freqs);
        d.put("currentIdx",  current);
        d.put("k",           k);
        int[] hv = new int[heap.size()];
        int[] hf = new int[heap.size()];
        for (int i = 0; i < heap.size(); i++) {
            hv[i] = heap.get(i)[0];
            hf[i] = heap.get(i)[1];
        }
        d.put("heapValues", hv);
        d.put("heapFreqs",  hf);
        return Frame.of("heap", d, note);
    }
}