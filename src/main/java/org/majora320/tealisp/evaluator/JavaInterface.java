package org.majora320.tealisp.evaluator;

import java.util.Arrays;
import java.util.Set;

public abstract class JavaInterface {
    public abstract Set<String> getSupportedFunctions();
    public abstract LispObject runFunction(String name, LispObject[] params, StackFrame frame) throws LispException;

    public void checkParams(String functionName, LispObject[] params, Class<?>[] types) throws LispException {
        if (params.length != types.length)
            throw new LispException("Arity mismatch: expected " + types.length + " parameters, got "
                    + params.length + " for function " + functionName);

        for (int i = 0; i < params.length; ++i) {
            if (!types[i].isAssignableFrom(params[i].getClass()))
                throw new LispException("Type mismatch: expected " + types[i].getSimpleName()
                        + ", got " + params[i].getClass().getSimpleName() + " for function " + functionName);
        }
    }
}
