package com.vbforge.binarysearch;

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

@DisplayName("Binary Search Pattern")
class BinarySearchTest {

    // ------------------------------------------------------------------
    // 1. Classic exact match
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Classic binarySearch")
    class ClassicTests {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void binarySearch(int[] nums, int target, int expected) {
            assertThat(BinarySearch.binarySearch(nums, target)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of(new int[]{-1, 0, 3, 5, 9, 12}, 9,  4),
                    Arguments.of(new int[]{-1, 0, 3, 5, 9, 12}, -1, 0),
                    Arguments.of(new int[]{-1, 0, 3, 5, 9, 12}, 12, 5),
                    Arguments.of(new int[]{-1, 0, 3, 5, 9, 12}, 2,  -1),
                    Arguments.of(new int[]{1},                   1,  0),
                    Arguments.of(new int[]{1},                   0,  -1),
                    Arguments.of(new int[]{},                    1,  -1)
            );
        }
    }

    // ------------------------------------------------------------------
    // 2. Bounds
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("lowerBound / upperBound / searchInsert")
    class BoundTests {

        @Test
        void lowerBoundOnDuplicates() {
            int[] a = {1, 2, 4, 4, 4, 5, 7};
            assertThat(BinarySearch.lowerBound(a, 4)).isEqualTo(2);
            assertThat(BinarySearch.lowerBound(a, 3)).isEqualTo(2);   // insertion point
            assertThat(BinarySearch.lowerBound(a, 0)).isZero();
            assertThat(BinarySearch.lowerBound(a, 8)).isEqualTo(7);   // = length
        }

        @Test
        void upperBoundOnDuplicates() {
            int[] a = {1, 2, 4, 4, 4, 5, 7};
            assertThat(BinarySearch.upperBound(a, 4)).isEqualTo(5);
            assertThat(BinarySearch.upperBound(a, 3)).isEqualTo(2);
            assertThat(BinarySearch.upperBound(a, 7)).isEqualTo(7);
        }

        @ParameterizedTest(name = "nums={0}, target={1} -> {2}")
        @MethodSource("insertCases")
        void searchInsert(int[] nums, int target, int expected) {
            assertThat(BinarySearch.searchInsert(nums, target)).isEqualTo(expected);
        }

        static Stream<Arguments> insertCases() {
            return Stream.of(
                    Arguments.of(new int[]{1, 3, 5, 6}, 5, 2),
                    Arguments.of(new int[]{1, 3, 5, 6}, 2, 1),
                    Arguments.of(new int[]{1, 3, 5, 6}, 7, 4),
                    Arguments.of(new int[]{1, 3, 5, 6}, 0, 0)
            );
        }
    }

    // ------------------------------------------------------------------
    // 3. Search range (first & last)
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("searchRange")
    class SearchRangeTests {

        @Test
        void found() {
            assertThat(BinarySearch.searchRange(new int[]{5, 7, 7, 8, 8, 10}, 8))
                    .containsExactly(3, 4);
        }

        @Test
        void singleOccurrence() {
            assertThat(BinarySearch.searchRange(new int[]{5, 7, 7, 8, 8, 10}, 5))
                    .containsExactly(0, 0);
        }

        @Test
        void notPresent() {
            assertThat(BinarySearch.searchRange(new int[]{5, 7, 7, 8, 8, 10}, 6))
                    .containsExactly(-1, -1);
        }

        @Test
        void emptyArray() {
            assertThat(BinarySearch.searchRange(new int[0], 0)).containsExactly(-1, -1);
        }

        @Test
        void wholeArray() {
            assertThat(BinarySearch.searchRange(new int[]{2, 2, 2}, 2))
                    .containsExactly(0, 2);
        }
    }

    // ------------------------------------------------------------------
    // 4. Rotated array
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Rotated sorted array")
    class RotatedTests {

        @ParameterizedTest(name = "search({0}, {1}) -> {2}")
        @MethodSource("searchCases")
        void searchRotated(int[] nums, int target, int expected) {
            assertThat(BinarySearch.searchRotated(nums, target)).isEqualTo(expected);
        }

        static Stream<Arguments> searchCases() {
            int[] a = {4, 5, 6, 7, 0, 1, 2};
            return Stream.of(
                    Arguments.of(a, 0,  4),
                    Arguments.of(a, 3,  -1),
                    Arguments.of(a, 4,  0),
                    Arguments.of(a, 2,  6),
                    Arguments.of(new int[]{1},        0, -1),
                    Arguments.of(new int[]{1},        1,  0),
                    Arguments.of(new int[]{3, 1},     1,  1),
                    Arguments.of(new int[]{5, 1, 3},  3,  2)
            );
        }

        @ParameterizedTest(name = "findMin({0}) -> {1}")
        @MethodSource("minCases")
        void findMinRotated(int[] nums, int expected) {
            assertThat(BinarySearch.findMinRotated(nums)).isEqualTo(expected);
        }

        static Stream<Arguments> minCases() {
            return Stream.of(
                    Arguments.of(new int[]{3, 4, 5, 1, 2},       1),
                    Arguments.of(new int[]{4, 5, 6, 7, 0, 1, 2}, 0),
                    Arguments.of(new int[]{11, 13, 15, 17},      11),   // not rotated
                    Arguments.of(new int[]{2, 1},                1),
                    Arguments.of(new int[]{1},                   1),
                    Arguments.of(new int[]{1, 1, 1, 0, 1},       0)     // with duplicates
            );
        }

