package com.vbforge.visual;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Visualizes Maximum Sum Subarray of Size K.
 *
 * Emits one frame for the initial window, one per slide, and one final frame
 * showing the best window found. Uses the "array+pointers" renderer — the
 * window is highlighted between the left and right pointers.
 */
public class SlidingWindowTrace {

    public static void trace(Consumer<Frame> emit) {
        int[] nums = {2, 1, 5, 1, 3, 2};
        int k = 3;

        int sum = 0;
        for (int i = 0; i < k; i++) sum += nums[i];
        int best = sum;
        int bestLeft = 0;

        emit.accept(Frame.of("array+pointers",
                data(nums, 0, k - 1, k, sum, best),
                String.format("initial window [0..%d]: sum = %d", k - 1, sum)));

        for (int i = k; i < nums.length; i++) {
            int left = i - k + 1;
            int removed = nums[i - k];
            int added   = nums[i];
            sum += added - removed;
            boolean isNewBest = sum > best;
            if (isNewBest) {
                best = sum;
                bestLeft = left;
            }
            emit.accept(Frame.of("array+pointers",
                    data(nums, left, i, k, sum, best),
                    String.format("slide: +%d -%d => sum = %d%s",
                            added, removed, sum,
                            isNewBest ? "   ★ new best" : "")));
        }

        emit.accept(Frame.of("array+pointers",
                data(nums, bestLeft, bestLeft + k - 1, k, best, best),
                String.format("BEST window [%d..%d] = %s  (sum = %d)",
                        bestLeft, bestLeft + k - 1,
                        sliceToString(nums, bestLeft, bestLeft + k - 1),
                        best)));
    }

    private static Map<String, Object> data(int[] nums, int left, int right,
                                            int k, int sum, int best) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("values", nums);
        d.put("left",   left);
        d.put("right",  right);
        d.put("k",      k);
        d.put("sum",    sum);
        d.put("best",   best);
        return d;
    }

    private static String sliceToString(int[] nums, int from, int to) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = from; i <= to; i++) {
            if (i > from) sb.append(", ");
            sb.append(nums[i]);
        }
        return sb.append("]").toString();
    }
}