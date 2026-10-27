package com.vbforge.bfs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Graph BFS Pattern")
class GraphBFSTest {

    // ==================================================================
    //  Independent oracles. Each uses a DIFFERENT algorithm family
    //  from the implementation under test.
    // ==================================================================

    /** Dijkstra with unit weights — independent of BFS level-tracking. */
    private static int shortestPathDijkstra(int n, int[][] edges, int start, int end) {
        if (start == end) return 0;
        List<List<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            adj.get(e[1]).add(e[0]);
        }
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[start] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.offer(new int[]{start, 0});
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            if (cur[1] > dist[cur[0]]) continue;
            if (cur[0] == end) return cur[1];
            for (int v : adj.get(cur[0])) {
                if (dist[v] > cur[1] + 1) {
                    dist[v] = cur[1] + 1;
                    pq.offer(new int[]{v, dist[v]});
                }
            }
        }
        return -1;
    }

    /** Minute-by-minute literal simulation oracle for rotting oranges. */
    private static int rottingOrangesSimulation(int[][] grid) {
        if (grid == null || grid.length == 0 || grid[0].length == 0) return 0;
        int m = grid.length, n = grid[0].length;
        int[][] g = new int[m][n];
        for (int i = 0; i < m; i++) g[i] = grid[i].clone();

        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        int minutes = 0;
        while (true) {
            List<int[]> toRot = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    if (g[i][j] != 1) continue;
                    boolean adj = false;
                    for (int[] d : dirs) {
                        int ni = i + d[0], nj = j + d[1];
                        if (ni >= 0 && ni < m && nj >= 0 && nj < n && g[ni][nj] == 2) {
                            adj = true; break;
                        }
                    }
                    if (adj) toRot.add(new int[]{i, j});
                }
            }
            if (toRot.isEmpty()) break;
            for (int[] p : toRot) g[p[0]][p[1]] = 2;
            minutes++;
        }
        for (int i = 0; i < m; i++) for (int j = 0; j < n; j++) {
            if (g[i][j] == 1) return -1;
        }
        return minutes;
    }

    /** Brute-force nearest-0 scan for the 01 Matrix problem. */
    private static int[][] updateMatrixBrute(int[][] mat) {
        int m = mat.length, n = mat[0].length;
        int[][] out = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                int best = Integer.MAX_VALUE;
                for (int x = 0; x < m; x++) {
                    for (int y = 0; y < n; y++) {
                        if (mat[x][y] == 0) {
                            best = Math.min(best, Math.abs(x - i) + Math.abs(y - j));
                        }
                    }
                }
                out[i][j] = best;
            }
        }
        return out;
    }

    /** Single-direction BFS oracle for Word Ladder. */
    private static int ladderLengthSingle(String begin, String end, List<String> words) {
        // Same order fix as GraphBFS.ladderLength — "already there" first.
        if (begin.equals(end)) return 1;

        Set<String> dict = new HashSet<>(words);
        if (!dict.contains(end)) return 0;

        Deque<String> queue = new ArrayDeque<>();
        queue.offer(begin);
        Set<String> visited = new HashSet<>();
        visited.add(begin);
        int steps = 1;

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int k = 0; k < size; k++) {
                String word = queue.poll();
                if (word.equals(end)) return steps;
                char[] chars = word.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    char orig = chars[i];
                    for (char c = 'a'; c <= 'z'; c++) {
                        if (c == orig) continue;
                        chars[i] = c;
                        String cand = new String(chars);
                        if (dict.contains(cand) && visited.add(cand)) {
                            queue.offer(cand);
                        }
                    }
                    chars[i] = orig;
                }
            }
            steps++;
        }
        return 0;
    }

    /** Dijkstra with weighted edges (weight = grid cell value). */
    private static int minimumObstaclesDijkstra(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int[][] dist = new int[m][n];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
        dist[0][0] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[2]));
        pq.offer(new int[]{0, 0, 0});
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            if (cur[2] > dist[cur[0]][cur[1]]) continue;
            if (cur[0] == m - 1 && cur[1] == n - 1) return cur[2];
            for (int[] d : dirs) {
                int nr = cur[0] + d[0], nc = cur[1] + d[1];
                if (nr < 0 || nr >= m || nc < 0 || nc >= n) continue;
                int nd = cur[2] + grid[nr][nc];
                if (nd < dist[nr][nc]) {
                    dist[nr][nc] = nd;
                    pq.offer(new int[]{nr, nc, nd});
                }
            }
        }
        return -1;
    }

    /** Bellman-Ford-style iterative relaxation oracle for 8-dir grid. */
    private static int shortestPathBinaryMatrixRelax(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        if (grid[0][0] != 0 || grid[m - 1][n - 1] != 0) return -1;

        int[][] dist = new int[m][n];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
        dist[0][0] = 1;
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{-1,1},{1,-1},{1,1}};

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int r = 0; r < m; r++) {
                for (int c = 0; c < n; c++) {
                    if (grid[r][c] != 0 || dist[r][c] == Integer.MAX_VALUE) continue;
                    for (int[] d : dirs) {
                        int nr = r + d[0], nc = c + d[1];
                        if (nr < 0 || nr >= m || nc < 0 || nc >= n) continue;
                        if (grid[nr][nc] != 0) continue;
                        if (dist[nr][nc] > dist[r][c] + 1) {
                            dist[nr][nc] = dist[r][c] + 1;
                            changed = true;
                        }
                    }
                }
            }
        }
        return dist[m - 1][n - 1] == Integer.MAX_VALUE ? -1 : dist[m - 1][n - 1];
    }

    // ------------------------------------------------------------------
    // 1. Shortest Path — unweighted graph
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("shortestPathUnweighted")
    class ShortestPathTests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void shortestPath(int n, int[][] edges, int start, int end, int expected) {
            assertThat(GraphBFS.shortestPathUnweighted(n, edges, start, end))
                    .isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // 0→1→2→3 chain → 3
                    Arguments.of(4, new int[][]{{0,1},{1,2},{2,3}}, 0, 3, 3),
                    // disconnected → -1
                    Arguments.of(4, new int[][]{{0,1},{2,3}}, 0, 3, -1),
                    // direct edge → 1
                    Arguments.of(3, new int[][]{{0,1},{1,2},{0,2}}, 0, 2, 1),
                    // start == end → 0
                    Arguments.of(1, new int[][]{}, 0, 0, 0),
                    // no edges, unreachable → -1
                    Arguments.of(2, new int[][]{}, 0, 1, -1),
                    // complete square → 2
                    Arguments.of(4, new int[][]{{0,1},{1,3},{0,2},{2,3}}, 0, 3, 2),
                    // longer path
                    Arguments.of(6, new int[][]{{0,1},{1,2},{2,3},{3,4},{4,5}}, 0, 5, 5)
            );
        }

        @Test
        void rejectsOutOfRangeStart() {
            assertThatThrownBy(() -> GraphBFS.shortestPathUnweighted(3, new int[][]{}, -1, 1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> GraphBFS.shortestPathUnweighted(3, new int[][]{}, 3, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsOutOfRangeEdge() {
            // The bounds-check fixture regression guard from GraphDFS
            assertThatThrownBy(() -> GraphBFS.shortestPathUnweighted(
                    3, new int[][]{{0,5}}, 0, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void matchesDijkstraOracle() {
            Object[][] cases = {
                    {4, new int[][]{{0,1},{1,2},{2,3}}, 0, 3},
                    {4, new int[][]{{0,1},{2,3}}, 0, 3},
                    {3, new int[][]{{0,1},{1,2},{0,2}}, 0, 2},
                    {1, new int[][]{}, 0, 0},
                    {2, new int[][]{}, 0, 1},
                    {4, new int[][]{{0,1},{1,3},{0,2},{2,3}}, 0, 3},
                    {6, new int[][]{{0,1},{1,2},{2,3},{3,4},{4,5}}, 0, 5},
                    {5, new int[][]{{0,1},{1,2},{3,4}}, 2, 3}
            };
            for (Object[] c : cases) {
                int n = (int) c[0];
                int[][] edges = (int[][]) c[1];
                int s = (int) c[2];
                int e = (int) c[3];
                assertThat(GraphBFS.shortestPathUnweighted(n, edges, s, e))
                        .as("n=%d, edges=%s, s=%d, e=%d",
                                n, Arrays.deepToString(edges), s, e)
                        .isEqualTo(shortestPathDijkstra(n, edges, s, e));
            }
        }
    }

    // ------------------------------------------------------------------
    // 2. Rotting Oranges — multi-source BFS
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("rottingOranges (LC 994)")
    class RottingOrangesTests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void rottingOranges(int[][] grid, int expected) {
            assertThat(GraphBFS.rottingOranges(grid)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // LC 994 example 1 → 4
                    Arguments.of(new int[][]{
                            {2,1,1},
                            {1,1,0},
                            {0,1,1}
                    }, 4),

                    // LC 994 example 2 → -1 (rot at (0,0), fresh at (1,1) isolated)
                    Arguments.of(new int[][]{
                            {2,1,1},
                            {0,1,1},
                            {1,0,1}
                    }, -1),

                    // no fresh — nothing to rot → 0
                    Arguments.of(new int[][]{{0,2}}, 0),

                    // single fresh, no rot → -1
                    Arguments.of(new int[][]{{1}}, -1),

                    // single rotten → 0
                    Arguments.of(new int[][]{{2}}, 0),

                    // all fresh, no rot → -1
                    Arguments.of(new int[][]{{1,1},{1,1}}, -1),

                    // empty → 0
                    Arguments.of(new int[][]{}, 0)
            );
        }

        @Test
        void doesNotMutateInput() {
            int[][] grid = {
                    {2,1,1},
                    {1,1,0},
                    {0,1,1}
            };
            int[][] before = {
                    {2,1,1},
                    {1,1,0},
                    {0,1,1}
            };
            GraphBFS.rottingOranges(grid);
            assertThat(grid).isDeepEqualTo(before);
        }

        @Test
        void matchesSimulationOracle() {
            int[][][] cases = {
                    {{2,1,1},{1,1,0},{0,1,1}},
                    {{2,1,1},{0,1,1},{1,0,1}},
                    {{0,2}},
                    {{1}},
                    {{2}},
                    {{1,1},{1,1}},
                    {{}},
                    {{2,2,2,2},{2,2,2,2}},
                    {{2,1,1,1,1,1,1}},
                    {{2,2,2},{2,1,2},{2,2,2}}
            };
            for (int[][] g : cases) {
                int[][] copy = new int[g.length][];
                for (int i = 0; i < g.length; i++) copy[i] = g[i].clone();
                assertThat(GraphBFS.rottingOranges(copy))
                        .as("grid=%s", Arrays.deepToString(g))
                        .isEqualTo(rottingOrangesSimulation(g));
            }
        }
    }

    // ------------------------------------------------------------------
    // 3. 01 Matrix
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("updateMatrix (LC 542)")
    class UpdateMatrixTests {

        @Test
        void classic() {
            // 1 at (1,1) → dist 1; (2,0)/(2,2) → 1; (2,1) → 2
            int[][] result = GraphBFS.updateMatrix(new int[][]{
                    {0,0,0},
                    {0,1,0},
                    {1,1,1}
            });
            assertThat(result).isDeepEqualTo(new int[][]{
                    {0,0,0},
                    {0,1,0},
                    {1,2,1}
            });
        }

        @Test
        void singleZero() {
            assertThat(GraphBFS.updateMatrix(new int[][]{{0}}))
                    .isDeepEqualTo(new int[][]{{0}});
        }

        @Test
        void linearGrowth() {
            assertThat(GraphBFS.updateMatrix(new int[][]{
                    {0,1,1,1},
                    {1,1,1,1}
            })).isDeepEqualTo(new int[][]{
                    {0,1,2,3},
                    {1,2,3,4}
            });
        }

        @Test
        void doesNotMutateInput() {
            int[][] input = {{0,0,0},{0,1,0},{1,1,1}};
            int[][] before = {{0,0,0},{0,1,0},{1,1,1}};
            GraphBFS.updateMatrix(input);
            assertThat(input).isDeepEqualTo(before);
        }

        @Test
        void matchesBruteForceOracle() {
            int[][][] cases = {
                    {{0,0,0},{0,1,0},{1,1,1}},
                    {{0}},
                    {{0,1,1,1},{1,1,1,1}},
                    {{1,1,1},{1,0,1},{1,1,1}},
                    {{0,1,0},{1,1,1},{0,1,0}},
                    {{1,0,1,0,1},{0,1,0,1,0}}
            };
            for (int[][] c : cases) {
                assertThat(GraphBFS.updateMatrix(c))
                        .as("mat=%s", Arrays.deepToString(c))
                        .isDeepEqualTo(updateMatrixBrute(c));
            }
        }
    }

    // ------------------------------------------------------------------
    // 4. Word Ladder — bidirectional BFS
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("ladderLength (LC 127)")
    class LadderLengthTests {

        @Test
        void classic() {
            // hit→hot→dot→dog→cog, 5 words
            assertThat(GraphBFS.ladderLength(
                    "hit", "cog",
                    List.of("hot","dot","dog","lot","log","cog")))
                    .isEqualTo(5);
        }

        @Test
        void endNotInList() {
            assertThat(GraphBFS.ladderLength(
                    "hit", "cog",
                    List.of("hot","dot","dog","lot","log")))
                    .isZero();
        }

        @Test
        void beginEqualsEnd() {
            assertThat(GraphBFS.ladderLength("hit", "hit", List.of("hot")))
                    .isEqualTo(1);
        }

        @Test
        void singleStep() {
            // "ab" → "cb" → "cd" — 3 words
            assertThat(GraphBFS.ladderLength(
                    "ab", "cd", List.of("cb", "cd")))
                    .isEqualTo(3);
        }

        @Test
        void rejectsLengthMismatch() {
            assertThatThrownBy(() -> GraphBFS.ladderLength(
                    "hit", "cog", List.of("hi")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void matchesSingleDirectionBfsOracle() {
            Object[][] cases = {
                    {"hit", "cog", List.of("hot","dot","dog","lot","log","cog")},
                    {"hit", "cog", List.of("hot","dot","dog","lot","log")},
                    {"hit", "hit", List.of("hot")},
                    {"ab",  "cd",  List.of("cb", "cd")},
                    {"a",   "b",   List.of("b")},
                    {"hot", "dog", List.of("hot", "dot", "dog")},
                    {"abc","def",  List.of("abd","abe","def")}
            };
            for (Object[] c : cases) {
                String begin = (String) c[0];
                String end = (String) c[1];
                @SuppressWarnings("unchecked")
                List<String> words = (List<String>) c[2];
                assertThat(GraphBFS.ladderLength(begin, end, words))
                        .as("begin=%s, end=%s, words=%s", begin, end, words)
                        .isEqualTo(ladderLengthSingle(begin, end, words));
            }
        }
    }

    // ------------------------------------------------------------------
    // 5. Minimum Obstacles — 0-1 BFS
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("minimumObstacles (LC 2290)")
    class MinimumObstaclesTests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void minimumObstacles(int[][] grid, int expected) {
            assertThat(GraphBFS.minimumObstacles(grid)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // Right-down path removes (0,1) and (0,2) → 2
                    Arguments.of(new int[][]{
                            {0,1,1},
                            {1,1,0},
                            {1,1,0}
                    }, 2),

                    // Free path exists
                    Arguments.of(new int[][]{
                            {0,1,0,0,0},
                            {0,1,0,1,0},
                            {0,0,0,1,0}
                    }, 0),

                    // Trivial
                    Arguments.of(new int[][]{{0}}, 0),

                    // Single obstacle to remove (either path costs 1)
                    Arguments.of(new int[][]{{0,1},{1,0}}, 1),

                    // All zero
                    Arguments.of(new int[][]{
                            {0,0},
                            {0,0}
                    }, 0),

                    Arguments.of(new int[][]{
                            {0,1,0},
                            {1,1,1},
                            {0,1,0}
                    }, 2)
            );
        }

        @Test
        void doesNotMutateInput() {
            int[][] grid = {{0,1,1},{1,1,0},{1,1,0}};
            int[][] before = {{0,1,1},{1,1,0},{1,1,0}};
            GraphBFS.minimumObstacles(grid);
            assertThat(grid).isDeepEqualTo(before);
        }

        @Test
        void matchesDijkstraOracle() {
            int[][][] cases = {
                    {{0,1,1},{1,1,0},{1,1,0}},
                    {{0,1,0,0,0},{0,1,0,1,0},{0,0,0,1,0}},
                    {{0}},
                    {{0,1},{1,0}},
                    {{0,0},{0,0}},
                    {{0,1,0},{1,1,1},{0,1,0}},
                    {{0,0,1,0},{1,0,1,0},{1,0,0,0}}
            };
            for (int[][] c : cases) {
                assertThat(GraphBFS.minimumObstacles(c))
                        .as("grid=%s", Arrays.deepToString(c))
                        .isEqualTo(minimumObstaclesDijkstra(c));
            }
        }
    }

    // ------------------------------------------------------------------
    // 6. Shortest Path Binary Matrix — 8-directional BFS
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("shortestPathBinaryMatrix (LC 1091)")
    class ShortestPathBinaryMatrixTests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void shortestPathBinaryMatrix(int[][] grid, int expected) {
            assertThat(GraphBFS.shortestPathBinaryMatrix(grid)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // Diagonal move: (0,0)→(1,1), 2 cells
                    Arguments.of(new int[][]{{0,1},{1,0}}, 2),

                    // Right, right, down, down (4 edges, 5 cells) — but a shorter
                    // diagonal exists: (0,0)→(0,1)→(1,2)→(2,2) = 4 cells
                    Arguments.of(new int[][]{
                            {0,0,0},
                            {1,1,0},
                            {1,1,0}
                    }, 4),

                    // Start blocked
                    Arguments.of(new int[][]{
                            {1,0,0},
                            {1,1,0},
                            {1,1,0}
                    }, -1),

                    // Single cell free → 1
                    Arguments.of(new int[][]{{0}}, 1),

                    // Single cell blocked → -1
                    Arguments.of(new int[][]{{1}}, -1),

                    // Impossible — all blocked except start
                    Arguments.of(new int[][]{
                            {0,1,1},
                            {1,1,1},
                            {1,1,1}
                    }, -1)
            );
        }

        @Test
        void doesNotMutateInput() {
            int[][] grid = {{0,1},{1,0}};
            int[][] before = {{0,1},{1,0}};
            GraphBFS.shortestPathBinaryMatrix(grid);
            assertThat(grid).isDeepEqualTo(before);
        }

        @Test
        void matchesBellmanFordOracle() {
            int[][][] cases = {
                    {{0,1},{1,0}},
                    {{0,0,0},{1,1,0},{1,1,0}},
                    {{1,0,0},{1,1,0},{1,1,0}},
                    {{0}},
                    {{1}},
                    {{0,1,1},{1,1,1},{1,1,1}},
                    {{0,0,1,0},{1,0,1,0},{1,0,0,0}}
            };
            for (int[][] c : cases) {
                assertThat(GraphBFS.shortestPathBinaryMatrix(c))
                        .as("grid=%s", Arrays.deepToString(c))
                        .isEqualTo(shortestPathBinaryMatrixRelax(c));
            }
        }
    }
}