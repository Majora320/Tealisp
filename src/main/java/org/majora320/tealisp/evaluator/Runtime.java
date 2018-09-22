package org.majora320.tealisp.evaluator;


import java.util.Arrays;

public class Runtime {
    protected JavaRegistry registry;
    private Interpreter interpreter;

    protected Runtime(Interpreter interpreter, JavaRegistry registry) {
        this.interpreter = interpreter;
        this.registry = registry;
    }

    public LispObject callFunction(String name, LispObject... args) throws LispException {
        return interpreter.applyNoSpecials(name, Arrays.asList(args), interpreter.globalFrame);
    }
}
