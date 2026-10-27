package com.vbforge.visual;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PatternRunner {

    public static List<Frame> run(String pattern) {
        List<Frame> frames = new ArrayList<>();
        Consumer<Frame> emit = frames::add;
        switch (pattern) {
            case "two-pointers"          -> TwoPointersTrace.trace(emit);
            case "sliding-window"        -> SlidingWindowTrace.trace(emit);
            case "binary-search"         -> BinarySearchTrace.trace(emit);
            case "frequency-counting"    -> FrequencyCountingTrace.trace(emit);
            case "prefix-sum"            -> PrefixSumTrace.trace(emit);
            case "matrix-traversal"      -> MatrixTraversalTrace.trace(emit);
            case "dfs"                   -> DFSTrace.trace(emit);
            case "bfs"                   -> BFSTrace.trace(emit);
            case "monotonic-stack"       -> MonotonicStackTrace.trace(emit);
            case "overlapping-intervals" -> OverlappingIntervalsTrace.trace(emit);
            case "top-k-elements"        -> TopKElementsTrace.trace(emit);
            case "binary-tree-traversal" -> BinaryTreeTraversalTrace.trace(emit);
            case "backtracking"          -> BacktrackingTrace.trace(emit);
            case "greedy"                -> GreedyTrace.trace(emit);
            case "dynamic-programming"   -> DynamicProgrammingTrace.trace(emit);
            default -> throw new IllegalArgumentException("unknown pattern: " + pattern);
        }
        return frames;
    }
}