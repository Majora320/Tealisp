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

    public LispObject callFunction(String name, Object... args) throws NoSuchMethodException, LispException {
        List<LispObject> params = new ArrayList<>(args.length);
        for (Object arg : args)
            params.add(evaluator.javaObjectToLisp(arg));

        return evaluator.applyNoSpecials(name, params, evaluator.globalFrame);
    }
}
