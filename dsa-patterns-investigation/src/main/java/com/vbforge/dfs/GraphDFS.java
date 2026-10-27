package com.vbforge.dfs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <h2>Graph DFS Pattern</h2>
 *
 * <p>DFS on a general graph differs from tree DFS in one critical way:
 * a graph can have cycles, so we <b>must</b> track visited state. Three
 * flavours of state tracking appear here:</p>
 *
 * <ul>
 *   <li><b>Binary {@code visited[]}</b> — connected components, reachability
 *       on undirected graphs.</li>
 *   <li><b>Parent tracking</b> — undirected cycle detection. A back edge to
 *       {@code parent} is not a cycle; a back edge to any <em>other</em>
 *       visited node is.</li>
 *   <li><b>Three colors</b> — directed cycle detection. WHITE (unvisited),
 *       GRAY (on current stack), BLACK (fully processed). A back edge to
 *       GRAY is a cycle; a back edge to BLACK is a forward/cross edge and
 *       is fine.</li>
 * </ul>
 *
 * <p>All methods are pure — inputs are never mutated.</p>
 */
public final class GraphDFS {

    private GraphDFS() { }

    /** Mutable graph node for {@link #cloneGraph(Node)} — LC 133. */
    public static final class Node {
        public int val;
        public List<Node> neighbors;

        public Node() { val = 0; neighbors = new ArrayList<>(); }
        public Node(int val) { this.val = val; neighbors = new ArrayList<>(); }
        public Node(int val, List<Node> neighbors) {
            this.val = val;
            this.neighbors = neighbors;
        }
    }

    // ---- adjacency helpers ------------------------------------------

