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
 * Classic closed-interval binary search ({@code lo <= hi}).
 *
 * <p>Time O(log n), space O(1). Renderer: {@code array}.
 */
public final class BinarySearchTrace implements Algorithm {

    private static final List<String> PSEUDO = List.of(
            "lo = 0, hi = n - 1",
            "while lo <= hi:",
            "    mid = lo + (hi - lo) / 2        // never (lo+hi)/2 – overflow",
            "    if a[mid] == target: return mid",
            "    if a[mid] < target:  lo = mid + 1   // discard left half",
            "    else:                hi = mid - 1   // discard right half",
            "return -1       // lo is the insertion point");

    @Override
    public Meta meta() {
        return new Meta("binary-search", "Binary Search", "Binary Search",
                "Halve the search space every step by comparing the middle element with the target.",
                "O(log n)", "O(1)",
                "Closed [lo, hi] invariant: the target, if present, is always inside the window. "
                        + "Use mid = lo + (hi - lo) / 2. When the loop ends without a hit, lo is exactly "
                        + "where the target would be inserted – the same idea behind lowerBound.",
                PSEUDO,
                List.of(
                        Field.intList("nums", "Array (sorted automatically)", new int[]{1, 3, 5, 7, 9, 11, 13, 15, 17, 19},
                                "comma-separated integers", -99, 99, 1, 24),
                        Field.integer("target", "Target", 13, "value to look for", -99, 99, "element:nums")),
                List.of(
                        Preset.of("Found", "nums", new int[]{1, 3, 5, 7, 9, 11, 13, 15, 17, 19}, "target", 13),
                        Preset.of("Missing", "nums", new int[]{1, 3, 5, 7, 9, 11, 13, 15, 17, 19}, "target", 8),
                        Preset.of("First element", "nums", new int[]{2, 4, 6, 8, 10, 12, 14, 16}, "target", 2)));
    }

    @Override
    public void trace(Params p, Consumer<Frame> emit) {
        int[] nums = p.ints("nums", 1, 24, -99, 99);
        int target = p.integer("target", -99, 99);
        int n = nums.length;

        int[] sorted = nums.clone();
        Arrays.sort(sorted);
        boolean wasSorted = Arrays.equals(nums, sorted);
        nums = sorted;

        int lo = 0, hi = n - 1;
        frame(emit, nums, lo, null, hi, target, Viz.marks(n, lo, hi, '.'),
                (wasSorted ? "" : "Input sorted first.  ") + "Search space = the whole array [0 .. " + hi + "]", 0);

        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            frame(emit, nums, lo, mid, hi, target, Viz.marks(n, lo, hi, 'c', mid),
                    String.format("mid = %d + (%d - %d) / 2 = %d   →   a[%d] = %d", lo, hi, lo, mid, mid, nums[mid]), 2);

            if (nums[mid] == target) {
                frame(emit, nums, lo, mid, hi, target, Viz.marks(n, lo, hi, 'f', mid),
                        "a[" + mid + "] == " + target + "  →  FOUND at index " + mid, 3);
                return;
            }
            if (nums[mid] < target) {
                lo = mid + 1;
                frame(emit, nums, lo, null, hi, target, Viz.marks(n, lo, hi, '.'),
                        nums[mid] + " < " + target + "  →  target is to the right, lo = " + lo, 4);
            } else {
                hi = mid - 1;
                frame(emit, nums, lo, null, hi, target, Viz.marks(n, lo, hi, '.'),
                        nums[mid] + " > " + target + "  →  target is to the left, hi = " + hi, 5);
            }
        }
        frame(emit, nums, lo, null, hi, target, Viz.marks(n, 0, -1, '.'),
                "lo > hi  →  " + target + " is not in the array (it would be inserted at index " + lo + ")", 6);
    }

    private static void frame(Consumer<Frame> emit, int[] nums, int lo, Integer mid, int hi,
                              int target, String marks, String note, int line) {
        var ptr = Viz.kv("lo", lo, "mid", mid, "hi", hi);
        emit.accept(Frame.of("array", Data.of()
                .with("values", nums)
                .with("pointers", ptr)
                .with("marks", marks)
                .with("info", Viz.kv("target", target, "lo", lo, "mid", mid, "hi", hi)), note, line));
    }
}
