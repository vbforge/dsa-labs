package com.vbforge.greedy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h2>Greedy Pattern</h2>
 *
 * <p>A greedy algorithm makes the <em>locally optimal</em> choice at every
 * step in the hope of reaching a <em>globally optimal</em> solution. Greedy
 * correctness always requires a proof — either an exchange argument
 * ("swapping any non-greedy choice for the greedy one never makes the answer
 * worse") or an invariant ("the greedy state always dominates every feasible
 * alternative"). The Javadoc for each method states the argument it uses.</p>
 *
 * <p>Failing to state the proof is the number one source of "greedy bugs"
 * — algorithms that look right, pass the first five tests, and fail on case
 * six. This package pairs each greedy implementation with a brute-force
 * oracle wherever the correctness isn't a one-liner.</p>
 */
public final class Greedy {

    private Greedy() { }

    // =====================================================================
    //  1. Jump Game I  (LC 55)
    // =====================================================================

    /**
     * <b>Jump Game</b> — can we reach the last index starting from index 0,
     * where {@code nums[i]} is the max jump length from position {@code i}?
     *
     * <p><b>Invariant:</b> {@code maxReach} is the farthest index reachable
     * from any position in {@code [0, i]}. If {@code i > maxReach}, we're
     * stuck — return {@code false}.</p>
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static boolean canJump(int[] nums) {
        int maxReach = 0;
        for (int i = 0; i < nums.length; i++) {
            if (i > maxReach) return false;              // gap we can never cross
            maxReach = Math.max(maxReach, i + nums[i]);
        }
        return true;
    }

    // =====================================================================
    //  2. Jump Game II  (LC 45)
    // =====================================================================

    /**
     * <b>Jump Game II</b> — minimum number of jumps to reach the last index.
     * Assumes the input is <em>guaranteed reachable</em> (LC 45 contract).
     *
     * <p><b>Exchange argument:</b> consider the "frontier" of positions
     * reachable in {@code k} jumps. To use {@code k+1} jumps optimally,
     * pick the position in the frontier that extends farthest — any other
     * pick cannot extend more. The greedy makes this choice implicitly by
     * tracking {@code farthest} over all positions in the current level.</p>
     *
     * <p>Implementation is BFS-shaped: {@code curEnd} marks the last index
     * of the current jump level; when {@code i} reaches it, we've exhausted
     * the current level → increment {@code jumps} and set
     * {@code curEnd = farthest}.</p>
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static int jump(int[] nums) {
        int jumps = 0;
        int curEnd = 0;
        int farthest = 0;

        for (int i = 0; i < nums.length - 1; i++) {
            farthest = Math.max(farthest, i + nums[i]);
            if (i == curEnd) {
                jumps++;
                curEnd = farthest;
                if (curEnd >= nums.length - 1) break;
            }
        }
        return jumps;
    }

    // =====================================================================
    //  3. Gas Station  (LC 134)
    // =====================================================================

    /**
     * <b>Gas Station</b> — unique starting index at which a circuit is
     * possible, or {@code -1}.
     *
     * <p><b>Two facts:</b></p>
     * <ol>
     *   <li>If {@code sum(gas) < sum(cost)}, no start works → return -1.</li>
     *   <li>If a valid start exists, any prefix with negative running
     *       balance can be abandoned — the true start lies strictly after
     *       it. Reset {@code start = i + 1} and clear the running balance.</li>
     * </ol>
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static int canCompleteCircuit(int[] gas, int[] cost) {
        int total = 0;
        int tank  = 0;
        int start = 0;

        for (int i = 0; i < gas.length; i++) {
            int net = gas[i] - cost[i];
            total += net;
            tank  += net;
            if (tank < 0) {                              // can't reach i+1 from `start`
                start = i + 1;
                tank = 0;
            }
        }
        return total >= 0 ? start : -1;
    }

    // =====================================================================
    //  4. Candy  (LC 135)
    // =====================================================================

    /**
     * <b>Candy</b> — minimum candies so that every child with a higher
     * rating than a neighbour gets more candy.
     *
     * <p><b>Exchange argument:</b> the optimal solution is the
     * <em>pointwise minimum</em> satisfying all constraints. Two passes
     * suffice because constraints are only between adjacent positions:</p>
     * <ul>
     *   <li>Left pass enforces {@code ratings[i] > ratings[i-1]} ⇒ strictly
     *       more candy than the left neighbour.</li>
     *   <li>Right pass enforces the symmetric right-neighbour rule, taking
     *       {@code max} with whatever the left pass assigned.</li>
     * </ul>
     * Any constraint is satisfied after both passes, and each pass never
     * increases a value beyond what's required.</p>
     *
     * <p>Time: O(n) — Space: O(n)</p>
     */
    public static int candy(int[] ratings) {
        int n = ratings.length;
        int[] candies = new int[n];
        java.util.Arrays.fill(candies, 1);

        for (int i = 1; i < n; i++) {
            if (ratings[i] > ratings[i - 1]) {
                candies[i] = candies[i - 1] + 1;
            }
        }
        for (int i = n - 2; i >= 0; i--) {
            if (ratings[i] > ratings[i + 1]) {
                candies[i] = Math.max(candies[i], candies[i + 1] + 1);
            }
        }

        int total = 0;
        for (int c : candies) total += c;
        return total;
    }

    // =====================================================================
    //  5. Task Scheduler  (LC 621)
    // =====================================================================

    /**
     * <b>Task Scheduler</b> — minimum total intervals to run all tasks with
     * a cooldown of {@code n} between identical tasks.
     *
     * <p><b>Formula:</b> let {@code maxCount} = highest task frequency and
     * {@code numMax} = number of tasks achieving it. The <em>skeleton</em>
     * is {@code maxCount - 1} blocks of length {@code (n + 1)}, plus a final
     * row of {@code numMax} tasks:</p>
     * <pre>{@code skeleton = (maxCount - 1) * (n + 1) + numMax}</pre>
     *
     * <p>If other tasks "spill over" the skeleton, they fill the gaps and
     * the answer becomes {@code totalTasks} (no idle time). We take the
     * max of the two.</p>
     *
     * <p>Time: O(n) — Space: O(1) (26-letter alphabet)</p>
     */
    public static int leastInterval(char[] tasks, int n) {
        int[] freq = new int[26];
        for (char t : tasks) freq[t - 'A']++;

        int maxCount = 0;
        for (int f : freq) maxCount = Math.max(maxCount, f);

        int numMax = 0;
        for (int f : freq) if (f == maxCount) numMax++;

        int skeleton = (maxCount - 1) * (n + 1) + numMax;
        return Math.max(skeleton, tasks.length);
    }

    // =====================================================================
    //  6. Partition Labels  (LC 763)
    // =====================================================================

    /**
     * <b>Partition Labels</b> — split string into maximum number of parts
     * so that no letter appears in two parts.
     *
     * <p><b>Construction:</b> precompute each letter's last index. Then for
     * the current partition starting at {@code start}, extend {@code end}
     * to {@code max(end, last[s[i]])} for every {@code i} up to {@code end}.
     * When {@code i == end}, the partition closes and we record its length.</p>
     *
     * <p>Time: O(|s|) — Space: O(1) (alphabet)</p>
     */
    public static List<Integer> partitionLabels(String s) {
        int[] last = new int[26];
        for (int i = 0; i < s.length(); i++) last[s.charAt(i) - 'a'] = i;

        List<Integer> result = new ArrayList<>();
        int start = 0, end = 0;
        for (int i = 0; i < s.length(); i++) {
            end = Math.max(end, last[s.charAt(i) - 'a']);
            if (i == end) {
                result.add(end - start + 1);
                start = end + 1;
            }
        }
        return result;
    }

    // =====================================================================
    //  7. Best Time to Buy and Sell Stock II  (LC 122)
    // =====================================================================

    /**
     * <b>Best Time to Buy and Sell Stock II</b> — unlimited transactions,
     * maximum profit.
     *
     * <p><b>Exchange argument:</b> every positive price change can be
     * captured by a buy-sell pair. Summing all positive gaps overcounts
     * nothing and misses nothing — it is exactly the optimal total profit.</p>
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static int maxProfit(int[] prices) {
        int profit = 0;
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] > prices[i - 1]) {
                profit += prices[i] - prices[i - 1];
            }
        }
        return profit;
    }
}