package com.vbforge.visual;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Visualizes Pre-order Traversal of a binary tree.
 * Uses the "tree" renderer.
 *
 * <p>The tree shape is emitted inline (public fields so Gson serializes it
 * recursively). Null children are omitted from JSON by Gson — the renderer
 * treats absent / null children identically.</p>
 */
public class BinaryTreeTraversalTrace {

    /** Plain class rather than a record — maximum Gson compatibility. */
    public static class TreeNode {
        public final int val;
        public final TreeNode left;
        public final TreeNode right;
        public TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val; this.left = left; this.right = right;
        }
    }

    public static void trace(Consumer<Frame> emit) {
        //         1
        //        / \
        //       2   3
        //      / \   \
        //     4   5   6
        TreeNode root = new TreeNode(1,
                new TreeNode(2,
                        new TreeNode(4, null, null),
                        new TreeNode(5, null, null)),
                new TreeNode(3,
                        null,
                        new TreeNode(6, null, null)));

        List<Integer> visited = new ArrayList<>();
        emit.accept(frame(root, visited, -1,
                "Ready to traverse — pre-order (root → left → right)"));
        preorder(root, visited, emit);
        emit.accept(frame(root, visited, -1,
                "Done — " + String.join(" → ", toStrings(visited))));
    }

    private static void preorder(TreeNode node, List<Integer> visited, Consumer<Frame> emit) {
        if (node == null) return;
        visited.add(node.val);
        emit.accept(frame(nodeOf(node, node), visited, node.val, "Visit " + node.val));
        preorder(node.left,  visited, emit);
        preorder(node.right, visited, emit);
    }

    /** Helper — returns the node itself (existed to disambiguate earlier draft). */
    private static TreeNode nodeOf(TreeNode n, TreeNode m) { return m; }

    private static List<String> toStrings(List<Integer> xs) {
        List<String> out = new ArrayList<>(xs.size());
        for (int x : xs) out.add(String.valueOf(x));
        return out;
    }

    private static Frame frame(TreeNode root, List<Integer> visited,
                               int currentVal, String note) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("tree",         root);
        d.put("visitedOrder", new ArrayList<>(visited));   // defensive copy
        d.put("currentVal",   currentVal);
        return Frame.of("tree", d, note);
    }
}