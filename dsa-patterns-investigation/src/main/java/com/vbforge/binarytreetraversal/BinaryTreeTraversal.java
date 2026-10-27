package com.vbforge.binarytreetraversal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * <h2>Binary Tree Traversal Pattern</h2>
 *
 * <p>Six different tree-shaped algorithms, each with a recursive and an
 * iterative form where applicable:</p>
 * <ul>
 *   <li><b>Pre-order</b>: root, left, right — topological copy / serialization.</li>
 *   <li><b>In-order</b>: left, root, right — BST sorted iteration.</li>
 *   <li><b>Post-order</b>: left, right, root — size / aggregation.</li>
 *   <li><b>Level-order</b>: BFS queue — level-by-level views.</li>
 * </ul>
 *
 * <p><b>Recursive vs iterative:</b> the recursive forms are the natural
 * definition and are used as oracles in the tests; the iterative forms are
 * the ones that have to be verified.</p>
 *
 * <p>All methods return fresh collections and <b>never mutate the tree</b>.</p>
 */
public final class BinaryTreeTraversal {

    private BinaryTreeTraversal() { }

    /** Classic binary tree node — mutable so tests can build arbitrary shapes. */
    public static final class TreeNode {
        public int val;
        public TreeNode left;
        public TreeNode right;

        public TreeNode() { }
        public TreeNode(int val) { this.val = val; }
        public TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    // =====================================================================
    //  Tree construction — LeetCode-style level-order with nulls
    // =====================================================================

    /**
     * Builds a tree from an {@code Integer[]} using LeetCode's level-order
     * encoding: the array is read left-to-right; non-null entries become
     * nodes in BFS order, and nulls mark "no child in this slot."
     */
    public static TreeNode build(Integer[] values) {
        if (values == null || values.length == 0 || values[0] == null) return null;
        TreeNode root = new TreeNode(values[0]);
        Deque<TreeNode> q = new ArrayDeque<>();
        q.offer(root);
        int i = 1;
        while (!q.isEmpty() && i < values.length) {
            TreeNode node = q.poll();
            if (i < values.length && values[i] != null) {
                node.left = new TreeNode(values[i]);
                q.offer(node.left);
            }
            i++;
            if (i < values.length && values[i] != null) {
                node.right = new TreeNode(values[i]);
                q.offer(node.right);
            }
            i++;
        }
        return root;
    }

    // =====================================================================
    //  1. Pre-order — root, left, right
    // =====================================================================

    /** Recursive pre-order. Reference implementation. */
    public static List<Integer> preorderRecursive(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        preorderHelper(root, out);
        return out;
    }

    private static void preorderHelper(TreeNode node, List<Integer> out) {
        if (node == null) return;
        out.add(node.val);
        preorderHelper(node.left, out);
        preorderHelper(node.right, out);
    }

    /**
     * Iterative pre-order using an explicit stack.
     *
     * <p>Push <b>right before left</b> so that left pops first — the stack
     * is LIFO, and we want to visit left before right.</p>
     */
    public static List<Integer> preorderIterative(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        if (root == null) return out;

        Deque<TreeNode> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode node = stack.pop();
            out.add(node.val);
            if (node.right != null) stack.push(node.right);    // right pushed first
            if (node.left != null)  stack.push(node.left);     // left pushed second → pops first
        }
        return out;
    }

    // =====================================================================
    //  2. In-order — left, root, right
    // =====================================================================

    /** Recursive in-order. Reference implementation. */
    public static List<Integer> inorderRecursive(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        inorderHelper(root, out);
        return out;
    }

    private static void inorderHelper(TreeNode node, List<Integer> out) {
        if (node == null) return;
        inorderHelper(node.left, out);
        out.add(node.val);
        inorderHelper(node.right, out);
    }

