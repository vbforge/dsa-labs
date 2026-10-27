package com.vbforge.binarytreetraversal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Binary Tree Traversal Pattern")
class BinaryTreeTraversalTest {

    // ------------------------------------------------------------------
    //  Structural equality helper
    // ------------------------------------------------------------------
    private static void assertSameTree(BinaryTreeTraversal.TreeNode a,
                                       BinaryTreeTraversal.TreeNode b,
                                       String msg) {
        if (a == null && b == null) return;
        if (a == null || b == null) {
            throw new AssertionError(msg + ": one side null, other is "
                    + (a == null ? "left" : "right"));
        }
        if (a.val != b.val) {
            throw new AssertionError(msg + ": values differ — "
                    + a.val + " vs " + b.val);
        }
        assertSameTree(a.left,  b.left,  msg + ".left");
        assertSameTree(a.right, b.right, msg + ".right");
    }

    // ------------------------------------------------------------------
    //  Independent oracles
    // ------------------------------------------------------------------

    /** Level-order oracle via depth-recording DFS — structurally distinct from BFS. */
    private static List<List<Integer>> levelOrderOracle(BinaryTreeTraversal.TreeNode root) {
        Map<Integer, List<Integer>> byDepth = new TreeMap<>();
        collectDepth(root, 0, byDepth);
        return new ArrayList<>(byDepth.values());
    }

    private static void collectDepth(BinaryTreeTraversal.TreeNode node, int depth,
                                     Map<Integer, List<Integer>> byDepth) {
        if (node == null) return;
        byDepth.computeIfAbsent(depth, k -> new ArrayList<>()).add(node.val);
        collectDepth(node.left,  depth + 1, byDepth);
        collectDepth(node.right, depth + 1, byDepth);
    }

    /** Vertical-order oracle — enumerate all (col, row, val) tuples, sort, group. */
    private static List<List<Integer>> verticalOracle(BinaryTreeTraversal.TreeNode root) {
        List<int[]> entries = new ArrayList<>();
        collectEntries(root, 0, 0, entries);
        entries.sort((a, b) -> a[0] != b[0]
                ? Integer.compare(a[0], b[0])
                : a[1] != b[1]
                ? Integer.compare(a[1], b[1])
                : Integer.compare(a[2], b[2]));
        Map<Integer, List<Integer>> byCol = new TreeMap<>();
        for (int[] e : entries) byCol.computeIfAbsent(e[0], k -> new ArrayList<>()).add(e[2]);
        return new ArrayList<>(byCol.values());
    }

    private static void collectEntries(BinaryTreeTraversal.TreeNode n, int col, int row,
                                       List<int[]> out) {
        if (n == null) return;
        out.add(new int[]{col, row, n.val});
        collectEntries(n.left,  col - 1, row + 1, out);
        collectEntries(n.right, col + 1, row + 1, out);
    }

    // ------------------------------------------------------------------
    //  Reference trees
    // ------------------------------------------------------------------

    private static BinaryTreeTraversal.TreeNode makeReferenceTree() {
        //         1
        //        / \
        //       2   3
        //      / \   \
        //     4   5   6
        return BinaryTreeTraversal.build(
                new Integer[]{1, 2, 3, 4, 5, null, 6});
    }

    // ------------------------------------------------------------------
    // 1. Pre-order — recursive vs iterative
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Pre-order (root, left, right)")
    class PreorderTests {

        @Test
        void referenceTreeRecursive() {
            // root→left→right: 1,2,4,5,3,6
            assertThat(BinaryTreeTraversal.preorderRecursive(makeReferenceTree()))
                    .containsExactly(1, 2, 4, 5, 3, 6);
        }

        @Test
        void referenceTreeIterative() {
            assertThat(BinaryTreeTraversal.preorderIterative(makeReferenceTree()))
                    .containsExactly(1, 2, 4, 5, 3, 6);
        }

