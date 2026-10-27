package com.vbforge.twopointers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Two Pointers Pattern")
class TwoPointersTest {

    // ------------------------------------------------------------------
    // 1. Two Sum II
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Two Sum II (sorted)")
    class TwoSumIITests {

        @Test
        void findsPair() {
            assertThat(TwoPointers.twoSumSorted(new int[]{2, 7, 11, 15}, 9))
                    .containsExactly(1, 2);
        }

        @Test
        void findsPairAtEnds() {
            assertThat(TwoPointers.twoSumSorted(new int[]{1, 2, 3, 4, 5}, 6))
                    .containsExactly(1, 5);
        }

        @Test
        void returnsEmptyWhenNoPair() {
            assertThat(TwoPointers.twoSumSorted(new int[]{1, 2, 3}, 100)).isEmpty();
        }
    }

    // ------------------------------------------------------------------
    // 2. Valid Palindrome
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Valid Palindrome")
    class ValidPalindromeTests {

        @ParameterizedTest(name = "[{index}] \"{0}\" -> true")
        @ValueSource(strings = {
                "A man, a plan, a canal: Panama",
                " ",
                "a",
                "abba",
                "Aa",
                ".,"
        })
        void isPalindrome(String s) {
            assertThat(TwoPointers.isPalindrome(s)).isTrue();
        }

        @ParameterizedTest(name = "[{index}] \"{0}\" -> false")
        @ValueSource(strings = {"race a car", "ab", "0P"})
        void isNotPalindrome(String s) {
            assertThat(TwoPointers.isPalindrome(s)).isFalse();
        }
    }

    // ------------------------------------------------------------------
    // 3. Container With Most Water
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Container With Most Water")
    class MaxAreaTests {

        @Test
        void classicExample() {
            assertThat(TwoPointers.maxArea(new int[]{1, 8, 6, 2, 5, 4, 8, 3, 7}))
                    .isEqualTo(49);
        }

        @Test
        void twoLines() {
            assertThat(TwoPointers.maxArea(new int[]{1, 1})).isEqualTo(1);
        }

        @Test
        void bestAtEdges() {
            assertThat(TwoPointers.maxArea(new int[]{4, 3, 2, 1, 4})).isEqualTo(16);
        }
    }

    // ------------------------------------------------------------------
    // 4. Remove Duplicates
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Remove Duplicates (sorted)")
    class RemoveDuplicatesTests {

        @Test
        void removesDuplicates() {
            int[] nums = {1, 1, 2};
            int k = TwoPointers.removeDuplicates(nums);
            assertThat(k).isEqualTo(2);
            assertThat(Arrays.copyOf(nums, k)).containsExactly(1, 2);
        }

        @Test
        void removesMoreDuplicates() {
            int[] nums = {0, 0, 1, 1, 1, 2, 2, 3, 3, 4};
            int k = TwoPointers.removeDuplicates(nums);
            assertThat(k).isEqualTo(5);
            assertThat(Arrays.copyOf(nums, k)).containsExactly(0, 1, 2, 3, 4);
        }

        @Test
        void emptyArray() {
            assertThat(TwoPointers.removeDuplicates(new int[0])).isZero();
        }
    }

    // ------------------------------------------------------------------
    // 5. Move Zeroes
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Move Zeroes")
    class MoveZeroesTests {

        @Test
        void movesZeroesToEnd() {
            int[] nums = {0, 1, 0, 3, 12};
            TwoPointers.moveZeroes(nums);
            assertThat(nums).containsExactly(1, 3, 12, 0, 0);
        }

        @Test
        void noZeroes() {
            int[] nums = {1, 2, 3};
            TwoPointers.moveZeroes(nums);
            assertThat(nums).containsExactly(1, 2, 3);
        }

        @Test
        void allZeroes() {
            int[] nums = {0, 0, 0};
            TwoPointers.moveZeroes(nums);
            assertThat(nums).containsExactly(0, 0, 0);
        }
    }

    // ------------------------------------------------------------------
    // 6. Reverse String
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Reverse String")
    class ReverseStringTests {

        @Test
        void reversesOddLength() {
            char[] s = "hello".toCharArray();
            TwoPointers.reverseString(s);
            assertThat(new String(s)).isEqualTo("olleh");
        }

        @Test
        void reversesEvenLength() {
            char[] s = "abcd".toCharArray();
            TwoPointers.reverseString(s);
            assertThat(new String(s)).isEqualTo("dcba");
        }
    }

    // ------------------------------------------------------------------
    // 7. Merge Sorted Array
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Merge Sorted Array")
    class MergeSortedTests {

        @Test
        void mergesTwoArrays() {
            int[] nums1 = {1, 2, 3, 0, 0, 0};
            TwoPointers.mergeSorted(nums1, 3, new int[]{2, 5, 6}, 3);
            assertThat(nums1).containsExactly(1, 2, 2, 3, 5, 6);
        }

        @Test
        void nums2Empty() {
            int[] nums1 = {1};
            TwoPointers.mergeSorted(nums1, 1, new int[0], 0);
            assertThat(nums1).containsExactly(1);
        }

        @Test
        void nums1Empty() {
            int[] nums1 = {0};
            TwoPointers.mergeSorted(nums1, 0, new int[]{1}, 1);
            assertThat(nums1).containsExactly(1);
        }
    }

    // ------------------------------------------------------------------
    // 8. 3Sum
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("3Sum")
    class ThreeSumTests {

        @Test
        void findsAllTriplets() {
            assertThat(TwoPointers.threeSum(new int[]{-1, 0, 1, 2, -1, -4}))
                    .containsExactlyInAnyOrder(
                            List.of(-1, -1, 2),
                            List.of(-1, 0, 1));
        }

        @Test
        void noTriplets() {
            assertThat(TwoPointers.threeSum(new int[]{1, 2, 3})).isEmpty();
        }

        @Test
        void allZeroes() {
            assertThat(TwoPointers.threeSum(new int[]{0, 0, 0}))
                    .containsExactly(List.of(0, 0, 0));
        }
    }

    // ------------------------------------------------------------------
    // 9. Sort Colors (Dutch National Flag)
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Sort Colors (Dutch National Flag)")
    class SortColorsTests {

        @Test
        void sortsMixedColors() {
            int[] nums = {2, 0, 2, 1, 1, 0};
            TwoPointers.sortColors(nums);
            assertThat(nums).containsExactly(0, 0, 1, 1, 2, 2);
        }

        @Test
        void alreadySorted() {
            int[] nums = {0, 1, 2};
            TwoPointers.sortColors(nums);
            assertThat(nums).containsExactly(0, 1, 2);
        }

        @Test
        void singleColor() {
            int[] nums = {1, 1, 1};
            TwoPointers.sortColors(nums);
            assertThat(nums).containsExactly(1, 1, 1);
        }
    }
}