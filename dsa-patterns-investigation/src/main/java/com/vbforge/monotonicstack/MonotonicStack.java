package com.vbforge.monotonicstack;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * <h2>Monotonic Stack Pattern</h2>
 *
 * <p>A stack whose contents are kept <em>sorted</em> (strictly increasing or
 * strictly decreasing) by popping violated elements before pushing. Used to
 * answer "nearest greater / smaller element" queries in <b>amortised O(n)</b>
 * — each element is pushed and popped at most once.</p>
 *
 * <p>Three invariants to remember:</p>
 * <ol>
 *   <li><b>Store indices, not values</b>, whenever you need a distance
 *       or a position in the answer.</li>
 *   <li><b>Pop condition decides the flavour</b>: {@code top < current}
 *       gives "next greater"; {@code top > current} gives "next smaller".</li>
 *   <li><b>Use {@link ArrayDeque}</b>, never {@code Stack} — {@code Stack}
 *       is a legacy {@code Vector} subclass and is synchronised.</li>
 * </ol>
 */
public final class MonotonicStack {

    private MonotonicStack() { }

    // =====================================================================
    //  1. Next Greater Element I  (LC 496)
    // =====================================================================

    /**
     * For each element of {@code nums1} (a subset of {@code nums2}), find
     * the first element to its right in {@code nums2} that is greater.
     * Returns {@code -1} where no such element exists.
     *
     * <p>Uses a <b>value-keyed</b> map because positions in {@code nums2}
     * are irrelevant — we only need the value answers.</p>
     *
     * <p>Time: O(|nums1| + |nums2|) — Space: O(|nums2|)</p>
     */
    public static int[] nextGreaterElement(int[] nums1, int[] nums2) {
        Map<Integer, Integer> nextGreater = new HashMap<>();
        Deque<Integer> stack = new ArrayDeque<>();

        for (int n : nums2) {
            while (!stack.isEmpty() && stack.peek() < n) {
                nextGreater.put(stack.pop(), n);
            }
            stack.push(n);
        }

        int[] result = new int[nums1.length];
        for (int i = 0; i < nums1.length; i++) {
            result[i] = nextGreater.getOrDefault(nums1[i], -1);
        }
        return result;
    }

    // =====================================================================
    //  2. Next Greater Element II  (LC 503) — circular
    // =====================================================================

