package com.vbforge.overlappingintervals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * <h2>Overlapping Intervals Pattern</h2>
 *
 * <p>Intervals are represented as {@code int[]{start, end}} throughout.
 * Unless stated otherwise, intervals are <b>closed</b> — {@code [1, 2]} and
 * {@code [2, 3]} touch at 2 and are considered overlapping for the purposes
 * of {@link #merge(int[][])} and {@link #insert(int[][], int[])}.</p>
 *
 * <p>{@link #eraseOverlapIntervals(int[][])}, {@link #minMeetingRooms(int[][])}
 * and {@link #findMinArrowShots(int[][])} treat intervals as <b>half-open</b>
 * {@code [start, end)} — touching endpoints do <em>not</em> overlap. This
 * matches LeetCode semantics for those problems.</p>
 *
 * <p>Input arrays are <b>never mutated</b> — every sorting method clones
 * its working copy. Outputs are fresh {@code int[][]} arrays in
 * start-ascending order.</p>
 */
public final class OverlappingIntervals {

    private OverlappingIntervals() { }

    /** Comparator by ascending start, tie-broken by ascending end. */
    private static final Comparator<int[]> BY_START =
            Comparator.<int[]>comparingInt(iv -> iv[0]).thenComparingInt(iv -> iv[1]);

    /** Comparator by ascending end, tie-broken by ascending start. */
    private static final Comparator<int[]> BY_END =
            Comparator.<int[]>comparingInt(iv -> iv[1]).thenComparingInt(iv -> iv[0]);

    /** Defensive copy so callers keep their input untouched. */
    private static int[][] copy(int[][] a) {
        int[][] out = new int[a.length][];
        for (int i = 0; i < a.length; i++) out[i] = a[i].clone();
        return out;
    }

    // =====================================================================
    //  1. Merge Intervals  (LC 56) — sort by start
    // =====================================================================

    /**
     * <b>Merge Intervals</b> — merges all overlapping (or touching) intervals
     * into a minimal set of disjoint intervals.
     *
     * <p>Sort by start; walk left-to-right extending the current interval's
     * end whenever the next start is {@code <= current end}.</p>
     *
     * <p>Time: O(n log n) — Space: O(n) (output + clone)</p>
     */
    public static int[][] merge(int[][] intervals) {
        if (intervals.length <= 1) return copy(intervals);

        int[][] sorted = copy(intervals);
        Arrays.sort(sorted, BY_START);

        List<int[]> result = new ArrayList<>();
        int[] current = sorted[0];

        for (int i = 1; i < sorted.length; i++) {
            int[] next = sorted[i];
            if (next[0] <= current[1]) {              // overlap or touch
                current[1] = Math.max(current[1], next[1]);
            } else {
                result.add(current);
                current = next;
            }
        }
        result.add(current);
        return result.toArray(new int[0][]);
    }

    // =====================================================================
    //  2. Insert Interval  (LC 57) — three-phase scan
    // =====================================================================

    /**
     * <b>Insert Interval</b> — inserts {@code newInterval} into a sorted,
     * non-overlapping list, merging where necessary.
     *
     * <p>Three phases, each one a single {@code while} loop:</p>
     * <ol>
     *   <li>Copy intervals that end <b>strictly before</b> {@code newInterval}
     *       starts.</li>
     *   <li>Merge every interval that starts at-or-before {@code newInterval}
     *       ends — accumulate the merged bounds into {@code newStart}/{@code newEnd}.</li>
     *   <li>Copy everything else verbatim.</li>
     * </ol>
     *
     * <p>Time: O(n) — Space: O(n)</p>
     */
    public static int[][] insert(int[][] intervals, int[] newInterval) {
        List<int[]> result = new ArrayList<>();
        int i = 0, n = intervals.length;
        int newStart = newInterval[0];
        int newEnd   = newInterval[1];

        // Phase 1 — intervals strictly before the new one
        while (i < n && intervals[i][1] < newStart) {
            result.add(intervals[i++].clone());
        }

        // Phase 2 — merge overlapping intervals into [newStart, newEnd]
        while (i < n && intervals[i][0] <= newEnd) {
            newStart = Math.min(newStart, intervals[i][0]);
            newEnd   = Math.max(newEnd,   intervals[i][1]);
            i++;
        }
        result.add(new int[]{newStart, newEnd});

        // Phase 3 — remaining intervals
        while (i < n) {
            result.add(intervals[i++].clone());
        }
        return result.toArray(new int[0][]);
    }

    // =====================================================================
    //  3. Erase Overlap Intervals  (LC 435) — sort by end
    // =====================================================================

    /**
     * <b>Non-overlapping Intervals</b> — minimum number of intervals to
     * remove so the rest don't overlap.
     *
     * <p>Equivalent to "keep the maximum non-overlapping subset, remove the
     * rest." The greedy rule is: sort by <b>end</b>, keep each interval
     * whose start is {@code >=} the last kept end. Any other interval in
     * between is removed.</p>
     *
     * <p>Why sort by end works: the interval with the earliest finish time
     * is always safe to include — it blocks the fewest future intervals.
     * This is the textbook "interval scheduling" greedy argument.</p>
     *
     * <p>Time: O(n log n) — Space: O(n) (clone)</p>
     */
    public static int eraseOverlapIntervals(int[][] intervals) {
        if (intervals.length == 0) return 0;

        int[][] sorted = copy(intervals);
        Arrays.sort(sorted, BY_END);

        int removed = 0;
        int lastEnd = Integer.MIN_VALUE;
        for (int[] iv : sorted) {
            if (iv[0] >= lastEnd) {                  // disjoint → keep
                lastEnd = iv[1];
            } else {
                removed++;
            }
        }
        return removed;
    }

    // =====================================================================
    //  4. Meeting Rooms I  (LC 252) — overlap check
    // =====================================================================

    /**
     * <b>Meeting Rooms</b> — can a person attend all meetings?
     * {@code true} iff no two meetings overlap (touching endpoints are OK).
     *
     * <p>Sort by start; two meetings overlap iff the later start is strictly
     * less than the earlier end.</p>
     *
     * <p>Time: O(n log n) — Space: O(n) (clone)</p>
     */
    public static boolean canAttendMeetings(int[][] intervals) {
        if (intervals.length <= 1) return true;

        int[][] sorted = copy(intervals);
        Arrays.sort(sorted, BY_START);

        for (int i = 1; i < sorted.length; i++) {
            if (sorted[i][0] < sorted[i - 1][1]) return false;
        }
        return true;
    }

    // =====================================================================
    //  5. Meeting Rooms II  (LC 253) — sweep line
    // =====================================================================

    /**
     * <b>Meeting Rooms II</b> — minimum number of rooms needed.
     *
     * <p>Sweep line: sort starts and ends separately, then walk both arrays
     * with two pointers.</p>
     *
     * <ul>
     *   <li>If {@code starts[i] < ends[endPtr]}, a new room is required
     *       (the earliest-ended meeting is still in progress).</li>
     *   <li>Otherwise, the room from the earliest-ended meeting is freed
     *       and can be reused — advance {@code endPtr}.</li>
     * </ul>
     *
     * <p>Time: O(n log n) — Space: O(n)</p>
     */
    public static int minMeetingRooms(int[][] intervals) {
        int n = intervals.length;
        if (n == 0) return 0;

        int[] starts = new int[n];
        int[] ends   = new int[n];
        for (int i = 0; i < n; i++) {
            starts[i] = intervals[i][0];
            ends[i]   = intervals[i][1];
        }
        Arrays.sort(starts);
        Arrays.sort(ends);

        int rooms = 0;
        int endPtr = 0;
        for (int i = 0; i < n; i++) {
            if (starts[i] < ends[endPtr]) {
                rooms++;                          // need a new room
            } else {
                endPtr++;                         // reuse the freed room
            }
        }
        return rooms;
    }

    // =====================================================================
    //  6. Minimum Arrows to Burst Balloons  (LC 452) — sort by end
    // =====================================================================

    /**
     * <b>Minimum Number of Arrows to Burst Balloons</b> — given balloons
     * spanning {@code [xstart, xend]}, how many vertical arrows (each at a
     * single x-coordinate) are needed to burst all?
     *
     * <p>Same greedy shape as {@link #eraseOverlapIntervals(int[][])}:
     * sort by end, shoot the first balloon's end, then for every balloon
     * starting strictly after that x-coordinate, add a new arrow.</p>
     *
     * <p>Why "shoot at the end"? Any balloon {@code [s, e]} is hit by an
     * arrow at {@code x} iff {@code s <= x <= e}. Shooting at {@code e}
     * maximises the chance of also hitting later balloons, since their
     * start just needs to be {@code <= e}.</p>
     *
     * <p>Time: O(n log n) — Space: O(n) (clone)</p>
     */
    public static int findMinArrowShots(int[][] points) {
        if (points.length == 0) return 0;

        int[][] sorted = copy(points);
        Arrays.sort(sorted, BY_END);

        int arrows = 1;
        int shootAt = sorted[0][1];
        for (int i = 1; i < sorted.length; i++) {
            if (sorted[i][0] > shootAt) {
                arrows++;
                shootAt = sorted[i][1];
            }
        }
        return arrows;
    }

    // =====================================================================
    //  7. Interval List Intersections  (LC 986) — two-pointer
    // =====================================================================

    /**
     * <b>Interval List Intersections</b> — pairwise intersections of two
     * sorted, disjoint interval lists.
     *
     * <p>Two-pointer scan: for each pair {@code (A[i], B[j])}, the
     * intersection is {@code [max(start), min(end)]} — emit it if
     * {@code lo <= hi}. Advance whichever interval has the smaller end,
     * since it cannot intersect anything further.</p>
     *
     * <p>Time: O(|A| + |B|) — Space: O(|A| + |B|) output only</p>
     */
    public static int[][] intervalIntersection(int[][] firstList, int[][] secondList) {
        List<int[]> result = new ArrayList<>();
        int i = 0, j = 0;

        while (i < firstList.length && j < secondList.length) {
            int lo = Math.max(firstList[i][0], secondList[j][0]);
            int hi = Math.min(firstList[i][1], secondList[j][1]);
            if (lo <= hi) {
                result.add(new int[]{lo, hi});
            }
            if (firstList[i][1] < secondList[j][1]) i++;
            else                                    j++;
        }
        return result.toArray(new int[0][]);
    }
}