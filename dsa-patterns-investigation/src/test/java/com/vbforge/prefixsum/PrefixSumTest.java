package com.vbforge.prefixsum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Prefix Sum Pattern")
class PrefixSumTest {

    // ------------------------------------------------------------------
    // 1. 1-D prefix sum
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("buildPrefix / rangeSum")
    class PrefixSum1DTests {

        @Test
        void buildsPrefixWithLeadingZero() {
            // [-2,0,3,-5,2,-1]
            // prefix = [0, -2, -2, 1, -4, -2, -3]
            int[] prefix = PrefixSum.buildPrefix(new int[]{-2, 0, 3, -5, 2, -1});
            assertThat(prefix).containsExactly(0, -2, -2, 1, -4, -2, -3);
        }

        @Test
        void rangeQueries() {
            int[] prefix = PrefixSum.buildPrefix(new int[]{-2, 0, 3, -5, 2, -1});
            // sum(-2..3) = 1   → P[3] - P[0] = 1 - 0   = 1
            assertThat(PrefixSum.rangeSum(prefix, 0, 2)).isEqualTo(1);
            // sum(3..-1) = -1  → P[6] - P[2] = -3-(-2) = -1
            assertThat(PrefixSum.rangeSum(prefix, 2, 5)).isEqualTo(-1);
            // sum of all  = -3 → P[6] - P[0] = -3 - 0  = -3
            assertThat(PrefixSum.rangeSum(prefix, 0, 5)).isEqualTo(-3);
            // single element sum(0) = -2 → P[1] - P[0] = -2
            assertThat(PrefixSum.rangeSum(prefix, 1, 1)).isZero();
        }

        @Test
        void emptyArray() {
            int[] prefix = PrefixSum.buildPrefix(new int[0]);
            assertThat(prefix).containsExactly(0);
        }
    }

    // ------------------------------------------------------------------
// 2. 2-D prefix sum
// ------------------------------------------------------------------
    @Nested
    @DisplayName("buildPrefix2D / rangeSum2D")
    class PrefixSum2DTests {

        private static final int[][] MATRIX = {
                {3, 0, 1, 4, 2},   // row 0 = 10
                {5, 6, 3, 2, 1},   // row 1 = 17
                {1, 2, 0, 1, 5},   // row 2 =  9
                {4, 1, 0, 1, 7},   // row 3 = 13
                {1, 0, 3, 0, 5}    // row 4 =  9
        };                     // grand total = 10+17+9+13+9 = 58

        @Test
        void prefixShape() {
            int[][] prefix = PrefixSum.buildPrefix2D(MATRIX);
            assertThat(prefix.length).isEqualTo(6);       // m + 1
            assertThat(prefix[0].length).isEqualTo(6);    // n + 1
            assertThat(prefix[0][0]).isZero();
            // Top-left cell (row 0, col 0) → prefix[1][1] = 3
            assertThat(prefix[1][1]).isEqualTo(3);
        }

        @Test
        void rangeQueries() {
            // Range rows 2..4, cols 1..3:
            //   row 2: 2, 0, 1  → 3
            //   row 3: 1, 0, 1  → 2
            //   row 4: 0, 3, 0  → 3
            //   total           → 8
            int[][] prefix = PrefixSum.buildPrefix2D(MATRIX);
            assertThat(PrefixSum.rangeSum2D(prefix, 2, 1, 4, 3)).isEqualTo(8);

            // Full matrix sum = 58 (see row sums above)
            assertThat(PrefixSum.rangeSum2D(prefix, 0, 0, 4, 4)).isEqualTo(58);

            // Single cell (0,0) = 3
            assertThat(PrefixSum.rangeSum2D(prefix, 0, 0, 0, 0)).isEqualTo(3);

            // Row 1 = 5+6+3+2+1 = 17
            assertThat(PrefixSum.rangeSum2D(prefix, 1, 0, 1, 4)).isEqualTo(17);
        }
    }

    // ------------------------------------------------------------------
    // 3. Subarray Sum Equals K
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("subarraySum (LC 560)")
    class SubarraySumTests {