        @Test
        void emptyAndSingle() {
            assertThat(BinaryTreeTraversal.preorderRecursive(null)).isEmpty();
            assertThat(BinaryTreeTraversal.preorderIterative(null)).isEmpty();

            BinaryTreeTraversal.TreeNode single = new BinaryTreeTraversal.TreeNode(42);
            assertThat(BinaryTreeTraversal.preorderRecursive(single)).containsExactly(42);
            assertThat(BinaryTreeTraversal.preorderIterative(single)).containsExactly(42);
        }

        @Test
        void leftSkewed() {
            // 1 → 2 → 3 (all left children)
            BinaryTreeTraversal.TreeNode root = BinaryTreeTraversal.build(
                    new Integer[]{1, 2, null, 3});
            assertThat(BinaryTreeTraversal.preorderIterative(root))
                    .containsExactly(1, 2, 3);
        }

        @Test
        void rightSkewed() {
            BinaryTreeTraversal.TreeNode root = BinaryTreeTraversal.build(
                    new Integer[]{1, null, 2, null, 3});
            assertThat(BinaryTreeTraversal.preorderIterative(root))
                    .containsExactly(1, 2, 3);
        }

        @Test
        void matchesRecursiveOracle() {
            Integer[][] trees = {
                    {1, 2, 3, 4, 5, null, 6},
                    {1},
                    {1, 2},
                    {1, null, 2},
                    {3, 9, 20, null, null, 15, 7},
                    {1, 2, 3, 4, 5, 6, 7, 8, 9}
            };
            for (Integer[] t : trees) {
                BinaryTreeTraversal.TreeNode root = BinaryTreeTraversal.build(t);
                assertThat(BinaryTreeTraversal.preorderIterative(root))
                        .as("tree=%s", java.util.Arrays.toString(t))
                        .isEqualTo(BinaryTreeTraversal.preorderRecursive(root));
            }
        }
    }

    // ------------------------------------------------------------------
    // 2. In-order — recursive vs iterative
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("In-order (left, root, right)")
    class InorderTests {

        @Test
        void referenceTreeRecursive() {
            // left→root→right: 4,2,5,1,3,6
            assertThat(BinaryTreeTraversal.inorderRecursive(makeReferenceTree()))
                    .containsExactly(4, 2, 5, 1, 3, 6);
        }

        @Test
        void referenceTreeIterative() {
            assertThat(BinaryTreeTraversal.inorderIterative(makeReferenceTree()))
                    .containsExactly(4, 2, 5, 1, 3, 6);
        }

        @Test
        void emptyAndSingle() {
            assertThat(BinaryTreeTraversal.inorderIterative(null)).isEmpty();
            BinaryTreeTraversal.TreeNode single = new BinaryTreeTraversal.TreeNode(7);
            assertThat(BinaryTreeTraversal.inorderIterative(single)).containsExactly(7);
        }

        @Test
        void bstProducesSortedOrder() {
            // BST: in-order = ascending
            BinaryTreeTraversal.TreeNode bst = BinaryTreeTraversal.build(
                    new Integer[]{4, 2, 6, 1, 3, 5, 7});
            assertThat(BinaryTreeTraversal.inorderIterative(bst))
                    .containsExactly(1, 2, 3, 4, 5, 6, 7);
        }

        @Test
        void matchesRecursiveOracle() {
            Integer[][] trees = {
                    {1, 2, 3, 4, 5, null, 6},
                    {1},
                    {1, 2},
                    {1, null, 2},
                    {3, 9, 20, null, null, 15, 7},
                    {5, 3, 8, 1, 4, 7, 9}
            };
            for (Integer[] t : trees) {
                BinaryTreeTraversal.TreeNode root = BinaryTreeTraversal.build(t);
                assertThat(BinaryTreeTraversal.inorderIterative(root))
                        .as("tree=%s", java.util.Arrays.toString(t))
                        .isEqualTo(BinaryTreeTraversal.inorderRecursive(root));
            }
        }
    }

    // ------------------------------------------------------------------
    // 3. Post-order — recursive vs iterative
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Post-order (left, right, root)")
    class PostorderTests {

        @Test
        void referenceTreeRecursive() {
            // left→right→root: 4,5,2,6,3,1
            assertThat(BinaryTreeTraversal.postorderRecursive(makeReferenceTree()))
                    .containsExactly(4, 5, 2, 6, 3, 1);
        }

