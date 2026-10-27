package com.vbforge.bfs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <h2>Graph BFS Pattern</h2>
 *
 * <p>Breadth-first search explores a graph in <b>levels</b> — all nodes at
 * distance {@code k} are fully processed before any at distance {@code k+1}.
 * Because of this, the first time we reach a node, it is via a <em>shortest</em>
 * path in an unweighted graph.</p>
 *
 * <p>Five technique variations are demonstrated:</p>
 * <ul>
 *   <li><b>Unweighted shortest path</b> — plain queue, level-by-level.</li>
 *   <li><b>Multi-source</b> — seed the queue with <em>all</em> sources; the BFS
 *       computes "distance to the nearest source" for every cell.</li>
 *   <li><b>Bidirectional</b> — expand the smaller frontier each round; when
 *       the two frontiers meet, the shortest path length is known.</li>
 *   <li><b>0-1 BFS</b> — {@link ArrayDeque} as a double-ended queue; edge
 *       weight 0 pushes to the <em>front</em>, weight 1 to the <em>back</em>.</li>
 *   <li><b>8-directional grid BFS</b> — same level-by-level skeleton, but
 *       the neighbour set includes diagonals.</li>
 * </ul>
 *
 * <p>All methods are pure — input grids and graphs are never mutated.</p>
 */
public final class GraphBFS {

    private GraphBFS() { }

    /** Four-directional neighbours: up, down, left, right. */
    private static final int[][] FOUR_DIRS = {{-1,0},{1,0},{0,-1},{0,1}};

    /** Eight-directional neighbours: orthogonal + diagonals. */
    private static final int[][] EIGHT_DIRS = {
            {-1,0},{1,0},{0,-1},{0,1},{-1,-1},{-1,1},{1,-1},{1,1}
    };

    // =====================================================================
    //  1. Shortest path — unweighted graph, level-by-level BFS
    // =====================================================================

    /**
     * <b>Shortest Path — Unweighted Graph</b>. Returns the number of
     * <em>edges</em> on the shortest path from {@code start} to {@code end},
     * or {@code -1} if no path exists. Returns {@code 0} when
     * {@code start == end}.
     *
     * <p>Level-by-level processing is what makes the first arrival at
     * {@code end} provably shortest.</p>
     *
     * @throws IllegalArgumentException if {@code start} / {@code end} are out
     *         of range, or any edge references a vertex outside {@code [0, n)}
     */
    public static int shortestPathUnweighted(int n, int[][] edges, int start, int end) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative: " + n);
        if (start < 0 || start >= n) {
            throw new IllegalArgumentException("start out of range: " + start);
        }
        if (end < 0 || end >= n) {
            throw new IllegalArgumentException("end out of range: " + end);
        }

        List<List<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        if (edges != null) {
            for (int[] e : edges) {
                if (e == null || e.length != 2) {
                    throw new IllegalArgumentException("malformed edge: " + Arrays.toString(e));
                }
                int u = e[0], v = e[1];
                if (u < 0 || u >= n || v < 0 || v >= n) {
                    throw new IllegalArgumentException(
                            "edge " + Arrays.toString(e) + " out of range for n=" + n);
                }
                adj.get(u).add(v);
                adj.get(v).add(u);
            }
        }

        if (start == end) return 0;

