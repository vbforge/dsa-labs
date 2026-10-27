package com.vbforge.visual;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BinarySearchTrace {

    public static void trace(Consumer<Frame> emit) {
        int[] nums = {1, 3, 5, 7, 9, 11, 13};
        int target = 5;
        int left = 0, right = nums.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            String cmp = nums[mid] == target ? "== target"
                       : nums[mid] <  target ? "< target  → move left up"
                       :                       "> target  → move right down";

            emit.accept(Frame.of("array+pointers",
                    data(nums, left, right, mid, target, List.of()),
                    String.format("mid = %d, nums[%d] = %d  %s",
                            mid, mid, nums[mid], cmp)));

            if (nums[mid] == target) {
                emit.accept(Frame.of("array+pointers",
                        data(nums, left, right, mid, target, List.of(mid)),
                        String.format("FOUND: nums[%d] = %d", mid, target)));
                return;
            }
            if (nums[mid] < target) left = mid + 1;
            else                    right = mid - 1;
        }
        emit.accept(Frame.of("array+pointers",
                data(nums, left, right, -1, target, List.of()),
                String.format("NOT FOUND: %d is not in the array", target)));
    }

    private static Map<String, Object> data(int[] nums, int left, int right, int mid,
                                            int target, List<Integer> highlight) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("values",    nums);
        d.put("left",      left);
        d.put("right",     right);
        d.put("mid",       mid);
        d.put("target",    target);
        d.put("highlight", highlight);
        return d;
    }
}