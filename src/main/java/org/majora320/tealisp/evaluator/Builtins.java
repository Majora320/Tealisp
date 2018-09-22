package org.majora320.tealisp.evaluator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Builtins extends JavaInterface {
    @Override
    public Set<String> getSupportedFunctions() {
        HashSet<String> res = new HashSet<>();
        res.add("+");
        res.add("-");
        res.add("*");
        res.add("/");
        res.add(">");
        res.add("<");
        res.add("=");
        res.add(">=");
        res.add("<=");
        res.add("cons");
        return res;
    }

    @Override
    public LispObject runFunction(String name, LispObject[] params, StackFrame frame) throws LispException {
        switch (name) {
            case "+":
                checkParams("+", params, new Class[]{ LispObject.Integer.class, LispObject.Integer.class });
                return new LispObject.Integer(((LispObject.Integer)params[0]).value + ((LispObject.Integer)params[1]).value);
            case "-":
                checkParams("-", params, new Class[]{ LispObject.Integer.class, LispObject.Integer.class });
                return new LispObject.Integer(((LispObject.Integer)params[0]).value - ((LispObject.Integer)params[1]).value);
            case "*":
                checkParams("*", params, new Class[]{ LispObject.Integer.class, LispObject.Integer.class });
                return new LispObject.Integer(((LispObject.Integer)params[0]).value * ((LispObject.Integer)params[1]).value);
            case "/":
                checkParams("/", params, new Class[]{ LispObject.Integer.class, LispObject.Integer.class });
                return new LispObject.Integer(((LispObject.Integer)params[0]).value / ((LispObject.Integer)params[1]).value);
            case ">":
                checkParams(">", params, new Class[]{ LispObject.Integer.class, LispObject.Integer.class });
                return new LispObject.Boolean(((LispObject.Integer)params[0]).value > ((LispObject.Integer)params[1]).value);
            case "<":
                checkParams("<", params, new Class[]{ LispObject.Integer.class, LispObject.Integer.class });
                return new LispObject.Boolean(((LispObject.Integer)params[0]).value < ((LispObject.Integer)params[1]).value);
            case "=":
                checkParams("=", params, new Class[]{ LispObject.Integer.class, LispObject.Integer.class });
                return new LispObject.Boolean(((LispObject.Integer)params[0]).value == ((LispObject.Integer)params[1]).value);
            case ">=":
                checkParams(">=", params, new Class[]{ LispObject.Integer.class, LispObject.Integer.class });
                return new LispObject.Boolean(((LispObject.Integer)params[0]).value >= ((LispObject.Integer)params[1]).value);
            case "<=":
                checkParams("<=", params, new Class[]{ LispObject.Integer.class, LispObject.Integer.class });
                return new LispObject.Boolean(((LispObject.Integer)params[0]).value <= ((LispObject.Integer)params[1]).value);
            case "cons":
                checkParams("cons", params, new Class[]{ LispObject.class, LispObject.List.class });
                List<LispObject> res = new ArrayList<>();
                res.add(params[0]);
                res.addAll(((LispObject.List)params[1]).elements);
                return new LispObject.List(res);
            default:
                return null;
        }
    }
}
