package org.majora320.tealisp.parser.util;

import org.majora320.tealisp.lexer.LexException;
import org.majora320.tealisp.parser.ParseException;

import java.io.IOException;

@FunctionalInterface
public interface CheckedBiFunction<T, U, R> {
    R apply(final T elem1, final U elem2) throws IOException, LexException, ParseException;
}
