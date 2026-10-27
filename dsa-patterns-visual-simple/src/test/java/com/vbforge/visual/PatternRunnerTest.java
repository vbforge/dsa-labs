package com.vbforge.visual;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PatternRunnerTest {

    private static final Set<String> VALID_RENDERERS = Set.of(
            "array+pointers", "grid", "stack", "intervals", "heap", "tree", "path"
    );

    @ParameterizedTest(name = "Pattern: {0}")
    @ValueSource(strings = {
            "two-pointers", "sliding-window", "binary-search", "frequency-counting",
            "prefix-sum", "matrix-traversal", "dfs", "bfs", "monotonic-stack",
            "overlapping-intervals", "top-k-elements", "binary-tree-traversal",
            "backtracking", "greedy", "dynamic-programming"
    })
    void testAllPatternsReturnValidFrames(String pattern) {
        List<Frame> frames = PatternRunner.run(pattern);

        assertNotNull(frames, "Frames list should not be null");
        assertFalse(frames.isEmpty(), "Frames list should not be empty");

        // Test the first and last frames for structural integrity
        for (Frame frame : frames) {
            assertNotNull(frame.renderer(), "Frame renderer must not be null");
            assertTrue(VALID_RENDERERS.contains(frame.renderer()),
                    "Unknown renderer: " + frame.renderer());
            
            assertNotNull(frame.data(), "Frame data map must not be null");
            assertNotNull(frame.note(), "Frame note must not be null");
            
            // Ensure data map is not empty (all our renderers require at least 1 key)
            assertFalse(frame.data().isEmpty(), "Frame data should contain keys");
        }
    }

    @Test
    void testUnknownPatternThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            PatternRunner.run("non-existent-pattern");
        });
    }

    @Test
    void testSpecificRendererPayloads() {
        // Verify that the Two Pointers pattern correctly uses the array+pointers renderer
        // and includes the expected keys.
        List<Frame> frames = PatternRunner.run("two-pointers");
        Map<String, Object> data = frames.get(0).data();
        
        assertEquals("array+pointers", frames.get(0).renderer());
        assertTrue(data.containsKey("values"));
        assertTrue(data.containsKey("left"));
        assertTrue(data.containsKey("right"));
        assertTrue(data.containsKey("target"));
    }
}