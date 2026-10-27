package com.vbforge.visual;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.Set;

/**
 * Validating accessor over the JSON object the UI sends. Every method throws
 * {@link IllegalArgumentException} with a message that is safe to show to the
 * user, so traces never have to do their own input checking.
 *
 * <p>Wire formats: intlist = number[], int = number, text = string,
 * intervals = number[][], maze = string[], tree = (number|null)[].
 */
public final class Params {

    private final JsonObject json;

    public Params(JsonObject json) {
        this.json = json;
    }

    public static Params parse(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("missing 'params'");
        try {
            JsonElement el = JsonParser.parseString(raw);
            if (!el.isJsonObject()) throw new IllegalArgumentException("'params' must be a JSON object");
            return new Params(el.getAsJsonObject());
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("'params' is not valid JSON");
        }
    }

    // ------------------------------------------------------------------ scalars

    public int integer(String key, int min, int max) {
        int v = toInt(key, need(key));
        if (v < min || v > max) throw bad(key, "must be between " + min + " and " + max);
        return v;
    }

    public String text(String key, int minLen, int maxLen) {
        JsonElement e = need(key);
        if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) throw bad(key, "must be text");
        String s = e.getAsString();
        if (s.length() < minLen || s.length() > maxLen)
            throw bad(key, "needs " + minLen + "–" + maxLen + " characters");
        return s;
    }

    public String choice(String key, Set<String> allowed) {
        String s = text(key, 1, 32);
        if (!allowed.contains(s)) throw bad(key, "must be one of " + allowed);
        return s;
    }

    // -------------------------------------------------------------- collections

    public int[] ints(String key, int minLen, int maxLen, int min, int max) {
        JsonArray a = array(key, minLen, maxLen, "values");
        int[] out = new int[a.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = toInt(key, a.get(i));
            if (out[i] < min || out[i] > max) throw bad(key, "values must be between " + min + " and " + max);
        }
        return out;
    }

    public int[][] intervals(String key, int minCount, int maxCount, int min, int max) {
        JsonArray a = array(key, minCount, maxCount, "intervals");
        int[][] out = new int[a.size()][];
        for (int i = 0; i < out.length; i++) {
            JsonElement el = a.get(i);
            if (!el.isJsonArray() || el.getAsJsonArray().size() != 2) throw bad(key, "must be [start, end] pairs");
            int s = toInt(key, el.getAsJsonArray().get(0));
            int e = toInt(key, el.getAsJsonArray().get(1));
            if (s > e) throw bad(key, "interval [" + s + ", " + e + "] starts after it ends");
            if (s < min || e > max) throw bad(key, "values must be between " + min + " and " + max);
            out[i] = new int[]{s, e};
        }
        return out;
    }

    /** Level-order tree (LeetCode style); {@code null} entries are missing children. */
    public Integer[] tree(String key, int maxNodes) {
        JsonArray a = array(key, 1, maxNodes, "entries");
        Integer[] out = new Integer[a.size()];
        for (int i = 0; i < out.length; i++) {
            JsonElement el = a.get(i);
            out[i] = el.isJsonNull() ? null : toInt(key, el);
        }
        if (out[0] == null) throw bad(key, "needs a root value (the first entry cannot be null)");
        return out;
    }

    /** Rows of '.', '#', 'S', 'E' – exactly one start and one end. */
    public String[] maze(String key, int maxRows, int maxCols) {
        JsonArray a = array(key, 1, maxRows, "rows");
        String[] rows = new String[a.size()];
        int starts = 0, ends = 0;
        for (int r = 0; r < rows.length; r++) {
            JsonElement el = a.get(r);
            if (!el.isJsonPrimitive() || !el.getAsJsonPrimitive().isString()) throw bad(key, "rows must be text");
            rows[r] = el.getAsString();
            if (rows[r].isEmpty() || rows[r].length() > maxCols || rows[r].length() != rows[0].length())
                throw bad(key, "rows must be equally long (1–" + maxCols + " cells)");
            for (char ch : rows[r].toCharArray()) {
                switch (ch) {
                    case 'S' -> starts++;
                    case 'E' -> ends++;
                    case '.', '#' -> { }
                    default -> throw bad(key, "unknown cell '" + ch + "'");
                }
            }
        }
        if (starts != 1 || ends != 1) throw bad(key, "needs exactly one start (S) and one end (E)");
        return rows;
    }

    // ----------------------------------------------------------------- plumbing

    private JsonElement need(String key) {
        JsonElement e = json.get(key);
        if (e == null || e.isJsonNull()) throw bad(key, "is required");
        return e;
    }

    private JsonArray array(String key, int minLen, int maxLen, String what) {
        JsonElement e = need(key);
        if (!e.isJsonArray()) throw bad(key, "must be a list");
        JsonArray a = e.getAsJsonArray();
        if (a.size() < minLen || a.size() > maxLen)
            throw bad(key, "needs between " + minLen + " and " + maxLen + " " + what);
        return a;
    }

    private static int toInt(String key, JsonElement e) {
        if (!e.isJsonPrimitive()) throw bad(key, "must contain whole numbers");
        double d;
        try {
            d = e.getAsJsonPrimitive().getAsDouble();
        } catch (NumberFormatException ex) {
            throw bad(key, "must contain whole numbers");
        }
        if (d != Math.rint(d) || Math.abs(d) > 1_000_000) throw bad(key, "must contain whole numbers (|n| ≤ 1,000,000)");
        return (int) d;
    }

    private static IllegalArgumentException bad(String key, String msg) {
        return new IllegalArgumentException("'" + key + "' " + msg);
    }
}