        @Test
        void referenceTreeIterative() {
            assertThat(BinaryTreeTraversal.postorderIterative(makeReferenceTree()))
                    .containsExactly(4, 5, 2, 6, 3, 1);
        }

        @Test
        void emptyAndSingle() {
            assertThat(BinaryTreeTraversal.postorderIterative(null)).isEmpty();
            BinaryTreeTraversal.TreeNode single = new BinaryTreeTraversal.TreeNode(3);
            assertThat(BinaryTreeTraversal.postorderIterative(single)).containsExactly(3);
        }

        @Test
        void matchesRecursiveOracle() {
            Integer[][] trees = {
                    {1, 2, 3, 4, 5, null, 6},
                    {1},
                    {1, 2},
                    {1, null, 2},
                    {3, 9, 20, null, null, 15, 7},
                    {1, 2, 3, 4, 5, 6, 7}
            };
            for (Integer[] t : trees) {
                BinaryTreeTraversal.TreeNode root = BinaryTreeTraversal.build(t);
                assertThat(BinaryTreeTraversal.postorderIterative(root))
                        .as("tree=%s", java.util.Arrays.toString(t))
                        .isEqualTo(BinaryTreeTraversal.postorderRecursive(root));
            }
        }
    }

    // ------------------------------------------------------------------
    // 4. Level-order
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Level-order (BFS)")
    class LevelOrderTests {

        @Test
        void referenceTree() {
            assertThat(BinaryTreeTraversal.levelOrder(makeReferenceTree()))
                    .containsExactly(
                            List.of(1),
                            List.of(2, 3),
                            List.of(4, 5, 6));
        }

        @Test
        void emptyAndSingle() {
            assertThat(BinaryTreeTraversal.levelOrder(null)).isEmpty();
            assertThat(BinaryTreeTraversal.levelOrder(
                    new BinaryTreeTraversal.TreeNode(1)))
                    .containsExactly(List.of(1));
        }

        @Test
        void matchesDepthRecordingOracle() {
            Integer[][] trees = {
                    {1, 2, 3, 4, 5, null, 6},
                    {1},
                    {1, 2},
                    {1, null, 2},
                    {3, 9, 20, null, null, 15, 7},
                    {1, 2, 3, 4, 5, 6, 7}
            };
            for (Integer[] t : trees) {
                BinaryTreeTraversal.TreeNode root = BinaryTreeTraversal.build(t);
                assertThat(BinaryTreeTraversal.levelOrder(root))
                        .as("tree=%s", java.util.Arrays.toString(t))
                        .isEqualTo(levelOrderOracle(root));
            }
        }
    }

    // ------------------------------------------------------------------
    // 5. Zigzag level order
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Zigzag level order")
    class ZigzagTests {

        @Test
        void referenceTree() {
            // level 1 (idx 1) reversed: [2,3] → [3,2]
            assertThat(BinaryTreeTraversal.zigzagLevelOrder(makeReferenceTree()))
                    .containsExactly(
                            List.of(1),
                            List.of(3, 2),
                            List.of(4, 5, 6));
        }

        @Test
        void fourLevels() {
            //        3
            //       / \
            //      9  20
            //        /  \
            //      15    7
            //   level 0: [3]
            //   level 1: [9,20] → reversed: [20,9]
            //   level 2: [15,7] → as-is: [15,7]
            BinaryTreeTraversal.TreeNode root = BinaryTreeTraversal.build(
                    new Integer[]{3, 9, 20, null, null, 15, 7});
            assertThat(BinaryTreeTraversal.zigzagLevelOrder(root))
                    .containsExactly(
                            List.of(3),
                            List.of(20, 9),
                            List.of(15, 7));
        }

