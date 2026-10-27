package com.vbforge.frequencycounting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Frequency Counting Pattern")
class FrequencyCountingTest {

    // ------------------------------------------------------------------
    // 1. Valid Anagram
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("isAnagram")
    class IsAnagramTests {

        @ParameterizedTest(name = "\"{0}\", \"{1}\" -> {2}")
        @CsvSource({
                // Counts match 1:1
                "anagram, nagaram, true",
                // Different multisets
                "rat,     car,     false",
                // Same length, one char swapped
                "aacc,    ccac,    false",
                // Length mismatch short-circuits to false
                "a,       ab,      false",
                // Trivial case
                "'',      '',      true"
        })
        void isAnagram(String s, String t, boolean expected) {
            assertThat(FrequencyCounting.isAnagram(s, t)).isEqualTo(expected);
        }
    }

    // ------------------------------------------------------------------
    // 2. First Unique Character
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("firstUniqChar")
    class FirstUniqCharTests {

        @ParameterizedTest(name = "\"{0}\" -> {1}")
        @MethodSource("cases")
        void firstUniqChar(String s, int expected) {
            assertThat(FrequencyCounting.firstUniqChar(s)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            // "leetcode":      l=1, e=3, t=1, c=1, o=1, d=1 → 'l' at 0
            // "loveleetcode":  l=2, o=2, v=1, e=4, t=1, c=1, d=1 → 'v' at 2
            // "aabb":          a=2, b=2 → none
            // "abcabc":        a=2, b=2, c=2 → none
            // "a":             a=1 → index 0
            // "":              nothing
            return Stream.of(
                    Arguments.of("leetcode",     0),
                    Arguments.of("loveleetcode", 2),
                    Arguments.of("aabb",        -1),
                    Arguments.of("abcabc",      -1),
                    Arguments.of("a",            0),
                    Arguments.of("",            -1)
            );
        }
    }

    // ------------------------------------------------------------------
    // 3. Ransom Note
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("canConstruct")
    class CanConstructTests {

        @ParameterizedTest(name = "ransom=\"{0}\", magazine=\"{1}\" -> {2}")
        @CsvSource({
                "a,  b,   false",
                "aa, ab,  false",
                "aa, aab, true",
                "a,  a,   true",
                "'', abc, true",
                "'', '',  true"
        })
        void canConstruct(String ransom, String magazine, boolean expected) {
            assertThat(FrequencyCounting.canConstruct(ransom, magazine)).isEqualTo(expected);
        }
    }

    // ------------------------------------------------------------------
    // 4. Group Anagrams
    //    Inner + outer lists are sorted before asserting so we don't
    //    depend on hash-map iteration order.
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("groupAnagrams")
    class GroupAnagramsTests {

        private static List<List<String>> normalize(List<List<String>> groups) {
            return groups.stream()
                    .map(g -> g.stream().sorted().toList())
                    .sorted(Comparator.comparing(g -> g.get(0)))
                    .toList();
        }

        @Test
        void classicGroups() {
            // "eat","tea","ate" → aet
            // "tan","nat"       → ant
            // "bat"             → abt
            List<List<String>> result = FrequencyCounting.groupAnagrams(
                    new String[]{"eat", "tea", "tan", "ate", "nat", "bat"});

            assertThat(normalize(result))
                    .isEqualTo(List.of(
                            List.of("ate", "eat", "tea"),
                            List.of("bat"),
                            List.of("nat", "tan")));
        }

        @Test
        void emptyInput() {
            assertThat(FrequencyCounting.groupAnagrams(new String[]{})).isEmpty();
        }

        @Test
        void singleEmptyString() {
            assertThat(FrequencyCounting.groupAnagrams(new String[]{""}))
                    .containsExactly(List.of(""));
        }
    }

    // ------------------------------------------------------------------
    // 5. Sort Characters by Frequency
    //    Tie-break in the impl: char ASC. Every expectation below uses it.
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("frequencySort")
    class FrequencySortTests {

        @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
        @MethodSource("cases")
        void frequencySort(String s, String expected) {
            assertThat(FrequencyCounting.frequencySort(s)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            // "tree":   t=1, r=1, e=2   → e(2), r(1), t(1) — tie r<t → "eert"
            // "cccaaa": c=3, a=3        → tie, char asc → a first → "aaaccc"
            // "Aabb":   A=1, a=1, b=2   → b(2), A(1), a(1) — tie A<a → "bbAa"
            return Stream.of(
                    Arguments.of("tree",   "eert"),
                    Arguments.of("cccaaa", "aaaccc"),
                    Arguments.of("Aabb",   "bbAa"),
                    Arguments.of("a",      "a"),
                    Arguments.of("",       "")
            );
        }
    }

