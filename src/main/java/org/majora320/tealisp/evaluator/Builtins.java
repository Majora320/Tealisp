package org.majora320.tealisp.evaluator;

import java.util.*;
import java.util.function.BiFunction;

public class Builtins extends JavaInterface {
    @Override
    public boolean isSupportedFunction(String name) {
        switch (name) {
            case "+": case "-": case "*": case "/":
            case ">": case "<": case "=": case ">=": case "<=":
            case "cons":
                return true;
            default:
                return false;
        }
    }

    @Override
    public LispObject runFunction(String name, LispObject[] params, StackFrame frame) throws LispException {
        switch (name) {
            case "+":
                checkParams("+", params, new Class[]{ LispObject.Number.class }, true);

                return mapReduceNumber(params, new LispObject.Integer(0), (a, b) -> a + b, (a, b) -> a + b);
            case "-":
                checkParams("+", params, new Class[]{ LispObject.Number.class }, true);

                return mapReduceNumber(
                        Arrays.asList(params)
                                .subList(1, params.length)
                                .toArray(new LispObject[] {}),
                        (LispObject.Number) params[0],
                        (a, b) -> a - b,
                        (a, b) -> a - b
                );
            case "*":
                checkParams("*", params, new Class[]{ LispObject.Number.class }, true);

                return mapReduceNumber(params, new LispObject.Integer(1), (a, b) -> a * b, (a, b) -> a * b);
            case "/":
                checkParams("/", params, new Class[]{ LispObject.Number.class }, true);

                return mapReduceNumber(
                        Arrays.asList(params)
                                .subList(1, params.length)
                                .toArray(new LispObject[] { }),
                        (LispObject.Number) params[0],
                        (a, b) -> a / b,
                        (a, b) -> a / b
                );
            case ">":
                checkParams(">", params, new Class[]{ LispObject.Number.class, LispObject.Number.class }, true);
                return mapReduceCompare(params, (a, b) -> a > b, (a, b) -> a > b);
            case "<":
                checkParams("<", params, new Class[]{ LispObject.Number.class, LispObject.Number.class }, true);
                return mapReduceCompare(params, (a, b) -> a < b, (a, b) -> a < b);
            case "=":
                checkParams("=", params, new Class[]{ LispObject.Number.class, LispObject.Number.class }, true);
                return mapReduceCompare(params, Integer::equals, Double::equals);
            case "!=":
                checkParams("!=", params, new Class[]{ LispObject.Number.class, LispObject.Number.class }, true);
                return mapReduceCompare(params, (a, b) -> !a.equals(b), (a, b) -> !a.equals(b));
            case ">=":
                checkParams(">=", params, new Class[]{ LispObject.Number.class, LispObject.Number.class }, true);
                return mapReduceCompare(params, (a, b) -> a >= b, (a, b) -> a >= b);
            case "<=":
                checkParams("<=", params, new Class[]{ LispObject.Number.class, LispObject.Number.class }, true);
                return mapReduceCompare(params, (a, b) -> a <= b, (a, b) -> a <= b);
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
            LispObject.Number init,
            BiFunction<Integer, Integer, Integer> intFn,
            BiFunction<Double, Double, Double> doubleFn
    ) {
        boolean isDouble = false;
        int intRes = 0;
        double doubleRes = 0;

        if (init instanceof LispObject.Integer) {
            intRes = ((LispObject.Integer) init).value;
            isDouble = false;
        } else if (init instanceof LispObject.Double) {
            doubleRes = ((LispObject.Double) init).value;
            isDouble = true;
        }


        for (LispObject param : params) {
            if (param instanceof LispObject.Integer) {
                if (isDouble)
                    doubleRes = doubleFn.apply(doubleRes, (double)((LispObject.Integer) param).value);
                else
                    intRes = intFn.apply(intRes, ((LispObject.Integer) param).value);
            } else {
                if (!isDouble) {
                    isDouble = true;
                    doubleRes = intRes;
                }
                doubleRes = doubleFn.apply(doubleRes, ((LispObject.Double) param).value);
            }
        }

        if (isDouble)
            return new LispObject.Double(doubleRes);
        else
            return new LispObject.Integer(intRes);
    }

    private LispObject.Boolean mapReduceCompare(
            LispObject[] params,
            BiFunction<Integer, Integer, Boolean> intFn,
            BiFunction<Double, Double, Boolean> doubleFn
    ) {
        boolean res = true;

        // Delicious Spaghetti
        for (int i = 0; i < params.length-1; ++i) {
            if (params[i] instanceof LispObject.Integer && params[i+1] instanceof LispObject.Integer) {
                res = intFn.apply(((LispObject.Integer) params[i]).value, ((LispObject.Integer) params[i+1]).value);
            } else if (params[i] instanceof LispObject.Integer && params[i+1] instanceof LispObject.Double) {
                res = doubleFn.apply((double)((LispObject.Integer) params[i]).value, ((LispObject.Double) params[i+1]).value);
            } else if (params[i] instanceof LispObject.Double && params[i+1] instanceof LispObject.Integer) {
                res = doubleFn.apply(((LispObject.Double) params[i]).value, (double)((LispObject.Integer) params[i+1]).value);
            } else if (params[i] instanceof LispObject.Double && params[i+1] instanceof LispObject.Double) {
                res = doubleFn.apply(((LispObject.Double) params[i]).value, ((LispObject.Double) params[i+1]).value);
            }

            if (!res)
                return new LispObject.Boolean(false);
        }

        return new LispObject.Boolean(true);
    }

}
