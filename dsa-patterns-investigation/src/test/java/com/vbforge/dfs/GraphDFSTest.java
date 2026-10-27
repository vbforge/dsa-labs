package com.vbforge.dfs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Graph DFS Pattern")
class GraphDFSTest {

    // ==================================================================
    //  Independent oracles
    // ==================================================================

    /** Union-Find oracle for connected components. */
    private static int countComponentsUF(int n, int[][] edges) {
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        int components = n;
        for (int[] e : edges) {
            int ru = find(parent, e[0]);
            int rv = find(parent, e[1]);
            if (ru != rv) { parent[ru] = rv; components--; }
        }
        return components;
    }

    private static int find(int[] parent, int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }

    /** Union-Find cycle detector for undirected graphs. */
    private static boolean hasCycleUF(int n, int[][] edges) {
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        for (int[] e : edges) {
            int ru = find(parent, e[0]);
            int rv = find(parent, e[1]);
            if (ru == rv) return true;                       // edge within component
            parent[ru] = rv;
        }
        return false;
    }

    /**
     * Verifies {@code order} is a valid topological ordering of the
     * directed graph {@code (n, edges)}: every vertex appears exactly once,
     * and every edge {@code u→v} has {@code u} before {@code v}.
     */
    private static boolean isValidTopoOrder(List<Integer> order, int n, int[][] edges) {
        if (order.size() != n) return false;
        int[] pos = new int[n];
        Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            int v = order.get(i);
            if (v < 0 || v >= n || pos[v] != -1) return false;   // out-of-range or duplicate
            pos[v] = i;
        }
        for (int v = 0; v < n; v++) if (pos[v] == -1) return false;
        for (int[] e : edges) {
            if (pos[e[0]] >= pos[e[1]]) return false;
        }
        return true;
    }

    /** Structural equality of two graphs, tolerant of different identity maps. */
    private static void assertGraphEquals(GraphDFS.Node a, GraphDFS.Node b, String msg) {
        assertGraphEquals(a, b, new HashMap<>(), msg);
    }

    private static void assertGraphEquals(GraphDFS.Node a, GraphDFS.Node b,
                                          Map<GraphDFS.Node, GraphDFS.Node> seen,
                                          String msg) {
        if (a == null && b == null) return;
        if (a == null || b == null) {
            throw new AssertionError(msg + ": one side null");
        }
        GraphDFS.Node prior = seen.get(a);
        if (prior != null) {
            if (prior != b) throw new AssertionError(msg + ": structure mismatch at " + a.val);
            return;
        }
        if (a.val != b.val) throw new AssertionError(msg + ": val mismatch " + a.val + " vs " + b.val);
        if (a.neighbors.size() != b.neighbors.size()) {
            throw new AssertionError(msg + ": neighbor-count mismatch at " + a.val);
        }
        seen.put(a, b);
        for (int i = 0; i < a.neighbors.size(); i++) {
            assertGraphEquals(a.neighbors.get(i), b.neighbors.get(i), seen,
                    msg + "." + a.val + "[" + i + "]");
        }
    }

    /** Collects all nodes reachable from {@code start}. */
    private static Set<GraphDFS.Node> reachableNodes(GraphDFS.Node start) {
        Set<GraphDFS.Node> seen = new HashSet<>();
        if (start == null) return seen;
        Deque<GraphDFS.Node> stack = new ArrayDeque<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            GraphDFS.Node n = stack.pop();
            if (!seen.add(n)) continue;
            for (GraphDFS.Node nb : n.neighbors) if (!seen.contains(nb)) stack.push(nb);
        }
        return seen;
    }

    /** Verifies the clone shares no node objects with the original. */
    private static void assertDisjointGraphs(GraphDFS.Node a, GraphDFS.Node b, String msg) {
        Set<GraphDFS.Node> aNodes = reachableNodes(a);
        Set<GraphDFS.Node> bNodes = reachableNodes(b);
        for (GraphDFS.Node n : aNodes) {
            if (bNodes.contains(n)) throw new AssertionError(msg + ": shared node " + n.val);
        }
    }

    /** Builds a graph from LC 133 adjacency-list input: adjList[i] lists 1-indexed neighbours of node i+1. */
    private static GraphDFS.Node buildGraph(int[][] adjList) {
        if (adjList == null || adjList.length == 0) return null;
        GraphDFS.Node[] nodes = new GraphDFS.Node[adjList.length];
        for (int i = 0; i < adjList.length; i++) nodes[i] = new GraphDFS.Node(i + 1);
        for (int i = 0; i < adjList.length; i++) {
            for (int neighbor : adjList[i]) nodes[i].neighbors.add(nodes[neighbor - 1]);
        }
        return nodes[0];
    }

    // ------------------------------------------------------------------
    // 1. Count connected components
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("countComponents (LC 323)")
    class CountComponentsTests {

        @ParameterizedTest(name = "n={0}, edges={1} -> {2}")
        @MethodSource("cases")
        void countComponents(int n, int[][] edges, int expected) {
            assertThat(GraphDFS.countComponents(n, edges)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // {0,1,2}, {3,4}       → 2 components
                    Arguments.of(5, new int[][] {{0, 1}, {1, 2}, {3, 4}}, 2),

                    // 0-1-2-3-4 chain       → 1 component
                    Arguments.of(5, new int[][] {{0, 1}, {1, 2}, {2, 3}, {3, 4}}, 1),

                    // no edges, 4 vertices  → 4 isolated components
                    Arguments.of(4, new int[][] {}, 4),

                    // no edges, 1 vertex    → 1 component
                    Arguments.of(1, new int[][] {}, 1),

                    // empty graph           → 0 components
                    Arguments.of(0, new int[][] {}, 0),

                    // path {0,1,2}          → 1 component
                    Arguments.of(3, new int[][] {{0, 1}, {1, 2}}, 1),

                    // triangle + isolated   → 2 components
                    Arguments.of(4, new int[][] {{0, 1}, {1, 2}, {2, 0}}, 2)
            );
        }

        @Test
        void matchesUnionFindOracle() {
            Object[][] cases = {
                    {5, new int[][]{{0,1},{1,2},{3,4}}},
                    {5, new int[][]{{0,1},{1,2},{2,3},{3,4}}},
                    {4, new int[][]{}},
                    {1, new int[][]{}},
                    {0, new int[][]{}},
                    {3, new int[][]{{0,1},{1,2}}},
                    {4, new int[][]{{0,1},{1,2},{2,0}}},
                    {6, new int[][]{{0,1},{2,3},{4,5},{0,5}}}
            };
            for (Object[] c : cases) {
                int n = (int) c[0];
                int[][] edges = (int[][]) c[1];
                assertThat(GraphDFS.countComponents(n, edges))
                        .as("n=%d, edges=%s", n, Arrays.deepToString(edges))
                        .isEqualTo(countComponentsUF(n, edges));
            }
        }
    }

    // ------------------------------------------------------------------
    // 2. Undirected cycle detection
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("hasCycleUndirected")
    class HasCycleUndirectedTests {

        @ParameterizedTest(name = "n={0}, edges={1} -> {2}")
        @MethodSource("cases")
        void hasCycleUndirected(int n, int[][] edges, boolean expected) {
            assertThat(GraphDFS.hasCycleUndirected(n, edges)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // triangle          → true
                    Arguments.of(3, new int[][]{{0,1},{1,2},{2,0}}, true),
                    // path              → false
                    Arguments.of(3, new int[][]{{0,1},{1,2}}, false),
                    // empty             → false
                    Arguments.of(1, new int[][]{}, false),
                    // square            → true
                    Arguments.of(5, new int[][]{{0,1},{1,2},{2,3},{3,0}}, true),
                    // star              → false
                    Arguments.of(5, new int[][]{{0,1},{0,2},{0,3},{0,4}}, false),
                    // triangle + isolated → true
                    Arguments.of(4, new int[][]{{0,1},{1,2},{2,0}}, true),
                    // two paths (forest) → false
                    Arguments.of(4, new int[][]{{0,1},{2,3}}, false)
            );
        }

        @Test
        void matchesUnionFindOracle() {
            Object[][] cases = {
                    {3, new int[][]{{0,1},{1,2},{2,0}}},
                    {3, new int[][]{{0,1},{1,2}}},
                    {1, new int[][]{}},
                    {5, new int[][]{{0,1},{1,2},{2,3},{3,0}}},
                    {5, new int[][]{{0,1},{0,2},{0,3},{0,4}}},
                    {4, new int[][]{{0,1},{1,2},{2,0}}},
                    {4, new int[][]{{0,1},{2,3}}},
                    {6, new int[][]{{0,1},{1,2},{2,3},{3,4},{4,5}}},
                    {6, new int[][]{{0,1},{1,2},{2,3},{3,4},{4,5},{5,0}}}
            };
            for (Object[] c : cases) {
                int n = (int) c[0];
                int[][] edges = (int[][]) c[1];
                assertThat(GraphDFS.hasCycleUndirected(n, edges))
                        .as("n=%d, edges=%s", n, Arrays.deepToString(edges))
                        .isEqualTo(hasCycleUF(n, edges));
            }
        }
    }

    // ------------------------------------------------------------------
    // 3. Directed cycle detection
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("hasCycleDirected")
    class HasCycleDirectedTests {

        @ParameterizedTest(name = "n={0}, edges={1} -> {2}")
        @MethodSource("cases")
        void hasCycleDirected(int n, int[][] edges, boolean expected) {
            assertThat(GraphDFS.hasCycleDirected(n, edges)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // 0→1→2→0           → true
                    Arguments.of(3, new int[][]{{0,1},{1,2},{2,0}}, true),
                    // 0→1→2             → false
                    Arguments.of(3, new int[][]{{0,1},{1,2}}, false),
                    // empty             → false
                    Arguments.of(1, new int[][]{}, false),
                    // 1→2→3→1 cycle     → true
                    Arguments.of(4, new int[][]{{0,1},{1,2},{2,3},{3,1}}, true),
                    // DAG (diamond)     → false
                    Arguments.of(4, new int[][]{{0,1},{0,2},{1,2},{2,3}}, false),
                    // self-loop         → true
                    Arguments.of(1, new int[][]{{0,0}}, true),
                    // two independent cycles → true
                    Arguments.of(4, new int[][]{{0,1},{1,0},{2,3},{3,2}}, true),
                    // cross edge — NOT a cycle → false
                    // (0→1, 0→2, 1→2: 1's completed branch reaches 2 again via 0, fine)
                    Arguments.of(3, new int[][]{{0,1},{0,2},{1,2}}, false)
            );
        }

        /**
         * Cross-check: {@code hasCycleDirected} must agree with
         * {@code topologicalSort(n, edges).isEmpty()} for {@code n > 0}.
         * When {@code n = 0}, both concepts are vacuously false/empty —
         * we skip that case here.
         */
        @Test
        void agreesWithTopologicalSortOnNonEmptyGraphs() {
            Object[][] cases = {
                    {3, new int[][]{{0,1},{1,2},{2,0}}},
                    {3, new int[][]{{0,1},{1,2}}},
                    {1, new int[][]{}},
                    {4, new int[][]{{0,1},{1,2},{2,3},{3,1}}},
                    {4, new int[][]{{0,1},{0,2},{1,2},{2,3}}},
                    {1, new int[][]{{0,0}}},
                    {4, new int[][]{{0,1},{1,0},{2,3},{3,2}}},
                    {3, new int[][]{{0,1},{0,2},{1,2}}},
                    {6, new int[][]{{5,2},{5,0},{4,0},{4,1},{2,3},{3,1}}}
            };
            for (Object[] c : cases) {
                int n = (int) c[0];
                int[][] edges = (int[][]) c[1];
                boolean viaDfs = GraphDFS.hasCycleDirected(n, edges);
                boolean viaTopo = n > 0 && GraphDFS.topologicalSort(n, edges).isEmpty();
                assertThat(viaDfs)
                        .as("n=%d, edges=%s — dfs=%b, topo-empty=%b",
                                n, Arrays.deepToString(edges), viaDfs, viaTopo)
                        .isEqualTo(viaTopo);
            }
        }
    }

    // ------------------------------------------------------------------
    // 4. Topological sort
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("topologicalSort (LC 210)")
    class TopologicalSortTests {

        @Test
        void classicSixNodes() {
            // n=6, edges: 5→2, 5→0, 4→0, 4→1, 2→3, 3→1
            // Expected shape: any valid topo order, e.g. [5,4,2,3,1,0] or [4,5,2,3,1,0]
            int n = 6;
            int[][] edges = {{5,2},{5,0},{4,0},{4,1},{2,3},{3,1}};
            List<Integer> result = GraphDFS.topologicalSort(n, edges);
            assertThat(result).hasSize(6);
            assertThat(isValidTopoOrder(result, n, edges))
                    .as("order = %s", result)
                    .isTrue();
        }

        @Test
        void forcedOrder() {
            // only edge 1→0 — forces [1, 0]
            assertThat(GraphDFS.topologicalSort(2, new int[][]{{1,0}}))
                    .containsExactly(1, 0);
        }

        @Test
        void cycleReturnsEmpty() {
            // 0→1→0 — no topo order
            assertThat(GraphDFS.topologicalSort(2, new int[][]{{1,0},{0,1}}))
                    .isEmpty();
        }

        @Test
        void selfLoopReturnsEmpty() {
            assertThat(GraphDFS.topologicalSort(1, new int[][]{{0,0}})).isEmpty();
        }

        @Test
        void singleNode() {
            assertThat(GraphDFS.topologicalSort(1, new int[][]{})).containsExactly(0);
        }

        @Test
        void emptyGraph() {
            assertThat(GraphDFS.topologicalSort(0, new int[][]{})).isEmpty();
        }

        @Test
        void independentNodes() {
            // no edges — every permutation is valid; verify set and validity
            int n = 4;
            List<Integer> result = GraphDFS.topologicalSort(n, new int[][]{});
            assertThat(result).containsExactlyInAnyOrder(0, 1, 2, 3);
        }

        @Test
        void multipleValidOrdersAllPassValidator() {
            Object[][] cases = {
                    {6, new int[][]{{5,2},{5,0},{4,0},{4,1},{2,3},{3,1}}},
                    {4, new int[][]{{0,1},{0,2},{1,3},{2,3}}},
                    {3, new int[][]{{0,1},{1,2}}},
                    {5, new int[][]{{0,1},{1,2},{2,3},{3,4}}},
                    {4, new int[][]{{3,0},{3,1},{3,2}}}
            };
            for (Object[] c : cases) {
                int n = (int) c[0];
                int[][] edges = (int[][]) c[1];
                List<Integer> order = GraphDFS.topologicalSort(n, edges);
                assertThat(order).as("n=%d, edges=%s", n, Arrays.deepToString(edges))
                        .hasSize(n);
                assertThat(isValidTopoOrder(order, n, edges))
                        .as("n=%d order=%s", n, order)
                        .isTrue();
            }
        }
    }

    // ------------------------------------------------------------------
    // 5. Number of Islands
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("numIslands (LC 200)")
    class NumIslandsTests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void numIslands(char[][] grid, int expected) {
            assertThat(GraphDFS.numIslands(grid)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // One L-shaped island + one isolated '1' → 2
                    Arguments.of(new char[][]{
                            {'1','1','0'},
                            {'1','0','0'},
                            {'0','0','1'}
                    }, 2),

                    // Single connected block → 1
                    Arguments.of(new char[][]{
                            {'1','1','1'},
                            {'1','1','1'}
                    }, 1),

                    // Single row: 1, 0, 1, 0, 1 → 3 islands
                    Arguments.of(new char[][]{
                            {'1','0','1','0','1'}
                    }, 3),

                    // Single cell '0' → 0
                    Arguments.of(new char[][]{{'0'}}, 0),

                    // Single cell '1' → 1
                    Arguments.of(new char[][]{{'1'}}, 1),

                    // No grid → 0
                    Arguments.of(new char[][]{}, 0),

                    // Diagonal-adjacent '1's are separate → 4
                    // (only 4-adjacency counts)
                    Arguments.of(new char[][]{
                            {'1','0','1'},
                            {'0','1','0'},
                            {'1','0','1'}
                    }, 5),

                    // Vertically connected: top col 0 → row 0/1, col 2 → row 1/2
                    // grid:
                    //   1 0 0
                    //   1 0 1
                    //   0 0 1
                    // (0,0)-(1,0) → island 1;  (1,2)-(2,2) → island 2  → 2
                    Arguments.of(new char[][]{
                            {'1','0','0'},
                            {'1','0','1'},
                            {'0','0','1'}
                    }, 2)
            );
        }

        @Test
        void doesNotMutateInput() {
            char[][] grid = {
                    {'1','1','0'},
                    {'1','0','0'},
                    {'0','0','1'}
            };
            char[][] before = {
                    {'1','1','0'},
                    {'1','0','0'},
                    {'0','0','1'}
            };
            GraphDFS.numIslands(grid);
            assertThat(grid).isDeepEqualTo(before);
        }

        @Test
        void repeatedCallsOnSameGrid() {
            char[][] grid = {
                    {'1','0','1'},
                    {'0','0','0'},
                    {'1','0','1'}
            };
            assertThat(GraphDFS.numIslands(grid)).isEqualTo(4);
            assertThat(GraphDFS.numIslands(grid)).isEqualTo(4);   // idempotent
        }
    }

    // ------------------------------------------------------------------
    // 6. Clone Graph
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("cloneGraph (LC 133)")
    class CloneGraphTests {

        @Test
        void nullGraph() {
            assertThat(GraphDFS.cloneGraph(null)).isNull();
        }

        @Test
        void singleNodeNoNeighbors() {
            GraphDFS.Node original = new GraphDFS.Node(1);
            GraphDFS.Node copy = GraphDFS.cloneGraph(original);
            assertThat(copy).isNotNull();
            assertThat(copy.val).isEqualTo(1);
            assertThat(copy.neighbors).isEmpty();
            assertThat(copy).isNotSameAs(original);
        }

        @Test
        void twoNodesBidirectionallyConnected() {
            GraphDFS.Node a = new GraphDFS.Node(1);
            GraphDFS.Node b = new GraphDFS.Node(2);
            a.neighbors.add(b);
            b.neighbors.add(a);

            GraphDFS.Node copyA = GraphDFS.cloneGraph(a);
            assertGraphEquals(a, copyA, "two-node graph");
            assertDisjointGraphs(a, copyA, "two-node graph");
        }

        @Test
        void fourNodeCycle() {
            //      1 - 2
            //      |   |
            //      4 - 3
            int[][] adjList = {{2, 4}, {1, 3}, {2, 4}, {1, 3}};
            GraphDFS.Node original = buildGraph(adjList);
            GraphDFS.Node copy = GraphDFS.cloneGraph(original);
            assertGraphEquals(original, copy, "four-node cycle");
            assertDisjointGraphs(original, copy, "four-node cycle");
        }

        @Test
        void doesNotMutateOriginal() {
            int[][] adjList = {{2, 4}, {1, 3}, {2, 4}, {1, 3}};
            GraphDFS.Node original = buildGraph(adjList);
            GraphDFS.Node snapshot = buildGraph(adjList);
            GraphDFS.cloneGraph(original);
            assertGraphEquals(original, snapshot, "original unchanged");
        }

        @Test
        void cloneIsAFullDeepCopy() {
            // Verify the clone has independent lists — mutating the copy
            // must not affect the original
            int[][] adjList = {{2}, {1}};
            GraphDFS.Node original = buildGraph(adjList);
            GraphDFS.Node copy = GraphDFS.cloneGraph(original);
            copy.neighbors.get(0).val = 999;
            assertThat(original.neighbors.get(0).val).isEqualTo(2);
        }
    }
}