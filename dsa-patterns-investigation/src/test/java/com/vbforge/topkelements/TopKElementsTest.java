package com.vbforge.topkelements;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Top-K Elements Pattern")
class TopKElementsTest {

    // ==================================================================
    //  Sort-based oracles. Each is O(n log n) and provably correct;
    //  used to cross-check the O(n) / O(n log k) fast implementations.
    // ==================================================================

    private static int kthLargestOracle(int[] nums, int k) {
        int[] sorted = nums.clone();
        Arrays.sort(sorted);
        return sorted[sorted.length - k];
    }

    private static int[] topKFrequentOracle(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums) freq.merge(n, 1, Integer::sum);
        return freq.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(k)
                .mapToInt(Map.Entry::getKey)
                .toArray();
    }

    private static int[][] kClosestOracle(int[][] points, int k) {
        int[][] sorted = new int[points.length][];
        for (int i = 0; i < points.length; i++) sorted[i] = points[i].clone();
        Arrays.sort(sorted, Comparator.<int[]>comparingLong(
                p -> (long) p[0] * p[0] + (long) p[1] * p[1]));
        return Arrays.copyOfRange(sorted, 0, k);
    }

    private static int[] mergeKListsOracle(TopKElements.ListNode[] lists) {
        List<Integer> all = new ArrayList<>();
        for (TopKElements.ListNode l : lists) {
            while (l != null) { all.add(l.val); l = l.next; }
        }
        Collections.sort(all);
        return all.stream().mapToInt(Integer::intValue).toArray();
    }

    // -------------------------- helpers ------------------------------

    /**
     * Compares two int multisets (order-insensitive). Suitable for problems
     * whose answer is unique up to permutation and — critically — where the
     * chosen elements are themselves unambiguous.
     *
     * <p><b>Does not work for tie-ambiguous results</b> (e.g. when two
     * elements share the frequency that decides membership in top-k). Those
     * are filtered out at the call site via {@link #hasUniqueTopK}.</p>
     */
    private static void assertSameMultiset(int[] actual, int[] expected, String msg) {
        int[] a = actual.clone();
        int[] e = expected.clone();
        Arrays.sort(a);
        Arrays.sort(e);
        // NOTE: assertThat(int[]) is AbstractIntArrayAssert — no isDeepEqualTo.
        assertThat(a).as(msg).containsExactly(e);
    }

    /**
     * Compares two "k closest" results <b>up to ties</b>.
     *
     * <p>If two points are equidistant from the origin, either is a valid
     * answer — the heap and the sort-based oracle may break the tie
     * differently. So we compare the <b>multiset of squared distances</b>,
     * not the point identities. This still catches incorrect distance
     * selections (the whole point of the algorithm) while ignoring
     * tie-break differences that are not part of the API contract.</p>
     */
    private static void assertSamePointSet(int[][] actual, int[][] expected, String msg) {
        long[] a = Arrays.stream(actual)
                .mapToLong(p -> (long) p[0] * p[0] + (long) p[1] * p[1])
                .sorted()
                .toArray();
        long[] e = Arrays.stream(expected)
                .mapToLong(p -> (long) p[0] * p[0] + (long) p[1] * p[1])
                .sorted()
                .toArray();
        assertThat(a).as(msg).containsExactly(e);
    }

    /**
     * Returns true iff the top-k by frequency is uniquely determined —
     * i.e. the k-th highest frequency is strictly greater than the
     * (k+1)-th. When false, two implementations may legitimately return
     * different answers and the oracle comparison is meaningless.
     */
    private static boolean hasUniqueTopK(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums) freq.merge(n, 1, Integer::sum);
        if (k >= freq.size()) return true;             // all included — no ambiguity
        List<Integer> sortedFreqs = freq.values().stream()
                .sorted(Comparator.reverseOrder())
                .toList();
        return sortedFreqs.get(k - 1) > sortedFreqs.get(k);
    }

    private static TopKElements.ListNode list(int... vals) {
        if (vals.length == 0) return null;
        TopKElements.ListNode head = new TopKElements.ListNode(vals[0]);
        TopKElements.ListNode curr = head;
        for (int i = 1; i < vals.length; i++) {
            curr.next = new TopKElements.ListNode(vals[i]);
            curr = curr.next;
        }
        return head;
    }

    private static int[] toArray(TopKElements.ListNode head) {
        List<Integer> out = new ArrayList<>();
        while (head != null) { out.add(head.val); head = head.next; }
        return out.stream().mapToInt(Integer::intValue).toArray();
    }

    // ------------------------------------------------------------------
    // 1. K-th Largest — Quickselect
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("findKthLargest — Quickselect")
    class QuickselectTests {

        @ParameterizedTest(name = "nums={0}, k={1} -> {2}")
        @MethodSource("cases")
        void findKthLargest(int[] nums, int k, int expected) {
            assertThat(TopKElements.findKthLargest(nums, k)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // sorted asc [1,2,3,4,5,6]; 2nd largest = idx 4 = 5
                    Arguments.of(new int[]{3, 2, 1, 5, 6, 4},                 2, 5),
                    // sorted asc [1,2,2,3,3,4,5,5,6]; 4th largest = idx 5 = 4
                    Arguments.of(new int[]{3, 2, 3, 1, 2, 4, 5, 5, 6},       4, 4),
                    // single
                    Arguments.of(new int[]{1},                                 1, 1),
                    // k=n → smallest
                    Arguments.of(new int[]{1, 2},                              2, 1),
                    // k=1 → largest
                    Arguments.of(new int[]{1, 2},                              1, 2),
                    // reverse-sorted, 1st largest = 5
                    Arguments.of(new int[]{5, 4, 3, 2, 1},                     1, 5),
                    // all-equal — 3-way partition must stay O(n)
                    Arguments.of(new int[]{7, 7, 7, 7, 7},                     3, 7),
                    // many duplicates
                    Arguments.of(new int[]{1, 1, 2, 2, 3, 3},                  3, 2)
            );
        }

        @Test
        void rejectsInvalidK() {
            assertThatThrownBy(() -> TopKElements.findKthLargest(new int[]{1, 2}, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> TopKElements.findKthLargest(new int[]{1, 2}, 3))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void doesNotMutateInput() {
            int[] input = {3, 2, 1, 5, 6, 4};
            int[] before = input.clone();
            TopKElements.findKthLargest(input, 2);
            // NOTE: assertThat(int[]) is AbstractIntArrayAssert — use containsExactly,
            // not isDeepEqualTo (which is for object/2D arrays only).
            assertThat(input).containsExactly(before);
        }

        /**
         * Randomized cross-check: both the quickselect and heap variants must
         * agree with the sort-based oracle on every input the RNG produces.
         * Covers sorted, reverse-sorted, all-equal, and duplicate-heavy inputs
         * because the RNG draw space includes them.
         */
        @Test
        void matchesOracleRandomized() {
            Random rnd = new Random(20_260_101L);
            for (int t = 0; t < 500; t++) {
                int n = rnd.nextInt(20) + 1;
                int[] nums = new int[n];
                for (int i = 0; i < n; i++) nums[i] = rnd.nextInt(11) - 5;
                int k = rnd.nextInt(n) + 1;
                int expected = kthLargestOracle(nums, k);

                assertThat(TopKElements.findKthLargest(nums, k))
                        .as("t=%d quickselect nums=%s k=%d", t, Arrays.toString(nums), k)
                        .isEqualTo(expected);

                assertThat(TopKElements.findKthLargestHeap(nums, k))
                        .as("t=%d heap nums=%s k=%d", t, Arrays.toString(nums), k)
                        .isEqualTo(expected);
            }
        }
    }

    // ------------------------------------------------------------------
    // 2. K-th Largest — Heap
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("findKthLargestHeap — min-heap of size k")
    class HeapKthTests {

        @ParameterizedTest(name = "nums={0}, k={1} -> {2}")
        @MethodSource("cases")
        void findKthLargestHeap(int[] nums, int k, int expected) {
            assertThat(TopKElements.findKthLargestHeap(nums, k)).isEqualTo(expected);
        }

        /**
         * Duplicated from {@link QuickselectTests#cases()} — JUnit 5's
         * {@code @MethodSource("ClassName#method")} resolves {@code ClassName}
         * as a <b>top-level</b> FQN (nested classes need {@code $}), so a
         * cross-nested-class reference in the annotation is fragile. A local
         * method just calls into the source of truth programmatically.
         */
        static Stream<Arguments> cases() {
            return QuickselectTests.cases();
        }

        @Test
        void rejectsInvalidK() {
            assertThatThrownBy(() -> TopKElements.findKthLargestHeap(new int[]{1}, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ------------------------------------------------------------------
    // 3. Top K Frequent
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("topKFrequent — min-heap of size k")
    class TopKFrequentTests {

        @Test
        void classic() {
            // freq: 1→3, 2→2, 3→1. Top 2 = {1, 2}
            int[] nums = {1, 1, 1, 2, 2, 3};
            assertSameMultiset(TopKElements.topKFrequent(nums, 2),
                    new int[]{1, 2},
                    "nums=" + Arrays.toString(nums) + ", k=2");
        }

        @Test
        void single() {
            assertThat(TopKElements.topKFrequent(new int[]{1}, 1)).containsExactly(1);
        }

        @Test
        void allSameFrequency() {
            // freq: 1→1, 2→1, 3→1. Top 2 = any two of {1, 2, 3}.
            // Output is genuinely ambiguous — only check size and membership.
            int[] result = TopKElements.topKFrequent(new int[]{1, 2, 3}, 2);
            assertThat(result).hasSize(2);
            for (int v : result) assertThat(v).isIn(1, 2, 3);
        }

        @Test
        void kEqualsDistinctCount() {
            // freq: 4→4, 5→3, 6→2, 7→1. Top 4 = {4,5,6,7} in any order
            int[] nums = {4, 4, 4, 4, 5, 5, 5, 6, 6, 7};
            assertSameMultiset(TopKElements.topKFrequent(nums, 4),
                    new int[]{4, 5, 6, 7},
                    "nums=" + Arrays.toString(nums) + ", k=4");
        }

        /**
         * Cross-check against a sort-based oracle, <b>only when the top-k
         * is uniquely determined</b> — i.e. the k-th and (k+1)-th frequencies
         * are distinct. When they tie, either selection is a valid answer
         * and comparing against the oracle would be testing HashMap
         * iteration order, not correctness.
         */
        @Test
        void matchesOracle() {
            int[][] cases = {
                    {1, 1, 1, 2, 2, 3},                 // 1→3, 2→2, 3→1
                    {4, 4, 4, 4, 5, 5, 5, 6, 6, 7},     // 4→4, 5→3, 6→2, 7→1
                    {1, 2, 3, 4, 5},                    // all freq 1 (skip: ambiguous)
                    {-1, -1, 2, 2, 3}                   // -1→2, 2→2, 3→1 (tie: skip)
            };
            for (int[] nums : cases) {
                Set<Integer> distinct = new HashSet<>();
                for (int n : nums) distinct.add(n);
                for (int k = 1; k <= distinct.size(); k++) {
                    if (!hasUniqueTopK(nums, k)) continue;   // tie → skip
                    assertSameMultiset(
                            TopKElements.topKFrequent(nums, k),
                            topKFrequentOracle(nums, k),
                            "nums=" + Arrays.toString(nums) + ", k=" + k);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // 4. K Closest Points to Origin
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("kClosest — max-heap of size k")
    class KClosestTests {

        @Test
        void classic() {
            // dists: [1,3]→10, [-2,2]→8. 1 closest = [-2,2]
            int[][] result = TopKElements.kClosest(
                    new int[][]{{1, 3}, {-2, 2}}, 1);
            assertSamePointSet(result, new int[][]{{-2, 2}}, "k=1");
        }

        @Test
        void twoClosest() {
            // dists: [3,3]→18, [5,-1]→26, [-2,4]→20
            // 2 closest: [3,3] (18) and [-2,4] (20)
            int[][] result = TopKElements.kClosest(
                    new int[][]{{3, 3}, {5, -1}, {-2, 4}}, 2);
            assertSamePointSet(result, new int[][]{{3, 3}, {-2, 4}}, "k=2");
        }

        @Test
        void bothClosest() {
            // dists: [0,1]→1, [1,0]→1. Both within k=2
            int[][] result = TopKElements.kClosest(
                    new int[][]{{0, 1}, {1, 0}}, 2);
            assertSamePointSet(result, new int[][]{{0, 1}, {1, 0}}, "k=2");
        }

        @Test
        void singlePoint() {
            int[][] result = TopKElements.kClosest(new int[][]{{1, 1}}, 1);
            assertSamePointSet(result, new int[][]{{1, 1}}, "k=1");
        }

        @Test
        void doesNotMutateInput() {
            int[][] input = {{3, 3}, {5, -1}, {-2, 4}};
            int[][] before = {{3, 3}, {5, -1}, {-2, 4}};
            TopKElements.kClosest(input, 2);
            assertThat(input).isDeepEqualTo(before);      // int[][] → isDeepEqualTo ✓
        }

        /**
         * Cross-check against oracle. Ties are handled by {@link #assertSamePointSet}
         * — it compares squared-distance multisets, not point identities.
         */
        @Test
        void matchesOracle() {
            int[][][] cases = {
                    {{1, 3}, {-2, 2}},
                    {{3, 3}, {5, -1}, {-2, 4}},
                    {{0, 1}, {1, 0}},                       // tie at dist 1
                    {{1, 1}},
                    {{1, 1}, {2, 2}, {3, 3}, {-1, -1}}       // tie at dist 2
            };
            for (int[][] pts : cases) {
                for (int k = 1; k <= pts.length; k++) {
                    assertSamePointSet(
                            TopKElements.kClosest(pts, k),
                            kClosestOracle(pts, k),
                            "pts=" + Arrays.deepToString(pts) + ", k=" + k);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // 5. K-th Smallest in a Sorted Matrix
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("kthSmallest — binary search on value")
    class KthSmallestMatrixTests {

        private static final int[][] MATRIX = {
                { 1,  5,  9},
                {10, 11, 13},
                {12, 13, 15}
        };

        @Test
        void classicKth8() {
            // sorted flat: [1,5,9,10,11,12,13,13,15], 8th (1-indexed) = 13
            assertThat(TopKElements.kthSmallest(MATRIX, 8)).isEqualTo(13);
        }

        @Test
        void kth1() {
            assertThat(TopKElements.kthSmallest(MATRIX, 1)).isEqualTo(1);
        }

        @Test
        void kthMax() {
            assertThat(TopKElements.kthSmallest(MATRIX, 9)).isEqualTo(15);
        }

        @Test
        void singleCell() {
            assertThat(TopKElements.kthSmallest(new int[][]{{-5}}, 1)).isEqualTo(-5);
        }

        @Test
        void twoByTwo() {
            // matrix [[1,2],[1,3]] → sorted flat [1,1,2,3]
            assertThat(TopKElements.kthSmallest(new int[][]{{1, 2}, {1, 3}}, 2))
                    .isEqualTo(1);
        }

        @Test
        void threeByThree() {
            // [[1,3,5],[6,7,12],[11,14,14]] sorted flat [1,3,5,6,7,11,12,14,14]
            // 6th = 11
            assertThat(TopKElements.kthSmallest(
                    new int[][]{{1, 3, 5}, {6, 7, 12}, {11, 14, 14}}, 6))
                    .isEqualTo(11);
        }

        @Test
        void matchesOracle() {
            int[][][] matrices = {
                    MATRIX,
                    {{-5}},
                    {{1, 2}, {1, 3}},
                    {{1, 3, 5}, {6, 7, 12}, {11, 14, 14}},
                    {{1, 2}, {3, 4}}
            };
            for (int[][] m : matrices) {
                int total = m.length * m[0].length;
                for (int k = 1; k <= total; k++) {
                    int[] flat = flatten(m);
                    Arrays.sort(flat);
                    assertThat(TopKElements.kthSmallest(m, k))
                            .as("matrix=%s, k=%d", Arrays.deepToString(m), k)
                            .isEqualTo(flat[k - 1]);
                }
            }
        }

        private static int[] flatten(int[][] m) {
            int[] out = new int[m.length * m[0].length];
            int idx = 0;
            for (int[] row : m) for (int v : row) out[idx++] = v;
            return out;
        }
    }

    // ------------------------------------------------------------------
    // 6. Merge k Sorted Lists
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("mergeKLists — min-heap of nodes")
    class MergeKListsTests {

        @Test
        void classic() {
            // lists: [1,4,5], [1,3,4], [2,6] → merged [1,1,2,3,4,4,5,6]
            TopKElements.ListNode[] lists = {
                    list(1, 4, 5),
                    list(1, 3, 4),
                    list(2, 6)
            };
            assertThat(toArray(TopKElements.mergeKLists(lists)))
                    .containsExactly(1, 1, 2, 3, 4, 4, 5, 6);
        }

        @Test
        void emptyArrayOfLists() {
            assertThat(TopKElements.mergeKLists(new TopKElements.ListNode[0]))
                    .isNull();
        }

        @Test
        void singleNullList() {
            assertThat(TopKElements.mergeKLists(
                    new TopKElements.ListNode[]{null})).isNull();
        }

        @Test
        void mixtureOfNullAndLists() {
            TopKElements.ListNode[] lists = {
                    null,
                    list(1),
                    null,
                    list(0, 2)
            };
            assertThat(toArray(TopKElements.mergeKLists(lists)))
                    .containsExactly(0, 1, 2);
        }

        @Test
        void singleList() {
            assertThat(toArray(TopKElements.mergeKLists(
                    new TopKElements.ListNode[]{list(1, 2, 3)})))
                    .containsExactly(1, 2, 3);
        }

        @Test
        void matchesOracle() {
            int[][][] cases = {
                    {{1, 4, 5}, {1, 3, 4}, {2, 6}},
                    {{}},
                    {{1}, {2}, {3}},
                    {{5}, {1, 4}, {3}},
                    {{-3, -1, 0}, {-2, 2}}
            };
            for (int[][] values : cases) {
                TopKElements.ListNode[] lists = new TopKElements.ListNode[values.length];
                for (int i = 0; i < values.length; i++) lists[i] = list(values[i]);
                int[] expected = mergeKListsOracle(cloneLists(values));

                assertThat(toArray(TopKElements.mergeKLists(lists)))
                        .as("input = %s", Arrays.deepToString(values))
                        .isEqualTo(expected);
            }
        }

        private static TopKElements.ListNode[] cloneLists(int[][] values) {
            TopKElements.ListNode[] out = new TopKElements.ListNode[values.length];
            for (int i = 0; i < values.length; i++) out[i] = list(values[i]);
            return out;
        }
    }
}