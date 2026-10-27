package com.vbforge.visual;

import java.util.LinkedHashMap;

/**
 * Tiny fluent map used to build a {@link Frame}'s payload:
 * {@code Data.of().with("values", nums).with("cur", i)}.
 *
 * <p>Frames are serialized the moment they are emitted, so a trace may pass
 * live (later mutated) arrays without copying them.
 */
public final class Data extends LinkedHashMap<String, Object> {
    private static final long serialVersionUID = 1L;


    public static Data of() {
        return new Data();
    }

    public Data with(String key, Object value) {
        put(key, value);
        return this;
    }
}
