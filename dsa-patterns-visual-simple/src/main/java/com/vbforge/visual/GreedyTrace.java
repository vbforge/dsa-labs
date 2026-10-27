package com.vbforge.visual;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GreedyTrace {

    public static void trace(Consumer<Frame> emit) {
        int[] nums = {2, 3, 1, 1, 4};
        int jumps = 0;
        int curEnd = 0;
        int farthest = 0;

        emit.accept(frame(nums, 0, curEnd, farthest, jumps,
                "Initial state — find minimum jumps to reach the end"));

        for (int i = 0; i < nums.length - 1; i++) {
            farthest = Math.max(farthest, i + nums[i]);

            String note = String.format("i=%d: jump range reaches up to %d", i, farthest);

            if (i == curEnd) {
                jumps++;
                curEnd = farthest;
                note = String.format("i=%d reached curEnd → jump! (jumps=%d, next window ends at %d)",
                        i, jumps, curEnd);
            }

            emit.accept(frame(nums, i, curEnd, farthest, jumps, note));
        }

        emit.accept(frame(nums, nums.length - 1, curEnd, farthest, jumps,
                "Reached the end in " + jumps + " jumps"));
    }

    private static Frame frame(int[] nums, int i, int curEnd, int farthest, int jumps, String note) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("values", nums);
        d.put("left", i);
        d.put("mid", curEnd);       // Purple pointer for curEnd
        d.put("right", farthest);   // Red pointer for farthest
        d.put("best", jumps);       // Display jumps as "best"
        return Frame.of("array+pointers", d, note);
    }
}