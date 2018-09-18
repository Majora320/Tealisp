package org.majora320.tealisp.evaluator;


import java.util.ArrayList;
import java.util.List;

public class Runtime {
    protected StackFrame frame = new StackFrame();

    protected Runtime() { }

    public void addJavaFunction(LispObject.JavaFunction fn) {

    }

    public LispObject callFunction(String name, LispObject... args) throws NoSuchMethodException {
        return new LispObject.Void();
    }
}
