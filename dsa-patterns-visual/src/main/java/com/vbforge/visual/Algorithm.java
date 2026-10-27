package com.vbforge.visual;

import java.util.function.Consumer;

/**
 * A visualizable algorithm: describes itself ({@link #meta()}) and, given
 * user input, narrates its execution as a stream of {@link Frame}s.
 *
 * <p>{@code emit.accept(...)} may <em>block</em> – that is how the live SSE
 * session pauses the computation mid-flight – and may throw an unchecked
 * exception to cancel the trace. Implementations must therefore not swallow
 * {@code RuntimeException}s thrown by {@code emit}.
 */
public interface Algorithm {

    Meta meta();

    /**
     * @throws IllegalArgumentException with a user-presentable message when the
     *                                  input is invalid
     */
    void trace(Params params, Consumer<Frame> emit);
}
