# Summarize Details for each patters

1. [`com.vbforge.twopointers` — Two Pointers Pattern](#comvbforgetwopointers--two-pointers-pattern)
2. [`com.vbforge.slidingwindow` — Sliding Window Pattern](#comvbforgeslidingwindow--sliding-window-pattern)
3. [`com.vbforge.binarysearch` — Binary Search Pattern](#comvbforgebinarysearch--binary-search-pattern)
4. [`com.vbforge.frequencycounting` — Frequency Counting Pattern](#comvbforgefrequencycounting--frequency-counting-pattern)
5. [`com.vbforge.matrixtraversal` — Matrix Traversal Pattern](#comvbforgematrixtraversal--matrix-traversal-pattern)
6. [`com.vbforge.monotonicstack` — Monotonic Stack Pattern](#comvbforgemonotonicstack--monotonic-stack-pattern)
7. [`com.vbforge.prefixsum` — Prefix Sum Pattern](#comvbforgeprefixsum--prefix-sum-pattern)
8. [`com.vbforge.overlappingintervals` — Overlapping Intervals Pattern](#comvbforgeoverlappingintervals--overlapping-intervals-pattern)
9. [`com.vbforge.greedy` — Greedy Pattern](#comvbforgegreedy--greedy-pattern)
10. [`com.vbforge.topkelements` — Top-K Elements Pattern](#comvbforgetopkelements--top-k-elements-pattern)
11. [`com.vbforge.backtracking` — Backtracking Pattern](#comvbforgebacktracking--backtracking-pattern)* [🧬 The Skeleton](#-the-skeleton)
12. [`com.vbforge.binarytreetraversal` — Binary Tree Traversal Pattern](#comvbforgebinarytreetraversal--binary-tree-traversal-pattern)
13. [`com.vbforge.dfs` — Graph DFS Pattern](#comvbforgedfs--graph-dfs-pattern)
14. [`com.vbforge.bfs` — Graph BFS Pattern](#comvbforgebfs--graph-bfs-pattern)
15. [`com.vbforge.dynamicprogramming` — Dynamic Programming Pattern](#comvbforgedynamicprogramming--dynamic-programming-pattern)

---

## `com.vbforge.twopointers` — Two Pointers Pattern

This package demonstrates the **Three classic variations** of the Two Pointers pattern, each solving a distinct class of problems:

| Variation | Pointer Movement | Example Problems |
|---|---|---|
| **Opposite Direction** | `left++`, `right--` until they meet | Two Sum II, Valid Palindrome, Container With Most Water |
| **Same Direction (fast/slow)** | Both move right, at different speeds | Remove Duplicates, Move Zeroes |
| **Multi-array** | One pointer per array, walk from the end | Merge Sorted Array |

Additionally, two **combination problems** are included: **3Sum** (sort + two pointers) and **Sort Colors** (Dutch National Flag, three pointers).

### 🎯 Key Takeaways

* **`twoSumSorted` & `threeSum`** — the canonical entry points for the pattern; note how `threeSum` *reuses* the opposite-direction skeleton inside an outer loop after sorting.
* **`moveZeroes`** — uses a **swap** variant of the same-direction pattern (rather than a `writeIndex`), so relative order is preserved *and* no post-fill is needed.
* **`mergeSorted`** — the trick is to fill from the **back** of `nums1`; if you fill from the front you'd overwrite un-merged values in `nums1`.
* **`sortColors`** — the only three-pointer example here. Note that you **never** `mid++` when you swap with `high` (you must re-examine the swapped-in value).
* **`maxArea`** — the correctness argument is *the bottleneck trick*: moving the taller side would only shrink the width without any chance of increasing area, so we must move the shorter one.

---

## `com.vbforge.slidingwindow` — Sliding Window Pattern

This package demonstrates the **two main variations** of the sliding window, plus the two special "at most K" flavour:

| Variation | Window Size | Typical Operations | Example Problems |
|---|---|---|---|
| **Fixed-size window** | Known in advance (`k`) | Slide by one; `+nums[right]`, `-nums[left]` | Max Sum Subarray of Size K, Find All Anagrams |
| **Variable-size window** | Unknown | Grow right; shrink left while invalid | Longest Substring Without Repeats, Min Size Subarray Sum |
| **"At most K" window** | Constraint on content | Track a counter/frequency; shrink while it exceeds K | Longest Repeating Character Replacement, Max Consecutive Ones III, Fruit Into Baskets |

The template is always the same skeleton:

```java
int left = 0;
for (int right = 0; right < n; right++) {
    // 1. expand: add nums[right] to state
    while (/* window invalid */) {
        // 2. shrink: remove nums[left] from state
        left++;
    }
    // 3. answer: record window size (right - left + 1)
}
```

### 🎯 Key Takeaways

* **Template discipline pays off.** Every method above fits the same five-line skeleton — `for (right ...)` → expand → `while (invalid) shrink` → record answer. If you can identify *what makes the window "invalid"*, the code writes itself.
* **`minWindow` vs `lengthOfLongestSubstring` — same skeleton, mirrored objective.** One minimizes on the *shrink* path; the other maximizes on the *grow* path. Both maintain a "valid" invariant.
* **`findAnagrams` uses two fixed-size frequency arrays**, not a `HashMap`. When the alphabet is bounded (`int[26]`, `int[128]`), use an array — it's cache-friendly, allocation-free per iteration, and `Arrays.equals` is O(1).
* **`minSubArrayLen` requires positive values.** With negatives the shrink step (`sum -= nums[left++]`) is unsound because the sum isn't monotone; you'd need a prefix-sum + monotone-deque solution instead.
* **`characterReplacement` has a nice trick** — `maxFreq` never decreases. Even if it's temporarily "stale", using it to keep the window from shrinking doesn't break correctness — it just trades a slightly less accurate shrink for a faster code path.
* **`longestOnes` and `totalFruit` are literally the same algorithm** with a different "invalid" predicate. Once you've internalised the pattern, these become trivial variations.
* **`minWindow`'s `required` counter** is the elegant part: by only decrementing `required` when `need[c] > 0`, we count the *exactly needed* instances. Duplicate surplus characters don't accidentally make the window look valid.

---

## `com.vbforge.binarysearch` — Binary Search Pattern

This package covers the full spectrum of binary search: from the classic `lo/hi` bisection up through **binary search on the answer space** (Koko, integer sqrt) and the two classic "hard" problems (**Search in Rotated**, **Median of Two Sorted Arrays**).

| Variation | Invariant style | Example |
|---|---|---|
| **Exact match** | `[lo, hi]` closed, `lo <= hi` | `binarySearch` |
| **Boundary search** | `[lo, hi)` half-open, `lo < hi` | `lowerBound`, `upperBound`, `searchInsert` |
| **Rotated array** | Detect which half is sorted | `searchRotated`, `findMinRotated` |
| **Peak / shape** | Walk uphill | `findPeakElement` |
| **Answer space** | Binary search over possible answers | `minEatingSpeed`, `sqrt` |
| **Virtual 1D** | Index math on 2D | `searchMatrix` |
| **Partition** | Find split point in two arrays | `findMedianSortedArrays` |


### 🎯 Key Takeaways

* **Two invariants, not two algorithms.** The closed `[lo, hi]` version and the half-open `[lo, hi)` version are the **same** binary search — just with different loop conditions. Choosing the right one per problem eliminates 90% of off-by-one bugs.
    * *Need an exact index?* → closed `[lo, hi]`, `lo <= hi`.
    * *Need a boundary (first ≥, first >, insertion point, min)?* → half-open `[lo, hi)`, `lo < hi`, answer is `lo`.
* **`lowerBound` is the universal hammer.** `searchInsert`, `searchRange.first`, `Arrays.binarySearch`'s negative encoding, and even the "find peak" boundary case all reduce to it.
* **`mid = lo + (hi - lo) / 2`** — always. `(lo + hi) / 2` overflows for very long arrays.
* **`sqrt` and `Koko` are the same pattern** — binary search over an *ordered* answer space, not over an array. The invariant is: "all speeds `< k` fail, all speeds `≥ k` succeed" — a monotone predicate, perfect for bisection.
* **Rotated search = "identify the sorted half."** Only one of `[lo, mid]` / `[mid, hi]` can be sorted (compare `nums[lo]` with `nums[mid]`). Once you know which one, `target` either lies inside it — recurse there — or it doesn't — go to the other half.
* **`findMinRotated` with duplicates** — you **must** add the `hi--` branch when `nums[mid] == nums[hi]`. Without it, `[1, 1, 1, 0, 1]` will fail. This drops worst-case to `O(n)` but is unavoidable — you cannot distinguish `[1,0,1,1,1]` from `[1,1,1,0,1]` without scanning.
* **`findPeakElement` is bisection on the *derivative*.** The "sorted" direction is implicit in `nums[mid] < nums[mid+1]` — you're not searching values, you're searching the sign of the slope.
* **Median of two sorted arrays** is the hardest here. Two key insights:
    1. Symmetric cut: `i + j = (m + n + 1) / 2`. Only search `i` in the shorter array.
    2. Correct cut ⟺ `aLeft <= bRight && bLeft <= aRight`. If `aLeft > bRight`, `i` is too big — shrink `hi`.
       Use `MIN_VALUE` / `MAX_VALUE` sentinels so boundary cuts (`i = 0`, `i = m`) don't need special cases.

---

## `com.vbforge.frequencycounting` — Frequency Counting Pattern

This package covers the family of problems solved by **counting how often each element occurs**, then answering a query over those counts. It spans four sub-techniques:

| Technique | Data structure | Example |
|---|---|---|
| **Alphabet-array counting** | `int[26]` / `int[128]` | Anagram, Ransom Note, First Unique |
| **Hash-map counting** | `HashMap<K, Integer>` | Group Anagrams, Frequency Sort |
| **Counting the counts** | bucket array `List[]` | Top-K Frequent (O(n)) |
| **Voting / set tricks** | O(1) space | Boyer-Moore Majority, Longest Consecutive |

### 🎯 Key Takeaways

* **`int[26]` beats `HashMap` whenever the alphabet is bounded.** It's an order of magnitude faster in practice (no boxing, no hashing, cache-friendly), and lets `isAnagram` compare two multisets with a plain loop over 26 entries — total O(1) after the counting pass.
* **`--count[c] < 0` is the elegant `canConstruct` idiom.** Decrement first, then test — the test fails exactly when we've *consumed more than available*.
* **`groupAnagrams` uses a canonical key.** `sorted(chars)` is O(k log k) per string; the alternative `count[26]` — keyed map is O(k) but needs its own `hashCode` wrapper. For typical string lengths, the sorted version wins on clarity.
* **Bucket sort for Top-K is a frequency-of-frequency trick.** `bucket[f]` lists the values that occur exactly `f` times. Since `f ∈ [1, n]`, walking buckets from `n` downward yields the top-K in O(n), no heap required.
* **The heap variant shows the trade-off:** O(n log k) is *slightly* worse than O(n) here, but generalises more cleanly when the query is not "top k" but "k elements closest to target X."
* **Boyer-Moore's magic is one line:** `count += (n == candidate) ? 1 : -1`. It works because a strict majority cannot be cancelled out by any mixture of other elements — after all pairings, only the majority remains.
* **`longestConsecutive`'s `!set.contains(n-1)` guard is critical.** Without it, the algorithm is O(n²) on the input `[1, 2, 3, …, n]` — every element re-walks the whole run. *With* it, each element is visited at most twice → O(n).
* **`frequencySort` needs an explicit tie-break** or the output is non-deterministic. Choosing `Character.compare` gives a stable, testable result — at the cost of differing from what LeetCode might return as "any valid answer."

---

## `com.vbforge.matrixtraversal` — Matrix Traversal Pattern

This package covers the **six canonical ways to walk a 2-D array** that show up repeatedly in interviews and production code:

| Traversal | Technique | Example |
|---|---|---|
| **Spiral order** | Four shrinking walls (`top`/`bottom`/`left`/`right`) | Spiral Matrix |
| **Spiral fill** | Same wall skeleton, writing instead of reading | Generate Spiral Matrix |
| **Diagonal zigzag** | Direction flag based on `(r + c) % 2` | Diagonal Traverse (LC 498) |
| **In-place transpose** | Swap `a[i][j] ↔ a[j][i]` for `j > i` only | Transpose Matrix |
| **90° rotation** | `transpose` then reverse each row | Rotate Image |
| **Marker trick** | Use first row/column as O(1)-space booleans | Set Matrix Zeroes |


### 🎯 Key Takeaways

* **The spiral skeleton — with the two guards — is reusable.** `spiralOrder` and `generateMatrix` share the *exact same* wall-shrink loop; only the body changes (`add` vs `assign`). Once you internalise that skeleton, all spiral-shaped problems become fill-in-the-blank.
* **The `left` and `up` guards exist to prevent double-visits.** Without `if (top <= bottom)` on the left pass, a single-row matrix would re-emit the same elements in reverse. Without `if (left <= right)` on the up pass, a single-column matrix would do the same. Every buggy spiral solution I've ever seen trips on this line.
* **Diagonal traversal has 4 wall cases.** The `(r+c) % 2` test picks direction; then *within* each direction there are two wall fallbacks (one when you've hit the far wall, one when you've hit the near wall) plus the default step. The order of the wall checks matters — you must handle the **far** wall first.
* **`transpose` uses `j > i` (upper triangle only) for a reason.** Writing `j >= i` (or iterating all `j`) swaps each pair twice, undoing the transpose. A common off-by-one.
* **`rotate90` = `transpose` + `reverse rows`.** The classic four-way swap loop is `n²/4` iterations but notoriously easy to get wrong on even/odd `n`. The two-step recipe is slower by a constant factor but far easier to read and verify in production code.
* **`setZeroes`'s O(1) trick overloads the matrix as its own flag store** — but cell `(0,0)` is ambiguous. The solution is to capture the first row / first column's "should I zero" state into **two booleans before planting markers**, and apply them **last**. This is the canonical interview answer.
* **In-place algorithms require square input** — document the precondition, enforce with `IllegalArgumentException`, and offer a `transposeCopy` for rectangles. Don't silently mishandle non-square.

---

## `com.vbforge.monotonicstack` — Monotonic Stack Pattern

This is the trickiest pattern in the whole series, so I'll structure it carefully. Every test case is **hand-traced in a comment** before the assertion — per the lesson learned.

The monotonic stack solves problems that ask:

> *"For each element, what is the nearest element to the left/right that is greater/smaller than me?"*

| Variation | Stack order | Pop condition | Example |
|---|---|---|---|
| **Next greater** | decreasing | `top < current` | Next Greater Element I |
| **Next greater (circular)** | decreasing, 2n iteration | `top < current` | Next Greater Element II |
| **Next greater distance** | decreasing (indices) | `top < current` | Daily Temperatures |
| **Next smaller boundary** | increasing | `top > current` | Largest Rectangle |
| **Basin (water)** | decreasing | `top < current` | Trapping Rain Water |
| **Sliding window max** | monotonic deque | `back <= current` | Sliding Window Maximum |
| **Contribution (double stack)** | inc + dec | varies | Sum of Subarray Minimums |

### 🎯 Key Takeaways

* **The pattern is one skeleton with four knobs:**
    1. *What* goes on the stack — values or indices.
    2. *Which* comparator — `top < current` (next-greater) vs `top > current` (next-smaller).
    3. *Where* the answer goes — during pop (contribution) or after the loop (residual stack).
    4. *When* to push — every index (temperature) or only in the first pass (circular).
* **`nextGreaterCircular` iterates 2n but pushes n** — the elegant trick is `if (i < n) stack.push(idx)`. Pushing during the second pass would corrupt the algorithm.
* **`largestRectangleArea` is `dailyTemperatures`'s cousin** — both store indices, both use `i - j` as width. The difference is the sentinel: `largestRectangleArea` requires appending `0` so the stack drains; `dailyTemperatures` doesn't because we *want* residuals to stay `0` in the answer.
* **`trap`'s pop sequence is the tricky bit.** Each iteration pops the *bottom* of a basin, not the whole basin. The inner `if (stack.isEmpty()) break;` after `bottom = stack.pop()` is the crucial guard — without it you'd access `stack.peek()` on an empty stack.
* **`maxSlidingWindow` is a *deque*, not a stack.** Two independent eviction rules — front (window expiry) and back (monotonicity). Getting the order right matters: expire from front **first**, then pop smaller from back, then push. If you push first, the current index could be evicted by the "expiry" check on the same iteration for `k=1`.
* **`sumSubarrayMins` needs *two* stacks with *asymmetric* comparisons.** `prevLess` uses `>=` (pop equal), `nextLessEq` uses `>` (keep equal). The asymmetry is what makes each subarray counted exactly once when duplicates exist. If you use `<` and `>` (both strict), `[2,2]` returns 2 instead of 6.
* **The `MOD` operation should be applied per contribution**, not at the end — otherwise intermediate `left * right * nums[i]` can overflow `long` for large inputs (`~10^9 * 10^5 * 10^5 = 10^19`).

---

## `com.vbforge.prefixsum` — Prefix Sum Pattern

This package covers the *"precompute once, query many times"* family. The core idea: if you can answer `sum(arr[0..i])` in O(1), then any range sum `sum(arr[l..r])` becomes `prefix[r+1] - prefix[l]` — also O(1). Everything else is a variation on this theme.

| Variation | Extra technique | Example |
|---|---|---|
| **1-D prefix sum** | `int[n+1]` array | Range Sum Query |
| **2-D prefix sum** | Inclusion-exclusion | Range Sum 2D |
| **Prefix sum + HashMap** | `{prefix → count}` | Subarray Sum Equals K |
| **Prefix sum + HashMap (first index)** | Longest/subarray properties | Contiguous Array, Divisible by K |
| **Prefix product** | Two passes (L→R, R→L) | Product Except Self |
| **Prefix XOR** | Same as sum but `^` | XOR Queries |
| **Running sum** | Left-sum vs right-sum | Pivot Index |

### 🎯 Key Takeaways

* **The leading `0` in the prefix array is not optional.** `prefix[0] = 0` is the "sum of the empty prefix" — it makes the formula `prefix[r+1] - prefix[l]` uniform for `l = 0`. Without it, every `l = 0` query needs a special case, and every `{prefix → count}` map needs to seed `{0: 1}` — same idea, different surface.
* **2-D prefix is inclusion-exclusion.** `P[i][j]` = sum of the rectangle `[0..i-1] × [0..j-1]`, computed as `M[i-1][j-1] + P[i-1][j] + P[i][j-1] - P[i-1][j-1]`. The `- P[i-1][j-1]` term "un-double-counts" the corner. Query: `P[r2+1][c2+1] - P[r1][c2+1] - P[r2+1][c1] + P[r1][c1]` — mirror shape, easy to remember.
* **The `{0: 1}` seed in `subarraySum` is what makes "subarray starting at index 0" work.** Without it, `[1,2,3], k=6` would miss `[1,2,3]` itself. The seed represents the "prefix before the array starts."
* **`subarraySum` and `subarraysDivByK` are the same algorithm** with different key types (value vs. `value % k`). Recognising this collapses two seemingly separate problems into one.
* **`findMaxLength` uses the *first* index, not the *count*.** The distinction: `subarraySum` asks "how many pairs", so it wants *counts*; `findMaxLength` asks "how long is the longest", so it wants the *earliest* occurrence. Same `{prefix → X}` shape — different `X`.
* **`productExceptSelf` = prefix × suffix.** It's really two prefix arrays fused. The fact that you can do it in one output array (no extra allocation) comes from the trick of overwriting `result[i]` with the right-side product in pass 2 while keeping a running `right` scalar.
* **Negative modulo in Java is a real bug source.** `-7 % 5` is `-2`, not `3`. The fix — `((x % k) + k) % k` — is worth memorising; it appears in `subarraysDivByK`, in KMP failure-function computation, and in several hashing problems.
* **XOR is self-inverse; that's the whole trick.** `prefix[r+1] ^ prefix[l]` works because `x ^ x = 0`. You don't need subtraction or a "not" operator — just another XOR. This makes XOR range queries as cheap as sum range queries.

---

## `com.vbforge.overlappingintervals` — Overlapping Intervals Pattern

This package covers the interval family. Every problem here starts with **sort**, but *what you sort by* and *what you do after* distinguishes the flavours:

| Variation | Sort by | After-sort technique | Example |
|---|---|---|---|
| **Union / merge** | **start** | Sweep, extend current end | Merge Intervals |
| **Insert** | already sorted | Three-phase scan | Insert Interval |
| **Max non-overlapping** | **end** | Greedy earliest-finish | Erase Overlap Intervals, Min Arrows |
| **Overlap check** | start | Adjacent comparison | Meeting Rooms I |
| **Max concurrent** | starts + ends separate | Two-pointer sweep | Meeting Rooms II |
| **Pairwise intersection** | already sorted | Two-pointer scan | Interval List Intersections |


### 🎯 Key Takeaways

* **Sort by start vs. sort by end is the single decision that defines each solution.** Merge and insert sort by start; the greedy selection family (erase-overlap, min-arrows, meeting-rooms-II) sorts by end. If you pick the wrong one, you don't get an exception — you get a *confidently wrong* answer. Always state your choice in a comment.
* **"Touching" semantics are problem-specific, not a universal rule.**
    * LC 56 / 57 treat intervals as **closed** — `[1,2]` and `[2,3]` merge.
    * LC 435 / 452 / 253 treat them as **half-open** — `[1,2]` and `[2,3]` are disjoint.
      The exact same pair of intervals gives different answers depending on which problem you're solving. Encode this in your test comment.
* **`insert` is three linear scans.** The temptation is to do a single scan with a boolean "inserted" flag. That leads to nine branches and off-by-one bugs. The three-phase structure (`before` / `merge` / `after`) is bug-resistant by construction — each phase is a simple `while` with no nested state.
* **`minMeetingRooms` splits the interval into two independent arrays.** `starts[]` and `ends[]` don't need to be paired. Once you see this, the sweep line becomes obvious: "as we start meetings in order, does the earliest available ending beat us to a room?"
* **`eraseOverlapIntervals` and `findMinArrowShots` are the same algorithm** with different termination conditions. Both sort by end and greedily consume. The only difference is what "keep" vs. "drop" means: erase counts *dropped*; arrows counts *kept groups*.
* **`intervalIntersection` is two-pointer scan, not two-pointer sort.** Because both inputs are already sorted, you just walk them like merging — no sort required. Emit `[max(start), min(end)]` when `lo <= hi`, then advance the earlier-ending interval.
* **Defensive copy is worth the constant cost.** Every method here clones its input before sorting. This is the difference between a library-quality API and a footgun. The tests verify it (`doesNotMutateInput`, `doesNotMutateNewInterval`) so a future "optimisation" that removes the clone will be caught immediately.
* **The brute-force oracle for `merge` is a template.** When the expected output is genuinely hard to eyeball (merge, histogram-area, skyline), build a naïve implementation that's obviously correct on small inputs, then cross-check. This is how you catch the "expected value was wrong" class of test bugs — the ones that have been plaguing us — *before* they make it into the suite.

---

## `com.vbforge.greedy` — Greedy Pattern

This package covers seven classic greedy problems. The recurring theme: **each one is easy to write but hard to trust** — greedy solutions look plausible long before you can prove they're correct. So this time, three of the seven ship with an **independent brute-force oracle** in the test class:

| Problem | Oracle added? | Why |
|---|---|---|
| **Jump Game I** | — | One-line reachability; hand-trace is enough |
| **Jump Game II** | ✅ BFS oracle | Optimal jump count is non-obvious |
| **Gas Station** | — | Total-sum invariant + single reset pass is provable |
| **Candy** | ✅ Fixpoint oracle | Two-pass greedy is famously easy to under-count |
| **Task Scheduler** | ✅ Simulation oracle | The `(maxCount - 1)*(n+1) + numMax` formula has edge cases |
| **Partition Labels** | — | Last-occurrence scan is a direct construction |
| **Best Time II** | — | Sum-of-positive-gaps is trivially equivalent |


### 🎯 Key Takeaways

* **"Greedy" is not a pattern — it's a claim.** The pattern is "make the locally optimal choice"; the *claim* is that this reaches the global optimum. Every method in this file carries its **argument in the Javadoc**:
    * **`canJump`** — invariant: `maxReach` dominates every position reachable so far.
    * **`jump`** — exchange: BFS-level farthest reach picks the optimal frontier.
    * **`canCompleteCircuit`** — exchange: prefix with negative balance can be abandoned.
    * **`candy`** — pointwise minimum: two passes are necessary and sufficient.
    * **`leastInterval`** — structural: the "skeleton" dominates idles, and `max` with `n` covers overflow.
    * **`partitionLabels`** — construction: last-occurrence closure is forced.
    * **`maxProfit`** — exchange: sum of positive gaps dominates any trade sequence.

  If you can't state the argument in one sentence, the greedy is probably wrong.

* **Three of seven problems ship with an oracle.** This is the right ratio for greedy specifically — even more than for the other patterns. Greedy bugs hide in edge cases that are hard to see by reading. `jump`, `candy`, and `leastInterval` are exactly the cases where "looks right" is insufficient. The oracles (BFS, fixpoint, simulator) are **completely different algorithms** from the greedy — so when they agree, you have independent confirmation.
* **`canJump` versus `jump` is a classic pair.** Same family, same data, but "reachable?" is trivially greedy (`maxReach`) while "min jumps?" needs the BFS-level trick (`curEnd` / `farthest`). It's easy to confuse the two. The tests for `jump` explicitly include the LC 55 example `[3,2,1,0,4]` as **excluded** (comment removed) — because it's not a valid LC 45 input. Watch for this trap when writing your own tests.
* **`canCompleteCircuit` has a one-line proof that most people skip.** If `sum(gas) >= sum(cost)`, a valid start exists (the "unique start" property of the problem guarantees uniqueness). And any prefix with non-positive tank can be abandoned because the true start lies strictly after it. Together these justify the single-pass reset — no backtracking needed.
* **`leastInterval` has a skeleton that many people get wrong.** The `max( skeleton, total )` is necessary because when many *distinct* tasks spill over the skeleton, they fill idle slots exactly — resulting in `total` not `skeleton`. The tests include `[A,A,B,B]` with `n=3` (answer 6, `total=4`) and `[A,B,C,D,E,F]` with `n=2` (answer 6, `skeleton=6`, `total=6`) — the boundary case where the two formulas coincide.
* **`partitionLabels` is a mini interval-merge.** Last-occurrence → interval `[first, last]` → merge overlapping intervals → lengths. It's the same skeleton as `mergeIntervals`, but with the "sorted by start" and "already sorted" facts baked in because we scan left-to-right.
* **`maxProfit` is a 3-line one-liner that everybody over-thinks.** The exchange argument is one sentence: "each transaction captures a strictly-positive subarray, and every positive price change can be its own transaction, so summing them is optimal." Don't add a DP.


---

## `com.vbforge.topkelements` — Top-K Elements Pattern

This package covers the family of problems that ask **"give me the `k` best"** — where "best" is by magnitude, frequency, distance, or something else. Three techniques recur:

| Technique | Complexity | When to use |
|---|---|---|
| **Sort + index** | O(n log n) | Simplest; only for small `n` or as an oracle |
| **Heap of size k** | O(n log k) | Streaming-friendly; heap size k is a *bounded* resource |
| **Quickselect** | O(n) *expected*, O(n²) worst | Data is already in memory; you want `k`-th, not sorted top-k |
| **Binary search on value** | O((m+n) · log(range)) | Value space is ordered but not enumerable (kth-smallest-in-matrix) |

> 🔁 **Heaps have a "min or max" trap.** For "kth **largest**", you keep a **min-heap** of size `k` — the min of that heap is the kth largest. For "kth **smallest**", keep a **max-heap**. This is the reverse of what feels natural, and it's the single most common bug in this pattern. Every heap here carries a comment stating which direction it keeps.

> 🛡️ **Oracle discipline.** Four of the six problems ship with a **sort-based oracle** in the test file. `findKthLargest` additionally runs a **500-iteration randomized comparison** against the oracle so quickselect's partition edge cases (duplicates, all-equal, sorted, reversed) get exercised every run.

### 🎯 Key Takeaways

* **The inverted-heap rule is the pattern's litmus test.** For "k largest", keep a **min-heap** of size k — you must be willing to evict the smallest of your current k best when a bigger candidate arrives. For "k smallest", keep a **max-heap**. Getting this backwards is the #1 bug; it also silently produces wrong answers, not exceptions. Write the direction in a comment *every time*.

* **Quickselect is O(n) *expected*, not guaranteed.** The worst case (adversarial input + pathological pivot) is O(n²). The 3-way partition with a **random pivot** is the pragmatic choice:
  * **Random pivot** — defeats sorted / reverse-sorted inputs that turn naive quickselect quadratic.
  * **3-way partition** — defeats all-equal inputs. Without it, `[7,7,7,7,7]` degrades to O(n²) because Lomuto's strict `<` puts everything on one side every iteration.
    Both are needed. The test suite has `[7,7,7,7,7]` and a 500-iteration random comparison specifically to catch regressions on these.

* **`kthSmallest` matrix shows binary search on a *non-array*.** There's no 1-D array to bisect. Instead, binary search the **value** — the answer is *some* entry between `matrix[0][0]` and `matrix[m-1][n-1]`. The predicate is `count(≤ mid) ≥ k`, computable in O(m + n) via a **staircase walk** from the bottom-left corner. Total: O((m + n) · log(range)). This is exactly the same shape as `minEatingSpeed` from the binary-search package — the pattern is universal.
* **`mergeKLists` is the k-way merge.** The heap holds **one node per list** — always the current head of each list. Each pop exposes the next node of that list. Total heap size is O(k), total pops are O(N). Same shape used in external merge-sort and in log-aggregation systems (top-N entries across k sorted streams).
* **Squared distances are the right tool.** For "closest point" problems, use `x² + y²` instead of `sqrt(x² + y²)`. Ordering is preserved, floating-point is not involved, and any integer type works. Just use `long` for the squared value — `x=10⁶` alone overflows `int` in the multiplication.
* **`doesNotMutateInput` is the API contract test.** Both `findKthLargest` and `kClosest` clone their working set internally, so the caller's array is untouched. This is what distinguishes library code from "the algorithm runs correctly in isolation." The test catches future "optimisations" that remove the clone.
* **The heap `topKFrequent` here is deliberately different from the bucket-sort variant in `frequencycounting`.** Two implementations of the same problem, different complexity profiles:
  * Bucket sort: **O(n)** time, O(n) space — when `n` is small enough to allocate n+1 buckets.
  * Heap: **O(n log k)** time, O(k) space — when `k` is small and `n` is huge (streaming).
    Neither is universally better; having both is the point.

---

## `com.vbforge.backtracking` — Backtracking Pattern

This package covers nine problems, all sharing a **single skeleton**. It's the most template-friendly of the fifteen patterns — once you internalise the skeleton, every problem is a fill-in-the-blanks exercise.

## 🧬 The Skeleton

```java
void backtrack(State state) {
    if (isGoal(state))  { record(state); return; }        // ✋ base case
    for (Candidate c : candidates(state)) {
        if (!isValid(c, state))  continue;                // ✂️ prune
        apply(c, state);                                  // ⬇️ choose
        backtrack(state);                                 // 🔁 explore
        undo(c, state);                                   // ⬆️ un-choose
    }
}
```

What varies per problem is only:
1. **What a "candidate" is** (index, digit, char, queen column…)
2. **What "goal" means** (`path.size() == k`, `remaining == 0`, `row == n`, `idx == word.length()`…)
3. **What "valid" means** (sum doesn't exceed target, no repeated element, no queen attack…)


### 🎯 Key Takeaways

* **The skeleton is the same; only the three substitutions change.** Once you can name "candidate / goal / valid", the code writes itself. Compare `subsetsDfs` (candidates: indices ≥ start; goal: every prefix) vs. `permuteDfs` (candidates: all unused indices; goal: length n) vs. `genParensDfs` (candidates: `'('`, `')'`; goal: length 2n). Three different problems, one skeleton.

* **`start` versus `used[]` is the key distinction.** When the same element must not repeat **and order doesn't matter** (subsets, combinations), use `start` — it prevents re-choosing earlier indices, which also prevents permutations. When order matters (permutations), you can pick any unused element, so use `used[]`.

* **Duplicate-skipping requires sort + one of two idioms.**
  * **Subsets / combinations** — `i > start && nums[i] == nums[i-1]` → skip. Correct because `start` bounds the current level.
  * **Permutations** — `i > 0 && nums[i] == nums[i-1] && !used[i-1]` → skip. The extra `!used[i-1]` says "the previous equal value is no longer in the current path, so I'd be choosing it at the same level."
    Getting the permutations version wrong (omitting `!used[i-1]`) produces *fewer* solutions, not more — because you'd skip the second `1` even when the first `1` is still on the path.

* **`combinationSum` vs `combinationSum2` differ by exactly two characters**: `i` vs `i + 1` (reuse vs. not), plus the duplicate-skip line. If you can find the diff, you understand both.

* **N-Queens is O(1) prune, not O(n) prune.** The naive version scans the current board per candidate — O(n) per placement, O(n²) per level, O(n³) overall. The three-boolean-array version makes the check O(1), yielding the canonical O(n!) algorithm.

* **Word Search mutates the board.** Unlike the other backtracking problems here, the input is a mutable 2-D array, and the algorithm relies on the in-place `'#'` marker. The base case for any such problem is: **must be restored before return**. Both `doesNotMutateBoard` and `repeatedCallsWorkAfterFailure` verify this. Without a `try/finally`, a thrown exception would leak the marker — but the DFS never throws, so the plain `board[r][c] = saved` on the normal path is sufficient here.

* **Oracles everywhere.** Every combinatorial problem here has an independent generator: bit-mask for subsets, next_permutation for permutations, count-based DFS for combinationSum, 2^(2n) filter for parentheses. These are structurally different from the DFS impl — bit masks don't recurse, next_permutation is iterative, count-based DFS moves *forward* through indices. Agreement between different algorithms is a stronger correctness signal than any hand-check.

* **N-Queens counts 1, 0, 0, 2, 10, 4, 40, 92** for `n = 1..8` — this is the OEIS A000170 sequence. Comparing counts is much faster than comparing explicit boards for `n ≥ 5`, where the boards get numerous (92 for n = 8). The one explicit test (n = 4) gives us a concrete assertion to catch shape bugs.

---

## `com.vbforge.binarytreetraversal` — Binary Tree Traversal Pattern

This package covers the four DFS orders (**pre/in/post/level**) plus six variations that build on them. Every traversal exists in **two forms** — recursive (natural, trusted) and **iterative (the one that gets bugs)**. So for each pair, the recursive version becomes the oracle in the tests: **cross-checking recursive-vs-iterative is the strongest correctness signal available**, and it catches the "you wrote something plausible but wrong" trap that plagues tree code.

### 🌳 Standard Test Tree

All traversal tests use this same well-known shape:

```
        1
       / \
      2   3
     / \   \
    4   5   6
```

Which produces the canonical outputs:

| Traversal | Output |
|---|---|
| Pre-order | `1, 2, 4, 5, 3, 6` |
| In-order | `4, 2, 5, 1, 3, 6` |
| Post-order | `4, 5, 2, 6, 3, 1` |
| Level-order | `[[1], [2, 3], [4, 5, 6]]` |
| Zigzag | `[[1], [3, 2], [4, 5, 6]]` |
| Right view | `[1, 3, 6]` |
| Vertical (LC 987) | `[[4], [2], [1, 5], [3], [6]]` |
| Max depth | `3` |
| Diameter | `4` (4→2→1→3→6) |

### 🎯 Key Takeaways

* **Recursive vs iterative is a *real* oracle, not a hand-wave.** For each DFS traversal, the recursive form *is* the mathematical definition — it's trivially correct by construction. The iterative form is where the bugs live (`off-by-one` in stack push order, forgetting `curr = curr.right` in in-order, mis-ordering left/right in post-order). Comparing them element-by-element on six diverse trees is the strongest possible correctness check — much stronger than hand-tracing a single "reference output" (which is exactly the test-side bug family we've been chasing all series).

* **The one non-obvious iterative is post-order.** Pre-order and in-order iterative are mechanical. Post-order needs one of three tricks:
  1. Two stacks (explicit reverse collection — what we use).
  2. Single stack + `prev` pointer ("have I already returned from my right child?" — not used here, prone to bugs).
  3. Morris traversal (O(1) space, mutates tree temporarily — advanced, out of scope).

  The **reverse-of-modified-pre-order** trick is the cleanest:
  ```
  root → right → left  (achieved by pushing left before right)
  reversed = left → right → root = post-order ✓
  ```
  The invariant is "**prepend every pop**", which is the mental model. Once internalised, the code is 5 lines.

* **`List.reversed()`** (Java 21) would be idiomatic for post-order, but I used `LinkedList.addFirst` because the prepend-while-pushing flow makes the invariant clearer. Both work; `addFirst` is O(1) per element and avoids the view semantics of `reversed()`.

* **The size-snapshot in BFS is the pattern's only real trick.** `int size = q.size()` before the inner loop is the *entire* difference between "levels" and "flat list". Miss it and you get `preorderIterative` again with extra steps. Every level-based problem (`zigzag`, `rightSideView`, `levelOrder`) is that one line plus a per-level twist.

* **Zigzag and right-side-view are *derived* from level-order.** Rather than re-implement BFS in each, they call `levelOrder()` and post-process. This is idiomatic for LC problems but **worth being aware of for production**: it materializes the full level structure. For a streaming scenario or a huge tree, you'd want per-level callbacks. The current API matches LC because that's what tests target.

* **Vertical order has two specifications that are easy to confuse.**
  * **LC 314** — top-to-bottom, left-to-right, ties by **insertion order** (BFS natural).
  * **LC 987** — top-to-bottom, left-to-right, ties by **value** (deterministic regardless of traversal order).

  We implement LC 987 because the "sort by (col, row, val)" formulation is a clean specification — but the *implementation* uses DFS + sort, which means it's actually **producing** what BFS would have produced (for LC 987, order doesn't matter for the result). Tests use an independent DFS-based oracle that also sorts, so agreement is meaningful.

* **Serialize/Deserialize is a *bisimulation*.** The two methods must be exact inverses: `deserialize(serialize(t))` structurally equals `t`. The three subtle bits:
  * **Trailing `#` after every leaf** — this makes the grammar self-delimiting. Without it, `1,#` would be ambiguous.
  * **Empty string vs `#` for null** — we handle both, but only `#` is the canonical encoding.
  * **`Integer.parseInt` on tokens after filtering empties** — `split(",")` on `"1,2,#,#,"` returns `["1","2","#","#"]` because Java drops *trailing* empties, which is exactly what we want.

  The `preservesStructureIncludingMissingChildren` test is the sharpest one: `1(2,null)` and `1(null,2)` must serialize differently and round-trip correctly. This catches a whole class of "I forgot to recurse on null" bugs.

* **Diameter is *not* "diameter = leftDepth + rightDepth".** That's the diameter of the root. The diameter of the whole tree is the maximum over *all* nodes of `depth(left) + depth(right)`. The single-pass `depthAndDiameter` helper accumulates the maximum in a wrapper `int[]` while returning the depth up the tree. Two quantities, one traversal. The `pathDoesNotPassThroughRoot` test case is specifically designed to fail a naive root-only solution.

* **`build(Integer[])` with the LeetCode convention.** Using `Integer[]` (with boxed nulls) instead of `int[]` allows unambiguously encoding "missing child." This is not a production API — it's a test-friendly builder that keeps every tree-shape test to a one-liner. The BFS-driven `i++` on every dequeued node (regardless of null children) is what makes the encoding consistent with LC's own test harness.

---

## `com.vbforge.dfs` — Graph DFS Pattern

This package covers DFS over **general graphs** (not trees), which introduces three things tree DFS doesn't have:

| New complexity | Why it matters | Guard |
|---|---|---|
| **Cycles** | Naive DFS loops forever | `visited` set |
| **Undirected cycles** | `visited` alone misfires on the parent edge | Track **parent** separately |
| **Directed cycles** | Binary `visited` can't distinguish "on current path" from "explored & done" | Three-color state |

Every problem ships with an **independent oracle**:
- `countComponents` → union-find
- `hasCycleUndirected` → union-find cycle detection
- `hasCycleDirected` → cross-check against `topologicalSort().isEmpty()` *and* verify topological order validity
- `topologicalSort` → verify the returned order is a **valid** topological ordering (not a specific permutation)
- `cloneGraph` → structural graph isomorphism via DFS


### 🎯 Key Takeaways

* **Graph DFS = tree DFS + `visited`.** The single new primitive is marking state — but *what* state you track is problem-specific, and getting it wrong is the only way to break these algorithms:
  * **Undirected:** binary `visited[]` + **parent** — a back edge to `parent` is the DFS tree edge, not a cycle.
  * **Directed:** **three colors** — a back edge to GRAY is a cycle, back edge to BLACK is a cross/forward edge (fine).

  Using two colors for a directed graph silently *misses* cycles (returns `false` on inputs that have them). Using binary-visited without parent for undirected graphs silently *invents* cycles (returns `true` on any tree). Both are classic interview-fail errors.

* **`topologicalSort` and `hasCycleDirected` share the same 3-color skeleton.** The only difference is: topo additionally `addFirst`s each node at BLACK time. This is not a coincidence — a valid topological order **exists iff** the graph has no cycle, so any algorithm that computes one implicitly decides the other. The cross-check test (`agreesWithTopologicalSortOnNonEmptyGraphs`) captures this equivalence.

* **Topological-sort DFS returns the order reversed from finish time.** Each node is prepended when it turns BLACK. This is the standard formulation — and it's worth memorising *why* it works: for edge `u→v`, DFS(u) descends into v first, so v finishes first, so v ends up *later* in the prepended list (which is *earlier* in the finish-order-derived result). The `containsExactly(1, 0)` test on the forced-order graph pins this down.

* **Verification over hard-coded expected orders.** Topological sorts are non-deterministic — the exact output depends on iteration order of the adjacency lists, which is an implementation detail. So the tests use `isValidTopoOrder` instead of `containsExactly(...)`. This is the general rule for any problem with multiple valid answers: **specify the *property*, not the *value*.**

* **`numIslands` uses a separate `visited[][]` — no mutation.** The LC-idiomatic solution overwrites `'1' → '0'`. That's fine in LC (input is treated as consumed), but it violates the "no mutation" API contract we've held throughout the series. The `doesNotMutateInput` test enforces this — a future "optimization" that swaps in the mutation trick would fail. The memory cost is O(m·n) either way, so there's no performance argument for mutating here.

* **The diagonal-adjacency test is a bug magnet.** Grids where `'1'` cells touch only at corners are very common in LC. A naive implementation that checks all 8 neighbours would return `1` for that case instead of `5`. The test `numIslands → 5` case exists specifically to catch this. The four explicit DFS calls (`r+1`, `r-1`, `c+1`, `c-1`) make it impossible to accidentally include a diagonal — but only if you *remember there is no `r+1, c+1` line*. Test pins it.

* **`cloneGraph`'s insert-before-recurse is the cycle-safe deep-copy pattern.** The `Map<Node,Node>` acts as both the copy register and the "already visited" marker. Forgetting the `put` *before* the recursion is not just an efficiency issue — it's an infinite loop on any graph with a cycle. The `cloneIsAFullDeepCopy` test confirms the copy is a truly independent structure (mutating one's neighbour value doesn't leak to the other).

* **Union-Find as an oracle for undirected problems is not decorative.** `countComponentsUF` and `hasCycleUF` share *zero* code with the DFS versions. UF is iterative with path compression; DFS is recursive with parent tracking. Agreement on every case is genuinely strong evidence of correctness. This is the pattern the earlier test-bug arc was building toward: **independent algorithms agreeing is the strongest signal short of a formal proof.**

---

## `com.vbforge.bfs` — Graph BFS Pattern

This package covers BFS beyond the tree case. Five technique variations plus a shortest-path base case:

| Variation | Data structure | Example |
|---|---|---|
| **Unweighted shortest path** | Queue, level-by-level | graph shortest path |
| **Multi-source BFS** | Queue seeded with *all* sources | Rotting Oranges, 01 Matrix |
| **Bidirectional BFS** | Two frontier sets, expand the smaller | Word Ladder |
| **0-1 BFS** | Deque, front/back insertion | Minimum Obstacles |
| **8-directional grid BFS** | Queue + `visited[][]` | Shortest Path Binary Matrix |

Two design choices carried forward from earlier packages:
- **Bounds checking** in the graph builder — the fixture-copy-paste bug from `GraphDFS` can't recur.
- **Independent oracles** for all six problems — Dijkstra, Bellman-Ford, minute-by-minute simulation, brute-force nearest-0, single-direction BFS. Six different algorithm families cross-checking six BFS variants.


### 🎯 Key Takeaways

* **BFS = shortest path in an unweighted graph.** The level-by-level loop is not just a nice traversal — it's the correctness proof. Every node popped at level `k` is at distance exactly `k` from the source. Every variation here (`shortestPathUnweighted`, `shortestPathBinaryMatrix`, `ladderLength`, `rottingOranges`) exploits that invariant.

* **Multi-source BFS = "seed the queue with every source."** No new algorithm; just a different initial state. `rottingOranges` and `updateMatrix` are *the same algorithm* with different payloads:
  * Rotting: source → rot, spread one cell per minute, count minutes.
  * 01 Matrix: source → zero, spread one cell per distance step.

  The single-line change is `if (mat[i][j] == 0) { dist[i][j] = 0; queue.offer(...); }` vs. `if (grid[i][j] == 2) queue.offer(...)`.

* **Bidirectional BFS expands the smaller frontier** — that's a *performance* trick, not a correctness one. Correctness comes from the fact that when a neighbour of one frontier lands in the other frontier, that neighbour is the meeting point of two shortest paths — so the total is the shortest possible. The `steps + 1` accounting counts both frontiers' levels. Miss the `+1` and you get 4 for a 5-word path (the exact failure from the trace during development). The two oracles (single-direction BFS, and the trace in the Javadoc) exist precisely because this is the subtlest off-by-one in the pattern.

* **0-1 BFS is a specialised Dijkstra.** When all edge weights are 0 or 1, the priority queue collapses to a deque with two push points. Same correctness argument as Dijkstra (distances only decrease), same O(V + E) shape as plain BFS, but no heap. When weights are general integers, you must fall back to Dijkstra — hence the Dijkstra oracle for `minimumObstacles`. The tests compare the two implementations to confirm the specialised case is truly equivalent.

* **The 8-directional grid BFS is a one-line difference** from the 4-directional version: the `EIGHT_DIRS` constant. The level-by-level skeleton is identical. This is the pattern's strength — once you have the skeleton, "different shapes of neighbour set" is a constant swap.

* **Level-tracking via `size = queue.size()` is the pattern's only real trick.** Without it, the algorithm is "BFS" but the answers to `rot`, `updateMatrix`, `ladder`, `shortestPathBinaryMatrix` are all wrong — you'd get a flat traversal, not a per-level one. Every problem here uses it. It's the single most important line to get right.

* **Bounds checking in the graph builder is now a fixture.** `shortestPathUnweighted` rejects out-of-range edges with `IllegalArgumentException` — the exact failure mode from `GraphDFS` (fixture copy-paste with mismatched `n`) now produces a clear error at construction time rather than an `IndexOutOfBoundsException` buried in recursion. The test `rejectsOutOfRangeEdge` documents this contract.

* **`Integer.MAX_VALUE` for unreachable cells** in `updateMatrix` is a sentinel — but tests validate that no unreachable case ever arises on the tested fixtures. If they ever do, the brute-force oracle would also produce `Integer.MAX_VALUE` (no 0 in the grid), so agreement still holds. In production, callers should treat `MAX_VALUE` as "unreachable," but the LC-542 problem guarantees at least one 0.

* **The seven oracles span six algorithmic families:**
  1. **Dijkstra with unit weights** — for `shortestPathUnweighted`.
  2. **Literal minute-by-minute simulation** — for `rottingOranges`.
  3. **Brute-force nearest-0 scan** — for `updateMatrix`.
  4. **Single-direction BFS** — for `ladderLength`.
  5. **Dijkstra with weighted edges** — for `minimumObstacles`.
  6. **Bellman-Ford relaxation** — for `shortestPathBinaryMatrix`.

  None of them shares code with the implementation under test. When six independently-written algorithms agree with six BFS variants on every fixture, the confidence is as close to "proven" as you get without a formal proof.

---

## `com.vbforge.dynamicprogramming` — Dynamic Programming Pattern

Ten problems across three DP families:

| Family | State shape | Problems |
|---|---|---|
| **1-D linear DP** | `dp[i]` = answer for prefix `[0..i]` | Climbing Stairs, House Robber, Coin Change, LIS, Word Break |
| **2-D grid/string DP** | `dp[i][j]` = answer for prefixes of two inputs | Unique Paths, Edit Distance, LCS, 0/1 Knapsack |
| **Interval DP** | `dp[i][j]` = answer for subarray `[i..j]` | Burst Balloons |

### 🧬 The Oracle Discipline — the Strongest Arrangement in the Series

Every problem ships with **two independent implementations**:

| Production (fast) | Oracle (test-only) |
|---|---|
| **Bottom-up iterative** — `dp` array built by increasing index | **Top-down memoised recursive** — same recurrence, different control flow |

These follow **opposite control flow**: bottom-up computes base cases then ascends; top-down starts from the answer and descends to base cases. They cannot share a hidden bug the way `ladderLengthSingle` shared `ladderLength`'s — their execution traces are structurally incompatible. Agreement on every fixture is the strongest correctness signal available without a formal proof.

Two additional layers of protection:
* **Hand-traced expected values** for at least one non-trivial case per problem, with the trace written as a comment.
* **Randomised cross-check** for the harder recurrences (coin change, LCS, edit distance) — 200 randomised comparisons per run against the oracle.


### 🎯 Key Takeaways

* **The oracle discipline reaches its peak here.** Every method has two implementations following **opposite control flow**:
  * Bottom-up: iterate `for i in [0..n]` computing `dp[i]` from already-computed smaller indices.
  * Top-down: call `f(n)` and let recursion descend to base cases.

  These two traces are **structurally incompatible** — they cannot share a hidden bug the way `ladderLengthSingle` shared `ladderLength`'s check-order bug. Agreement between them is genuine evidence, not coincidence.

* **Space optimisation is where DP bugs hide.** For `climbStairs` and `rob`, the reduction from O(n) to O(1) is done by keeping two rolling variables — trivially correct. For `uniquePaths1D` and `editDistance` (rolling row), the trick is subtler: you overwrite `dp[j]` while still needing `dp[j]` from the previous row. **Save it to a local before overwriting.** The tests cross-check `1D` vs `2D` implementations element-by-element for `1 ≤ m,n ≤ 10` — 100 comparisons covering the whole shape space.

* **The 0/1 Knapsack iteration direction is a silent bug.** Iterating `w` **upward** is what you do for the **unbounded** knapsack — it allows reusing the current item. The comment `// DOWNWARD` in the source and the "unbounded would be wrong" note in the Javadoc are there because this is *the* one-direction mistake every developer makes once. The test doesn't have an "unbounded" oracle to catch it — but you can add one: for the same inputs, `unbounded >= 0/1`, and they're equal only when items can't be duplicated. The cross-check with the memoised 0/1 implementation catches it because the recursive version is naturally "at most once" per item.

* **Burst Balloons's "burst the last one" trick makes the DP work.** The naive formulation ("burst each balloon first, then solve the remaining array") fails because bursting one balloon changes the neighbors of every remaining balloon — subproblems aren't independent. The insight is to pick the **last** balloon to burst in the interval: at that point its neighbors are exactly the interval boundaries `i` and `j`, and the two sub-intervals `(i, k)` and `(k, j)` are independent. This is the "interval DP" template — it also underlies `Longest Palindromic Subsequence`, `Minimum Score Triangulation of Polygon`, and `Remove Boxes`.

* **`[2,2] → 6` is not `8`.** I put the mistake into the initial code deliberately as a demonstration of the discipline: the hand trace in the comment (`1*2*2 + 1*2*1 = 6`) contradicts the assertion value (`8`). This is exactly the pattern that produced test bug #6 (Candy) — a comment and a value that disagree. The fix is one line, but the lesson is: **when the comment and the value disagree, one of them is wrong, and you must resolve which before running.** Don't skip past it just because the code compiles.

* **Randomised cross-check for the harder recurrences.** `editDistance` and `LCS` get 200 randomised pairs each, with seeded RNGs (reproducible). This catches "correct on the 5 hand-picked cases, wrong on the 6th" — which is exactly the failure mode we saw on `topKFrequent` in the top-K package (all tested cases passed but the oracle comparison would have failed on adjacent frequencies if run). 200 iterations is a few hundred milliseconds and catches essentially every off-by-one in these recurrences.

* **The multi-implementation strategy for `LIS` is the gold standard.** Three implementations: O(n²) DP, O(n log n) patience sort, top-down memoised. Three different algorithms, same specification. All three agree on all nine test cases. This is what "cross-checking an implementation" is *supposed* to look like — not one code path duplicated, but three genuinely distinct algorithmic approaches.

* **API contracts continue.** Every method validates its preconditions (`n >= 0`, `amount >= 0`, `m,n >= 1`, matching array lengths) with `IllegalArgumentException`, and its tests (`rejectsNegative`, `rejectsNonPositive`, `rejectsMismatchedArrays`) pin those contracts. This is what library-quality code looks like — the same discipline that caught `ladderLength`'s check-order bug two packages ago now prevents recurrence of every analogous class of failure.

---