    // ------------------------------------------------------------------
    // 6. Contains Duplicate
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("containsDuplicate")
    class ContainsDuplicateTests {

        @Test
        void detectsDuplicate() {
            assertThat(FrequencyCounting.containsDuplicate(new int[]{1, 2, 3, 1})).isTrue();
        }

        @Test
        void allDistinct() {
            assertThat(FrequencyCounting.containsDuplicate(new int[]{1, 2, 3, 4})).isFalse();
        }

        @Test
        void adjacentPair() {
            assertThat(FrequencyCounting.containsDuplicate(new int[]{1, 1})).isTrue();
        }

        @Test
        void empty() {
            assertThat(FrequencyCounting.containsDuplicate(new int[0])).isFalse();
        }
    }

    // ------------------------------------------------------------------
    // 7. Top K Frequent — bucket-sort and heap variants
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("topKFrequent (bucket sort, O(n))")
    class TopKFrequentBucketTests {

        @Test
        void classicExample() {
            // [1,1,1,2,2,3]: 1→3, 2→2, 3→1. Top 2 = {1, 2}
            assertThat(FrequencyCounting.topKFrequent(new int[]{1, 1, 1, 2, 2, 3}, 2))
                    .containsExactlyInAnyOrder(1, 2);
        }

        @Test
        void singleElement() {
            assertThat(FrequencyCounting.topKFrequent(new int[]{1}, 1))
                    .containsExactly(1);
        }

        @Test
        void allSameFrequency() {
            // 1→4, 2→4, 3→2. Top 2 = {1, 2}
            assertThat(FrequencyCounting.topKFrequent(
                    new int[]{1, 2, 1, 2, 1, 2, 3, 1, 3, 2}, 2))
                    .containsExactlyInAnyOrder(1, 2);
        }
    }

    @Nested
    @DisplayName("topKFrequentHeap (O(n log k))")
    class TopKFrequentHeapTests {

        @Test
        void classicExample() {
            assertThat(FrequencyCounting.topKFrequentHeap(new int[]{1, 1, 1, 2, 2, 3}, 2))
                    .containsExactlyInAnyOrder(1, 2);
        }

        @Test
        void singleElement() {
            assertThat(FrequencyCounting.topKFrequentHeap(new int[]{1}, 1))
                    .containsExactly(1);
        }
    }

    // ------------------------------------------------------------------
    // 8. Majority Element (Boyer-Moore)
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("majorityElement (Boyer-Moore)")
    class MajorityElementTests {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void majority(int[] nums, int expected) {
            assertThat(FrequencyCounting.majorityElement(nums)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // 3 appears 2/3 times
                    Arguments.of(new int[]{3, 2, 3},             3),
                    // 2 appears 4/7 times
                    Arguments.of(new int[]{2, 2, 1, 1, 1, 2, 2}, 2),
                    // only one element
                    Arguments.of(new int[]{1},                   1),
                    // candidate flips mid-array: 6,6,5,5,5 → 5 wins
                    Arguments.of(new int[]{6, 6, 5, 5, 5},       5)
            );
        }

        @Test
        void emptyRejected() {
            assertThatThrownBy(() -> FrequencyCounting.majorityElement(new int[0]))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ------------------------------------------------------------------
    // 9. Longest Consecutive Sequence
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("longestConsecutive")
    class LongestConsecutiveTests {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void longestConsecutive(int[] nums, int expected) {
            assertThat(FrequencyCounting.longestConsecutive(nums)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // {1,2,3,4}   → 4
                    Arguments.of(new int[]{100, 4, 200, 1, 3, 2}, 4),
                    // {0..8}      → 9
                    Arguments.of(new int[]{0, 3, 7, 2, 5, 8, 4, 6, 0, 1}, 9),
                    // {0,1,2}     → 3
                    Arguments.of(new int[]{1, 2, 0, 1}, 3),
                    // single value
                    Arguments.of(new int[]{5}, 1),
                    // already consecutive
                    Arguments.of(new int[]{1, 2, 3, 4, 5}, 5),
                    // empty
                    Arguments.of(new int[]{}, 0)
            );
        }
    }
}