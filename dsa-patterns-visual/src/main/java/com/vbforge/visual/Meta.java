package com.vbforge.visual;

import java.util.List;
import java.util.Map;

/**
 * Everything the UI needs to know about an algorithm: how to describe it,
 * which inputs to render, and the pseudo-code that frames point into.
 */
public record Meta(String id,
                   String name,
                   String pattern,
                   String blurb,
                   String time,
                   String space,
                   String insight,
                   List<String> pseudo,
                   List<Field> fields,
                   List<Preset> presets) {

    /**
     * One user-editable input.
     *
     * @param type one of intlist, int, text, intervals, maze, tree, select
     * @param def  default value in wire format (see {@link Params})
     * @param opts type specific options (bounds, alphabet, random generator …)
     */
    public record Field(String key, String label, String type, Object def,
                        String hint, Map<String, Object> opts) {

        public static Field intList(String key, String label, int[] def, String hint,
                                    int min, int max, int minLen, int maxLen) {
            return new Field(key, label, "intlist", def, hint,
                    Viz.kv("min", min, "max", max, "minLen", minLen, "maxLen", maxLen));
        }

        /** @param random optional generator hint: element:&lt;key&gt; | pairsum:&lt;key&gt; | upto:&lt;key&gt; */
        public static Field integer(String key, String label, int def, String hint,
                                    int min, int max, String random) {
            return new Field(key, label, "int", def, hint,
                    Viz.kv("min", min, "max", max, "random", random));
        }

        public static Field text(String key, String label, String def, String hint,
                                 int maxLen, String alphabet) {
            return new Field(key, label, "text", def, hint,
                    Viz.kv("maxLen", maxLen, "alphabet", alphabet));
        }

        public static Field intervals(String key, String label, int[][] def, String hint,
                                      int min, int max, int maxCount) {
            return new Field(key, label, "intervals", def, hint,
                    Viz.kv("min", min, "max", max, "maxCount", maxCount));
        }

        public static Field maze(String key, String label, String[] def, String hint,
                                 int maxRows, int maxCols) {
            return new Field(key, label, "maze", def, hint,
                    Viz.kv("maxRows", maxRows, "maxCols", maxCols));
        }

        public static Field tree(String key, String label, Integer[] def, String hint, int maxNodes) {
            return new Field(key, label, "tree", def, hint, Viz.kv("maxNodes", maxNodes));
        }

        public static Field select(String key, String label, String def, String hint, List<String> options) {
            return new Field(key, label, "select", def, hint, Viz.kv("options", options));
        }
    }

    /** A named set of input values the user can load with one click. */
    public record Preset(String name, Map<String, Object> values) {
        public static Preset of(String name, Object... kv) {
            return new Preset(name, Viz.kv(kv));
        }
    }
}
