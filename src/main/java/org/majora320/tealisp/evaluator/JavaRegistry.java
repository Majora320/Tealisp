package org.majora320.tealisp.evaluator;

import java.util.HashSet;
import java.util.Set;

/**
 * Register Java functions for use within TeaLisp here.
 */
public class JavaRegistry {
    private static JavaRegistry globalRegistry = new JavaRegistry(true);
    private Set<JavaInterface> interfaces = new HashSet<>();

    public static JavaRegistry getGlobalRegistry() {
        return globalRegistry;
    }

    public JavaRegistry() {
        this(true);
    }

    /**
     * @param includeStdLib Whether to include the set of TeaLisp builtin functions.
     */
    public JavaRegistry(boolean includeStdLib) {
        if (includeStdLib)
            registerInterface(new Builtins());
    }
    public JavaRegistry(JavaRegistry other) {
        interfaces = new HashSet<>(other.interfaces);
    }

    public LispObject.JavaFunction lookupFunction(String name) {
        for (JavaInterface iface : interfaces) {
            if (iface.getSupportedFunctions().contains(name))
                return new LispObject.JavaFunction(name, iface);
        }

        return null;
    }

    public void registerInterface(JavaInterface iface) {
        interfaces.add(iface);
    }

    public void registerInterfaces(Set<JavaInterface> ifaces) {
        interfaces.addAll(ifaces);
    }

    public void deregisterInterface(JavaInterface iface) {
        interfaces.remove(iface);
    }
}
