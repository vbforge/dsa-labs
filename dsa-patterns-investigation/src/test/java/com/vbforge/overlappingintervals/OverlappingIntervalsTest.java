package com.vbforge.overlappingintervals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Overlapping Intervals Pattern")
class OverlappingIntervalsTest {

    /**
     * Naïve merge oracle — paints every point on a <b>half-integer lattice</b>
     * (scaled by 2) and compresses contiguous runs.
     *
     * <p><b>Why scale by 2?</b> A plain integer lattice conflates "adjacent
     * integers" with "adjacent real points" — it would merge {@code [1,2]} and
     * {@code [3,4]} into {@code [1,4]}, which is wrong for closed intervals.
     * Scaling by 2 makes the point {@code 2.5} (scaled index 5) visible between
     * integers 2 and 3, so the gap is properly detected.</p>
     *
     * <p>Only valid for small bounded inputs — which is exactly what test cases
     * are. Never use this in production.</p>
     */
    private static int[][] bruteMerge(int[][] intervals) {
        if (intervals.length == 0) return new int[0][];

        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int[] iv : intervals) {
            min = Math.min(min, iv[0]);
            max = Math.max(max, iv[1]);
        }

        // Scaled lattice: point 2·x represents integer x, point 2·x+1 represents
        // the half-integer x + 0.5.
        int scaledMin = 2 * min;
        int scaledMax = 2 * max;
        boolean[] covered = new boolean[scaledMax - scaledMin + 1];

        for (int[] iv : intervals) {
            // Paint integers a..b inclusive, plus the half-integers strictly
            // between them, as one contiguous scaled run.
            for (int p = 2 * iv[0]; p <= 2 * iv[1]; p++) {
                covered[p - scaledMin] = true;
            }
        }

