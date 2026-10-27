package com.vbforge.monotonicstack;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Monotonic Stack Pattern")
class MonotonicStackTest {

    // ------------------------------------------------------------------
    // 1. Next Greater Element I
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("nextGreaterElement (LC 496)")
    class NextGreaterElementTests {

        @Test
        void classic() {
            // nums2 = [1,3,4,2] → map: {1→3, 3→4}
            // nums1 = [4,1,2]   → [-1, 3, -1]
            assertThat(MonotonicStack.nextGreaterElement(
                    new int[]{4, 1, 2}, new int[]{1, 3, 4, 2}))
                    .containsExactly(-1, 3, -1);
        }

        @Test
        void ascendingSubset() {
            // nums2 = [1,2,3,4] → map: {1→2, 2→3, 3→4}
            // nums1 = [2,4]     → [3, -1]
            assertThat(MonotonicStack.nextGreaterElement(
                    new int[]{2, 4}, new int[]{1, 2, 3, 4}))
                    .containsExactly(3, -1);
        }

        @Test
        void singleValue() {
            assertThat(MonotonicStack.nextGreaterElement(
                    new int[]{1}, new int[]{1}))
                    .containsExactly(-1);
        }

        @Test
        void descendingSourceNoGreater() {
            // nums2 = [3,2,1] → no element has a greater successor
            assertThat(MonotonicStack.nextGreaterElement(
                    new int[]{3, 2, 1}, new int[]{3, 2, 1}))
                    .containsExactly(-1, -1, -1);
        }
    }

    // ------------------------------------------------------------------
    // 2. Next Greater Element II  (circular)
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("nextGreaterCircular (LC 503)")
    class NextGreaterCircularTests {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void nextGreaterCircular(int[] nums, int[] expected) {
            assertThat(MonotonicStack.nextGreaterCircular(nums))
                    .containsExactly(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // [1,2,1]: idx0←2, idx1←-1, idx2←2 (wrap)
                    Arguments.of(new int[]{1, 2, 1},     new int[]{2, -1, 2}),
                    // [1,2,3,4,3]: 1←2, 2←3, 3←4, 4←-1, 3←4 (wrap)
                    Arguments.of(new int[]{1, 2, 3, 4, 3}, new int[]{2, 3, 4, -1, 4}),
                    // single element — can't wrap to itself strictly
                    Arguments.of(new int[]{5},           new int[]{-1}),
                    // all equal — never strictly greater
                    Arguments.of(new int[]{3, 3, 3},     new int[]{-1, -1, -1})
            );
        }
    }

    // ------------------------------------------------------------------
    // 3. Daily Temperatures
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("dailyTemperatures (LC 739)")
    class DailyTemperaturesTests {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void dailyTemperatures(int[] temps, int[] expected) {
            assertThat(MonotonicStack.dailyTemperatures(temps))
                    .containsExactly(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // Classic: warms at +1,+1,+4,+2,+1,+1,ends,ends
                    Arguments.of(new int[]{73, 74, 75, 71, 69, 72, 76, 73},
                                 new int[]{1, 1, 4, 2, 1, 1, 0, 0}),
                    // Strictly ascending → every gap is 1
                    Arguments.of(new int[]{30, 40, 50, 60},
                                 new int[]{1, 1, 1, 0}),
                    // Strictly descending → nobody warms
                    Arguments.of(new int[]{90, 80, 70, 60},
                                 new int[]{0, 0, 0, 0}),
                    // All equal → never strictly warmer
                    Arguments.of(new int[]{50, 50, 50},
                                 new int[]{0, 0, 0}),
                    // Single element
                    Arguments.of(new int[]{30},
                                 new int[]{0})
            );
        }
    }

