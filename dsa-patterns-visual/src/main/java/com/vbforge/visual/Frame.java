package com.vbforge.visual;

import java.util.Map;

/**
 * One step of a visualization.
 *
 * @param renderer one of "array", "stack", "intervals", "heap", "grid", "tree"
 * @param data     renderer-specific payload; see web/js/renderers.js for the
 *                 schema each renderer expects
 * @param note     one-line caption shown below the canvas
 * @param line     index of the pseudo-code line this step executes
 *                 (nullable → nothing highlighted)
 */
public record Frame(String renderer, Map<String, Object> data, String note, Integer line) {

    public static Frame of(String renderer, Map<String, Object> data, String note) {
        return new Frame(renderer, data, note, null);
    }

    public static Frame of(String renderer, Map<String, Object> data, String note, int line) {
        return new Frame(renderer, data, note, line);
    }
}
