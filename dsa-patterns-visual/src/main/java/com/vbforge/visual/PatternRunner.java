package com.vbforge.visual;

import com.vbforge.visual.patterns.BfsGridTrace;
import com.vbforge.visual.patterns.BinarySearchTrace;
import com.vbforge.visual.patterns.LcsTrace;
import com.vbforge.visual.patterns.MergeIntervalsTrace;
import com.vbforge.visual.patterns.MonotonicStackTrace;
import com.vbforge.visual.patterns.NQueensTrace;
import com.vbforge.visual.patterns.SlidingWindowTrace;
import com.vbforge.visual.patterns.TopKTrace;
import com.vbforge.visual.patterns.TreeTraversalTrace;
import com.vbforge.visual.patterns.TreeTraversalTrace.Order;
import com.vbforge.visual.patterns.TwoPointersTrace;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Registry of every visualizable algorithm. Registration order is the order
 * of the sidebar; algorithms sharing a {@code pattern} name are grouped.
 */
public final class PatternRunner {

    private static final Map<String, Algorithm> REGISTRY = new LinkedHashMap<>();

    static {
        register(new TwoPointersTrace());
        register(new SlidingWindowTrace());
        register(new BinarySearchTrace());
        register(new MonotonicStackTrace());
        register(new MergeIntervalsTrace());
        register(new TopKTrace());
        register(new BfsGridTrace());
        register(new TreeTraversalTrace(Order.PRE));
        register(new TreeTraversalTrace(Order.IN));
        register(new TreeTraversalTrace(Order.POST));
        register(new TreeTraversalTrace(Order.LEVEL));
        register(new LcsTrace());
        register(new NQueensTrace());
    }

    private PatternRunner() {}

    private static void register(Algorithm a) {
        REGISTRY.put(a.meta().id(), a);
    }

    public static List<Meta> catalog() {
        List<Meta> out = new ArrayList<>();
        REGISTRY.values().forEach(a -> out.add(a.meta()));
        return out;
    }

    public static Algorithm find(String id) {
        Algorithm a = id == null ? null : REGISTRY.get(id);
        if (a == null) throw new IllegalArgumentException("unknown algorithm: " + id);
        return a;
    }

    /** Runs a trace to completion and collects its frames (handy for tests). */
    public static List<Frame> run(String id, Params params) {
        List<Frame> frames = new ArrayList<>();
        Consumer<Frame> sink = frames::add;
        find(id).trace(params, sink);
        return frames;
    }
}