        @Test
        void oddLevelsReversed() {
            //        1
            //       / \
            //      2   3
            //     /   /
            //    4   5
            //   level 0: [1]
            //   level 1: [3,2]
            //   level 2: [4,5]
            BinaryTreeTraversal.TreeNode root = BinaryTreeTraversal.build(
                    new Integer[]{1, 2, 3, 4, null, 5, null});
            assertThat(BinaryTreeTraversal.zigzagLevelOrder(root))
                    .containsExactly(
                            List.of(1),
                            List.of(3, 2),
                            List.of(4, 5));
        }

        @Test
        void empty() {
            assertThat(BinaryTreeTraversal.zigzagLevelOrder(null)).isEmpty();
        }
    }

    // ------------------------------------------------------------------
    // 6. Right side view
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Right side view")
    class RightSideViewTests {

        @Test
        void referenceTree() {
            // level 0: 1; level 1: 3; level 2: 6
            assertThat(BinaryTreeTraversal.rightSideView(makeReferenceTree()))
                    .containsExactly(1, 3, 6);
        }

        @Test
        void rightSubtreeLonger() {
            //        1
            //       /
            //      2
            //       \
            //        3
            // only level 1 has a "rightmost" on the left side chain
            BinaryTreeTraversal.TreeNode root = BinaryTreeTraversal.build(
                    new Integer[]{1, 2, null, null, 3});
            // level 0: 1, level 1: 2, level 2: 3
            assertThat(BinaryTreeTraversal.rightSideView(root))
                    .containsExactly(1, 2, 3);
        }

        @Test
        void classicExample() {
            // [1,2,3,null,5,null,4] → right view [1,3,4]
            BinaryTreeTraversal.TreeNode root = BinaryTreeTraversal.build(
                    new Integer[]{1, 2, 3, null, 5, null, 4});
            assertThat(BinaryTreeTraversal.rightSideView(root))
                    .containsExactly(1, 3, 4);
        }

        @Test
        void emptyAndSingle() {
            assertThat(BinaryTreeTraversal.rightSideView(null)).isEmpty();
            assertThat(BinaryTreeTraversal.rightSideView(
                    new BinaryTreeTraversal.TreeNode(9)))
                    .containsExactly(9);
        }
    }

    // ------------------------------------------------------------------
    // 7. Vertical order (LC 987)
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Vertical order (LC 987 — sorted by col, row, val)")
    class VerticalOrderTests {

        @Test
        void referenceTree() {
            // 1@(col0,row0), 2@(-1,1), 3@(1,1),
            // 4@(-2,2), 5@(0,2), 6@(2,2)
            // Sorted: col -2 → [4]; col -1 → [2];
            //         col 0 → [1(row0), 5(row2)]; col 1 → [3]; col 2 → [6]
            assertThat(BinaryTreeTraversal.verticalTraversal(makeReferenceTree()))
                    .containsExactly(
                            List.of(4),
                            List.of(2),
                            List.of(1, 5),
                            List.of(3),
                            List.of(6));
        }

        @Test
        void tieBrokenByValue() {
            //        0
            //       / \
            //     -1   1
            //     /     \
            //   -2       2
            // col 0: only 0; other cols one node each — no ties
            BinaryTreeTraversal.TreeNode root = BinaryTreeTraversal.build(
                    new Integer[]{0, -1, 1, -2, null, null, 2});
            assertThat(BinaryTreeTraversal.verticalTraversal(root))
                    .containsExactly(
                            List.of(-2),
                            List.of(-1),
                            List.of(0),
                            List.of(1),
                            List.of(2));
        }

        @Test
        void sameColSameRowSortedByValue() {
            //        1
            //       / \
            //      2   3
            //    (col -1,row1) and (col +1,row1) — different cols
            // Need two at same (col, row):
            //        1
            //       / \
            //      2   3
            //       \ /
            //       4 5
            // 4 at (col0, row2), 5 at (col0, row2) — tie
            BinaryTreeTraversal.TreeNode root = BinaryTreeTraversal.build(
                    new Integer[]{1, 2, 3, null, 4, 5, null});
            // 2@(-1,1), 4@(0,2); 3@(1,1), 5@(0,2)
            // col 0 has 1(row0), 4(row2), 5(row2) — 4,5 tied, sorted → [1, 4, 5]
            assertThat(BinaryTreeTraversal.verticalTraversal(root))
                    .containsExactly(
                            List.of(2),
                            List.of(1, 4, 5),
                            List.of(3));
        }