    /**
     * For each index in {@code nums}, find the next strictly greater element
     * walking <em>circularly</em> to the right. Returns {@code -1} if none.
     *
     * <p>Trick: iterate {@code 2n} times using {@code i % n}, but push
     * indices only during the first {@code n} passes. Elements pushed but
     * never popped have no greater element.</p>
     *
     * <p>Time: O(n) — Space: O(n)</p>
     */
    public static int[] nextGreaterCircular(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < 2 * n; i++) {
            int idx = i % n;
            while (!stack.isEmpty() && nums[stack.peek()] < nums[idx]) {
                result[stack.pop()] = nums[idx];
            }
            if (i < n) stack.push(idx);            // push each index exactly once
        }
        return result;
    }

    // =====================================================================
    //  3. Daily Temperatures  (LC 739)
    // =====================================================================

    /**
     * For each day, how many days until a warmer day? {@code 0} if none.
     *
     * <p>Same skeleton as {@link #nextGreaterElement(int[], int[])}, but the
     * answer is the <em>distance</em> {@code i - j}, so we store <b>indices</b>.</p>
     *
     * <p>Time: O(n) — Space: O(n)</p>
     */
    public static int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] answer = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && temperatures[stack.peek()] < temperatures[i]) {
                int j = stack.pop();
                answer[j] = i - j;
            }
            stack.push(i);
        }
        return answer;
    }

    // =====================================================================
    //  4. Largest Rectangle in Histogram  (LC 84)
    // =====================================================================

    /**
     * <b>Largest Rectangle in Histogram</b> — the maximum rectangular area
     * under the bar chart.
     *
     * <p>Standard sentinel trick: append a virtual bar of height {@code 0}
     * at the end. This forces the stack to be fully drained before exit, so
     * no post-loop cleanup is needed.</p>
     *
     * <p>For each bar popped at index {@code i}:</p>
     * <ul>
     *   <li>height = {@code heights[popped]}</li>
     *   <li>width  = {@code i - stack.peek() - 1} (or {@code i} if stack empty)</li>
     * </ul>
     *
     * <p>Time: O(n) — Space: O(n)</p>
     */
    public static int largestRectangleArea(int[] heights) {
        int n = heights.length;
        int[] ext = new int[n + 1];
        System.arraycopy(heights, 0, ext, 0, n);
        // ext[n] = 0 — the sentinel

        Deque<Integer> stack = new ArrayDeque<>();
        int maxArea = 0;

        for (int i = 0; i <= n; i++) {
            while (!stack.isEmpty() && ext[stack.peek()] > ext[i]) {
                int h = ext[stack.pop()];
                int w = stack.isEmpty() ? i : i - stack.peek() - 1;
                maxArea = Math.max(maxArea, h * w);
            }
            stack.push(i);
        }
        return maxArea;
    }

    // =====================================================================
    //  5. Trapping Rain Water  (LC 42) — stack version
    // =====================================================================

    /**
     * <b>Trapping Rain Water</b> — total water trapped between bars.
     *
     * <p>Stack of decreasing heights. When a taller bar arrives, it forms a
     * "basin" with each shorter bar on the stack: pop the bottom, then the
     * current top is the left wall. The trapped slice contributes:</p>
     * <pre>{@code water += (min(currentHeight, topHeight) - bottomHeight)
     *          * (i - topIndex - 1)}</pre>
     *
     * <p>Time: O(n) — Space: O(n)</p>
     */
    public static int trap(int[] height) {
        int water = 0;
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < height.length; i++) {
            while (!stack.isEmpty() && height[i] > height[stack.peek()]) {
                int bottom = stack.pop();
                if (stack.isEmpty()) break;         // no left wall
                int top = stack.peek();
                int h = Math.min(height[i], height[top]) - height[bottom];
                int w = i - top - 1;
                water += h * w;
            }
            stack.push(i);
        }
        return water;
    }

    // =====================================================================
    //  6. Sliding Window Maximum  (LC 239) — monotonic deque
    // =====================================================================

    /**
     * <b>Sliding Window Maximum</b> — max of every window of size {@code k}.
     *
     * <p>Uses a <b>monotonic deque</b> of indices, kept in decreasing order
     * of {@code nums[index]}:</p>
     * <ul>
     *   <li><b>Front eviction</b> — remove indices that have left the window
     *       ({@code < i - k + 1}).</li>
     *   <li><b>Back eviction</b> — remove indices whose value is {@code <= }
     *       current, since they can never be the maximum again.</li>
     *   <li><b>Front of deque</b> is always the max of the current window.</li>
     * </ul>
     *
     * <p>Time: O(n) — Space: O(k)</p>
     *
     * @throws IllegalArgumentException if {@code k <= 0} or {@code k > nums.length}
     */
    public static int[] maxSlidingWindow(int[] nums, int k) {
        if (k <= 0 || k > nums.length) {
            throw new IllegalArgumentException("k must be in [1, nums.length]");
        }
        int n = nums.length;
        int[] result = new int[n - k + 1];
        Deque<Integer> dq = new ArrayDeque<>();     // indices, decreasing values

        for (int i = 0; i < n; i++) {
            // 1. evict from front — out of window
            while (!dq.isEmpty() && dq.peekFirst() < i - k + 1) {
                dq.pollFirst();
            }
            // 2. evict from back — dominated by nums[i]
            while (!dq.isEmpty() && nums[dq.peekLast()] <= nums[i]) {
                dq.pollLast();
            }
            // 3. add current index
            dq.addLast(i);
            // 4. record max once the window has filled
            if (i >= k - 1) {
                result[i - k + 1] = nums[dq.peekFirst()];
            }
        }
        return result;
    }

    // =====================================================================
    //  7. Sum of Subarray Minimums  (LC 907) — double stack
    // =====================================================================

    private static final long MOD = 1_000_000_007L;

    /**
     * <b>Sum of Subarray Minimums</b> — sum over all contiguous subarrays of
     * their minimum value, mod {@code 1_000_000_007}.
     *
     * <p>Contribution formula — element {@code nums[i]} is the minimum of
     * {@code (i - prevLess[i]) * (nextLessEq[i] - i)} subarrays, where:</p>
     * <ul>
     *   <li>{@code prevLess[i]} = previous <b>strictly smaller</b> index
     *       (pop while {@code >= current})</li>
     *   <li>{@code nextLessEq[i]} = next <b>smaller or equal</b> index
     *       (pop while {@code > current})</li>
     * </ul>
     *
     * <p>Using strict on one side and non-strict on the other ensures each
     * element is counted <em>exactly once</em> per subarray even with
     * duplicates.</p>
     *
     * <p>Time: O(n) — Space: O(n)</p>
     */
    public static int sumSubarrayMins(int[] nums) {
        int n = nums.length;
        int[] prevLess   = new int[n];
        int[] nextLessEq = new int[n];

        // prev strictly smaller — pop while top >= current
        Deque<Integer> stack = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && nums[stack.peek()] >= nums[i]) {
                stack.pop();
            }
            prevLess[i] = stack.isEmpty() ? -1 : stack.peek();
            stack.push(i);
        }

        // next smaller-or-equal — pop while top > current, scan right-to-left
        stack.clear();
        for (int i = n - 1; i >= 0; i--) {
            while (!stack.isEmpty() && nums[stack.peek()] > nums[i]) {
                stack.pop();
            }
            nextLessEq[i] = stack.isEmpty() ? n : stack.peek();
            stack.push(i);
        }

        long sum = 0;
        for (int i = 0; i < n; i++) {
            long left  = i - prevLess[i];
            long right = nextLessEq[i] - i;
            sum = (sum + (long) nums[i] * left * right) % MOD;
        }
        return (int) sum;
    }
}