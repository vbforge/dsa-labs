package com.vbforge.visual;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Visualizes Daily Temperatures (LC 739) — monotonic stack.
 * Uses the "stack" renderer.
 */
public class MonotonicStackTrace {

    public static void trace(Consumer<Frame> emit) {
        int[] temps  = {73, 74, 75, 71, 69, 72, 76, 73};
        int n = temps.length;
        int[] answer = new int[n];
        Arrays.fill(answer, -1);                        // -1 = not yet computed
        Deque<Integer> stack = new ArrayDeque<>();

        emit.accept(frame(temps, answer, -1, stack,
                "Initial — no answers yet, stack empty"));

        for (int i = 0; i < n; i++) {
            int popped = 0;
            while (!stack.isEmpty() && temps[stack.peek()] < temps[i]) {
                int idx = stack.pop();
                answer[idx] = i - idx;
                popped++;
            }
            stack.push(i);
            String note = (popped == 0)
                    ? String.format("i=%d (temp %d): no warmer in stack → push. stack size=%d",
                            i, temps[i], stack.size())
                    : String.format("i=%d (temp %d): popped %d, filled answers. stack size=%d",
                            i, temps[i], popped, stack.size());
            emit.accept(frame(temps, answer, i, stack, note));
        }

        // Drain remaining stack — those days have no warmer day
        while (!stack.isEmpty()) answer[stack.pop()] = 0;
        emit.accept(frame(temps, answer, -1, stack,
                "Done. Days with no warmer ahead → 0."));
    }

    private static Frame frame(int[] temps, int[] answer, int current,
                               Deque<Integer> stack, String note) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("values",       temps);
        d.put("answers",      answer.clone());
        d.put("currentIdx",   current);
        d.put("stackIndices", stackBottomToTop(stack));
        return Frame.of("stack", d, note);
    }

    private static int[] stackBottomToTop(Deque<Integer> stack) {
        int[] arr = new int[stack.size()];
        int i = arr.length - 1;
        for (int idx : stack) arr[i--] = idx;          // iterator = top→bottom; fill from end
        return arr;
    }
}