        @ParameterizedTest(name = "nums={0}, k={1} -> {2}")
        @MethodSource("cases")
        void subarraySum(int[] nums, int k, int expected) {
            assertThat(PrefixSum.subarraySum(nums, k)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // [1,1,1], k=2      → two subarrays: [0..1], [1..2]
                    Arguments.of(new int[]{1, 1, 1},         2, 2),
                    // [1,2,3], k=3      → [3], [1,2]
                    Arguments.of(new int[]{1, 2, 3},         3, 2),
                    // [1,-1,0], k=0     → [1,-1], [0], [1,-1,0]
                    Arguments.of(new int[]{1, -1, 0},        0, 3),
                    // [-1,-1,1], k=0    → [-1,-1,1], [1,-1] (as adjacent pair? no)
                    //                     Actually: [1..2]=[-1,1]=0, [0..2]=[-1,-1,1]=-1 no
                    //                     Let me recompute: [-1,-1,1]
                    //                     prefix: [-1,-2,-1]
                    //                     target k=0: need prefix[i]=prefix[j]
                    //                     pairs at -1: i=0, i=2 → one subarray [1..2]=[-1,1]=0
                    //                     pairs at -2: only one → no others
                    //                     answer: 1
                    Arguments.of(new int[]{-1, -1, 1},       0, 1),
                    // [3,4,7,2,-3,1,4,2], k=7 → 4 subarrays
                    Arguments.of(new int[]{3, 4, 7, 2, -3, 1, 4, 2}, 7, 4),
                    // Empty
                    Arguments.of(new int[]{},                0, 0)
            );
        }
    }

    // ------------------------------------------------------------------
    // 4. Product of Array Except Self
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("productExceptSelf (LC 238)")
    class ProductExceptSelfTests {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void productExceptSelf(int[] nums, int[] expected) {
            assertThat(PrefixSum.productExceptSelf(nums)).containsExactly(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // [1,2,3,4]:
                    //   left pass:  [1, 1, 2, 6]
                    //   right pass: [24,12, 8, 6]
                    Arguments.of(new int[]{1, 2, 3, 4},   new int[]{24, 12, 8, 6}),
                    // [-1,1,0,-3,3]:
                    //   left pass:  [1, -1, -1, 0, 0]
                    //   right pass: [0,  0,  9, 0, 0]
                    Arguments.of(new int[]{-1, 1, 0, -3, 3}, new int[]{0, 0, 9, 0, 0}),
                    // Single element → 1
                    Arguments.of(new int[]{5},            new int[]{1}),
                    // [2,3]:  2 → 3, 3 → 2
                    Arguments.of(new int[]{2, 3},         new int[]{3, 2})
            );
        }
    }

    // ------------------------------------------------------------------
    // 5. Pivot Index
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("pivotIndex (LC 724)")
    class PivotIndexTests {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void pivotIndex(int[] nums, int expected) {
            assertThat(PrefixSum.pivotIndex(nums)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // [1,7,3,6,5,6]: total=28
                    //   i=0: L=0, R=27 ✗
                    //   i=1: L=1, R=20 ✗
                    //   i=2: L=8, R=17 ✗
                    //   i=3: L=11,R=11 ✓ → 3
                    Arguments.of(new int[]{1, 7, 3, 6, 5, 6}, 3),
                    // [1,2,3]: total=6
                    //   i=0: L=0, R=5 ✗
                    //   i=1: L=1, R=3 ✗
                    //   i=2: L=3, R=0 ✗ → -1
                    Arguments.of(new int[]{1, 2, 3},          -1),
                    // [2,1,-1]: total=2
                    //   i=0: L=0, R=0 ✓ → 0
                    Arguments.of(new int[]{2, 1, -1},         0),
                    // Single element always balances
                    Arguments.of(new int[]{5},                0),
                    // [1,-1,3]: total=3
                    //   i=0: L=0, R=2 ✗
                    //   i=1: L=1, R=2 ✗
                    //   i=2: L=0, R=0 ✓ → 2
                    Arguments.of(new int[]{1, -1, 3},         2)
            );
        }
    }

