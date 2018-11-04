package org.majora320.tealisp.evaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class Builtins extends JavaInterface {
    @Override
    public boolean isSupportedFunction(String name) {
        switch (name) {
            case "+":
            case "-":
            case "*":
            case "/":
            case ">":
            case "<":
            case "=":
            case ">=":
            case "<=":
            case "symbol=?":
            case "string=?":
            case "empty?":
            case "cons":
            case "car":
            case "cdr":
            case "list":
            case "not":
            case "void":
                return true;
            default:
                return false;
        }
    }

    @Override
    public LispObject runFunction(String name, LispObject[] params, StackFrame frame) throws LispException {
        switch (name) {
            case "+":
                checkParams("+", params, new Class[]{LispObject.Number.class}, true);

                return mapReduceNumber(params, new LispObject.Integer(0), (a, b) -> a + b, (a, b) -> a + b);
            case "-":
                checkParams("+", params, new Class[]{LispObject.Number.class}, true);

                return mapReduceNumber(
                        Arrays.asList(params)
                                .subList(1, params.length)
                                .toArray(new LispObject[]{}),
                        (LispObject.Number) params[0],
                        (a, b) -> a - b,
                        (a, b) -> a - b
                );
            case "*":
                checkParams("*", params, new Class[]{LispObject.Number.class}, true);

                return mapReduceNumber(params, new LispObject.Integer(1), (a, b) -> a * b, (a, b) -> a * b);
            case "/":
                checkParams("/", params, new Class[]{LispObject.Number.class}, true);

                return mapReduceNumber(
                        Arrays.asList(params)
                                .subList(1, params.length)
                                .toArray(new LispObject[]{}),
                        (LispObject.Number) params[0],
                        (a, b) -> a / b,
                        (a, b) -> a / b
                );
            case ">":
                checkParams(">", params, new Class[]{LispObject.Number.class, LispObject.Number.class}, true);
                return mapReduceCompare(params, (a, b) -> a > b, (a, b) -> a > b);
            case "<":
                checkParams("<", params, new Class[]{LispObject.Number.class, LispObject.Number.class}, true);
                return mapReduceCompare(params, (a, b) -> a < b, (a, b) -> a < b);
            case "=":
                checkParams("=", params, new Class[]{LispObject.Number.class, LispObject.Number.class}, true);
                return mapReduceCompare(params, Integer::equals, Double::equals);
            case "!=":
                checkParams("!=", params, new Class[]{LispObject.Number.class, LispObject.Number.class}, true);
                return mapReduceCompare(params, (a, b) -> !a.equals(b), (a, b) -> !a.equals(b));
            case ">=":
                checkParams(">=", params, new Class[]{LispObject.Number.class, LispObject.Number.class}, true);
                return mapReduceCompare(params, (a, b) -> a >= b, (a, b) -> a >= b);
            case "<=":
                checkParams("<=", params, new Class[]{LispObject.Number.class, LispObject.Number.class}, true);
                return mapReduceCompare(params, (a, b) -> a <= b, (a, b) -> a <= b);
            case "symbol=?":
                checkParams("symbol=?", params, new Class[]{LispObject.Symbol.class, LispObject.Symbol.class}, false);
                return new LispObject.Boolean(((LispObject.Symbol)params[0]).getValue().equals(((LispObject.Symbol)params[1]).getValue()));
            case "string=?":
                checkParams("string=?", params, new Class[]{LispObject.String.class, LispObject.String.class}, true);
                boolean allEqual = true;
                String base = ((LispObject.String)params[0]).getValue();

                for (int i = 1; i < params.length; ++i) {
                    if (!((LispObject.String) params[i]).getValue().equals(base))
                        allEqual = false;
                }

                return new LispObject.Boolean(allEqual);
            case "cons":
                checkParams("cons", params, new Class[]{LispObject.class, LispObject.List.class}, false);
                List<LispObject> res = new ArrayList<>();
                res.add(params[0]);
                res.addAll(((LispObject.List) params[1]).getValue());
                return new LispObject.List(res);
            case "car":
                checkParams("car", params, new Class[]{LispObject.List.class}, false);
                return ((LispObject.List)params[0]).getValue().get(0);
            case "cdr":
                checkParams("car", params, new Class[]{LispObject.List.class}, false);
                List<LispObject> list = ((LispObject.List)params[0]).getValue();
                return new LispObject.List(list.subList(1, list.size()));
            case "list":
                checkParams("list", params, new Class[]{LispObject.class}, true);
                return new LispObject.List(Arrays.asList(params));
            case "empty?":
                checkParams("empty?", params, new Class[]{LispObject.List.class}, false);
                return new LispObject.Boolean(((LispObject.List)params[0]).getValue().isEmpty());
            case "not":
                checkParams("not", params, new Class[]{LispObject.Boolean.class}, false);
                return new LispObject.Boolean(!((LispObject.Boolean) params[0]).getValue());
            case "void":
                checkParams("void", params, new Class[]{}, false);
                return new LispObject.Void();
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
            intRes = ((LispObject.Integer) init).getValue();
            isDouble = false;
        } else if (init instanceof LispObject.Double) {
            doubleRes = ((LispObject.Double) init).getValue();
            isDouble = true;
        }


        for (LispObject param : params) {
            if (param instanceof LispObject.Integer) {
                if (isDouble)
                    doubleRes = doubleFn.apply(doubleRes, (double) ((LispObject.Integer) param).getValue());
                else
                    intRes = intFn.apply(intRes, ((LispObject.Integer) param).getValue());
            } else {
                if (!isDouble) {
                    isDouble = true;
                    doubleRes = intRes;
                }
                doubleRes = doubleFn.apply(doubleRes, ((LispObject.Double) param).getValue());
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
        for (int i = 0; i < params.length - 1; ++i) {
            if (params[i] instanceof LispObject.Integer && params[i + 1] instanceof LispObject.Integer) {
                res = intFn.apply(((LispObject.Integer) params[i]).getValue(), ((LispObject.Integer) params[i + 1]).getValue());
            } else if (params[i] instanceof LispObject.Integer && params[i + 1] instanceof LispObject.Double) {
                res = doubleFn.apply((double) ((LispObject.Integer) params[i]).getValue(), ((LispObject.Double) params[i + 1]).getValue());
            } else if (params[i] instanceof LispObject.Double && params[i + 1] instanceof LispObject.Integer) {
                res = doubleFn.apply(((LispObject.Double) params[i]).getValue(), (double) ((LispObject.Integer) params[i + 1]).getValue());
            } else if (params[i] instanceof LispObject.Double && params[i + 1] instanceof LispObject.Double) {
                res = doubleFn.apply(((LispObject.Double) params[i]).getValue(), ((LispObject.Double) params[i + 1]).getValue());
            }

            if (!res)
                return new LispObject.Boolean(false);
        }

        return new LispObject.Boolean(true);
    }

}
