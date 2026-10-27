package com.vbforge.greedy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Greedy Pattern")
class GreedyTest {

    // ==================================================================
    //  Brute-force oracles — one per non-obvious greedy.
    //  These are intentionally naïve; they are only used to cross-check
    //  the fast implementation on tiny inputs, never in production.
    // ==================================================================

    /** BFS shortest-path oracle for Jump Game II. */
    private static int jumpOracle(int[] nums) {
        int n = nums.length;
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[0] = 0;
        Deque<Integer> q = new ArrayDeque<>();
        q.offer(0);
        while (!q.isEmpty()) {
            int i = q.poll();
            for (int j = i + 1; j <= Math.min(n - 1, i + nums[i]); j++) {
                if (dist[j] > dist[i] + 1) {
                    dist[j] = dist[i] + 1;
                    q.offer(j);
                }
            }
        }
        return dist[n - 1];
    }

    /** Fixpoint relaxation oracle for Candy. */
    private static int candyOracle(int[] ratings) {
        int n = ratings.length;
        int[] c = new int[n];
        Arrays.fill(c, 1);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < n; i++) {
                if (i > 0 && ratings[i] > ratings[i - 1] && c[i] <= c[i - 1]) {
                    c[i] = c[i - 1] + 1;
                    changed = true;
                }
                if (i < n - 1 && ratings[i] > ratings[i + 1] && c[i] <= c[i + 1]) {
                    c[i] = c[i + 1] + 1;
                    changed = true;
                }
            }
        }
        int sum = 0;
        for (int x : c) sum += x;
        return sum;
    }

    /** Second-by-second simulator for Task Scheduler. */
    private static int taskOracle(char[] tasks, int n) {
        Map<Character, Integer> remaining = new HashMap<>();
        for (char t : tasks) remaining.merge(t, 1, Integer::sum);
        Map<Character, Integer> lastRun = new HashMap<>();
        int time = 0, pending = tasks.length;

        while (pending > 0) {
            char chosen = 0;
            int best = 0;
            for (Map.Entry<Character, Integer> e : remaining.entrySet()) {
                if (e.getValue() <= 0) continue;
                Integer last = lastRun.get(e.getKey());
                if (last != null && time - last <= n) continue;
                if (e.getValue() > best) {
                    best = e.getValue();
                    chosen = e.getKey();
                }
            }
            if (chosen != 0) {
                remaining.merge(chosen, -1, Integer::sum);
                lastRun.put(chosen, time);
                pending--;
            }
            time++;
        }
        return time;
    }

    // ------------------------------------------------------------------
    // 1. Jump Game I
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("canJump (LC 55)")
    class CanJumpTests {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void canJump(int[] nums, boolean expected) {
            assertThat(Greedy.canJump(nums)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // maxReach walk: [2,3,1,1,4]
                    //   i=0: reach=2; i=1: reach=4; i=2..4: no gap → true
                    Arguments.of(new int[]{2, 3, 1, 1, 4}, true),

                    // [3,2,1,0,4]: i=4 > maxReach=3 → false (index 3 has 0 jump)
                    Arguments.of(new int[]{3, 2, 1, 0, 4}, false),

                    // n=1, already at end
                    Arguments.of(new int[]{0}, true),

                    // [0,1]: stuck at 0 (can't reach index 1)
                    Arguments.of(new int[]{0, 1}, false),

                    // [2,0,0]: jump from 0 to 2 directly
                    Arguments.of(new int[]{2, 0, 0}, true),

                    // [1,1,1,1]: chain of single jumps
                    Arguments.of(new int[]{1, 1, 1, 1}, true),

                    // [5,4,3,2,1,0]: jump from 0 over everything
                    Arguments.of(new int[]{5, 4, 3, 2, 1, 0}, true)
            );
        }
    }

    // ------------------------------------------------------------------
    // 2. Jump Game II
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("jump (LC 45)")
    class JumpTests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void jump(int[] nums, int expected) {
            assertThat(Greedy.jump(nums)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // [2,3,1,1,4]: 0→1→4 (2 jumps)
                    Arguments.of(new int[]{2, 3, 1, 1, 4}, 2),

                    // [2,3,0,1,4]: 0→1→4 (2 jumps)
                    Arguments.of(new int[]{2, 3, 0, 1, 4}, 2),

                    // [1,1,1,1]: 0→1→2→3 (3 jumps)
                    Arguments.of(new int[]{1, 1, 1, 1}, 3),

                    // n=1 — already at end
                    Arguments.of(new int[]{0}, 0),

                    // [5,...]: 1 jump covers everything
                    Arguments.of(new int[]{5, 4, 3, 2, 1, 0}, 1),

                    // [1,2,3]: 0→1, then from 1 jump 2 to end
                    Arguments.of(new int[]{1, 2, 3}, 2),

                    // [2,1]: 1 jump
                    Arguments.of(new int[]{2, 1}, 1),

                    // [3,2,1,0,4] (LC 45 guarantee: always reachable)
                    // path 0→1→2→3→... hmm. 0(3)→3(0)? that's a dead end.
                    // 0(3)→2(1)→3(0)? dead end too.
                    // 0(3)→1(2)→3(0)? dead end.
                    // Actually the greedy gives: i=0: far=3, jumps=1, curEnd=3
                    //   i=1: far=max(3,1+2)=3; i=2: far=3; i=3: far=max(3,3+0)=3, i==curEnd → jumps=2, curEnd=3
                    //   i=4: far=max(3,4+4)=8 ... but n-1=4, loop stops at i<n-1=4, i.e. i=0..3
                    // Wait let me re-read the loop: `for (int i = 0; i < nums.length - 1; i++)`
                    //   i=0: far=3; i==curEnd(0) → jumps=1, curEnd=3; 3>=4? no
                    //   i=1: far=max(3,1+2)=3; i!=curEnd(3)
                    //   i=2: far=max(3,2+1)=3; i!=curEnd(3)
                    //   i=3: far=max(3,3+0)=3; i==curEnd(3) → jumps=2, curEnd=3; 3>=4? no
                    // loop ends (i=4 exits)
                    // return 2
                    // But that's wrong! With [3,2,1,0,4], can we reach the end?
                    // From 0: can jump to 1, 2, or 3. From 3: jump 0 → stuck. From 2: jump 1 → reach 3. From 1: jump 2 → reach 3. Stuck!
                    // So [3,2,1,0,4] is NOT reachable — that's the LC 55 example, not a valid LC 45 input.
                    // Let me remove this case.
                    // Arguments.of(new int[]{3, 2, 1, 0, 4}, 2),
                    // Instead use [2,3,1,1,4] variants. Enough already.
                    // Add [3,0,0,0,0,0,1]? n=7; from 0 jump to 3 → stuck (jump 0)
                    // Actually from 0(3) jump to 1,2,3. 3 has 0. 1,2 have 0. Stuck. Not reachable.
                    // [2,0,1,0] n=4: from 0(2) jump to 1 or 2. From 2(1) jump to 3. Reachable in 2.
                    Arguments.of(new int[]{2, 0, 1, 0}, 2)
            );
        }

        @Test
        void matchesBfsOracle() {
            int[][] cases = {
                    {2, 3, 1, 1, 4},
                    {2, 3, 0, 1, 4},
                    {1, 1, 1, 1},
                    {0},
                    {5, 4, 3, 2, 1, 0},
                    {1, 2, 3},
                    {2, 1},
                    {2, 0, 1, 0},
                    {1, 1, 1, 1, 1}
            };
            for (int[] c : cases) {
                assertThat(Greedy.jump(c))
                        .as("input = %s", Arrays.toString(c))
                        .isEqualTo(jumpOracle(c));
            }
        }
    }

    // ------------------------------------------------------------------
    // 3. Gas Station
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("canCompleteCircuit (LC 134)")
    class GasStationTests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void canCompleteCircuit(int[] gas, int[] cost, int expected) {
            assertThat(Greedy.canCompleteCircuit(gas, cost)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // total gas 15 == total cost 15; start=3
                    //   tank path from 3: +3, +6, +4, -2(reset here), -2 ... actually
                    //   i=0: 1-3=-2 → start=1
                    //   i=1: 2-4=-2 → start=2
                    //   i=2: 3-5=-2 → start=3
                    //   i=3: 4-1=+3 (tank=3)
                    //   i=4: 5-2=+3 (tank=6)
                    //   total = 0 → possible → start=3
                    Arguments.of(
                            new int[]{1, 2, 3, 4, 5},
                            new int[]{3, 4, 5, 1, 2}, 3),

                    // total gas 9 < total cost 10 → -1
                    Arguments.of(
                            new int[]{2, 3, 4},
                            new int[]{3, 4, 3}, -1),

                    // single station, enough gas
                    Arguments.of(new int[]{5}, new int[]{4}, 0),

                    // single station, not enough
                    Arguments.of(new int[]{5}, new int[]{6}, -1),

                    // classic — starts at 4
                    // gas=[5,1,2,3,4], cost=[4,4,1,5,1]
                    //   i=0: +1 (tank=1)
                    //   i=1: 1-4=-3 → start=2, tank=0
                    //   i=2: +1 (tank=1)
                    //   i=3: 3-5=-2 → start=4, tank=0
                    //   i=4: +3 (tank=3)
                    //   total = -1? No wait: (5+1+2+3+4) - (4+4+1+5+1) = 15 - 15 = 0
                    //   start=4 ✓
                    Arguments.of(
                            new int[]{5, 1, 2, 3, 4},
                            new int[]{4, 4, 1, 5, 1}, 4)
            );
        }
    }

    // ------------------------------------------------------------------
    // 4. Candy
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("candy (LC 135)")
    class CandyTests {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void candy(int[] ratings, int expected) {
            assertThat(Greedy.candy(ratings)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // [1,0,2]: L→R [1,1,2]; R→L [2,1,2]; sum 5
                    Arguments.of(new int[]{1, 0, 2},            5),

                    // [1,2,2]: L→R [1,2,1]; R→L no change; sum 4
                    Arguments.of(new int[]{1, 2, 2},            4),

                    // [1,3,4,5,2]: L→R [1,2,3,4,1]; R→L no change; sum 11
                    Arguments.of(new int[]{1, 3, 4, 5, 2},     11),

                    // [1]: trivial 1
                    Arguments.of(new int[]{1},                  1),

                    // [1,2,3,4,5]: monotone up, [1,2,3,4,5] = 15
                    Arguments.of(new int[]{1, 2, 3, 4, 5},     15),

                    // [5,4,3,2,1]: monotone down, R→L [1,2,3,4,5] = 15
                    Arguments.of(new int[]{5, 4, 3, 2, 1},     15),

                    // [1,3,2,2,1]: L→R [1,2,1,1,1]; R→L [1,2,1,2,1] = 7
                    Arguments.of(new int[]{1, 3, 2, 2, 1},      7),

                    // [1,2,2,1]: constraints c[1]>c[0], c[2]>c[3]; no c[1]/c[2] constraint.
                    // L→R: [1,2,1,1]; R→L bumps c[2] to 2 → [1,2,2,1]; sum = 6.
                    Arguments.of(new int[]{1, 2, 2, 1},         6),

                    // [1,2,3,1]: L→R [1,2,3,1]; R→L no change = 7
                    Arguments.of(new int[]{1, 2, 3, 1},         7),

                    // [2,2,2]: all equal → each gets 1, sum 3
                    Arguments.of(new int[]{2, 2, 2},            3)
            );
        }

        @Test
        void matchesFixpointOracle() {
            int[][] cases = {
                    {1, 0, 2},
                    {1, 2, 2},
                    {1, 3, 4, 5, 2},
                    {1},
                    {1, 2, 3, 4, 5},
                    {5, 4, 3, 2, 1},
                    {1, 3, 2, 2, 1},
                    {1, 2, 2, 1},
                    {1, 2, 3, 1},
                    {2, 2, 2},
                    {1, 2, 3, 4, 3, 2, 1},
                    {4, 3, 2, 1, 2, 3, 4}
            };
            for (int[] c : cases) {
                assertThat(Greedy.candy(c))
                        .as("ratings = %s", Arrays.toString(c))
                        .isEqualTo(candyOracle(c));
            }
        }
    }

    // ------------------------------------------------------------------
    // 5. Task Scheduler
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("leastInterval (LC 621)")
    class TaskSchedulerTests {

        @ParameterizedTest(name = "case {index}")
        @MethodSource("cases")
        void leastInterval(char[] tasks, int n, int expected) {
            assertThat(Greedy.leastInterval(tasks, n)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // [A,A,A,B,B,B], n=2
                    //   maxCount=3, numMax=2 → (2)*(3)+2 = 8
                    Arguments.of(new char[]{'A', 'A', 'A', 'B', 'B', 'B'}, 2, 8),

                    // Same but n=0 → skeleton=4 < 6 → 6
                    Arguments.of(new char[]{'A', 'A', 'A', 'B', 'B', 'B'}, 0, 6),

                    // [A,A,A,A,A,A,B,C,D,E,F,G], n=2
                    //   maxCount=6, numMax=1 → 5*3+1 = 16 > 12 → 16
                    Arguments.of(new char[]{
                            'A', 'A', 'A', 'A', 'A', 'A', 'B', 'C', 'D', 'E', 'F', 'G'
                    }, 2, 16),

                    // single task
                    Arguments.of(new char[]{'A'}, 0, 1),
                    Arguments.of(new char[]{'A'}, 5, 1),

                    // [A,A,B,B], n=3
                    //   maxCount=2, numMax=2 → 1*4+2 = 6 > 4 → 6
                    Arguments.of(new char[]{'A', 'A', 'B', 'B'}, 3, 6),

                    // [A,B,C,D,E,F], n=2
                    //   maxCount=1, numMax=6 → 0*3+6 = 6 == 6 → 6 (all distinct, no cooldown)
                    Arguments.of(new char[]{'A', 'B', 'C', 'D', 'E', 'F'}, 2, 6)
            );
        }

        @Test
        void matchesSimulatorOracle() {
            Object[][] cases = {
                    {new char[]{'A', 'A', 'A', 'B', 'B', 'B'}, 2},
                    {new char[]{'A', 'A', 'A', 'B', 'B', 'B'}, 0},
                    {new char[]{'A', 'A', 'A', 'A', 'A', 'A', 'B', 'C', 'D', 'E', 'F', 'G'}, 2},
                    {new char[]{'A'}, 0},
                    {new char[]{'A'}, 5},
                    {new char[]{'A', 'A', 'B', 'B'}, 3},
                    {new char[]{'A', 'B', 'C', 'D', 'E', 'F'}, 2},
                    {new char[]{'A', 'A', 'A', 'B', 'B', 'B', 'C', 'C', 'C'}, 3}
            };
            for (Object[] c : cases) {
                char[] tasks = (char[]) c[0];
                int n = (int) c[1];
                assertThat(Greedy.leastInterval(tasks, n))
                        .as("tasks = %s, n = %d", Arrays.toString(tasks), n)
                        .isEqualTo(taskOracle(tasks, n));
            }
        }
    }

    // ------------------------------------------------------------------
    // 6. Partition Labels
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("partitionLabels (LC 763)")
    class PartitionLabelsTests {

        @Test
        void classicExample() {
            // last: a=8, b=5, c=7, d=14, e=15, f=11, g=13, h=19, i=22, j=23, k=20, l=21
            // i=0 'a' end=8 → within 0..8, b(last 5), c(7) don't extend; i=8 → partition [0..8]=9
            // i=9 'd' end=14 → e extends to 15; i=15 → partition [9..15]=7
            // i=16 'h' end=19 → i,j,k,l extend to 23; i=23 → partition [16..23]=8
            assertThat(Greedy.partitionLabels("ababcbacadefegdehijhklij"))
                    .containsExactly(9, 7, 8);
        }

        @Test
        void oneCharacter() {
            assertThat(Greedy.partitionLabels("a")).containsExactly(1);
        }

        @Test
        void allDistinct() {
            assertThat(Greedy.partitionLabels("abcd")).containsExactly(1, 1, 1, 1);
        }

        @Test
        void allSame() {
            assertThat(Greedy.partitionLabels("aaaa")).containsExactly(4);
        }

        @Test
        void twoPartitions() {
            // last: a=2, b=1, c=3
            // i=0 'a' end=2; i=1 'b'(1), i=2 'a'(2) → partition [0..2]=3
            // i=3 'c' end=3 → partition [3..3]=1
            assertThat(Greedy.partitionLabels("abac")).containsExactly(3, 1);
        }

        @Test
        void interleavedPair() {
            // "eccbbbbdec": last e=8, c=9, b=6, d=7
            //   i=0 'e' end=8
            //   i=1 'c' end=max(8,9)=9
            //   i=2..7 don't extend (b max 6, d max 7)
            //   i=8 'e'(8) no extend
            //   i=9 'c'(9) → i==end=9 → partition [0..9]=10
            assertThat(Greedy.partitionLabels("eccbbbbdec")).containsExactly(10);
        }

        @Test
        void empty() {
            assertThat(Greedy.partitionLabels("")).isEmpty();
        }
    }

    // ------------------------------------------------------------------
    // 7. Best Time to Buy and Sell Stock II
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("maxProfit (LC 122)")
    class MaxProfitTests {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void maxProfit(int[] prices, int expected) {
            assertThat(Greedy.maxProfit(prices)).isEqualTo(expected);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // [7,1,5,3,6,4]: +4 (1→5) +3 (3→6) = 7
                    Arguments.of(new int[]{7, 1, 5, 3, 6, 4}, 7),

                    // [1,2,3,4,5]: +1+1+1+1 = 4
                    Arguments.of(new int[]{1, 2, 3, 4, 5}, 4),

                    // [7,6,4,3,1]: all decreasing → 0
                    Arguments.of(new int[]{7, 6, 4, 3, 1}, 0),

                    // single element → 0
                    Arguments.of(new int[]{1}, 0),

                    // [2,1,2,0,1]: +1 (1→2) +1 (0→1) = 2
                    Arguments.of(new int[]{2, 1, 2, 0, 1}, 2),

                    // flat prices → 0
                    Arguments.of(new int[]{3, 3, 3, 3}, 0)
            );
        }
    }
}