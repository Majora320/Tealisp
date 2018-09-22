package org.majora320.tealisp.evaluator;

import java.util.*;

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
                checkParams("+", params, new Class[]{ LispObject.Integer.class }, true);
                return new LispObject.Integer(
                        Arrays.stream(params)
                                .map(obj -> ((LispObject.Integer)obj).value)
                                .reduce(0, (a, b) -> a + b)
                );
            case "-":
                checkParams("-", params, new Class[]{ LispObject.Integer.class, LispObject.Integer.class }, true);
                return new LispObject.Integer(
                        Arrays.asList(params).subList(1, params.length)
                                .stream()
                                .map(obj -> ((LispObject.Integer)obj).value)
                                .reduce(((LispObject.Integer)params[0]).value, (a, b) -> a - b)
                );
            case "*":
                checkParams("*", params, new Class[]{ LispObject.Integer.class }, true);
                return new LispObject.Integer(
                        Arrays.stream(params)
                                .map(obj -> ((LispObject.Integer)obj).value)
                                .reduce(1, (a, b) -> a * b)
                );
            case "/":
                return new LispObject.Integer(
                        Arrays.asList(params).subList(1, params.length)
                                .stream()
                                .map(obj -> ((LispObject.Integer)obj).value)
                                .reduce(((LispObject.Integer)params[0]).value, (a, b) -> a / b)
                );
            case ">":
                checkParams(">", params, new Class[]{ LispObject.Integer.class, LispObject.Integer.class }, true);
                return new LispObject.Boolean(((LispObject.Integer)params[0]).value > ((LispObject.Integer)params[1]).value);
            case "<":
                checkParams("<", params, new Class[]{ LispObject.Integer.class, LispObject.Integer.class }, true);
                return new LispObject.Boolean(((LispObject.Integer)params[0]).value < ((LispObject.Integer)params[1]).value);
            case "=":
                checkParams("=", params, new Class[]{ LispObject.Integer.class, LispObject.Integer.class }, true);
                return new LispObject.Boolean(((LispObject.Integer)params[0]).value == ((LispObject.Integer)params[1]).value);
            case ">=":
                checkParams(">=", params, new Class[]{ LispObject.Integer.class, LispObject.Integer.class }, true);
                return new LispObject.Boolean(((LispObject.Integer)params[0]).value >= ((LispObject.Integer)params[1]).value);
            case "<=":
                checkParams("<=", params, new Class[]{ LispObject.Integer.class, LispObject.Integer.class }, true);
                return new LispObject.Boolean(((LispObject.Integer)params[0]).value <= ((LispObject.Integer)params[1]).value);
            case "cons":
                checkParams("cons", params, new Class[]{ LispObject.class, LispObject.List.class }, false);
                List<LispObject> res = new ArrayList<>();
                res.add(params[0]);
                res.addAll(((LispObject.List)params[1]).elements);
                return new LispObject.List(res);
            default:
                return null;
        }
    }
}
