package org.majora320.tealisp.evaluator;

import java.util.HashMap;
import java.util.Map;

public class StackFrame {
    private StackFrame parent;
    private Map<String, LispObject> bindings = new HashMap<>();

    public StackFrame() {
        parent = null;
    }

    public StackFrame(StackFrame parent) {
        this.parent = parent;
    }

    public LispObject lookupBinding(String name) {
        LispObject value = bindings.get(name);

        if (value == null) {
            if (parent == null)
                return null;
            else
                return parent.lookupBinding(name);
        }

        return value;
    }

    public void storeBinding(String name, LispObject value) {
        bindings.put(name, value);
    }

    public void modifyBinding(String name, LispObject value) throws LispException {
        if (bindings.containsKey(name)) {
            bindings.put(name, value);
            return;
        }

        if (parent == null)
            throw new LispException("Tried to call set! on an nonexistant identifier");
        else
            parent.modifyBinding(name, value);
    }

    @Override
    public String toString() {
        return bindings.toString() + "\nParent: " + parent;
    }
}
