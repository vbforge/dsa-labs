package com.vbforge.visual;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Visualizes Two Sum II — sorted array, opposite-direction two pointers.
 *
 * Output frames use the "array+pointers" renderer.
 */
public class TwoPointersTrace {

    public static void trace(Consumer<Frame> emit) {
        int[] nums = {2, 7, 11, 15};
        int target = 9;
        int left = 0;
        int right = nums.length - 1;

        while (left < right) {
            int sum = nums[left] + nums[right];
            emit.accept(Frame.of("array+pointers",
                    data(nums, left, right, target, sum),
                    String.format("sum = %d + %d = %d  (target %d)",
                            nums[left], nums[right], sum, target)));

            if (sum == target) {
                emit.accept(Frame.of("array+pointers",
                        data(nums, left, right, target, sum),
                        String.format("FOUND: indices [%d, %d]", left, right)));
                return;
            }
            if (sum < target) left++;
            else              right--;
        }

        emit.accept(Frame.of("array+pointers",
                data(nums, left, right, target, -1),
                "No pair sums to target"));
    }

    private static Map<String, Object> data(int[] nums, int left, int right,
                                            int target, int sum) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("values", nums);
        d.put("left",   left);
        d.put("right",  right);
        d.put("target", target);
        d.put("sum",    sum);
        return d;
    }
}