    // ------------------------------------------------------------------
    // 6. Contiguous Array
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("findMaxLength (LC 525)")
    class ContiguousArrayTests {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void findMaxLength(int[] nums, int expected) {
            assertThat(PrefixSum.findMaxLength(nums)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // [0,1] → transform [-1,1] → sum [-1,0], 0 reappears at i=1
                    //   length = 1 - (-1) = 2
                    Arguments.of(new int[]{0, 1}, 2),
                    // [0,1,0] → transform [-1,1,-1] → sum [-1,0,-1]
                    //   i=1: sum 0 at firstIndex -1 → 2
                    //   i=2: sum -1 at firstIndex 0 → 2
                    //   max = 2
                    Arguments.of(new int[]{0, 1, 0}, 2),
                    // [0,1,1,1,1,1,0,0,0]
                    //   sum path: -1,0,1,2,3,4,3,2,1
                    //   i=8 (sum 1) first seen at i=2 → length 6
                    Arguments.of(new int[]{0, 1, 1, 1, 1, 1, 0, 0, 0}, 6),
                    // [0,0,0] → sum goes -1,-2,-3, never repeats → 0
                    Arguments.of(new int[]{0, 0, 0}, 0),
                    // [1,1,1] → sum goes 1,2,3, never repeats → 0
                    Arguments.of(new int[]{1, 1, 1}, 0),
                    // empty
                    Arguments.of(new int[]{}, 0)
            );
        }
    }

    /// ------------------------------------------------------------------
// 7. Subarray Sums Divisible by K
// ------------------------------------------------------------------
    @Nested
    @DisplayName("subarraysDivByK (LC 974)")
    class SubarraysDivByKTests {

        @ParameterizedTest(name = "nums={0}, k={1} -> {2}")
        @MethodSource("cases")
        void subarraysDivByK(int[] nums, int k, int expected) {
            assertThat(PrefixSum.subarraysDivByK(nums, k)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // [4,5,0,-2,-3,1], k=5
                    //   prefix: 4, 9, 9, 7, 4, 5
                    //   mods:   4, 4, 4, 2, 4, 0
                    //   walk:   0+1+2+0+3+1 = 7
                    Arguments.of(new int[]{4, 5, 0, -2, -3, 1}, 5, 7),

                    // [5], k=9
                    //   prefix: 5
                    //   mods:   5
                    //   walk:   0  → 0 valid subarrays
                    Arguments.of(new int[]{5},                  9, 0),

                    // [-1, 2, 9], k=2
                    //   valid subarrays by inspection: [2]=2, [-1,2,9]=10  → 2
                    //   prefix: -1, 1, 10
                    //   mods:    1, 1, 0
                    //   walk:    0 + 1 + 1 = 2
                    Arguments.of(new int[]{-1, 2, 9},           2, 2),

                    // [5,5,5,5], k=5
                    //   prefix: 5, 10, 15, 20  → all divisible by 5
                    //   every subarray works → n*(n+1)/2 = 4*5/2 = 10
                    Arguments.of(new int[]{5, 5, 5, 5},         5, 10),

                    // Empty — no subarrays
                    Arguments.of(new int[]{},                   2, 0)
            );
        }
    }

    // ------------------------------------------------------------------
    // 8. XOR Queries
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("xorQueries (LC 1310)")
    class XorQueriesTests {

        @Test
        void classicExample() {
            // arr = [1,3,4,8]
            // prefix xor: [0, 1, 2, 6, 14]
            //   queries: [0,1]→2^0=2  [1,2]→6^1=7  [0,3]→14^0=14  [3,3]→14^6=8
            int[] arr = {1, 3, 4, 8};
            int[][] queries = {{0, 1}, {1, 2}, {0, 3}, {3, 3}};
            assertThat(PrefixSum.xorQueries(arr, queries))
                    .containsExactly(2, 7, 14, 8);
        }

        @Test
        void singleElementQueries() {
            // arr = [5, 7]
            // prefix: [0, 5, 2]
            //   [0,0]→5^0=5, [1,1]→2^5=7
            int[] arr = {5, 7};
            int[][] queries = {{0, 0}, {1, 1}};
            assertThat(PrefixSum.xorQueries(arr, queries))
                    .containsExactly(5, 7);
        }

        @Test
        void emptyQueries() {
            int[] arr = {1, 2, 3};
            int[][] queries = {};
            assertThat(PrefixSum.xorQueries(arr, queries)).isEmpty();
        }

        @Test
        void prefixXorBuildsCorrectly() {
            // [1,3,4,8] → [0, 1, 2, 6, 14]
            assertThat(PrefixSum.buildPrefixXor(new int[]{1, 3, 4, 8}))
                    .containsExactly(0, 1, 2, 6, 14);
        }
    }
}