        @Test
        void singleNode() {
            assertThat(BinaryTreeTraversal.verticalTraversal(
                    new BinaryTreeTraversal.TreeNode(42)))
                    .containsExactly(List.of(42));
        }

        @Test
        void empty() {
            assertThat(BinaryTreeTraversal.verticalTraversal(null)).isEmpty();
        }

        @Test
        void matchesTupleSortOracle() {
            Integer[][] trees = {
                    {1, 2, 3, 4, 5, null, 6},
                    {1},
                    {1, 2, 3, null, 4, 5, null},
                    {3, 1, 4, null, 2},
                    {1, 2, 3, 4, 5, 6, 7}
            };
            for (Integer[] t : trees) {
                BinaryTreeTraversal.TreeNode root = BinaryTreeTraversal.build(t);
                assertThat(BinaryTreeTraversal.verticalTraversal(root))
                        .as("tree=%s", java.util.Arrays.toString(t))
                        .isEqualTo(verticalOracle(root));
            }
        }
    }

    // ------------------------------------------------------------------
    // 8. Serialize / Deserialize
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Serialize / Deserialize (pre-order with # for null)")
    class SerializeTests {

        @Test
        void referenceTreeEncoding() {
            // Preorder with nulls: 1, 2, 4, #, #, 5, #, #, 3, #, 6, #, #
            assertThat(BinaryTreeTraversal.serialize(makeReferenceTree()))
                    .isEqualTo("1,2,4,#,#,5,#,#,3,#,6,#,#,");
        }

        @Test
        void roundTripReferenceTree() {
            BinaryTreeTraversal.TreeNode original = makeReferenceTree();
            BinaryTreeTraversal.TreeNode restored =
                    BinaryTreeTraversal.deserialize(BinaryTreeTraversal.serialize(original));
            assertSameTree(original, restored, "roundtrip reference tree");
        }

        @Test
        void roundTripRandomish() {
            Integer[][] trees = {
                    {1, 2, 3, 4, 5, null, 6},
                    {1},
                    {1, 2},
                    {1, null, 2},
                    {3, 9, 20, null, null, 15, 7},
                    {1, 2, 3, 4, 5, 6, 7},
                    {-1, -2, -3},
                    {1, null, 2, null, 3, null, 4}
            };
            for (Integer[] t : trees) {
                BinaryTreeTraversal.TreeNode original = BinaryTreeTraversal.build(t);
                String encoded = BinaryTreeTraversal.serialize(original);
                BinaryTreeTraversal.TreeNode restored = BinaryTreeTraversal.deserialize(encoded);
                assertSameTree(original, restored, "roundtrip tree="
                        + java.util.Arrays.toString(t));
            }
        }

        @Test
        void nullRoundTrip() {
            assertThat(BinaryTreeTraversal.serialize(null)).isEqualTo("#,");
            assertThat(BinaryTreeTraversal.deserialize("#,")).isNull();
            assertThat(BinaryTreeTraversal.deserialize("")).isNull();
            assertThat(BinaryTreeTraversal.deserialize(null)).isNull();
        }

        @Test
        void singleNodeRoundTrip() {
            BinaryTreeTraversal.TreeNode single = new BinaryTreeTraversal.TreeNode(7);
            String encoded = BinaryTreeTraversal.serialize(single);
            assertThat(encoded).isEqualTo("7,#,#,");
            assertSameTree(single, BinaryTreeTraversal.deserialize(encoded), "single node");
        }

        @Test
        void preservesStructureIncludingMissingChildren() {
            // left-skewed but with an explicit right-null marker difference:
            // 1(2, null) vs 1(null, 2) must serialize differently and restore correctly
            BinaryTreeTraversal.TreeNode leftOnly = BinaryTreeTraversal.build(
                    new Integer[]{1, 2, null});
            BinaryTreeTraversal.TreeNode rightOnly = BinaryTreeTraversal.build(
                    new Integer[]{1, null, 2});

            String l = BinaryTreeTraversal.serialize(leftOnly);
            String r = BinaryTreeTraversal.serialize(rightOnly);
            assertThat(l).isNotEqualTo(r);

            assertSameTree(leftOnly, BinaryTreeTraversal.deserialize(l), "left-only");
            assertSameTree(rightOnly, BinaryTreeTraversal.deserialize(r), "right-only");
        }
    }

    // ------------------------------------------------------------------
    // 9. Max depth
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("maxDepth")
    class MaxDepthTests {

        @Test
        void referenceTree() {
            assertThat(BinaryTreeTraversal.maxDepth(makeReferenceTree())).isEqualTo(3);
        }

        @Test
        void emptyAndSingle() {
            assertThat(BinaryTreeTraversal.maxDepth(null)).isZero();
            assertThat(BinaryTreeTraversal.maxDepth(
                    new BinaryTreeTraversal.TreeNode(1))).isEqualTo(1);
        }

        @Test
        void skewed() {
            assertThat(BinaryTreeTraversal.maxDepth(BinaryTreeTraversal.build(
                    new Integer[]{1, 2, 3, 4, 5, 6, 7}))).isEqualTo(3);
            assertThat(BinaryTreeTraversal.maxDepth(BinaryTreeTraversal.build(
                    new Integer[]{1, 2, null, 3, null, 4}))).isEqualTo(4);
        }

        @Test
        void matchesLevelCountOracle() {
            Integer[][] trees = {
                    {1, 2, 3, 4, 5, null, 6},
                    {1},
                    {1, 2, null, 3, null, 4},
                    {3, 9, 20, null, null, 15, 7},
                    {1, 2, 3, 4, 5, 6, 7}
            };
            for (Integer[] t : trees) {
                BinaryTreeTraversal.TreeNode root = BinaryTreeTraversal.build(t);
                assertThat(BinaryTreeTraversal.maxDepth(root))
                        .as("tree=%s", java.util.Arrays.toString(t))
                        .isEqualTo(BinaryTreeTraversal.levelOrder(root).size());
            }
        }
    }

    // ------------------------------------------------------------------
    // 10. Diameter
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("diameterOfBinaryTree")
    class DiameterTests {

        @Test
        void referenceTree() {
            // longest path 4 → 2 → 1 → 3 → 6, which is 4 edges
            assertThat(BinaryTreeTraversal.diameterOfBinaryTree(makeReferenceTree()))
                    .isEqualTo(4);
        }

        @Test
        void emptyAndSingle() {
            assertThat(BinaryTreeTraversal.diameterOfBinaryTree(null)).isZero();
            assertThat(BinaryTreeTraversal.diameterOfBinaryTree(
                    new BinaryTreeTraversal.TreeNode(1))).isZero();
        }

        @Test
        void twoNodeChain() {
            assertThat(BinaryTreeTraversal.diameterOfBinaryTree(
                    BinaryTreeTraversal.build(new Integer[]{1, 2}))).isEqualTo(1);
        }

        @Test
        void balancedTree() {
            // root with two leaves: diameter = 2
            assertThat(BinaryTreeTraversal.diameterOfBinaryTree(
                    BinaryTreeTraversal.build(new Integer[]{1, 2, 3}))).isEqualTo(2);
        }

        @Test
        void pathDoesNotPassThroughRoot() {
            //     1
            //    /
            //   2
            //  / \
            // 4   5
            //       \
            //        6
            // Diameter: 4→2→5→6 = 3 edges
            BinaryTreeTraversal.TreeNode root = BinaryTreeTraversal.build(
                    new Integer[]{1, 2, null, 4, 5, null, null, null, 6});
            assertThat(BinaryTreeTraversal.diameterOfBinaryTree(root)).isEqualTo(3);
        }

        @Test
        void skewed() {
            // 1-2-3-4 chain — diameter = 3 (edges)
            assertThat(BinaryTreeTraversal.diameterOfBinaryTree(
                    BinaryTreeTraversal.build(new Integer[]{1, 2, null, 3, null, 4})))
                    .isEqualTo(3);
        }
    }
}