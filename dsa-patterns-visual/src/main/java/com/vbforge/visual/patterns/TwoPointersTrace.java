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
import java.util.List;
import java.util.function.Consumer;

/**
 * Two Sum II – opposite-direction two pointers over a sorted array.
 *
 * <p>Time O(n), space O(1). Renderer: {@code array}.
 */
public final class TwoPointersTrace implements Algorithm {

    private static final List<String> PSEUDO = List.of(
            "left = 0, right = n - 1",
            "while left < right:",
            "    sum = a[left] + a[right]",
            "    if sum == target: return [left, right]",
            "    if sum < target:  left++    // need a bigger sum",
            "    else:             right--   // need a smaller sum",
            "return not found");

    @Override
    public Meta meta() {
        return new Meta("two-pointers", "Two Sum II", "Two Pointers",
                "Walk two pointers inward from both ends of a sorted array until the pair hits the target.",
                "O(n)", "O(1)",
                "If the sum is too small, no pair using a[left] can ever work (a[right] is already the largest "
                        + "partner left) – so left is discarded for good. Symmetrically for right. "
                        + "Every step eliminates one element, hence a single linear pass.",
                PSEUDO,
                List.of(
                        Field.intList("nums", "Array (sorted automatically)", new int[]{2, 7, 11, 15},
                                "comma-separated integers", -99, 99, 2, 24),
                        Field.integer("target", "Target sum", 9, "the pair must add up to this",
                                -400, 400, "pairsum:nums")),
                List.of(
                        Preset.of("Classic", "nums", new int[]{2, 7, 11, 15}, "target", 9),
                        Preset.of("Longer walk", "nums", new int[]{1, 3, 4, 6, 8, 10, 13, 17, 21, 25}, "target", 38),
                        Preset.of("No pair", "nums", new int[]{1, 2, 4, 9, 12}, "target", 100)));
    }

    @Override
    public void trace(Params p, Consumer<Frame> emit) {
        int[] nums = p.ints("nums", 2, 24, -99, 99);
        int target = p.integer("target", -400, 400);
        int n = nums.length;

        int[] sortedCopy = nums.clone();
        Arrays.sort(sortedCopy);
        boolean wasSorted = Arrays.equals(nums, sortedCopy);
        nums = sortedCopy;

        int left = 0, right = n - 1;
        frame(emit, nums, left, right, target, null, Viz.marks(n, 0, n - 1, '.'),
                wasSorted ? "Start: left on the first element, right on the last"
                        : "Input was unsorted – sorted first. left on the smallest, right on the largest", 0);

        while (left < right) {
            int sum = nums[left] + nums[right];
            frame(emit, nums, left, right, target, sum, Viz.marks(n, left, right, 'c', left, right),
                    String.format("sum = %d + %d = %d   (target %d)", nums[left], nums[right], sum, target), 2);

            if (sum == target) {
                frame(emit, nums, left, right, target, sum, Viz.marks(n, 0, -1, 'f', left, right),
                        String.format("FOUND  a[%d] + a[%d] = %d + %d = %d  →  indices [%d, %d]",
                                left, right, nums[left], nums[right], target, left, right), 3);
                return;
            }
            if (sum < target) {
                left++;
                frame(emit, nums, left, right, target, sum, Viz.marks(n, left, right, 'w', left, right),
                        sum + " < " + target + "  →  too small, discard a[" + (left - 1) + "], left → " + left, 4);
            } else {
                right--;
                frame(emit, nums, left, right, target, sum, Viz.marks(n, left, right, 'w', left, right),
                        sum + " > " + target + "  →  too big, discard a[" + (right + 1) + "], right → " + right, 5);
            }
        }
        frame(emit, nums, left, right, target, null, Viz.marks(n, 0, -1, '.'),
                "Pointers met – no two elements add up to " + target, 6);
    }

    private static void frame(Consumer<Frame> emit, int[] nums, int l, int r, int target,
                              Integer sum, String marks, String note, int line) {
        emit.accept(Frame.of("array", Data.of()
                .with("values", nums)
                .with("pointers", Viz.kv("left", l, "right", r))
                .with("marks", marks)
                .with("info", Viz.kv("target", target, "sum", sum)), note, line));
    }
}