        List<int[]> result = new ArrayList<>();
        int i = 0;
        while (i < covered.length) {
            if (!covered[i]) { i++; continue; }
            int start = i;
            while (i < covered.length && covered[i]) i++;
            int end = i - 1;
            // Unscale: scaled index s → (s + scaledMin) / 2 = s/2 + min.
            // start and end are always even because intervals paint their integer
            // endpoints at even positions and half-integers at odd positions.
            result.add(new int[]{start / 2 + min, end / 2 + min});
        }
        return result.toArray(new int[0][]);
    }

    // ------------------------------------------------------------------
    // 1. Merge Intervals
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("merge (LC 56)")
    class MergeTests {

        @Test
        void classicExample() {
            // sort by start: [1,3],[2,6],[8,10],[15,18]
            //   merge 1: [1,3] & [2,6] → 2<=3 → [1,6]
            //   [8,10]: 8>6 → push [1,6], new current [8,10]
            //   [15,18]: 15>10 → push [8,10], new current [15,18]
            //   push [15,18]
            int[][] result = OverlappingIntervals.merge(
                    new int[][]{{1, 3}, {2, 6}, {8, 10}, {15, 18}});
            assertThat(result).isDeepEqualTo(new int[][]{
                    {1, 6}, {8, 10}, {15, 18}});
        }

        @Test
        void touchingIntervalsMerge() {
            // [1,4] & [4,5]: 4<=4 → merge to [1,5]
            int[][] result = OverlappingIntervals.merge(
                    new int[][]{{1, 4}, {4, 5}});
            assertThat(result).isDeepEqualTo(new int[][]{{1, 5}});
        }

        @Test
        void containment() {
            // sort by start: [1,4],[2,3]
            //   [1,4] & [2,3]: 2<=4 → end = max(4,3) = 4 → [1,4]
            int[][] result = OverlappingIntervals.merge(
                    new int[][]{{1, 4}, {2, 3}});
            assertThat(result).isDeepEqualTo(new int[][]{{1, 4}});
        }

        @Test
        void extendLeft() {
            // sort by start: [0,4],[1,4]
            //   [0,4] & [1,4]: 1<=4 → end = max(4,4) = 4 → [0,4]
            int[][] result = OverlappingIntervals.merge(
                    new int[][]{{1, 4}, {0, 4}});
            assertThat(result).isDeepEqualTo(new int[][]{{0, 4}});
        }

        @Test
        void disjoint() {
            // [1,2] & [3,4]: 3>2 → separate
            int[][] result = OverlappingIntervals.merge(
                    new int[][]{{1, 2}, {3, 4}});
            assertThat(result).isDeepEqualTo(new int[][]{{1, 2}, {3, 4}});
        }

        @Test
        void unsortedInput() {
            // sort by start: [1,10],[2,3],[4,5],[6,7]
            //   [1,10] absorbs everything (all starts <= 10)
            int[][] result = OverlappingIntervals.merge(
                    new int[][]{{2, 3}, {4, 5}, {6, 7}, {1, 10}});
            assertThat(result).isDeepEqualTo(new int[][]{{1, 10}});
        }

        @Test
        void singleInterval() {
            assertThat(OverlappingIntervals.merge(new int[][]{{1, 4}}))
                    .isDeepEqualTo(new int[][]{{1, 4}});
        }

        @Test
        void empty() {
            assertThat(OverlappingIntervals.merge(new int[0][]))
                    .isDeepEqualTo(new int[0][]);
        }

        @Test
        void doesNotMutateInput() {
            int[][] input = {{2, 6}, {1, 3}, {15, 18}, {8, 10}};
            int[][] before = {{2, 6}, {1, 3}, {15, 18}, {8, 10}};
            OverlappingIntervals.merge(input);
            assertThat(input).isDeepEqualTo(before);
        }

        /**
         * Cross-check: the fast implementation must agree with a naïve
         * brute-force oracle on every bundled case. This is the guard
         * against a bad "expected" value sneaking in unnoticed.
         */
        @Test
        void matchesBruteForceOracle() {
            int[][][] cases = {
                    {{1, 3}, {2, 6}, {8, 10}, {15, 18}},
                    {{1, 4}, {4, 5}},
                    {{1, 4}, {2, 3}},
                    {{1, 4}, {0, 4}},
                    {{1, 2}, {3, 4}},
                    {{2, 3}, {4, 5}, {6, 7}, {1, 10}},
                    {{0, 0}, {1, 1}, {2, 2}},
                    {{0, 2}, {1, 1}, {2, 4}},
                    {{5, 10}},
                    {}
            };
            for (int[][] c : cases) {
                assertThat(OverlappingIntervals.merge(c))
                        .as("input = " + Arrays.deepToString(c))
                        .isDeepEqualTo(bruteMerge(c));
            }
        }
    }

    // ------------------------------------------------------------------
    // 2. Insert Interval
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("insert (LC 57)")
    class InsertTests {

        @Test
        void classicExample() {
            // intervals = [[1,3],[6,9]], new = [2,5]
            //   phase 1: 3<2? no
            //   phase 2: [1,3] merges: new=[1,5]; [6,9]: 6<=5? no
            //   push [1,5], then [6,9]
            int[][] result = OverlappingIntervals.insert(
                    new int[][]{{1, 3}, {6, 9}}, new int[]{2, 5});
            assertThat(result).isDeepEqualTo(new int[][]{{1, 5}, {6, 9}});
        }

        @Test
        void mergesMultiple() {
            // [[1,2],[3,5],[6,7],[8,10],[12,16]], new = [4,8]
            //   phase 1: [1,2] kept (2<4)
            //   phase 2: [3,5] → new=[3,8]; [6,7] → new=[3,8];
            //            [8,10] → new=[3,10]; [12,16]: 12<=10? no
            //   push [3,10], then [12,16]
            int[][] result = OverlappingIntervals.insert(
                    new int[][]{{1, 2}, {3, 5}, {6, 7}, {8, 10}, {12, 16}},
                    new int[]{4, 8});
            assertThat(result).isDeepEqualTo(new int[][]{
                    {1, 2}, {3, 10}, {12, 16}});
        }

        @Test
        void insertedIntoEmpty() {
            assertThat(OverlappingIntervals.insert(new int[0][], new int[]{5, 7}))
                    .isDeepEqualTo(new int[][]{{5, 7}});
        }

        @Test
        void fullyContained() {
            // [1,5] fully absorbs [2,3] → [1,5]
            assertThat(OverlappingIntervals.insert(
                    new int[][]{{1, 5}}, new int[]{2, 3}))
                    .isDeepEqualTo(new int[][]{{1, 5}});
        }

        @Test
        void fullyContains() {
            // [2,3] nested in nothing → insert between? No — [1,5] is smaller.
            // [1,5] contains [2,3] → single result
            assertThat(OverlappingIntervals.insert(
                    new int[][]{{1, 5}}, new int[]{0, 6}))
                    .isDeepEqualTo(new int[][]{{0, 6}});
        }

        @Test
        void afterAll() {
            assertThat(OverlappingIntervals.insert(
                    new int[][]{{1, 5}}, new int[]{6, 8}))
                    .isDeepEqualTo(new int[][]{{1, 5}, {6, 8}});
        }

        @Test
        void beforeAll() {
            assertThat(OverlappingIntervals.insert(
                    new int[][]{{1, 5}}, new int[]{-1, 0}))
                    .isDeepEqualTo(new int[][]{{-1, 0}, {1, 5}});
        }

        @Test
        void touchesFirst() {
            // [1,5] & [5,7]: closed intervals touch at 5 → merge to [1,7]
            assertThat(OverlappingIntervals.insert(
                    new int[][]{{1, 5}}, new int[]{5, 7}))
                    .isDeepEqualTo(new int[][]{{1, 7}});
        }

        @Test
        void doesNotMutateNewInterval() {
            int[] newInterval = {2, 5};
            OverlappingIntervals.insert(new int[][]{{1, 3}, {6, 9}}, newInterval);
            assertThat(newInterval).containsExactly(2, 5);
        }
    }

    // ------------------------------------------------------------------
    // 3. Erase Overlap Intervals
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("eraseOverlapIntervals (LC 435)")
    class EraseOverlapTests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void eraseOverlap(int[][] intervals, int expected) {
            assertThat(OverlappingIntervals.eraseOverlapIntervals(intervals))
                    .isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // [[1,2],[2,3],[3,4],[1,3]]
                    //   sorted by end: [[1,2],[2,3],[1,3],[3,4]]
                    //   keep [1,2] (lastEnd=2)
                    //   [2,3]: 2>=2 keep (lastEnd=3)
                    //   [1,3]: 1<3 remove (1)
                    //   [3,4]: 3>=3 keep (lastEnd=4)
                    //   removed = 1
                    Arguments.of(new int[][]{{1, 2}, {2, 3}, {3, 4}, {1, 3}}, 1),

                    // [[1,2],[1,2],[1,2]] — keep one, remove two
                    Arguments.of(new int[][]{{1, 2}, {1, 2}, {1, 2}},         2),

                    // [[1,2],[2,3]] — touching = disjoint in half-open sense
                    Arguments.of(new int[][]{{1, 2}, {2, 3}},                 0),

                    // [[1,2]] — single interval, nothing to remove
                    Arguments.of(new int[][]{{1, 2}},                         0),

                    // empty
                    Arguments.of(new int[0][],                                0),

                    // [[1,100],[11,22],[1,11],[2,12]]
                    //   sorted by end: [[1,11],[2,12],[11,22],[1,100]]
                    //   keep [1,11] (lastEnd=11)
                    //   [2,12]: 2<11 remove (1)
                    //   [11,22]: 11>=11 keep (lastEnd=22)
                    //   [1,100]: 1<22 remove (2)
                    //   removed = 2
                    Arguments.of(new int[][]{{1, 100}, {11, 22}, {1, 11}, {2, 12}}, 2),

                    // Nested: [[1,10],[2,3],[3,4],[5,6]]
                    //   sorted by end: [[2,3],[3,4],[5,6],[1,10]]
                    //   keep [2,3]; [3,4] keep; [5,6] keep; [1,10]: 1<6 remove
                    //   removed = 1
                    Arguments.of(new int[][]{{1, 10}, {2, 3}, {3, 4}, {5, 6}}, 1)
            );
        }
    }

    // ------------------------------------------------------------------
    // 4. Meeting Rooms I
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("canAttendMeetings (LC 252)")
    class MeetingRoomsITests {

        @Test
        void overlapping() {
            // [0,30] & [5,10]: 5 < 30 → false
            assertThat(OverlappingIntervals.canAttendMeetings(
                    new int[][]{{0, 30}, {5, 10}, {15, 20}})).isFalse();
        }

        @Test
        void disjointAfterSort() {
            // sorted: [[2,4],[7,10]] → 7 >= 4 → true
            assertThat(OverlappingIntervals.canAttendMeetings(
                    new int[][]{{7, 10}, {2, 4}})).isTrue();
        }

        @Test
        void touchingEndpoints() {
            // [1,5] & [5,10]: 5 >= 5 → true (touching is OK)
            assertThat(OverlappingIntervals.canAttendMeetings(
                    new int[][]{{1, 5}, {5, 10}})).isTrue();
        }

        @Test
        void empty() {
            assertThat(OverlappingIntervals.canAttendMeetings(new int[0][])).isTrue();
        }

        @Test
        void singleMeeting() {
            assertThat(OverlappingIntervals.canAttendMeetings(
                    new int[][]{{1, 5}})).isTrue();
        }
    }

    // ------------------------------------------------------------------
    // 5. Meeting Rooms II
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("minMeetingRooms (LC 253)")
    class MeetingRoomsIITests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void minMeetingRooms(int[][] intervals, int expected) {
            assertThat(OverlappingIntervals.minMeetingRooms(intervals))
                    .isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // [[0,30],[5,10],[15,20]]
                    //   starts: [0,5,15]
                    //   ends:   [10,20,30]
                    //   i=0: 0<10 → rooms=1
                    //   i=1: 5<10 → rooms=2
                    //   i=2: 15<10? no → endPtr=1
                    //   rooms = 2
                    Arguments.of(new int[][]{{0, 30}, {5, 10}, {15, 20}}, 2),

                    // [[7,10],[2,4]]
                    //   starts: [2,7]; ends: [4,10]
                    //   i=0: 2<4 → rooms=1
                    //   i=1: 7<4? no → endPtr=1
                    //   rooms = 1
                    Arguments.of(new int[][]{{7, 10}, {2, 4}},            1),

                    // [[1,5],[5,10]]
                    //   starts: [1,5]; ends: [5,10]
                    //   i=0: 1<5 → rooms=1
                    //   i=1: 5<5? no → endPtr=1
                    //   rooms = 1 (touching means reuse)
                    Arguments.of(new int[][]{{1, 5}, {5, 10}},            1),

                    // [[1,5],[2,6],[3,7]] — all overlap pairwise
                    //   starts: [1,2,3]; ends: [5,6,7]
                    //   i=0: 1<5 → rooms=1
                    //   i=1: 2<5 → rooms=2
                    //   i=2: 3<5 → rooms=3
                    //   rooms = 3
                    Arguments.of(new int[][]{{1, 5}, {2, 6}, {3, 7}},     3),

                    // empty
                    Arguments.of(new int[0][],                            0)
            );
        }
    }

    // ------------------------------------------------------------------
    // 6. Minimum Arrows to Burst Balloons
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("findMinArrowShots (LC 452)")
    class MinArrowsTests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void findMinArrowShots(int[][] points, int expected) {
            assertThat(OverlappingIntervals.findMinArrowShots(points))
                    .isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // [[10,16],[2,8],[1,6],[7,12]]
                    //   sorted by end: [[1,6],[2,8],[7,12],[10,16]]
                    //   arrows=1, shootAt=6
                    //   [2,8]: 2>6? no → no new arrow
                    //   [7,12]: 7>6? yes → arrows=2, shootAt=12
                    //   [10,16]: 10>12? no → no new arrow
                    //   answer = 2
                    Arguments.of(new int[][]{{10, 16}, {2, 8}, {1, 6}, {7, 12}}, 2),

                    // [[1,2],[3,4],[5,6],[7,8]] — all disjoint
                    //   sorted same; each start > last end → 4 arrows
                    Arguments.of(new int[][]{{1, 2}, {3, 4}, {5, 6}, {7, 8}},   4),

                    // [[1,2],[2,3],[3,4],[4,5]] — touching
                    //   sorted by end: same order
                    //   shootAt=2; [2,3]: 2>2? no → hit; [3,4]: 3>2 yes → new, shootAt=4
                    //   [4,5]: 4>4? no → hit
                    //   answer = 2
                    Arguments.of(new int[][]{{1, 2}, {2, 3}, {3, 4}, {4, 5}},   2),

                    // [[1,2]] — single balloon
                    Arguments.of(new int[][]{{1, 2}},                            1),

                    // empty
                    Arguments.of(new int[0][],                                   0)
            );
        }
    }

    // ------------------------------------------------------------------
    // 7. Interval List Intersections
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("intervalIntersection (LC 986)")
    class IntervalIntersectionTests {

        @Test
        void classicExample() {
            // A = [[0,2],[5,10],[13,23],[24,25]]
            // B = [[1,5],[8,12],[15,24],[25,26]]
            //   A0[0,2] vs B0[1,5]: lo=1, hi=2 → [1,2]; end(2)<end(5)→i++
            //   A1[5,10] vs B0[1,5]: lo=5, hi=5 → [5,5]; end(10)>end(5)→j++
            //   A1[5,10] vs B1[8,12]: lo=8, hi=10 → [8,10]; end(10)<end(12)→i++
            //   A2[13,23] vs B1[8,12]: lo=13,hi=12 → empty; end(23)>end(12)→j++
            //   A2[13,23] vs B2[15,24]: lo=15,hi=23 → [15,23]; end(23)<end(24)→i++
            //   A3[24,25] vs B2[15,24]: lo=24,hi=24 → [24,24]; end(25)>end(24)→j++
            //   A3[24,25] vs B3[25,26]: lo=25,hi=25 → [25,25]; end(25)<end(26)→i++
            //   i=4 → exit
            int[][] result = OverlappingIntervals.intervalIntersection(
                    new int[][]{{0, 2}, {5, 10}, {13, 23}, {24, 25}},
                    new int[][]{{1, 5}, {8, 12}, {15, 24}, {25, 26}});
            assertThat(result).isDeepEqualTo(new int[][]{
                    {1, 2}, {5, 5}, {8, 10}, {15, 23}, {24, 24}, {25, 25}});
        }

        @Test
        void noIntersections() {
            // A=[1,3], B=[5,7]: lo=5,hi=3 → empty; end(3)<end(7)→i++; i=1→exit
            assertThat(OverlappingIntervals.intervalIntersection(
                    new int[][]{{1, 3}}, new int[][]{{5, 7}}))
                    .isDeepEqualTo(new int[0][]);
        }

        @Test
        void firstListEmpty() {
            assertThat(OverlappingIntervals.intervalIntersection(
                    new int[0][], new int[][]{{1, 5}}))
                    .isDeepEqualTo(new int[0][]);
        }

        @Test
        void secondListEmpty() {
            assertThat(OverlappingIntervals.intervalIntersection(
                    new int[][]{{1, 5}}, new int[0][]))
                    .isDeepEqualTo(new int[0][]);
        }

        @Test
        void singleIntersection() {
            // A=[1,7], B=[3,10]: lo=3,hi=7 → [3,7]
            assertThat(OverlappingIntervals.intervalIntersection(
                    new int[][]{{1, 7}}, new int[][]{{3, 10}}))
                    .isDeepEqualTo(new int[][]{{3, 7}});
        }
    }
}