    /**
     * Iterative in-order — dive left first, then pop-and-visit, then dive
     * right. The "dive left" loop naturally re-traverses after each pop.
     */
    public static List<Integer> inorderIterative(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        Deque<TreeNode> stack = new ArrayDeque<>();
        TreeNode curr = root;
        while (curr != null || !stack.isEmpty()) {
            while (curr != null) {                              // dive as far left as possible
                stack.push(curr);
                curr = curr.left;
            }
            curr = stack.pop();                                 // leftmost unvisited
            out.add(curr.val);                                  // visit
            curr = curr.right;                                  // then its right subtree
        }
        return out;
    }

    // =====================================================================
    //  3. Post-order — left, right, root
    // =====================================================================

    /** Recursive post-order. Reference implementation. */
    public static List<Integer> postorderRecursive(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        postorderHelper(root, out);
        return out;
    }

    private static void postorderHelper(TreeNode node, List<Integer> out) {
        if (node == null) return;
        postorderHelper(node.left, out);
        postorderHelper(node.right, out);
        out.add(node.val);
    }

    /**
     * Iterative post-order via "reverse of modified pre-order".
     *
     * <p>The trick: generate {@code root → right → left} (a mirror of
     * pre-order, achieved by pushing left before right), prepending each
     * value into a {@code LinkedList} (O(1) {@code addFirst}). The final
     * result is {@code left → right → root} = post-order.</p>
     *
     * <p>Why not a single-stack "dive and visit when no children" version?
     * That works too, but it requires a {@code prev} pointer and is far
     * easier to get wrong. The two-stack-reverse approach has one invariant:
     * "prepend every pop."</p>
     */
    public static List<Integer> postorderIterative(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        if (root == null) return out;

        // Build root → right → left in a stack, prepend each value.
        // The prepend reverses the accumulated order into left → right → root.
        LinkedList<Integer> reversed = new LinkedList<>();
        Deque<TreeNode> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode node = stack.pop();
            reversed.addFirst(node.val);
            if (node.left != null)  stack.push(node.left);     // left pushed first → pops last
            if (node.right != null) stack.push(node.right);    // right pushed second → pops first
        }
        out.addAll(reversed);
        return out;
    }

    // =====================================================================
    //  4. Level-order — BFS queue, level-by-level
    // =====================================================================

    /**
     * <b>Level-order traversal</b> — one inner list per depth.
     *
     * <p>The size-snapshot trick ({@code int size = q.size()}) is what
     * separates levels — without it, we'd get one flat list.</p>
     */
    public static List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> out = new ArrayList<>();
        if (root == null) return out;

        Deque<TreeNode> q = new ArrayDeque<>();
        q.offer(root);
        while (!q.isEmpty()) {
            int size = q.size();
            List<Integer> level = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                TreeNode node = q.poll();
                level.add(node.val);
                if (node.left != null)  q.offer(node.left);
                if (node.right != null) q.offer(node.right);
            }
            out.add(level);
        }
        return out;
    }

    // =====================================================================
    //  5. Zigzag level-order
    // =====================================================================

    /**
     * <b>Binary Tree Zigzag Level Order</b> — same BFS skeleton, but every
     * odd-numbered level (0-indexed) is reversed.
     */
    public static List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        List<List<Integer>> out = levelOrder(root);
        for (int i = 1; i < out.size(); i += 2) {
            Collections.reverse(out.get(i));
        }
        return out;
    }

    // =====================================================================
    //  6. Right side view
    // =====================================================================

    /** <b>Binary Tree Right Side View</b> — the last element of each level. */
    public static List<Integer> rightSideView(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        for (List<Integer> level : levelOrder(root)) {
            out.add(level.get(level.size() - 1));
        }
        return out;
    }

    // =====================================================================
    //  7. Vertical order (LC 987 — sorted by col, row, val)
    // =====================================================================

    /**
     * <b>Vertical Order Traversal II</b> — group nodes by column
     * ({@code root} at column 0, left = col-1, right = col+1), sorted:
     *
     * <ol>
     *   <li>Groups ordered by ascending column.</li>
     *   <li>Within a group, ordered by ascending row.</li>
     *   <li>Ties (same col, same row) broken by ascending <b>value</b>.</li>
     * </ol>
     *
     * <p>Implementation collects {@code (col, row, val)} tuples via DFS,
     * sorts, then groups. The sort-then-group step is what makes ties
     * deterministic — an alternative BFS-with-columns approach gives a
     * different tie-break (insertion order) that matches LC 314, not 987.</p>
     */
    public static List<List<Integer>> verticalTraversal(TreeNode root) {
        List<int[]> entries = new ArrayList<>();            // [col, row, val]
        collectVertical(root, 0, 0, entries);
        entries.sort((a, b) -> {
            if (a[0] != b[0]) return Integer.compare(a[0], b[0]);   // col
            if (a[1] != b[1]) return Integer.compare(a[1], b[1]);   // row
            return Integer.compare(a[2], b[2]);                     // val
        });

        Map<Integer, List<Integer>> byCol = new TreeMap<>();
        for (int[] e : entries) {
            byCol.computeIfAbsent(e[0], k -> new ArrayList<>()).add(e[2]);
        }
        return new ArrayList<>(byCol.values());
    }

    private static void collectVertical(TreeNode node, int col, int row, List<int[]> out) {
        if (node == null) return;
        out.add(new int[]{col, row, node.val});
        collectVertical(node.left,  col - 1, row + 1, out);
        collectVertical(node.right, col + 1, row + 1, out);
    }

    // =====================================================================
    //  8. Serialize / Deserialize — pre-order with '#' for null
    // =====================================================================

    /**
     * <b>Serialize</b> — pre-order traversal, {@code "#"} for nulls,
     * comma-separated. The trailing {@code "#,"} pair is what makes the
     * deserializer self-delimiting.
     */
    public static String serialize(TreeNode root) {
        StringBuilder sb = new StringBuilder();
        serializeHelper(root, sb);
        return sb.toString();
    }

    private static void serializeHelper(TreeNode node, StringBuilder sb) {
        if (node == null) {
            sb.append("#,");
            return;
        }
        sb.append(node.val).append(',');
        serializeHelper(node.left, sb);
        serializeHelper(node.right, sb);
    }

    /**
     * <b>Deserialize</b> — parse pre-order tokens; {@code "#"} returns
     * {@code null} and consumes one token. The recursive structure mirrors
     * {@link #serialize} exactly, so the two are inverses.
     *
     * @throws IllegalArgumentException if the input is malformed
     */
    public static TreeNode deserialize(String data) {
        if (data == null || data.isEmpty()) return null;
        Deque<String> tokens = new ArrayDeque<>();
        for (String t : data.split(",")) {
            if (!t.isEmpty()) tokens.offer(t);
        }
        return deserializeHelper(tokens);
    }

    private static TreeNode deserializeHelper(Deque<String> tokens) {
        String t = tokens.poll();
        if (t == null || t.equals("#")) return null;
        TreeNode node = new TreeNode(Integer.parseInt(t));
        node.left  = deserializeHelper(tokens);
        node.right = deserializeHelper(tokens);
        return node;
    }

    // =====================================================================
    //  9. Max depth
    // =====================================================================

    /** <b>Maximum Depth</b> — 1 + max(left depth, right depth), 0 for null. */
    public static int maxDepth(TreeNode root) {
        if (root == null) return 0;
        return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
    }

    // =====================================================================
    //  10. Diameter — longest path between any two nodes
    // =====================================================================

    /**
     * <b>Diameter of Binary Tree</b> — number of edges on the longest path
     * between any two nodes.
     *
     * <p>Key insight: the diameter path goes through some node {@code X}
     * and has length {@code depth(left of X) + depth(right of X)}. A single
     * post-order DFS computes both the depth (returned) and the diameter
     * (accumulated in an outer variable). Time O(n).</p>
     */
    public static int diameterOfBinaryTree(TreeNode root) {
        int[] best = {0};
        depthAndDiameter(root, best);
        return best[0];
    }

    private static int depthAndDiameter(TreeNode node, int[] best) {
        if (node == null) return 0;
        int left  = depthAndDiameter(node.left, best);
        int right = depthAndDiameter(node.right, best);
        best[0] = Math.max(best[0], left + right);      // path through this node
        return 1 + Math.max(left, right);
    }
}