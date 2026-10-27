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
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Shortest path on an unweighted grid with breadth-first search.
 *
 * <p>Time O(R·C), space O(R·C). Renderer: {@code grid}.
 */
public final class BfsGridTrace implements Algorithm {

    private static final List<String> PSEUDO = List.of(
            "queue = [start]; dist[start] = 0",
            "while queue is not empty:",
            "    cell = queue.poll()                  // FIFO → level by level",
            "    if cell == end: return dist[cell]",
            "    for each open, unseen neighbour n:",
            "        dist[n] = dist[cell] + 1; queue.add(n)",
            "return -1       // end unreachable");

    private static final String[] DEFAULT = {
            "S..#.....#.....",
            ".#.#.###...###.",
            ".#...#...#...#.",
            ".#####.#####.#.",
            ".....#.....#.#.",
            ".###.#####.#.#.",
            "...#.......#...",
            "##.#######.###E"};

    @Override
    public Meta meta() {
        return new Meta("bfs-grid", "Grid Shortest Path", "Breadth-First Search",
                "Flood outward from the start one ring at a time – the first time BFS touches the end is the shortest path.",
                "O(R·C)", "O(R·C)",
                "BFS is shortest-path for unweighted graphs: every cell dequeued at level k is exactly k steps away. "
                        + "The queue (FIFO) is what keeps the rings in order. Multi-source BFS is the same algorithm "
                        + "with every source seeded into the queue up front.",
                PSEUDO,
                List.of(
                        Field.maze("grid", "Grid", DEFAULT, "paint walls, move S and E", 16, 30),
                        Field.select("moves", "Moves", "4", "4 = up/down/left/right · 8 = also diagonals",
                                List.of("4", "8"))),
                List.of(
                        Preset.of("Labyrinth", "grid", DEFAULT, "moves", "4"),
                        Preset.of("Open field", "grid", new String[]{
                                "S.........", "..........", "..........", "..........", ".........E"}, "moves", "4"),
                        Preset.of("Diagonal squeeze (try 4!)", "grid", new String[]{
                                "S.#.....", "..#.....", ".#......", "#......E"}, "moves", "8"),
                        Preset.of("Unreachable", "grid", new String[]{
                                "S..#....", "...#....", "...#..#.", "...####.", "......#E"}, "moves", "4")));
    }

    @Override
    public void trace(Params p, Consumer<Frame> emit) {
        String[] g = p.maze("grid", 16, 30);
        boolean diag = "8".equals(p.choice("moves", Set.of("4", "8")));
        int R = g.length, C = g[0].length();

        int sr = 0, sc = 0, er = 0, ec = 0;
        char[][] st = new char[R][C];
        for (int r = 0; r < R; r++)
            for (int c = 0; c < C; c++) {
                char ch = g[r].charAt(c);
                st[r][c] = ch;
                if (ch == 'S') { sr = r; sc = c; }
                if (ch == 'E') { er = r; ec = c; }
            }

        int[][] dist = new int[R][C];
        for (int[] row : dist) Arrays.fill(row, -1);
        int[] parent = new int[R * C];
        Arrays.fill(parent, -1);

        int[][] dirs = diag
                ? new int[][]{{-1, 0}, {0, 1}, {1, 0}, {0, -1}, {-1, 1}, {1, 1}, {1, -1}, {-1, -1}}
                : new int[][]{{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

        Deque<int[]> queue = new ArrayDeque<>();
        dist[sr][sc] = 0;
        queue.add(new int[]{sr, sc});
        int visited = 0;

        frame(emit, st, dist, queue.size(), visited, null, "Enqueue the start cell with distance 0", 0);

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int r = cur[0], c = cur[1];
            visited++;
            if (st[r][c] == '.' || st[r][c] == 'f') st[r][c] = 'c';

            if (r == er && c == ec) {
                frame(emit, st, dist, queue.size(), visited, dist[r][c],
                        "Dequeued E at distance " + dist[r][c] + "  →  shortest path found, tracing it back", 3);
                tracePath(emit, st, dist, parent, C, sr, sc, er, ec, queue.size(), visited);
                return;
            }

            int found = 0;
            for (int[] d : dirs) {
                int nr = r + d[0], nc = c + d[1];
                if (nr < 0 || nc < 0 || nr >= R || nc >= C || st[nr][nc] == '#' || dist[nr][nc] != -1) continue;
                dist[nr][nc] = dist[r][c] + 1;
                parent[nr * C + nc] = r * C + c;
                queue.add(new int[]{nr, nc});
                if (st[nr][nc] == '.') st[nr][nc] = 'f';
                found++;
            }
            frame(emit, st, dist, queue.size(), visited, dist[r][c],
                    "dequeue (" + r + "," + c + ") at distance " + dist[r][c] + "  →  "
                            + (found == 0 ? "no new neighbours" : found + " new cell" + (found > 1 ? "s" : "") + " queued"), 4);
            if (st[r][c] == 'c') st[r][c] = 'v';
        }
        frame(emit, st, dist, 0, visited, null,
                "Queue is empty and E was never reached  →  no path exists", 6);
    }

    private static void tracePath(Consumer<Frame> emit, char[][] st, int[][] dist, int[] parent, int C,
                                  int sr, int sc, int er, int ec, int queueSize, int visited) {
        List<int[]> path = new ArrayList<>();
        for (int at = parent[er * C + ec]; at != -1 && at != sr * C + sc; at = parent[at])
            path.add(new int[]{at / C, at % C});
        Collections.reverse(path);

        if (path.isEmpty()) {
            frame(emit, st, dist, queueSize, visited, dist[er][ec], "Path length " + dist[er][ec] + " – start and end are adjacent", 3);
            return;
        }
        int step = 0;
        for (int[] cell : path) {
            st[cell[0]][cell[1]] = 'p';
            step++;
            frame(emit, st, dist, queueSize, visited, dist[er][ec],
                    "shortest path: step " + step + " of " + (path.size() + 1) + "  (length " + dist[er][ec] + ")", 3);
        }
    }

    private static void frame(Consumer<Frame> emit, char[][] st, int[][] dist, int queueSize, int visited,
                              Integer d, String note, int line) {
        int R = st.length, C = st[0].length;
        String[] rows = new String[R];
        int[][] nums = new int[R][C];
        for (int r = 0; r < R; r++) {
            rows[r] = new String(st[r]);
            for (int c = 0; c < C; c++) {
                boolean marker = st[r][c] == 'S' || st[r][c] == 'E' || st[r][c] == '#';
                nums[r][c] = (dist[r][c] >= 0 && !marker) ? dist[r][c] : -1;
            }
        }
        emit.accept(Frame.of("grid", Data.of()
                .with("rows", R).with("cols", C)
                .with("cells", rows)
                .with("nums", nums)
                .with("info", Viz.kv("queue", queueSize, "visited", visited, "dist", d)), note, line));
    }
}