    private static List<List<Integer>> undirected(int n, int[][] edges) {
        List<List<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);
        }
        return adj;
    }

    private static List<List<Integer>> directed(int n, int[][] edges) {
        List<List<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) adj.get(e[0]).add(e[1]);
        return adj;
    }

    // =====================================================================
    //  1. Connected Components — undirected graph
    // =====================================================================

    /**
     * <b>Number of Connected Components</b> (LC 323) — undirected graph,
     * vertices {@code 0..n-1}.
     *
     * <p>Outer loop over every vertex; each unvisited vertex triggers a
     * DFS that marks its entire component. Count of triggers = count of
     * components.</p>
     *
     * <p>Time: O(V + E) — Space: O(V)</p>
     */
    public static int countComponents(int n, int[][] edges) {
        List<List<Integer>> adj = undirected(n, edges);
        boolean[] visited = new boolean[n];
        int components = 0;
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                dfsMark(i, adj, visited);
                components++;
            }
        }
        return components;
    }

    private static void dfsMark(int u, List<List<Integer>> adj, boolean[] visited) {
        visited[u] = true;
        for (int v : adj.get(u)) {
            if (!visited[v]) dfsMark(v, adj, visited);
        }
    }

    // =====================================================================
    //  2. Undirected Cycle Detection — parent tracking
    // =====================================================================

    /**
     * <b>Undirected Cycle Detection</b>.
     *
     * <p>During DFS, when we see an already-visited neighbour, it's
     * <em>usually</em> a cycle — <b>except</b> when that neighbour is the
     * one we just came from. That's the tree edge of the DFS forest, not a
     * back edge.</p>
     *
     * <p>So we carry {@code parent} through the recursion. A back edge to
     * anything other than {@code parent} proves a cycle.</p>
     *
     * <p>Time: O(V + E) — Space: O(V)</p>
     */
    public static boolean hasCycleUndirected(int n, int[][] edges) {
        List<List<Integer>> adj = undirected(n, edges);
        boolean[] visited = new boolean[n];
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                if (hasCycleUndirectedDfs(i, -1, adj, visited)) return true;
            }
        }
        return false;
    }

    private static boolean hasCycleUndirectedDfs(int u, int parent,
                                                 List<List<Integer>> adj,
                                                 boolean[] visited) {
        visited[u] = true;
        for (int v : adj.get(u)) {
            if (!visited[v]) {
                if (hasCycleUndirectedDfs(v, u, adj, visited)) return true;
            } else if (v != parent) {
                return true;                                // back edge = cycle
            }
        }
        return false;
    }

    // =====================================================================
    //  3. Directed Cycle Detection — three colors
    // =====================================================================

    /** Color constants: WHITE = unseen, GRAY = on stack, BLACK = done. */
    private static final int WHITE = 0, GRAY = 1, BLACK = 2;

    /**
     * <b>Directed Cycle Detection</b>.
     *
     * <p>A plain {@code visited[]} is not enough for directed graphs. If u
     * points to v, and v was <em>finished</em> in an earlier DFS branch,
     * that's a legitimate cross-edge (no cycle). If v is still on the
     * current recursion stack, that's a back edge — cycle.</p>
     *
     * <p>Three colors distinguish the two:</p>
     * <ul>
     *   <li>WHITE — never seen.</li>
     *   <li>GRAY — currently on the recursion stack.</li>
     *   <li>BLACK — finished (all descendants done).</li>
     * </ul>
     * Edge to GRAY = cycle. Edge to BLACK = fine.
     *
     * <p>Time: O(V + E) — Space: O(V)</p>
     */
    public static boolean hasCycleDirected(int n, int[][] edges) {
        List<List<Integer>> adj = directed(n, edges);
        int[] color = new int[n];                            // all WHITE
        for (int i = 0; i < n; i++) {
            if (color[i] == WHITE) {
                if (hasCycleDirectedDfs(i, adj, color)) return true;
            }
        }
        return false;
    }

    private static boolean hasCycleDirectedDfs(int u, List<List<Integer>> adj, int[] color) {
        color[u] = GRAY;
        for (int v : adj.get(u)) {
            if (color[v] == GRAY)   return true;             // back edge
            if (color[v] == WHITE && hasCycleDirectedDfs(v, adj, color)) return true;
        }
        color[u] = BLACK;
        return false;
    }

    // =====================================================================
    //  4. Topological Sort — DFS post-order reversed
    // =====================================================================

    /**
     * <b>Topological Sort</b> (LC 210) — DFS post-order, prepended on
     * finish. Returns an empty list if the graph has a cycle (since no
     * topological order exists).
     *
     * <p><b>Why prepend-on-finish works:</b> for edge u→v, DFS(u) visits
     * v first, so v finishes first. Prepending at finish time places the
     * later-finished node earlier in the output — so u comes before v.</p>
     *
     * <p>Time: O(V + E) — Space: O(V)</p>
     */
    public static List<Integer> topologicalSort(int n, int[][] edges) {
        List<List<Integer>> adj = directed(n, edges);
        int[] color = new int[n];
        LinkedList<Integer> result = new LinkedList<>();     // addFirst at finish
        for (int i = 0; i < n; i++) {
            if (color[i] == WHITE) {
                if (topoDfs(i, adj, color, result)) return List.of();   // cycle
            }
        }
        return result;
    }

    private static boolean topoDfs(int u, List<List<Integer>> adj, int[] color,
                                   LinkedList<Integer> result) {
        color[u] = GRAY;
        for (int v : adj.get(u)) {
            if (color[v] == GRAY)   return true;
            if (color[v] == WHITE && topoDfs(v, adj, color, result)) return true;
        }
        color[u] = BLACK;
        result.addFirst(u);                                 // finish time
        return false;
    }

    // =====================================================================
    //  5. Number of Islands — grid DFS
    // =====================================================================

    /**
     * <b>Number of Islands</b> (LC 200) — count 4-connected components of
     * {@code '1'} cells in a {@code m × n} grid.
     *
     * <p>Same skeleton as {@link #countComponents}, but the "graph" is the
     * grid's 4-adjacency. Uses a separate {@code visited[][]} instead of
     * mutating the input — safer for reuse.</p>
     *
     * <p>Time: O(m·n) — Space: O(m·n) for visited (or O(min(m,n)) with
     * the standard in-place {@code '0'} trick, at the cost of mutation)</p>
     */
    public static int numIslands(char[][] grid) {
        if (grid == null || grid.length == 0 || grid[0].length == 0) return 0;

        int m = grid.length, n = grid[0].length;
        boolean[][] visited = new boolean[m][n];
        int count = 0;

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == '1' && !visited[i][j]) {
                    dfsIsland(grid, visited, i, j, m, n);
                    count++;
                }
            }
        }
        return count;
    }

    private static void dfsIsland(char[][] grid, boolean[][] visited,
                                  int r, int c, int m, int n) {
        if (r < 0 || r >= m || c < 0 || c >= n)   return;
        if (visited[r][c] || grid[r][c] != '1')   return;

        visited[r][c] = true;
        dfsIsland(grid, visited, r + 1, c, m, n);
        dfsIsland(grid, visited, r - 1, c, m, n);
        dfsIsland(grid, visited, r, c + 1, m, n);
        dfsIsland(grid, visited, r, c - 1, m, n);
    }

    // =====================================================================
    //  6. Clone Graph — DFS with identity map
    // =====================================================================

    /**
     * <b>Clone Graph</b> (LC 133) — deep copy of a connected undirected
     * graph (may contain cycles).
     *
     * <p>The DFS carries a {@code Map<Node, Node>} of original → copy.
     * Before descending, we install {@code copy} in the map so that when
     * a neighbour loops back to a node we've already started copying, we
     * return the in-progress copy rather than recursing forever.</p>
     *
     * <p>This "insert-before-recurse" ordering is the key to cycle safety
     * — the same trick that makes deep-copying any cyclic structure work.</p>
     *
     * <p>Time: O(V + E) — Space: O(V)</p>
     */
    public static Node cloneGraph(Node node) {
        return cloneGraph(node, new HashMap<>());
    }

    private static Node cloneGraph(Node node, Map<Node, Node> cloned) {
        if (node == null) return null;
        Node existing = cloned.get(node);
        if (existing != null) return existing;

        Node copy = new Node(node.val);
        cloned.put(node, copy);                              // install BEFORE recursing
        for (Node neighbor : node.neighbors) {
            copy.neighbors.add(cloneGraph(neighbor, cloned));
        }
        return copy;
    }
}