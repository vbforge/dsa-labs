package com.vbforge.binarysearch;

import java.util.Arrays;

/**
 * <h2>Binary Search Pattern</h2>
 *
 * <p>All methods run in <b>O(log n)</b> unless stated. Two invariants
 * dominate:</p>
 * <ul>
 *   <li><b>Closed</b> — search range {@code [lo, hi]}, loop condition
 *       {@code lo <= hi}. Mid is always a candidate.</li>
 *   <li><b>Half-open</b> — search range {@code [lo, hi)}, loop condition
 *       {@code lo < hi}. Used for lower/upper bound — the answer is always
 *       {@code lo} when the loop exits.</li>
 * </ul>
 *
 * <p>Always compute {@code mid = lo + (hi - lo) / 2} — never
 * {@code (lo + hi) / 2} — to avoid integer overflow.</p>
 */
public final class BinarySearch {

    private BinarySearch() { }

    // =====================================================================
    //  1. Classic exact match
    // =====================================================================

    /**
     * Classic binary search. Returns the index of {@code target} or
     * {@code -1} if not present.
     *
     * <p>Time: O(log n) — Space: O(1)</p>
     */
    public static int binarySearch(int[] nums, int target) {
        int lo = 0, hi = nums.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] == target) return mid;
            if (nums[mid] < target)  lo = mid + 1;
            else                     hi = mid - 1;
        }
        return -1;
    }

    // =====================================================================
    //  2. Lower / upper bound
    // =====================================================================

    /**
     * First index {@code i} with {@code nums[i] >= target}, or
     * {@code nums.length} if none. This is the answer for
     * <b>search-insert-position</b> too.
     *
     * <p>Time: O(log n) — Space: O(1)</p>
     */
    public static int lowerBound(int[] nums, int target) {
        int lo = 0, hi = nums.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] < target) lo = mid + 1;
            else                    hi = mid;
        }
        return lo;
    }

    /**
     * First index {@code i} with {@code nums[i] > target}, or
     * {@code nums.length} if none.
     *
     * <p>Time: O(log n) — Space: O(1)</p>
     */
    public static int upperBound(int[] nums, int target) {
        int lo = 0, hi = nums.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] <= target) lo = mid + 1;
            else                     hi = mid;
        }
        return lo;
    }

    /** Search-insert-position — identical to {@link #lowerBound(int[], int)}. */
    public static int searchInsert(int[] nums, int target) {
        return lowerBound(nums, target);
    }

    /**
     * <b>Find First and Last Position</b> of {@code target}, or
     * {@code [-1, -1]} if absent.
     *
     * <p>Built on top of {@link #lowerBound} and {@link #upperBound}.</p>
     *
     * <p>Time: O(log n) — Space: O(1)</p>
     */
    public static int[] searchRange(int[] nums, int target) {
        int first = lowerBound(nums, target);
        if (first == nums.length || nums[first] != target) {
            return new int[]{-1, -1};
        }
        int last = upperBound(nums, target) - 1;
        return new int[]{first, last};
    }

    // =====================================================================
    //  3. Rotated sorted array
    // =====================================================================

    /**
     * <b>Search in Rotated Sorted Array</b> — array of <em>distinct</em>
     * values rotated at an unknown pivot. Returns the index of {@code target}
     * or {@code -1}.
     *
     * <p>Key idea: one of the two halves {@code [lo, mid]} or
     * {@code [mid, hi]} is always sorted. Decide which, then check whether
     * {@code target} falls inside that half.</p>
     *
     * <p>Time: O(log n) — Space: O(1)</p>
     */
    public static int searchRotated(int[] nums, int target) {
        int lo = 0, hi = nums.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] == target) return mid;

            if (nums[lo] <= nums[mid]) {                    // left half sorted
                if (nums[lo] <= target && target < nums[mid]) {
                    hi = mid - 1;
                } else {
                    lo = mid + 1;
                }
            } else {                                        // right half sorted
                if (nums[mid] < target && target <= nums[hi]) {
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }
        }
        return -1;
    }

    /**
     * <b>Find Minimum in Rotated Sorted Array</b>. Handles duplicates — if
     * {@code nums[mid] == nums[hi]} we shrink {@code hi} by one (worst case
     * O(n), e.g. all-equal input).
     *
     * <p>Time: O(log n) average, O(n) worst — Space: O(1)</p>
     *
     * @throws IllegalArgumentException if {@code nums} is empty
     */
    public static int findMinRotated(int[] nums) {
        if (nums.length == 0) {
            throw new IllegalArgumentException("nums must not be empty");
        }
        int lo = 0, hi = nums.length - 1;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] > nums[hi])      lo = mid + 1;
            else if (nums[mid] < nums[hi]) hi = mid;
            else                           hi--;        // duplicate — shrink
        }
        return nums[lo];
    }

    // =====================================================================
    //  4. Peak (shape) search
    // =====================================================================

    /**
     * <b>Find Peak Element</b> — returns the index of any local peak.
     * Assumes adjacent elements are distinct; if they aren't, this returns
     * <em>a</em> peak (not necessarily unique). Boundaries count as
     * {@code -∞} on both sides.
     *
     * <p>Time: O(log n) — Space: O(1)</p>
     *
     * @throws IllegalArgumentException if {@code nums} is empty
     */
    public static int findPeakElement(int[] nums) {
        if (nums.length == 0) {
            throw new IllegalArgumentException("nums must not be empty");
        }
        int lo = 0, hi = nums.length - 1;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] < nums[mid + 1]) lo = mid + 1;    // uphill → right
            else                           hi = mid;        // downhill → left
        }
        return lo;
    }

    // =====================================================================
    //  5. Binary search on the answer space
    // =====================================================================

    /**
     * <b>Sqrt(x)</b> — integer square root (floor). Uses {@code long}
     * internally so {@code mid * mid} cannot overflow.
     *
     * <p>Time: O(log x) — Space: O(1)</p>
     *
     * @throws IllegalArgumentException if {@code x < 0}
     */
    public static int sqrt(int x) {
        if (x < 0) throw new IllegalArgumentException("x must be >= 0");
        if (x < 2) return x;

        long lo = 1, hi = x / 2;
        long ans = 0;
        while (lo <= hi) {
            long mid = lo + (hi - lo) / 2;
            if (mid * mid <= x) {
                ans = mid;
                lo  = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return (int) ans;
    }

    /**
     * <b>Koko Eating Bananas</b> — minimum eating speed {@code k} so that
     * all {@code piles} are consumed within {@code h} hours.
     *
     * <p>The feasible set of speeds is monotone: if speed {@code k} works,
     * every {@code k' > k} works. Binary search the smallest working speed
     * in {@code [1, max(piles)]}.</p>
     *
     * <p>Time: O(n log max(piles)) — Space: O(1)</p>
     *
     * @throws IllegalArgumentException if {@code piles} is empty or
     *         {@code h < piles.length} (impossible — each pile takes ≥ 1h)
     */
    public static int minEatingSpeed(int[] piles, int h) {
        if (piles.length == 0) {
            throw new IllegalArgumentException("piles must not be empty");
        }
        if (h < piles.length) {
            throw new IllegalArgumentException("h is too small to finish all piles");
        }

        int lo = 1, hi = 0;
        for (int p : piles) hi = Math.max(hi, p);

        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (canFinish(piles, mid, h)) hi = mid;
            else                          lo = mid + 1;
        }
        return lo;
    }

    private static boolean canFinish(int[] piles, int speed, int h) {
        long hours = 0;
        for (int p : piles) {
            hours += (p + speed - 1) / speed;    // ceil(p / speed)
            if (hours > h) return false;
        }
        return true;
    }

    // =====================================================================
    //  6. Virtual 1-D — search in a sorted 2-D matrix
    // =====================================================================

    /**
     * <b>Search a 2D Matrix</b> — rows sorted left-to-right, first element of
     * each row greater than last of the previous. Effectively a single sorted
     * 1-D array. Treats index {@code i} as {@code (i / n, i % n)}.
     *
     * <p>Time: O(log(m·n)) — Space: O(1)</p>
     */
    public static boolean searchMatrix(int[][] matrix, int target) {
        if (matrix.length == 0 || matrix[0].length == 0) return false;

        int m = matrix.length, n = matrix[0].length;
        int lo = 0, hi = m * n - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int val = matrix[mid / n][mid % n];
            if (val == target) return true;
            if (val < target)  lo = mid + 1;
            else               hi = mid - 1;
        }
        return false;
    }

    // =====================================================================
    //  7. Partition — median of two sorted arrays
    // =====================================================================

    /**
     * <b>Median of Two Sorted Arrays</b> — O(log min(m, n)) partition search.
     * Swaps the arrays internally so {@code a} is always the shorter one.
     *
     * <p>We pick a cut {@code i} in {@code a} and a matching cut
     * {@code j = (m + n + 1) / 2 - i} in {@code b} such that the left halves
     * together hold {@code (m + n + 1) / 2} elements. The cut is correct when
     * {@code aLeft <= bRight} and {@code bLeft <= aRight}. Median follows.</p>
     *
     * <p>Time: O(log min(m, n)) — Space: O(1)</p>
     */
    public static double findMedianSortedArrays(int[] a, int[] b) {
        if (a.length > b.length) {
            return findMedianSortedArrays(b, a);       // ensure a is the shorter one
        }
        int m = a.length, n = b.length;
        int lo = 0, hi = m;

        while (lo <= hi) {
            int i = lo + (hi - lo) / 2;
            int j = (m + n + 1) / 2 - i;

            int aLeft  = (i == 0) ? Integer.MIN_VALUE : a[i - 1];
            int aRight = (i == m) ? Integer.MAX_VALUE : a[i];
            int bLeft  = (j == 0) ? Integer.MIN_VALUE : b[j - 1];
            int bRight = (j == n) ? Integer.MAX_VALUE : b[j];

            if (aLeft <= bRight && bLeft <= aRight) {
                if ((m + n) % 2 == 1) {
                    return Math.max(aLeft, bLeft);
                }
                return (Math.max(aLeft, bLeft) + Math.min(aRight, bRight)) / 2.0;
            }
            if (aLeft > bRight)      hi = i - 1;      // i too big
            else                     lo = i + 1;      // i too small
        }
        throw new IllegalStateException("unreachable — inputs are sorted");
    }

    // =====================================================================
    //  Bonus — interop note for the JDK's helper
    // =====================================================================

    /**
     * Demonstrates the JDK {@link Arrays#binarySearch(int[], int)} contract:
     * returns the index if found, otherwise {@code -(insertionPoint) - 1}.
     * Provided as a comparison to {@link #lowerBound(int[], int)}, not as a
     * replacement.
     */
    public static int jdkBinarySearch(int[] nums, int target) {
        return Arrays.binarySearch(nums, target);
    }
}