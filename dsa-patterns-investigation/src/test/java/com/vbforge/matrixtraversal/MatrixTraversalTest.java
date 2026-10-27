package com.vbforge.matrixtraversal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Matrix Traversal Pattern")
class MatrixTraversalTest {

    // ------------------------------------------------------------------
    // 1. Spiral order
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("spiralOrder")
    class SpiralOrderTests {

        @Test
        void threeByThree() {
            // ring 1: 1,2,3 | 6,9 | 8,7 | 4   → then centre 5
            int[][] m = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
            assertThat(MatrixTraversal.spiralOrder(m))
                    .containsExactly(1, 2, 3, 6, 9, 8, 7, 4, 5);
        }

        @Test
        void threeByFour() {
            // 1,2,3,4 | 8,12 | 11,10,9 | 5 | 6,7
            int[][] m = {
                    {1, 2, 3, 4},
                    {5, 6, 7, 8},
                    {9, 10, 11, 12}
            };
            assertThat(MatrixTraversal.spiralOrder(m))
                    .containsExactly(1, 2, 3, 4, 8, 12, 11, 10, 9, 5, 6, 7);
        }

        @Test
        void twoByTwo() {
            int[][] m = {{1, 2}, {3, 4}};
            assertThat(MatrixTraversal.spiralOrder(m)).containsExactly(1, 2, 4, 3);
        }

        @Test
        void singleRow() {
            // no down/left/up — only the right pass runs
            int[][] m = {{1, 2, 3}};
            assertThat(MatrixTraversal.spiralOrder(m)).containsExactly(1, 2, 3);
        }

        @Test
        void singleColumn() {
            // only right + down; left/up must be skipped to avoid duplicating
            int[][] m = {{1}, {2}, {3}};
            assertThat(MatrixTraversal.spiralOrder(m)).containsExactly(1, 2, 3);
        }

        @Test
        void singleCell() {
            assertThat(MatrixTraversal.spiralOrder(new int[][]{{42}}))
                    .containsExactly(42);
        }

        @Test
        void empty() {
            assertThat(MatrixTraversal.spiralOrder(new int[0][])).isEmpty();
            assertThat(MatrixTraversal.spiralOrder(new int[][]{{}})).isEmpty();
        }
    }

    // ------------------------------------------------------------------
    // 2. Spiral fill
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("generateMatrix")
    class GenerateMatrixTests {

        @Test
        void nEqualsOne() {
            assertThat(MatrixTraversal.generateMatrix(1))
                    .isDeepEqualTo(new int[][]{{1}});
        }

        @Test
        void nEqualsTwo() {
            // ring: 1,2 | 4 | 3
            assertThat(MatrixTraversal.generateMatrix(2))
                    .isDeepEqualTo(new int[][]{
                            {1, 2},
                            {4, 3}});
        }

        @Test
        void nEqualsThree() {
            // ring: 1,2,3 | 4,5 | 6,7 | 8   → centre 9
            assertThat(MatrixTraversal.generateMatrix(3))
                    .isDeepEqualTo(new int[][]{
                            {1, 2, 3},
                            {8, 9, 4},
                            {7, 6, 5}});
        }

        @Test
        void nEqualsFour() {
            // 1,2,3,4 | 5,6,7 | 8,9,10 | 11,12 | 13,14 | 15 | 16
            assertThat(MatrixTraversal.generateMatrix(4))
                    .isDeepEqualTo(new int[][]{
                            {1,  2,  3,  4},
                            {12, 13, 14, 5},
                            {11, 16, 15, 6},
                            {10, 9,  8,  7}});
        }