        @Test
        void findMinRejectsEmpty() {
            assertThatThrownBy(() -> BinarySearch.findMinRotated(new int[0]))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ------------------------------------------------------------------
    // 5. Peak
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("findPeakElement")
    class PeakTests {

        @Test
        void findsPeakInMiddle() {
            int[] nums = {1, 2, 3, 1};
            int idx = BinarySearch.findPeakElement(nums);
            assertThat(idx).isEqualTo(2);
        }

        @Test
        void peakAtStart() {
            assertThat(BinarySearch.findPeakElement(new int[]{5, 4, 3, 2, 1})).isZero();
        }

        @Test
        void peakAtEnd() {
            assertThat(BinarySearch.findPeakElement(new int[]{1, 2, 3, 4, 5})).isEqualTo(4);
        }

        @Test
        void singleElement() {
            assertThat(BinarySearch.findPeakElement(new int[]{7})).isZero();
        }

        @Test
        void emptyRejected() {
            assertThatThrownBy(() -> BinarySearch.findPeakElement(new int[0]))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ------------------------------------------------------------------
    // 6. Answer-space: sqrt & Koko
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("sqrt (integer)")
    class SqrtTests {

        @ParameterizedTest(name = "sqrt({0}) -> {1}")
        @CsvSource({
                "0, 0",
                "1, 1",
                "2, 1",
                "3, 1",
                "4, 2",
                "8, 2",
                "9, 3",
                "15, 3",
                "16, 4",
                "2147395599, 46339"
        })
        void sqrt(int x, int expected) {
            assertThat(BinarySearch.sqrt(x)).isEqualTo(expected);
        }

        @Test
        void negativeRejected() {
            assertThatThrownBy(() -> BinarySearch.sqrt(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Koko Eating Bananas")
    class KokoTests {

        @Test
        void classic() {
            assertThat(BinarySearch.minEatingSpeed(new int[]{3, 6, 7, 11}, 8)).isEqualTo(4);
        }

        @Test
        void exactHours() {
            assertThat(BinarySearch.minEatingSpeed(new int[]{30, 11, 23, 4, 20}, 5)).isEqualTo(30);
        }

        @Test
        void generousHours() {
            assertThat(BinarySearch.minEatingSpeed(new int[]{30, 11, 23, 4, 20}, 6)).isEqualTo(23);
        }

        @Test
        void singlePile() {
            assertThat(BinarySearch.minEatingSpeed(new int[]{10}, 1)).isEqualTo(10);
            assertThat(BinarySearch.minEatingSpeed(new int[]{10}, 4)).isEqualTo(3);
        }

        @Test
        void rejectsImpossible() {
            assertThatThrownBy(() -> BinarySearch.minEatingSpeed(new int[]{1, 1, 1}, 2))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ------------------------------------------------------------------
    // 7. 2-D matrix
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("searchMatrix (virtual 1-D)")
    class MatrixTests {

        private static final int[][] MATRIX = {
                {1,  3,  5,  7},
                {10, 11, 16, 20},
                {23, 30, 34, 60}
        };

        @Test
        void present() {
            assertThat(BinarySearch.searchMatrix(MATRIX, 3)).isTrue();
            assertThat(BinarySearch.searchMatrix(MATRIX, 60)).isTrue();
        }

        @Test
        void absent() {
            assertThat(BinarySearch.searchMatrix(MATRIX, 13)).isFalse();
        }

        @Test
        void emptyMatrix() {
            assertThat(BinarySearch.searchMatrix(new int[0][], 1)).isFalse();
            assertThat(BinarySearch.searchMatrix(new int[][]{{}}, 1)).isFalse();
        }
    }

    // ------------------------------------------------------------------
    // 8. Median of two sorted arrays
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("findMedianSortedArrays")
    class MedianTests {

        @ParameterizedTest(name = "a={0}, b={1} -> {2}")
        @MethodSource("medianCases")
        void median(int[] a, int[] b, double expected) {
            assertThat(BinarySearch.findMedianSortedArrays(a, b)).isEqualTo(expected);
        }

        static Stream<Arguments> medianCases() {
            return Stream.of(
                    Arguments.of(new int[]{1, 3},       new int[]{2},         2.0),
                    Arguments.of(new int[]{1, 2},       new int[]{3, 4},      2.5),
                    Arguments.of(new int[]{},           new int[]{1},         1.0),
                    Arguments.of(new int[]{2},          new int[]{},          2.0),
                    Arguments.of(new int[]{1, 2, 3, 4}, new int[]{},          2.5),
                    Arguments.of(new int[]{1, 5, 9},    new int[]{2, 3, 8},   4.0),
                    Arguments.of(new int[]{1, 3, 5},    new int[]{2, 4, 6},   3.5)  //merged: [1, 2, 3, 4, 5, 6] → (3 + 4) / 2.0 = 3.5  --> explicit 3.5 fractional case
            );
        }
    }

    // ------------------------------------------------------------------
    // 9. JDK interop (documentation test)
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("JDK binarySearch interop")
    class JdkInteropTests {

        @Test
        void foundMatchesIndex() {
            assertThat(BinarySearch.jdkBinarySearch(new int[]{1, 3, 5, 7}, 5)).isEqualTo(2);
        }

        @Test
        void notFoundReturnsNegativeEncoding() {
            // target 4 → insertion point 2 → returns -(2) - 1 = -3
            assertThat(BinarySearch.jdkBinarySearch(new int[]{1, 3, 5, 7}, 4)).isEqualTo(-3);
        }
    }
}