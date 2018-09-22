package org.majora320.tealisp.evaluator;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Runtime {
    protected JavaRegistry registry;
    private Evaluator evaluator;

    protected Runtime(Evaluator evaluator, JavaRegistry registry) {
        this.evaluator = evaluator;
        this.registry = registry;
    }

    public LispObject callFunction(String name, LispObject... args) throws NoSuchMethodException, LispException {
        return evaluator.applyNoSpecials(name, Arrays.asList(args), evaluator.globalFrame);
    }
}
