package com.vbforge.topkelements;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <h2>Top-K Elements Pattern</h2>
 *
 * <p>Given an array and a rank {@code k}, return the k-th largest/smallest
 * element, the k most frequent, the k closest to a target, etc.</p>
 *
 * <p>Key choices:</p>
 * <ul>
 *   <li><b>Heap of size k</b> — O(n log k), streaming-friendly.
 *       Direction is <em>always inverted</em>: "k largest" needs a
 *       <b>min-heap</b>; "k smallest" needs a <b>max-heap</b>.</li>
 *   <li><b>Quickselect</b> — O(n) expected, O(n²) worst. Uses
 *       <b>3-way partition</b> (Dutch National Flag) so all-equal inputs
 *       stay O(n).</li>
 *   <li><b>Binary search on value</b> — when the answer space is sorted
 *       but implicit (e.g. a row-and-column sorted matrix).</li>
 * </ul>
 */
public final class TopKElements {

    private TopKElements() { }

    /** Minimal mutable linked-list node — used by {@link #mergeKLists}. */
    public static final class ListNode {
        public int val;
        public ListNode next;
        public ListNode() { }
        public ListNode(int val) { this.val = val; }
        public ListNode(int val, ListNode next) { this.val = val; this.next = next; }
    }

    // =====================================================================
    //  1. K-th largest — Quickselect (3-way partition)
    // =====================================================================

    /**
     * <b>K-th Largest Element</b> — Quickselect variant, average O(n).
     *
     * <p>Trick: the k-th largest is the {@code (n-k)}-th smallest
     * (0-indexed). We search for that rank via 3-way partitioning so
     * duplicates collapse in one pass — no O(n²) on all-equal inputs.</p>
     *
     * <p>Time: O(n) expected, O(n²) worst (rare with random pivot) —
     * Space: O(1) (in-place on a clone)</p>
     *
     * @throws IllegalArgumentException if {@code k} is outside {@code [1, nums.length]}
     */
    public static int findKthLargest(int[] nums, int k) {
        if (k < 1 || k > nums.length) {
            throw new IllegalArgumentException("k must be in [1, " + nums.length + "]");
        }
        int[] a = nums.clone();
        return quickselect(a, nums.length - k);
    }

    /** Finds the element at sorted-index {@code k} (0-indexed) via 3-way Quickselect. */
    private static int quickselect(int[] a, int k) {
        int lo = 0, hi = a.length - 1;
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        while (lo <= hi) {
            int pivot = a[lo + rnd.nextInt(hi - lo + 1)];
            int lt = lo, gt = hi, i = lo;
            while (i <= gt) {
                if      (a[i] < pivot) swap(a, lt++, i++);
                else if (a[i] > pivot) swap(a, i, gt--);
                else                   i++;
            }
            // a[lo..lt-1] < pivot, a[lt..gt] == pivot, a[gt+1..hi] > pivot
            if      (k < lt) hi = lt - 1;
            else if (k > gt) lo = gt + 1;
            else             return a[k];
        }
        throw new IllegalStateException("unreachable: lo=" + lo + " hi=" + hi);
    }

    private static void swap(int[] a, int i, int j) {
        int t = a[i]; a[i] = a[j]; a[j] = t;
    }

    // =====================================================================
    //  2. K-th largest — Min-heap of size k
    // =====================================================================

    /**
     * <b>K-th Largest — Heap</b> — O(n log k), streaming-friendly.
     *
     * <p><b>Min-heap</b>, not max-heap: we keep the {@code k} largest seen
     * so far. Any new element smaller than the heap's min cannot possibly
     * be in the top-k. When size exceeds {@code k}, we evict the min —
     * the smallest of our k best. At the end, the min <em>is</em> the k-th
     * largest.</p>
     *
     * <p>Time: O(n log k) — Space: O(k)</p>
     */
    public static int findKthLargestHeap(int[] nums, int k) {
        if (k < 1 || k > nums.length) {
            throw new IllegalArgumentException("k must be in [1, " + nums.length + "]");
        }
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        for (int n : nums) {
            minHeap.offer(n);
            if (minHeap.size() > k) minHeap.poll();
        }
        return minHeap.peek();          // = k-th largest
    }

    // =====================================================================
    //  3. Top K Frequent — Min-heap of size k
    // =====================================================================

