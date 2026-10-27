package com.vbforge.visual.patterns;

import com.vbforge.visual.Algorithm;
import com.vbforge.visual.Data;
import com.vbforge.visual.Frame;
import com.vbforge.visual.Meta;
import com.vbforge.visual.Meta.Field;
import com.vbforge.visual.Meta.Preset;
import com.vbforge.visual.Params;
import com.vbforge.visual.Viz;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Binary tree traversals, shown the way they are written in production: the
 * <em>iterative</em> versions, so the explicit stack / queue is visible.
 *
 * <p>Time O(n), space O(h) for DFS orders and O(w) for level order. Renderer: {@code tree}.
 */
public final class TreeTraversalTrace implements Algorithm {

    public enum Order { PRE, IN, POST, LEVEL }

    private static final int MAX_NODES = 31, MAX_DEPTH = 6;

    private final Order order;

    public TreeTraversalTrace(Order order) {
        this.order = order;
    }

    // -------------------------------------------------------------------- meta

    @Override
    public Meta meta() {
        return switch (order) {
            case PRE -> info("tree-preorder", "Pre-order  (root · left · right)", "Binary Tree Traversal",
                    "Visit the node first, then its subtrees. Push the right child before the left so the left pops first.",
                    "O(n)", "O(h)",
                    "Pre-order is the order you would use to copy or serialize a tree – the root always comes before "
                            + "its descendants. The recursive form is the definition; the iterative one is where bugs live.",
                    List.of("stack = [root]",
                            "while stack is not empty:",
                            "    node = stack.pop()",
                            "    visit(node)",
                            "    push node.right, then node.left     // left is popped first"));
            case IN -> info("tree-inorder", "In-order  (left · root · right)", "Binary Tree Traversal",
                    "Dive down the left spine pushing nodes; pop one, visit it, then turn to its right subtree.",
                    "O(n)", "O(h)",
                    "In-order on a binary SEARCH tree yields the keys in sorted order – try the BST preset. "
                            + "Forgetting `cur = cur.right` after the visit is the classic iterative bug.",
                    List.of("stack = []; cur = root",
                            "while cur != null or stack is not empty:",
                            "    while cur != null: push(cur); cur = cur.left",
                            "    cur = stack.pop()",
                            "    visit(cur)",
                            "    cur = cur.right"));
            case POST -> info("tree-postorder", "Post-order  (left · right · root)", "Binary Tree Traversal",
                    "Children before parent. One stack plus a `last visited` pointer tells us whether we already came back from the right.",
                    "O(n)", "O(h)",
                    "Post-order is what you need to delete a tree or compute heights: a node is handled only when "
                            + "both subtrees are done. It is the one iterative DFS that is not mechanical – you need the "
                            + "`last` pointer (or two stacks).",
                    List.of("stack = []; cur = root; last = null",
                            "while cur != null or stack is not empty:",
                            "    if cur != null: push(cur); cur = cur.left",
                            "    else: top = stack.peek()",
                            "        if top.right != null and top.right != last: cur = top.right",
                            "        else: visit(top); last = stack.pop()"));
            case LEVEL -> info("tree-levelorder", "Level-order  (BFS)", "Binary Tree Traversal",
                    "A FIFO queue visits the tree ring by ring, top to bottom, left to right.",
                    "O(n)", "O(w)",
                    "Swap the stack for a queue and DFS becomes BFS. Snapshotting `size = queue.size()` before the "
                            + "inner loop is the one trick that turns a flat traversal into per-level lists "
                            + "(zigzag, right-side view …).",
                    List.of("queue = [root]",
                            "while queue is not empty:",
                            "    node = queue.poll()",
                            "    visit(node)",
                            "    queue.add(node.left), queue.add(node.right)   // if present"));
        };
    }

