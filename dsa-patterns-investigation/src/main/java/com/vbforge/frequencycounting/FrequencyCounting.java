package com.vbforge.frequencycounting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <h2>Frequency Counting Pattern</h2>
 *
 * <p>Almost every problem here follows three steps:</p>
 * <ol>
 *   <li><b>Count</b> — build a frequency map / array in one pass.</li>
 *   <li><b>Query</b> — answer a question over the counts (top-K, first unique,
 *       whether one multiset is contained in another, …).</li>
 *   <li><b>Output</b> — reconstruct, sort by count, or return an index.</li>
 * </ol>
 *
 * <p>When the alphabet is bounded, prefer an {@code int[26]} (or {@code int[128]})
 * over a {@code HashMap}: it's cache-friendly, allocation-free, and lets you use
 * {@link Arrays#equals(int[], int[])} to compare whole multisets in O(1).</p>
 */
public final class FrequencyCounting {

    private FrequencyCounting() { }

    // =====================================================================
    //  1. Alphabet-array counting
    // =====================================================================

    /**
     * <b>Valid Anagram</b> — assumes lowercase a–z. Builds the count of
     * {@code s}, subtracts the count of {@code t}, and requires all zeros.
     *
     * <p>Time: O(|s| + |t|) — Space: O(1)</p>
     */
    public static boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) return false;

        int[] count = new int[26];
        for (int i = 0; i < s.length(); i++) {
            count[s.charAt(i) - 'a']++;
            count[t.charAt(i) - 'a']--;
        }
        for (int c : count) {
            if (c != 0) return false;
        }
        return true;
    }

    /**
     * <b>First Unique Character in a String</b> — returns its index, or
     * {@code -1} if none. Two passes: count, then find the first index whose
     * count is 1. Assumes lowercase a–z.
     *
     * <p>Time: O(n) — Space: O(1)</p>
     */
    public static int firstUniqChar(String s) {
        int[] count = new int[26];
        for (int i = 0; i < s.length(); i++) count[s.charAt(i) - 'a']++;

        for (int i = 0; i < s.length(); i++) {
            if (count[s.charAt(i) - 'a'] == 1) return i;
        }
        return -1;
    }

    /**
     * <b>Ransom Note</b> — {@code true} if {@code ransomNote} can be built
     * from letters of {@code magazine}, each used at most as often as it
     * appears. Assumes lowercase a–z.
     *
     * <p>Time: O(|ransomNote| + |magazine|) — Space: O(1)</p>
     */
    public static boolean canConstruct(String ransomNote, String magazine) {
        int[] count = new int[26];
        for (char c : magazine.toCharArray()) count[c - 'a']++;
        for (char c : ransomNote.toCharArray()) {
            if (--count[c - 'a'] < 0) return false;
        }
        return true;
    }

    // =====================================================================
    //  2. Hash-map counting
    // =====================================================================

    /**
     * <b>Group Anagrams</b> — clusters strings that are anagrams of each
     * other. Uses the <em>sorted characters</em> as the hash key.
     *
     * <p>Time: O(n · k log k) where {@code k} = max string length —
     * Space: O(n · k)</p>
     */
    public static List<List<String>> groupAnagrams(String[] strs) {
        Map<String, List<String>> byKey = new HashMap<>();
        for (String s : strs) {
            char[] chars = s.toCharArray();
            Arrays.sort(chars);
            String key = new String(chars);
            byKey.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        }
        return new ArrayList<>(byKey.values());
    }

    /**
     * <b>Sort Characters by Frequency</b> — descending by count; ties are
     * broken by ascending {@code char} value so the output is deterministic.
     *
     * <p>Time: O(n + k log k) where {@code k} = distinct chars —
     * Space: O(k)</p>
     */
    public static String frequencySort(String s) {
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : s.toCharArray()) freq.merge(c, 1, Integer::sum);

        return freq.entrySet().stream()
                .sorted((a, b) -> {
                    int byCountDesc = b.getValue() - a.getValue();
                    if (byCountDesc != 0) return byCountDesc;
                    return Character.compare(a.getKey(), b.getKey());   // tie-break
                })
                .map(e -> String.valueOf(e.getKey()).repeat(e.getValue()))
                .collect(Collectors.joining());
    }

    /**
     * <b>Contains Duplicate</b> — {@code true} iff any value repeats.
     *
     * <p>Time: O(n) average — Space: O(n)</p>
     */
    public static boolean containsDuplicate(int[] nums) {
        Set<Integer> seen = new HashSet<>();
        for (int n : nums) {
            if (!seen.add(n)) return true;
        }
        return false;
    }

    // =====================================================================
    //  3. Counting the counts — Top-K in O(n)
    // =====================================================================

    /**
     * <b>Top K Frequent Elements</b> — returns the {@code k} most frequent
     * values in any order.
     *
     * <p>Standard heap approach is O(n log k). The <b>bucket sort</b>
     * approach used here is <b>O(n)</b>: since a value can appear at most
     * {@code n} times, create {@code n + 1} buckets indexed by frequency,
     * then read from the highest bucket down.</p>
     *
     * <p>Time: O(n) — Space: O(n)</p>
     */
    @SuppressWarnings("unchecked")
    public static int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums) freq.merge(n, 1, Integer::sum);

        // bucket[f] = list of values with frequency exactly f
        List<Integer>[] bucket = new List[nums.length + 1];
        for (Map.Entry<Integer, Integer> e : freq.entrySet()) {
            int f = e.getValue();
            if (bucket[f] == null) bucket[f] = new ArrayList<>();
            bucket[f].add(e.getKey());
        }

        int[] result = new int[k];
        int idx = 0;
        for (int f = bucket.length - 1; f >= 0 && idx < k; f--) {
            if (bucket[f] == null) continue;
            for (int value : bucket[f]) {
                result[idx++] = value;
                if (idx == k) break;
            }
        }
        return result;
    }

    /**
     * <b>Top K Frequent — Heap variant</b> — provided for comparison.
     * Keeps a min-heap of size {@code k} keyed by frequency.
     *
     * <p>Time: O(n log k) — Space: O(n)</p>
     */
    public static int[] topKFrequentHeap(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums) freq.merge(n, 1, Integer::sum);

        PriorityQueue<Map.Entry<Integer, Integer>> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<Integer, Integer> e : freq.entrySet()) {
            minHeap.offer(e);
            if (minHeap.size() > k) minHeap.poll();
        }

        int[] result = new int[k];
        for (int i = k - 1; i >= 0; i--) result[i] = minHeap.poll().getKey();
        return result;
    }

    // =====================================================================
    //  4. O(1)-space frequency tricks
    // =====================================================================

    /**
     * <b>Majority Element</b> — the element appearing strictly more than
     * {@code n/2} times. Uses the <b>Boyer–Moore Voting</b> algorithm:
     * a majority survives any pairwise cancellation.
     *
     * <p>Time: O(n) — Space: O(1)</p>
     *
     * @throws IllegalArgumentException if {@code nums} is empty (assumed
     *         problem precondition is that a majority element exists)
     */
    public static int majorityElement(int[] nums) {
        if (nums.length == 0) {
            throw new IllegalArgumentException("nums must not be empty");
        }
        int candidate = nums[0];
        int count = 0;
        for (int n : nums) {
            if (count == 0) candidate = n;
            count += (n == candidate) ? 1 : -1;
        }
        return candidate;
    }

    /**
     * <b>Longest Consecutive Sequence</b> — length of the longest run of
     * consecutive integers (order in input irrelevant).
     *
     * <p>Key trick: iterate each value only if {@code n - 1} is <em>not</em>
     * present. That single check identifies sequence starts, so each element
     * is visited at most twice overall — total work is O(n) even though the
     * inner loop exists.</p>
     *
     * <p>Time: O(n) — Space: O(n)</p>
     */
    public static int longestConsecutive(int[] nums) {
        Set<Integer> set = new HashSet<>();
        for (int n : nums) set.add(n);

        int best = 0;
        for (int n : set) {
            if (set.contains(n - 1)) continue;          // not a sequence start
            int len = 1;
            while (set.contains(n + len)) len++;
            best = Math.max(best, len);
        }
        return best;
    }
}