    /**
     * <b>Top K Frequent Elements</b> — heap variant. Same shape as
     * {@link #findKthLargestHeap}: min-heap ordered by frequency, evict
     * the least frequent when size exceeds {@code k}. The values left are
     * the k most frequent (order not guaranteed — ties are arbitrary).
     *
     * <p>Time: O(n log k) — Space: O(n) for the frequency map plus O(k) heap</p>
     */
    public static int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums) freq.merge(n, 1, Integer::sum);

        PriorityQueue<int[]> minHeap = new PriorityQueue<>(
                Comparator.comparingInt(e -> e[1]));    // order by frequency

        for (Map.Entry<Integer, Integer> e : freq.entrySet()) {
            minHeap.offer(new int[]{e.getKey(), e.getValue()});
            if (minHeap.size() > k) minHeap.poll();
        }

        int[] result = new int[k];
        for (int i = k - 1; i >= 0; i--) {
            result[i] = minHeap.poll()[0];
        }
        return result;
    }

    // =====================================================================
    //  4. K Closest Points to Origin — Max-heap of size k
    // =====================================================================

    /**
     * <b>K Closest Points to Origin</b>.
     *
     * <p><b>Max-heap</b>, not min-heap: we keep the {@code k} closest seen
     * so far, evicting the farthest when size exceeds {@code k}. Every
     * eviction discards a point that is strictly farther than all
     * survivors.</p>
     *
     * <p>Distances are compared as squared Euclidean
     * ({@code x² + y²}) — no {@code sqrt}, which preserves ordering and
     * avoids floating-point precision issues. Uses {@code long} to avoid
     * overflow when coordinates are large.</p>
     *
     * <p>Time: O(n log k) — Space: O(k) — output order is unspecified.</p>
     */
    public static int[][] kClosest(int[][] points, int k) {
        PriorityQueue<int[]> maxHeap = new PriorityQueue<>(
                (a, b) -> Long.compare(distSq(b), distSq(a)));    // max-heap by distance

        for (int[] p : points) {
            maxHeap.offer(p);
            if (maxHeap.size() > k) maxHeap.poll();
        }
        return maxHeap.toArray(new int[0][]);
    }

    private static long distSq(int[] p) {
        return (long) p[0] * p[0] + (long) p[1] * p[1];
    }

    // =====================================================================
    //  5. K-th Smallest in a Sorted Matrix — binary search on value
    // =====================================================================

    /**
     * <b>K-th Smallest Element in a Sorted Matrix</b> — rows sorted
     * left-to-right, columns sorted top-to-bottom.
     *
     * <p>Binary search on the <em>value</em>, not an index. For a candidate
     * value {@code mid}: count how many matrix entries are ≤ {@code mid}
     * via a staircase scan from the bottom-left corner (O(m + n) per
     * count). If {@code count < k}, the answer must be strictly larger;
     * else it is ≤ mid.</p>
     *
     * <p>Time: O((m + n) · log(max - min)) — Space: O(1)</p>
     */
    public static int kthSmallest(int[][] matrix, int k) {
        int m = matrix.length, n = matrix[0].length;
        int lo = matrix[0][0];
        int hi = matrix[m - 1][n - 1];

        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (countLE(matrix, mid) < k) lo = mid + 1;
            else                          hi = mid;
        }
        return lo;
    }

    /** Counts matrix entries ≤ x using a staircase from bottom-left. */
    private static int countLE(int[][] matrix, int x) {
        int m = matrix.length, n = matrix[0].length;
        int count = 0;
        int row = m - 1, col = 0;
        while (row >= 0 && col < n) {
            if (matrix[row][col] <= x) {
                count += row + 1;         // every cell above is also ≤ x
                col++;
            } else {
                row--;                    // this column's remaining cells are all > x
            }
        }
        return count;
    }

    // =====================================================================
    //  6. Merge k Sorted Lists — min-heap of list nodes
    // =====================================================================

    /**
     * <b>Merge k Sorted Lists</b> — classic "k-way merge" using a
     * <b>min-heap of list nodes</b> keyed by {@code val}. Pop the smallest,
     * append it to the result, push its successor.
     *
     * <p>Input lists are consumed (mutated) — this is standard for
     * linked-list problems. Callers who need to preserve inputs should
     * clone first.</p>
     *
     * <p>Time: O(N log k) where {@code N} = total nodes —
     * Space: O(k) for the heap (output reuses input nodes).</p>
     */
    public static ListNode mergeKLists(ListNode[] lists) {
        PriorityQueue<ListNode> pq = new PriorityQueue<>(
                Comparator.comparingInt(n -> n.val));

        for (ListNode head : lists) {
            if (head != null) pq.offer(head);
        }

        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;
        while (!pq.isEmpty()) {
            ListNode min = pq.poll();
            tail.next = min;
            tail = min;
            if (min.next != null) pq.offer(min.next);
        }
        return dummy.next;
    }
}