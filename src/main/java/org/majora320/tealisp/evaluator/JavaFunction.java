package org.majora320.tealisp.evaluator;

import java.util.function.Function;

/**
 * A java function as accessed from inside TeaLisp.
 */
public class JavaFunction {
    public Function<LispObject[], LispObject> function;
    public String name;
    public int numParams;

    /**
     * All parameters of `function` must be `LispObject`s. It should return a `LispObject`.
     * If the function does not have a value to return, it should return `LispVoid`, not `null`.
     */
    public JavaFunction(Function<LispObject[], LispObject> function, String name, int numParams) {
        this.function = function;
        this.name = name;
        this.numParams = numParams;
    }
}
