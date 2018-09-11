package org.majora320.tealisp.evaluator;


import java.util.ArrayList;
import java.util.List;

public class Runtime {
    private List<JavaFunction> javaFunctions = new ArrayList<>();

    protected Runtime() { }

    public void addJavaFunction(JavaFunction fn) {
        javaFunctions.add(fn);
    }

    public LispObject callFunction(String name, LispObject... args) throws NoSuchMethodException {
        for (JavaFunction function : javaFunctions) {
            if (function.name.equals(name) && args.length == function.numParams) {
                function.function.apply(args);
            }
        }

        // TODO: handle actual lisp functions as well

        throw new NoSuchMethodException();
    }
}
