package org.majora320.tealisp.evaluator;

import java.util.*;
import java.util.function.BiFunction;

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
                checkParams("+", params, new Class[]{ LispObject.Number.class }, true);

                return mapReduceNumber(params, 0, (a, b) -> a + b, (a, b) -> a + b);
            case "-":
                checkParams("+", params, new Class[]{ LispObject.Number.class }, true);

                return mapReduceNumber(params, 0, (a, b) -> a - b, (a, b) -> a - b);
            case "*":
                checkParams("*", params, new Class[]{ LispObject.Number.class }, true);

                return mapReduceNumber(params, 1, (a, b) -> a * b, (a, b) -> a * b);
            case "/":
                checkParams("/", params, new Class[]{ LispObject.Number.class }, true);

                return mapReduceNumber(params, 1, (a, b) -> a / b, (a, b) -> a / b);
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

    private LispObject.Number mapReduceNumber(
            LispObject[] params,
            int init,
            BiFunction<Integer, Integer, Integer> intFn,
            BiFunction<Double, Double, Double> doubleFn
    ) {
        boolean isDouble = false;
        int intRes = init;
        double doubleRes = init;

        for (LispObject param : params) {
            if (param instanceof LispObject.Integer) {
                if (isDouble)
                    doubleRes = doubleFn.apply(doubleRes, (double)((LispObject.Integer) param).value);
                else
                    intRes = intFn.apply(intRes, ((LispObject.Integer) param).value);
            } else {
                isDouble = true;
                doubleRes = intRes;
                doubleRes = doubleFn.apply(doubleRes, ((LispObject.Double) param).value);
            }
        }

        if (isDouble)
            return new LispObject.Double(doubleRes);
        else
            return new LispObject.Integer(intRes);
    }
}
