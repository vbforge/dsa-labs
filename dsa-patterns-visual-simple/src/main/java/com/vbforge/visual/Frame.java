package com.vbforge.visual;

import java.util.Map;

/**
 * One step of a visualization.
 *
 * @param renderer one of "array+pointers", "grid", "tree", "stack",
 *                 "intervals", "heap", "path"
 * @param data     renderer-specific payload; see app.js for the schema
 *                 each renderer expects
 * @param note     one-line caption shown below the canvas
 */
public record Frame(String renderer, Map<String, Object> data, String note) {
    public static Frame of(String renderer, Map<String, Object> data, String note) {
        return new Frame(renderer, data, note);
    }
}