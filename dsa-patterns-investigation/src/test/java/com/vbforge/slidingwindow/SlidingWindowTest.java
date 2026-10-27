package com.vbforge.slidingwindow;

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

@DisplayName("Sliding Window Pattern")
class SlidingWindowTest {

    // ------------------------------------------------------------------
    // 1. Max Sum Subarray of Size K
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Max Sum Subarray of Size K (fixed window)")
    class MaxSumSubarrayTests {

        @Test
        void findsMaxSum() {
            assertThat(SlidingWindow.maxSumSubarray(new int[]{2, 1, 5, 1, 3, 2}, 3))
                    .isEqualTo(9);
        }

        @Test
        void windowEqualsArray() {
            assertThat(SlidingWindow.maxSumSubarray(new int[]{1, 2, 3}, 3)).isEqualTo(6);
        }

        @Test
        void handlesNegatives() {
            assertThat(SlidingWindow.maxSumSubarray(new int[]{-1, -2, -3, -4}, 2))
                    .isEqualTo(-3);
        }

        @Test
        void rejectsInvalidK() {
            assertThatThrownBy(() -> SlidingWindow.maxSumSubarray(new int[]{1, 2}, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> SlidingWindow.maxSumSubarray(new int[]{1, 2}, 3))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ------------------------------------------------------------------
    // 2. Find All Anagrams
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Find All Anagrams in a String (fixed window)")
    class FindAnagramsTests {

        @Test
        void findsAnagramStarts() {
            assertThat(SlidingWindow.findAnagrams("cbaebabacd", "abc"))
                    .containsExactly(0, 6);
        }

        @Test
        void overlappingAnagrams() {
            assertThat(SlidingWindow.findAnagrams("abab", "ab"))
                    .containsExactly(0, 1, 2);
        }

        @Test
        void noAnagrams() {
            assertThat(SlidingWindow.findAnagrams("hello", "xyz")).isEmpty();
        }

        @Test
        void patternLongerThanSource() {
            assertThat(SlidingWindow.findAnagrams("ab", "abc")).isEmpty();
        }
    }

    // ------------------------------------------------------------------
    // 3. Longest Substring Without Repeating Characters
    //    NOTE: use @MethodSource — @CsvSource collapses "" → null.
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Longest Substring Without Repeating Characters")
    class LongestSubstringTests {

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of("abcabcbb", 3),   // "abc"
                    Arguments.of("bbbbb",    1),   // "b"
                    Arguments.of("pwwkew",   3),   // "wke"
                    Arguments.of("",         0),   // empty input
                    Arguments.of(" ",        1),   // single space
                    Arguments.of("dvdf",     3)    // "vdf"
            );
        }

        @ParameterizedTest(name = "\"{0}\" -> {1}")
        @MethodSource("cases")
        void longestSubstring(String s, int expected) {
            assertThat(SlidingWindow.lengthOfLongestSubstring(s)).isEqualTo(expected);
        }
    }

    // ------------------------------------------------------------------
    // 4. Minimum Size Subarray Sum
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Minimum Size Subarray Sum")
    class MinSubArrayLenTests {

        @Test
        void findsMinimum() {
            assertThat(SlidingWindow.minSubArrayLen(7, new int[]{2, 3, 1, 2, 4, 3}))
                    .isEqualTo(2);
        }

        @Test
        void singleElementSuffices() {
            assertThat(SlidingWindow.minSubArrayLen(4, new int[]{1, 4, 4})).isEqualTo(1);
        }

        @Test
        void impossible() {
            assertThat(SlidingWindow.minSubArrayLen(11, new int[]{1, 1, 1, 1, 1, 1, 1, 1}))
                    .isZero();
        }
    }

    // ------------------------------------------------------------------
    // 5. Longest Repeating Character Replacement
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Longest Repeating Character Replacement")
    class CharacterReplacementTests {