        boolean[] visited = new boolean[n];
        Deque<Integer> queue = new ArrayDeque<>();
        queue.offer(start);
        visited[start] = true;
        int distance = 0;

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int k = 0; k < size; k++) {
                int u = queue.poll();
                for (int v : adj.get(u)) {
                    if (visited[v]) continue;
                    if (v == end) return distance + 1;
                    visited[v] = true;
                    queue.offer(v);
                }
            }
            distance++;
        }
        return -1;
    }

    // =====================================================================
    //  2. Rotting Oranges — multi-source BFS
    // =====================================================================

    /**
     * <b>Rotting Oranges</b> (LC 994) — minutes until all fresh oranges rot,
     * or {@code -1} if impossible. Cell values: {@code 0} empty,
     * {@code 1} fresh, {@code 2} rotten.
     *
     * <p>Seeding the queue with <b>all</b> initially-rotten oranges makes
     * the BFS compute "distance to the nearest rot" for every cell —
     * simultaneously, in one pass.</p>
     *
     * <p>Time: O(m·n) — Space: O(m·n) working copy (input preserved)</p>
     */
    public static int rottingOranges(int[][] grid) {
        if (grid == null || grid.length == 0 || grid[0].length == 0) return 0;

        int m = grid.length, n = grid[0].length;
        int[][] g = new int[m][n];
        for (int i = 0; i < m; i++) g[i] = grid[i].clone();

        Deque<int[]> queue = new ArrayDeque<>();
        int fresh = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if      (g[i][j] == 2) queue.offer(new int[]{i, j});
                else if (g[i][j] == 1) fresh++;
            }
        }
        if (fresh == 0) return 0;                                // nothing to rot
        if (queue.isEmpty()) return -1;                          // fresh but no rot source

        int minutes = 0;
        while (!queue.isEmpty() && fresh > 0) {
            int size = queue.size();
            minutes++;
            for (int k = 0; k < size; k++) {
                int[] pos = queue.poll();
                for (int[] d : FOUR_DIRS) {
                    int nr = pos[0] + d[0], nc = pos[1] + d[1];
                    if (nr < 0 || nr >= m || nc < 0 || nc >= n) continue;
                    if (g[nr][nc] != 1) continue;
                    g[nr][nc] = 2;
                    fresh--;
                    queue.offer(new int[]{nr, nc});
                }
            }
        }
        return fresh == 0 ? minutes : -1;
    }

    // =====================================================================
    //  3. 01 Matrix — reverse multi-source BFS
    // =====================================================================

    /**
     * <b>01 Matrix</b> (LC 542) — for each cell, the distance to the nearest
     * {@code 0}. Only 4-directional moves.
     *
     * <p>Rather than BFS <em>from each 1</em> (O(m·n · m·n)), BFS <em>from
     * all 0s simultaneously</em>. Same multi-source skeleton as
     * {@link #rottingOranges(int[][])}, only the payload differs.</p>
     *
     * <p>Time: O(m·n) — Space: O(m·n)</p>
     */
    public static int[][] updateMatrix(int[][] mat) {
        int m = mat.length, n = mat[0].length;
        int[][] dist = new int[m][n];
        Deque<int[]> queue = new ArrayDeque<>();

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (mat[i][j] == 0) {
                    dist[i][j] = 0;
                    queue.offer(new int[]{i, j});
                } else {
                    dist[i][j] = Integer.MAX_VALUE;
                }
            }
        }

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            for (int[] d : FOUR_DIRS) {
                int nr = pos[0] + d[0], nc = pos[1] + d[1];
                if (nr < 0 || nr >= m || nc < 0 || nc >= n) continue;
                if (dist[nr][nc] > dist[pos[0]][pos[1]] + 1) {
                    dist[nr][nc] = dist[pos[0]][pos[1]] + 1;
                    queue.offer(new int[]{nr, nc});
                }
            }
        }
        return dist;
    }

    // =====================================================================
    //  4. Word Ladder — bidirectional BFS
    // =====================================================================

    /**
     * <b>Word Ladder</b> (LC 127) — length of the shortest transformation
     * sequence from {@code beginWord} to {@code endWord}, or {@code 0} if
     * impossible. Words differ by exactly one letter at each step; all
     * intermediate words must be in {@code wordList}.
     *
     * <p><b>Bidirectional BFS:</b> maintain two frontiers, one from
     * {@code beginWord} and one from {@code endWord}. Each round, expand
     * the <em>smaller</em> frontier — this keeps the branching factor low.
     * The moment a generated word is already in the other frontier, we've
     * closed the loop and {@code steps + 1} is the answer.</p>
     *
     * <p>Time: O(N · L²) where N = list size, L = word length —
     * Space: O(N · L)</p>
     *
     * @throws IllegalArgumentException if any word has a different length
     */
    public static int ladderLength(String beginWord, String endWord, List<String> wordList) {
        if (beginWord.length() != endWord.length()) {
            throw new IllegalArgumentException("begin/end word length mismatch");
        }
        for (String w : wordList) {
            if (w.length() != beginWord.length()) {
                throw new IllegalArgumentException("wordList word length mismatch: " + w);
            }
        }

        // ──────────────────────────────────────────────────────────────
        // ORDER MATTERS: "already there" dominates "target in dict".
        // If beginWord == endWord, the shortest sequence is [beginWord],
        // length 1 — no word-list check is required.
        // ──────────────────────────────────────────────────────────────
        if (beginWord.equals(endWord)) return 1;

        Set<String> dict = new HashSet<>(wordList);
        if (!dict.contains(endWord)) return 0;

        Set<String> front = new HashSet<>();
        Set<String> back  = new HashSet<>();
        front.add(beginWord);
        back.add(endWord);
        int steps = 1;

        while (!front.isEmpty() && !back.isEmpty()) {
            if (front.size() > back.size()) {
                Set<String> tmp = front;
                front = back;
                back  = tmp;
            }
            Set<String> next = new HashSet<>();
            for (String word : front) {
                char[] chars = word.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    char orig = chars[i];
                    for (char c = 'a'; c <= 'z'; c++) {
                        if (c == orig) continue;
                        chars[i] = c;
                        String candidate = new String(chars);
                        if (back.contains(candidate)) return steps + 1;
                        if (dict.contains(candidate)) {
                            next.add(candidate);
                            dict.remove(candidate);
                        }
                    }
                    chars[i] = orig;
                }
            }
            front = next;
            steps++;
        }
        return 0;
    }

    // =====================================================================
    //  5. Minimum Obstacles — 0-1 BFS
    // =====================================================================

    /**
     * <b>Minimum Obstacles to Remove</b> (LC 2290) — min number of {@code 1}
     * cells to remove to make a 4-connected path from (0,0) to (m-1,n-1).
     *
     * <p><b>0-1 BFS:</b> edges into {@code 0}-cells have weight 0, edges
     * into {@code 1}-cells have weight 1. When relaxing a weight-0 edge,
     * push the neighbour to the <b>front</b> of the deque; for weight-1,
     * push to the <b>back</b>. This gives O(V + E) — better than Dijkstra's
     * O((V + E) log V) for this special case.</p>
     *
     * <p>Time: O(m·n) — Space: O(m·n)</p>
     */
    public static int minimumObstacles(int[][] grid) {
        int m = grid.length, n = grid[0].length;
        int[][] dist = new int[m][n];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
        dist[0][0] = 0;

        Deque<int[]> dq = new ArrayDeque<>();
        dq.offerFirst(new int[]{0, 0});

        while (!dq.isEmpty()) {
            int[] pos = dq.pollFirst();
            int r = pos[0], c = pos[1];
            for (int[] d : FOUR_DIRS) {
                int nr = r + d[0], nc = c + d[1];
                if (nr < 0 || nr >= m || nc < 0 || nc >= n) continue;
                int weight = grid[nr][nc];
                int newDist = dist[r][c] + weight;
                if (newDist < dist[nr][nc]) {
                    dist[nr][nc] = newDist;
                    if (weight == 0) dq.offerFirst(new int[]{nr, nc});
                    else             dq.offerLast(new int[]{nr, nc});
                }
            }
        }
        return dist[m - 1][n - 1];
    }

    // =====================================================================
    //  6. Shortest Path in Binary Matrix — 8-directional grid BFS
    // =====================================================================

    /**
     * <b>Shortest Path in Binary Matrix</b> (LC 1091) — number of cells in
     * the shortest 8-directional path from (0,0) to (m-1,n-1) through
     * {@code 0}-cells, or {@code -1} if none.
     *
     * <p>Same level-by-level skeleton as {@link #shortestPathUnweighted},
     * but the neighbour set includes diagonals and success is measured in
     * <b>cells visited</b> (1-indexed), not edges.</p>
     *
     * <p>Time: O(m·n) — Space: O(m·n)</p>
     */
    public static int shortestPathBinaryMatrix(int[][] grid) {
        if (grid == null || grid.length == 0 || grid[0].length == 0) return -1;
        int m = grid.length, n = grid[0].length;
        if (grid[0][0] != 0 || grid[m - 1][n - 1] != 0) return -1;

        boolean[][] visited = new boolean[m][n];
        Deque<int[]> queue = new ArrayDeque<>();
        queue.offer(new int[]{0, 0});
        visited[0][0] = true;
        int distance = 1;

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int k = 0; k < size; k++) {
                int[] pos = queue.poll();
                if (pos[0] == m - 1 && pos[1] == n - 1) return distance;
                for (int[] d : EIGHT_DIRS) {
                    int nr = pos[0] + d[0], nc = pos[1] + d[1];
                    if (nr < 0 || nr >= m || nc < 0 || nc >= n) continue;
                    if (visited[nr][nc] || grid[nr][nc] != 0) continue;
                    visited[nr][nc] = true;
                    queue.offer(new int[]{nr, nc});
                }
            }
            distance++;
        }
        return -1;
    }
}