    // ------------------------------------------------------------------
    // 4. Largest Rectangle in Histogram
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("largestRectangleArea (LC 84)")
    class LargestRectangleTests {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void largestRectangle(int[] heights, int expected) {
            assertThat(MonotonicStack.largestRectangleArea(heights)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // [2,1,5,6,2,3]: 5 & 6 give 5*2=10
                    Arguments.of(new int[]{2, 1, 5, 6, 2, 3},        10),
                    // [2,4]: full width × height 2 = 4
                    Arguments.of(new int[]{2, 4},                     4),
                    // [6,2,5,4,5,1,6]: middle 5,4,5 → 4*3=12
                    Arguments.of(new int[]{6, 2, 5, 4, 5, 1, 6},     12),
                    // single bar
                    Arguments.of(new int[]{5},                        5),
                    // all equal — full width × height
                    Arguments.of(new int[]{3, 3, 3, 3},               12)
            );
        }

        @Test
        void empty() {
            assertThat(MonotonicStack.largestRectangleArea(new int[0])).isZero();
        }
    }

    // ------------------------------------------------------------------
    // 5. Trapping Rain Water
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("trap (LC 42)")
    class TrapTests {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void trap(int[] height, int expected) {
            assertThat(MonotonicStack.trap(height)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // Classic — 6 units trapped
                    Arguments.of(new int[]{0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1}, 6),
                    // Basin — 9 units: 2 + 2 + 1 + 4
                    Arguments.of(new int[]{4, 2, 0, 3, 2, 5},                   9),
                    // Single valley — 5 units
                    Arguments.of(new int[]{5, 0, 5},                             5),
                    // All same — no water
                    Arguments.of(new int[]{5, 5, 5},                             0),
                    // Monotonic — no water
                    Arguments.of(new int[]{1, 2, 3, 4, 5},                       0),
                    // Single element
                    Arguments.of(new int[]{5},                                   0),
                    // Empty
                    Arguments.of(new int[0],                                     0)
            );
        }
    }

    // ------------------------------------------------------------------
    // 6. Sliding Window Maximum
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("maxSlidingWindow (LC 239)")
    class SlidingWindowMaximumTests {

        @Test
        void classic() {
            // [1,3,-1,-3,5,3,6,7], k=3 → [3,3,5,5,6,7]
            assertThat(MonotonicStack.maxSlidingWindow(
                    new int[]{1, 3, -1, -3, 5, 3, 6, 7}, 3))
                    .containsExactly(3, 3, 5, 5, 6, 7);
        }

        @Test
        void kEqualsOne() {
            assertThat(MonotonicStack.maxSlidingWindow(new int[]{1, -1}, 1))
                    .containsExactly(1, -1);
        }

        @Test
        void kEqualsLength() {
            assertThat(MonotonicStack.maxSlidingWindow(new int[]{9, 11}, 2))
                    .containsExactly(11);
        }

        @Test
        void singleElement() {
            assertThat(MonotonicStack.maxSlidingWindow(new int[]{1}, 1))
                    .containsExactly(1);
        }

        @Test
        void rejectsInvalidK() {
            assertThatThrownBy(() -> MonotonicStack.maxSlidingWindow(new int[]{1}, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> MonotonicStack.maxSlidingWindow(new int[]{1}, 2))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ------------------------------------------------------------------
    // 7. Sum of Subarray Minimums
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("sumSubarrayMins (LC 907)")
    class SumSubarrayMinsTests {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void sumSubarrayMins(int[] nums, int expected) {
            assertThat(MonotonicStack.sumSubarrayMins(nums)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // [3,1,2,4]: 3 + 6 + 4 + 4 = 17
                    Arguments.of(new int[]{3, 1, 2, 4},             17),
                    // [11,81,94,43,3]: 44 + 162 + 94 + 129 + 15 = 444
                    Arguments.of(new int[]{11, 81, 94, 43, 3},      444),
                    // single element
                    Arguments.of(new int[]{1},                       1),
                    // duplicates [1,1]: [1] + [1] + [1,1] = 3
                    Arguments.of(new int[]{1, 1},                    3),
                    // all equal [2,2]: 2 + 2 + 4 = 6  (three subarrays, mins: 2+2+2)
                    Arguments.of(new int[]{2, 2},                    6)
            );
        }
    }
}