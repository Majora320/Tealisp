package org.majora320.tealisp.evaluator;

import java.util.HashMap;
import java.util.Map;

/**
 * Register Java functions for use within TeaLisp here.
 */
public class JavaRegistry {
    private static JavaRegistry globalRegistry = new JavaRegistry();
    private Map<String, LispObject.JavaFunction> functions = new HashMap<>();

    public static JavaRegistry getGlobalRegistry() {
        return globalRegistry;
    }

    public JavaRegistry() { }
    public JavaRegistry(JavaRegistry other) {
        functions = new HashMap<>(other.functions);
    }

    public LispObject.JavaFunction lookupFunction(String name) {
        return functions.get(name);
    }

    public void registerFunction(String name, Object function) {
        functions.put(name, new LispObject.JavaFunction(name, function));
    }

    /**
     * Returns true if the function was successfully deregistered, false if it did not exist.
     */
    public boolean deregisterFunction(String name) {
        if (!functions.containsKey(name))
            return false;

        functions.remove(name);
        return true;
    }
}
