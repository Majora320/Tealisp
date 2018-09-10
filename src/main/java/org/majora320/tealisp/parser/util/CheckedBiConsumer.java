package org.majora320.tealisp.parser.util;

@FunctionalInterface
public interface CheckedBiConsumer<T, U> {
    void accept(final T elem1, final U elem2);
}