        @ParameterizedTest(name = "\"{0}\", k={1} -> {2}")
        @CsvSource({
                "ABAB,    2, 4",
                "AABABBA, 1, 4",
                "AAAA,    0, 4",
                "ABCDE,   1, 2"
        })
        void characterReplacement(String s, int k, int expected) {
            assertThat(SlidingWindow.characterReplacement(s, k)).isEqualTo(expected);
        }
    }

    // ------------------------------------------------------------------
    // 6. Max Consecutive Ones III
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Max Consecutive Ones III")
    class LongestOnesTests {

        @Test
        void classicExample() {
            int[] nums = {1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 0};
            assertThat(SlidingWindow.longestOnes(nums, 2)).isEqualTo(6);
        }

        @Test
        void kZeroesAllowed() {
            int[] nums = {0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1};
            assertThat(SlidingWindow.longestOnes(nums, 3)).isEqualTo(10);
        }

        @Test
        void noFlipsNeeded() {
            assertThat(SlidingWindow.longestOnes(new int[]{1, 1, 1, 1}, 0)).isEqualTo(4);
        }

        @Test
        void allZeros() {
            assertThat(SlidingWindow.longestOnes(new int[]{0, 0, 0}, 2)).isEqualTo(2);
        }
    }

    // ------------------------------------------------------------------
    // 7. Fruit Into Baskets
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Fruit Into Baskets (at most 2 distinct)")
    class TotalFruitTests {

        @Test
        void twoDistinctFromStart() {
            assertThat(SlidingWindow.totalFruit(new int[]{1, 2, 1})).isEqualTo(3);
        }

        @Test
        void secondFruitLaterInWindow() {
            assertThat(SlidingWindow.totalFruit(new int[]{0, 1, 2, 2})).isEqualTo(3);
        }

        @Test
        void longestMixedRun() {
            assertThat(SlidingWindow.totalFruit(new int[]{1, 2, 3, 2, 2})).isEqualTo(4);
        }

        @Test
        void singleType() {
            assertThat(SlidingWindow.totalFruit(new int[]{5, 5, 5})).isEqualTo(3);
        }
    }

    // ------------------------------------------------------------------
    // 8. Minimum Window Substring
    //    Split into "found" and "missing" groups so an empty-string
    //    expected value never ends up as null via CSV coercion.
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Minimum Window Substring")
    class MinWindowTests {

        static Stream<Arguments> foundCases() {
            return Stream.of(
                    Arguments.of("ADOBECODEBANC", "ABC", "BANC"),
                    Arguments.of("a",              "a",   "a"),
                    Arguments.of("aa",             "aa",  "aa"),
                    Arguments.of("ab",             "b",   "b"),
                    Arguments.of("cabwefgewcwaefgcf", "cae", "cwae")
            );
        }

        @ParameterizedTest(name = "s=\"{0}\", t=\"{1}\" -> \"{2}\"")
        @MethodSource("foundCases")
        void minWindowFound(String s, String t, String expected) {
            assertThat(SlidingWindow.minWindow(s, t)).isEqualTo(expected);
        }

        /**
         * Cases where no window exists. Note {@code ("a","a")} is NOT here —
         * it's a valid match and belongs to {@link #foundCases()}.
         */
        static Stream<Arguments> missingCases() {
            return Stream.of(
                    Arguments.of("a",   "b"),      // mismatch
                    Arguments.of("abc", "xyz"),    // no overlap
                    Arguments.of("abc", "abcd"),   // t longer than s
                    Arguments.of("ab",  "abc")     // t longer than s
            );
        }

        @ParameterizedTest(name = "s=\"{0}\", t=\"{1}\" -> \"\"")
        @MethodSource("missingCases")
        void minWindowMissing(String s, String t) {
            assertThat(SlidingWindow.minWindow(s, t)).isEmpty();
        }

        @Test
        void emptyPattern() {
            assertThat(SlidingWindow.minWindow("anything", "")).isEmpty();
        }
    }
}