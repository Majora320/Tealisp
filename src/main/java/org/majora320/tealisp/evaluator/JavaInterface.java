package org.majora320.tealisp.evaluator;

import java.util.Set;

public abstract class JavaInterface {
    public abstract Set<String> getSupportedFunctions();
    public abstract LispObject runFunction(String name, LispObject[] params, StackFrame frame) throws LispException;

    /**
     * If 'variadic' is true, parameters with the same type as the last member of 'types' can be repeated from zero (!)
     * to infinity times as long as they are at the end of the parameter list.
     */

    public void checkParams(String functionName, LispObject[] params, Class<?>[] types, boolean variadic) throws LispException {
        if (variadic) {
            if (params.length < types.length-1)
                throw new LispException("Arity mismatch: expected " + (types.length - 1) + " or more parameters, got "
                        + params.length + " for function " + functionName);
        } else {
            if (params.length != types.length)
                throw new LispException("Arity mismatch: expected " + types.length + " parameters, got "
                        + params.length + " for function " + functionName);
        }

        for (int i = 0; i < params.length; ++i) {
            if (variadic && (i >= types.length)) {
                if (!types[types.length - 1].isAssignableFrom(params[i].getClass()))
                    throw new LispException("Type mismatch: expected " + types[types.length-1].getSimpleName()
                            + ", got " + params[i].getClass().getSimpleName() + " for function " + functionName);
                continue;
            }


            if (!types[i].isAssignableFrom(params[i].getClass()))
                throw new LispException("Type mismatch: expected " + types[i].getSimpleName()
                        + ", got " + params[i].getClass().getSimpleName() + " for function " + functionName);
        }
    }
}