    private static Meta info(String id, String name, String pattern, String blurb, String time, String space,
                             String insight, List<String> pseudo) {
        return new Meta(id, name, pattern, blurb, time, space, insight, pseudo,
                List.of(Field.tree("tree", "Tree (level-order, null = missing)",
                        new Integer[]{1, 2, 3, 4, 5, null, 6},
                        "LeetCode style: 1,2,3,4,5,null,6  (max 31 nodes, 6 levels)", MAX_NODES)),
                List.of(
                        Preset.of("Textbook", "tree", new Integer[]{1, 2, 3, 4, 5, null, 6}),
                        Preset.of("Perfect (7 nodes)", "tree", new Integer[]{1, 2, 3, 4, 5, 6, 7}),
                        Preset.of("Binary search tree", "tree", new Integer[]{8, 3, 10, 1, 6, null, 14, null, null, 4, 7, 13}),
                        Preset.of("Right-leaning", "tree", new Integer[]{1, null, 2, null, 3, null, 4})));
    }

    // ------------------------------------------------------------------- trace

    private static final class Tree {
        final int[] val, left, right;
        final int size;

        Tree(Integer[] tokens) {
            List<Integer> vals = new ArrayList<>();
            List<int[]> kids = new ArrayList<>();
            Deque<Integer> q = new ArrayDeque<>();
            vals.add(tokens[0]);
            kids.add(new int[]{-1, -1});
            q.add(0);
            int pos = 1;
            while (!q.isEmpty() && pos < tokens.length) {
                int parent = q.poll();
                for (int side = 0; side < 2 && pos < tokens.length; side++) {
                    Integer t = tokens[pos++];
                    if (t == null) continue;
                    vals.add(t);
                    kids.add(new int[]{-1, -1});
                    kids.get(parent)[side] = vals.size() - 1;
                    q.add(vals.size() - 1);
                }
            }
            if (pos < tokens.length)
                throw new IllegalArgumentException("'tree' has extra values – children of null nodes are not listed");
            size = vals.size();
            val = vals.stream().mapToInt(Integer::intValue).toArray();
            left = new int[size];
            right = new int[size];
            for (int i = 0; i < size; i++) {
                left[i] = kids.get(i)[0];
                right[i] = kids.get(i)[1];
            }
            if (depth(0) > MAX_DEPTH)
                throw new IllegalArgumentException("'tree' is too deep (max " + MAX_DEPTH + " levels)");
        }

        int depth(int n) {
            return n < 0 ? 0 : 1 + Math.max(depth(left[n]), depth(right[n]));
        }

        Map<String, Object> shape() {
            return Viz.kv("v", val, "l", left, "r", right);
        }
    }

    /** Shared state + frame builder for one run. */
    private static final class Run {
        final Tree t;
        final char[] mark;
        final List<Integer> out = new ArrayList<>();
        final Consumer<Frame> emit;
        final String auxLabel;
        Deque<Integer> aux = new ArrayDeque<>();

        Run(Tree t, Consumer<Frame> emit, String auxLabel) {
            this.t = t;
            this.emit = emit;
            this.auxLabel = auxLabel;
            this.mark = new char[t.size];
            Arrays.fill(mark, '.');
        }

        void show(String note, int line) {
            List<Integer> auxVals = new ArrayList<>();
            // stacks are shown bottom → top, the queue front → back
            Iterator<Integer> it = auxLabel.startsWith("stack") ? aux.descendingIterator() : aux.iterator();
            while (it.hasNext()) auxVals.add(t.val[it.next()]);
            emit.accept(Frame.of("tree", Data.of()
                    .with("tree", t.shape())
                    .with("marks", new String(mark))
                    .with("info", Viz.kv("visited", out.size() + " / " + t.size))
                    .with("lists", List.of(Viz.list(auxLabel, auxVals), Viz.list("output", out))), note, line));
        }

        void visit(int n) {
            out.add(t.val[n]);
        }
    }

    @Override
    public void trace(Params p, Consumer<Frame> emit) {
        Tree t = new Tree(p.tree("tree", MAX_NODES));
        switch (order) {
            case PRE -> pre(t, emit);
            case IN -> in(t, emit);
            case POST -> post(t, emit);
            case LEVEL -> level(t, emit);
        }
    }