        @Test
        void rejectsNonPositive() {
            assertThatThrownBy(() -> MatrixTraversal.generateMatrix(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> MatrixTraversal.generateMatrix(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ------------------------------------------------------------------
    // 3. Diagonal traverse
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("diagonalTraverse")
    class DiagonalTraverseTests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void diagonalTraverse(int[][] matrix, int[] expected) {
            assertThat(MatrixTraversal.diagonalTraverse(matrix)).containsExactly(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // 3x3: [1] [2,4] [7,5,3] [6,8] [9] → 1,2,4,7,5,3,6,8,9
                    Arguments.of(
                            new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}},
                            new int[]{1, 2, 4, 7, 5, 3, 6, 8, 9}),
                    // 2x2: [1] [2,3] [4] → 1,2,3,4
                    Arguments.of(
                            new int[][]{{1, 2}, {3, 4}},
                            new int[]{1, 2, 3, 4}),
                    // 1x1
                    Arguments.of(
                            new int[][]{{7}},
                            new int[]{7}),
                    // 1x3: single row, all up-right
                    Arguments.of(
                            new int[][]{{1, 2, 3}},
                            new int[]{1, 2, 3}),
                    // 3x1: single column
                    Arguments.of(
                            new int[][]{{1}, {2}, {3}},
                            new int[]{1, 2, 3})
            );
        }

        @Test
        void empty() {
            assertThat(MatrixTraversal.diagonalTraverse(new int[0][])).isEmpty();
        }
    }

    // ------------------------------------------------------------------
    // 4. Transpose
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("transpose (in place)")
    class TransposeTests {

        @Test
        void threeByThree() {
            int[][] m = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
            MatrixTraversal.transpose(m);
            assertThat(m).isDeepEqualTo(new int[][]{
                    {1, 4, 7},
                    {2, 5, 8},
                    {3, 6, 9}});
        }

        @Test
        void twoByTwo() {
            int[][] m = {{1, 2}, {3, 4}};
            MatrixTraversal.transpose(m);
            assertThat(m).isDeepEqualTo(new int[][]{
                    {1, 3},
                    {2, 4}});
        }

        @Test
        void oneByOne() {
            int[][] m = {{1}};
            MatrixTraversal.transpose(m);
            assertThat(m).isDeepEqualTo(new int[][]{{1}});
        }

        @Test
        void rejectsNonSquare() {
            assertThatThrownBy(() ->
                    MatrixTraversal.transpose(new int[][]{{1, 2, 3}, {4, 5, 6}}))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("transposeCopy (rectangular)")
    class TransposeCopyTests {

        @Test
        void rectangular() {
            int[][] m = {{1, 2, 3}, {4, 5, 6}};   // 2x3
            assertThat(MatrixTraversal.transposeCopy(m)).isDeepEqualTo(new int[][]{
                    {1, 4},
                    {2, 5},
                    {3, 6}});                     // 3x2
        }

        @Test
        void square() {
            int[][] m = {{1, 2}, {3, 4}};
            assertThat(MatrixTraversal.transposeCopy(m)).isDeepEqualTo(new int[][]{
                    {1, 3},
                    {2, 4}});
        }
    }

    // ------------------------------------------------------------------
    // 5. 90° clockwise rotation
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("rotate90")
    class Rotate90Tests {

        @Test
        void threeByThree() {
            // transpose → {{1,4,7},{2,5,8},{3,6,9}}
            // reverse rows → {{7,4,1},{8,5,2},{9,6,3}}
            int[][] m = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
            MatrixTraversal.rotate90(m);
            assertThat(m).isDeepEqualTo(new int[][]{
                    {7, 4, 1},
                    {8, 5, 2},
                    {9, 6, 3}});
        }

        @Test
        void twoByTwo() {
            int[][] m = {{1, 2}, {3, 4}};
            MatrixTraversal.rotate90(m);
            assertThat(m).isDeepEqualTo(new int[][]{
                    {3, 1},
                    {4, 2}});
        }

        @Test
        void fourByFour() {
            // rotate90 of:
            //   [ 5, 1, 9,11]
            //   [ 2, 4, 8,10]
            //   [13, 3, 6, 7]
            //   [15,14,12,16]
            // expected:
            //   [15,13, 2, 5]
            //   [14, 3, 4, 1]
            //   [12, 6, 8, 9]
            //   [16, 7,10,11]
            int[][] m = {
                    { 5,  1,  9, 11},
                    { 2,  4,  8, 10},
                    {13,  3,  6,  7},
                    {15, 14, 12, 16}};
            MatrixTraversal.rotate90(m);
            assertThat(m).isDeepEqualTo(new int[][]{
                    {15, 13, 2, 5},
                    {14,  3, 4, 1},
                    {12,  6, 8, 9},
                    {16,  7, 10, 11}});
        }

        @Test
        void oneByOne() {
            int[][] m = {{1}};
            MatrixTraversal.rotate90(m);
            assertThat(m).isDeepEqualTo(new int[][]{{1}});
        }

        @Test
        void fourRotationsRestoreOriginal() {
            int[][] original = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
            int[][] m = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
            for (int i = 0; i < 4; i++) MatrixTraversal.rotate90(m);
            assertThat(m).isDeepEqualTo(original);
        }

        @Test
        void rejectsNonSquare() {
            assertThatThrownBy(() ->
                    MatrixTraversal.rotate90(new int[][]{{1, 2, 3}, {4, 5, 6}}))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ------------------------------------------------------------------
    // 6. Set Zeroes
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("setZeroes")
    class SetZeroesTests {

        @Test
        void threeByThreeSingleInteriorZero() {
            // zero at (1,1) → entire row 1 and col 1 become zero
            int[][] m = {
                    {1, 1, 1},
                    {1, 0, 1},
                    {1, 1, 1}};
            MatrixTraversal.setZeroes(m);
            assertThat(m).isDeepEqualTo(new int[][]{
                    {1, 0, 1},
                    {0, 0, 0},
                    {1, 0, 1}});
        }

        @Test
        void firstRowAndColZeroed() {
            // zeros at (0,0) and (0,3) → row 0 and col 0 and col 3 zeroed
            int[][] m = {
                    {0, 1, 2, 0},
                    {3, 4, 5, 2},
                    {1, 3, 1, 5}};
            MatrixTraversal.setZeroes(m);
            assertThat(m).isDeepEqualTo(new int[][]{
                    {0, 0, 0, 0},
                    {0, 4, 5, 0},
                    {0, 3, 1, 0}});
        }

        @Test
        void noZerosNoChange() {
            int[][] m = {
                    {1, 2, 3},
                    {4, 5, 6},
                    {7, 8, 9}};
            MatrixTraversal.setZeroes(m);
            assertThat(m).isDeepEqualTo(new int[][]{
                    {1, 2, 3},
                    {4, 5, 6},
                    {7, 8, 9}});
        }

        @Test
        void singleCellZero() {
            int[][] m = {{0}};
            MatrixTraversal.setZeroes(m);
            assertThat(m).isDeepEqualTo(new int[][]{{0}});
        }

        @Test
        void singleCellNonZero() {
            int[][] m = {{5}};
            MatrixTraversal.setZeroes(m);
            assertThat(m).isDeepEqualTo(new int[][]{{5}});
        }

        @Test
        void empty() {
            int[][] m = new int[0][];
            MatrixTraversal.setZeroes(m);
            assertThat(m).isEmpty();
        }
    }
}