    private void pre(Tree t, Consumer<Frame> emit) {
        Run r = new Run(t, emit, "stack (top →)");
        r.aux.push(0);
        r.mark[0] = 'f';
        r.show("Push the root", 0);
        while (!r.aux.isEmpty()) {
            int n = r.aux.pop();
            r.mark[n] = 'c';
            r.visit(n);
            r.show("pop " + t.val[n] + "  →  visit it", 3);
            r.mark[n] = 'v';
            if (t.right[n] >= 0) { r.aux.push(t.right[n]); r.mark[t.right[n]] = 'f'; }
            if (t.left[n] >= 0)  { r.aux.push(t.left[n]);  r.mark[t.left[n]] = 'f'; }
            if (t.right[n] >= 0 || t.left[n] >= 0)
                r.show("push the children of " + t.val[n] + " (right first, so left is popped first)", 4);
        }
        r.show("Stack empty – pre-order complete: " + r.out, 1);
    }

    private void in(Tree t, Consumer<Frame> emit) {
        Run r = new Run(t, emit, "stack (top →)");
        int cur = 0;
        r.show("Start at the root with an empty stack", 0);
        while (cur != -1 || !r.aux.isEmpty()) {
            while (cur != -1) {
                r.aux.push(cur);
                r.mark[cur] = 'c';
                r.show("go left: push " + t.val[cur] + (t.left[cur] >= 0 ? "" : "  (no left child)"), 2);
                r.mark[cur] = 'f';
                cur = t.left[cur];
            }
            cur = r.aux.pop();
            r.mark[cur] = 'c';
            r.visit(cur);
            r.show("pop " + t.val[cur] + "  →  visit it" + (t.right[cur] >= 0 ? ", then turn right" : ""), 4);
            r.mark[cur] = 'v';
            cur = t.right[cur];
        }
        r.show("Stack empty – in-order complete: " + r.out, 1);
    }

    private void post(Tree t, Consumer<Frame> emit) {
        Run r = new Run(t, emit, "stack (top →)");
        int cur = 0, last = -1;
        r.show("Start at the root; `last` = nothing visited yet", 0);
        while (cur != -1 || !r.aux.isEmpty()) {
            if (cur != -1) {
                r.aux.push(cur);
                r.mark[cur] = 'c';
                r.show("descend left: push " + t.val[cur], 2);
                r.mark[cur] = 'f';
                cur = t.left[cur];
            } else {
                int top = r.aux.peek();
                if (t.right[top] >= 0 && last != t.right[top]) {
                    r.mark[top] = 'c';
                    r.show(t.val[top] + " has an unvisited right child  →  go right", 3);
                    r.mark[top] = 'f';
                    cur = t.right[top];
                } else {
                    r.aux.pop();
                    r.mark[top] = 'c';
                    r.visit(top);
                    r.show("both subtrees of " + t.val[top] + " are done  →  visit it", 5);
                    r.mark[top] = 'v';
                    last = top;
                }
            }
        }
        r.show("Stack empty – post-order complete: " + r.out, 1);
    }

    private void level(Tree t, Consumer<Frame> emit) {
        Run r = new Run(t, emit, "queue (front →)");
        r.aux.add(0);
        r.mark[0] = 'f';
        r.show("Enqueue the root", 0);
        while (!r.aux.isEmpty()) {
            int n = r.aux.poll();
            r.mark[n] = 'c';
            r.visit(n);
            r.show("poll " + t.val[n] + "  →  visit it", 2);
            r.mark[n] = 'v';
            int added = 0;
            if (t.left[n] >= 0)  { r.aux.add(t.left[n]);  r.mark[t.left[n]] = 'f'; added++; }
            if (t.right[n] >= 0) { r.aux.add(t.right[n]); r.mark[t.right[n]] = 'f'; added++; }
            if (added > 0) r.show("enqueue the " + (added == 2 ? "two children" : "child") + " of " + t.val[n], 4);
        }
        r.show("Queue empty – level-order complete: " + r.out, 1);
    }
}
