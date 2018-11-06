package org.majora320.tealisp.evaluator;

import org.majora320.tealisp.parser.AstNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class SpecialForms {
    static LispObject handleSpecials(String name, List<AstNode> contents, StackFrame frame, Interpreter interpreter) throws LispException {
        switch (name) {
            case "define":
                return define(contents, frame, interpreter);
            case "lambda":
                return lambda(contents);
            case "let":
            case "let*":
                return let(name, contents, frame, interpreter);
            case "set!":
                return set(contents, frame, interpreter);
            case "if":
                return if_(contents, frame, interpreter);
            case "when":
            case "unless":
                return whenUnless(name, contents, frame, interpreter);
            case "cond":
                return cond(contents, frame, interpreter);
            case "and":
                return and(contents, frame, interpreter);
            case "or":
                return or(contents, frame, interpreter);
            case "quote":
                return quote(contents, frame, interpreter);
        }

        throw new LispException("Something has gone horribly wrong. Please consult a local moose to update this.");
    }

    private static LispObject define(List<AstNode> contents, StackFrame frame, Interpreter interpreter) throws LispException {
        if (contents.size() < 2)
            throw new LispException("Define missing body");

        AstNode defineSpec = contents.get(0);
        if (!(defineSpec instanceof AstNode.Name || defineSpec instanceof AstNode.Sexp))
            throw new LispException("Expected variable or procedure definition, got something else: " + defineSpec);

        if (defineSpec instanceof AstNode.Name) {
            if (contents.size() != 2)
                throw new LispException("Extra terms for variable definition");

            frame.storeBinding(((AstNode.Name) defineSpec).value, interpreter.eval(contents.get(1), new StackFrame(frame)));
        } else if (defineSpec instanceof AstNode.Sexp) {
            List<AstNode> functionSpec = ((AstNode.Sexp) defineSpec).contents;

            if (functionSpec.size() < 1)
                throw new LispException("Expected function name, got nothing");

            for (AstNode node : functionSpec) {
                if (!(node instanceof AstNode.Name))
                    throw new LispException("Expected function specification, got something else: " + new AstNode.Sexp(contents));
            }

            String name = ((AstNode.Name) functionSpec.get(0)).value;
            List<String> paramNames = functionSpec
                    .subList(1, functionSpec.size())
                    .stream()
                    .map(node -> ((AstNode.Name) node).value)
                    .collect(Collectors.toList());

            frame.storeBinding(name, new LispObject.Function(name, paramNames, contents.subList(1, contents.size())));
        }

        return new LispObject.Void();
    }

    private static LispObject lambda(List<AstNode> contents) throws LispException {
        if (contents.size() < 2)
            throw new LispException("Lambda missing body");

        AstNode lambdaSpec = contents.get(0);
        if (!(lambdaSpec instanceof AstNode.Sexp))
            throw new LispException("Expected argument list, got something else: " + lambdaSpec);

        List<AstNode> functionSpec = ((AstNode.Sexp) lambdaSpec).contents;

        for (AstNode node : functionSpec) {
            if (!(node instanceof AstNode.Name))
                throw new LispException("Expected argument list, got something else: " + new AstNode.Sexp(contents));
        }

        List<String> paramNames = functionSpec
                .stream()
                .map(node -> ((AstNode.Name) node).value)
                .collect(Collectors.toList());

        return new LispObject.Function(null, paramNames, contents.subList(1, contents.size()));
    }

    private static LispObject let(String name, List<AstNode> contents, StackFrame frame, Interpreter interpreter) throws LispException {
        if (contents.size() < 2)
            throw new LispException("Let clause missing pairs or body");

        AstNode rawPairs = contents.get(0);

        if (!(rawPairs instanceof AstNode.Sexp))
            throw new LispException("Expected a list of binding pairs in let clause, got something else: " + rawPairs);

        AstNode.Sexp pairs = (AstNode.Sexp) rawPairs;

        StackFrame newFrame = new StackFrame(frame);
        for (AstNode rawPair : pairs.contents) {
            if (!(rawPair instanceof AstNode.Sexp))
                throw new LispException("Expected a binding pair, got something else: " + rawPair);

            AstNode.Sexp pair = (AstNode.Sexp) rawPair;

            if (pair.contents.size() != 2)
                throw new LispException("Expected identifier-value pair, got some other number of values");

            AstNode rawName = pair.contents.get(0);
            if (!(rawName instanceof AstNode.Name))
                throw new LispException("Expected name, got something else: " + rawName);

            AstNode.Name varName = (AstNode.Name) rawName;

            if (name.equals("let"))
                newFrame.storeBinding(varName.value, interpreter.eval(pair.contents.get(1), new StackFrame(frame)));
            else // let*
                newFrame.storeBinding(varName.value, interpreter.eval(pair.contents.get(1), new StackFrame(newFrame)));
        }

        LispObject res = null; // must always be one statement, so this can never return null
        for (int i = 1; i < contents.size(); ++i)
            res = interpreter.eval(contents.get(i), newFrame);

        return res;
    }

    private static LispObject set(List<AstNode> contents, StackFrame frame, Interpreter interpreter) throws LispException {
        if (contents.size() != 2)
            throw new LispException("set! has the wrong number of arguments");

        AstNode rawName = contents.get(0);

        if (!(rawName instanceof AstNode.Name))
            throw new LispException("Expected identifier passed to set!");

        LispObject value = interpreter.eval(contents.get(1), new StackFrame(frame));
        frame.modifyBinding(((AstNode.Name) rawName).value, value);

        return new LispObject.Void();
    }

    private static LispObject if_(List<AstNode> contents, StackFrame frame, Interpreter interpreter) throws LispException {
        if (contents.size() != 3)
            throw new LispException("If expression must have exactly 3 arguments");

        LispObject ifCondition = interpreter.eval(contents.get(0), new StackFrame(frame));

        if ((ifCondition instanceof LispObject.Boolean) && !((LispObject.Boolean) ifCondition).getValue())
            return interpreter.eval(contents.get(2), new StackFrame(frame));
        else
            return interpreter.eval(contents.get(1), new StackFrame(frame));
    }

    private static LispObject whenUnless(String name, List<AstNode> contents, StackFrame frame, Interpreter interpreter) throws LispException {
        if (contents.size() < 2)
            throw new LispException((name.equals("when") ? "When" : "Unless") + " expression must have two or more arguments");

        // must always be one statement or we throw an exception, so this can never return null
        LispObject res = null;
        LispObject condition = interpreter.eval(contents.get(0), new StackFrame(frame));

        if (condition instanceof LispObject.Boolean && !((LispObject.Boolean) condition).getValue()) {
            if (name.equals("when")) {
                return new LispObject.Void();
            } else {
                for (int i = 1; i < contents.size(); ++i)
                    res = interpreter.eval(contents.get(i), new StackFrame(frame));
                return res;
            }
        } else {
            if (name.equals("when")) {
                for (int i = 1; i < contents.size(); ++i)
                    res = interpreter.eval(contents.get(i), new StackFrame(frame));
                return res;
            } else {
                return new LispObject.Void();
            }
        }
    }

    private static LispObject cond(List<AstNode> contents, StackFrame frame, Interpreter interpreter) throws LispException {
        LispObject res = new LispObject.Void();
        if (contents.size() == 0)
            throw new LispException("Encountered a disappointing lack of arguments in cond");

        for (int i = 0; i < contents.size(); ++i) {
            AstNode node = contents.get(i);

            if (!(node instanceof AstNode.Sexp))
                throw new LispException("Cond expects Sexps");

            List<AstNode> clause = ((AstNode.Sexp) node).contents;

            if (clause.size() == 0)
                throw new LispException("Encountered blank thing in cond list thing");

            if (clause.get(0) instanceof AstNode.Name
                    && ((AstNode.Name) clause.get(0)).value.equals("else")) {
                for (int j = 1; j < clause.size(); ++j) {
                    res = interpreter.eval(clause.get(j), new StackFrame(frame));
                }

                if (i != contents.size() - 1)
                    throw new LispException("Else must be the last clause in a cond block");

                return res;
            }


            LispObject condCondition = interpreter.eval(clause.get(0), new StackFrame(frame));
            if (!(condCondition instanceof LispObject.Boolean
                    && !((LispObject.Boolean) condCondition).getValue())) {
                res = condCondition;

                for (int j = 1; j < clause.size(); j++) {
                    res = interpreter.eval(clause.get(j), new StackFrame(frame));
                }

                return res;
            }
        }

        return res;
    }

    private static LispObject and(List<AstNode> contents, StackFrame frame, Interpreter interpreter) throws LispException {
        LispObject andRes = new LispObject.Boolean(true);
        for (AstNode node : contents) {
            andRes = interpreter.eval(node, new StackFrame(frame));
            if (andRes instanceof LispObject.Boolean && !((LispObject.Boolean) andRes).getValue())
                return andRes;
        }

        return andRes;
    }

    private static LispObject or(List<AstNode> contents, StackFrame frame, Interpreter interpreter) throws LispException {
        for (AstNode node : contents) {
            LispObject orRes = interpreter.eval(node, new StackFrame(frame));
            if (orRes instanceof LispObject.Boolean && !((LispObject.Boolean) orRes).getValue())
                continue;

            return orRes;
        }

        return new LispObject.Boolean(false);
    }

    private static LispObject quote(List<AstNode> contents, StackFrame frame, Interpreter interpreter) throws LispException {
        if (contents.size() != 1) {
            throw new LispException("Arity mismatch: expected 1 argument, got " + contents.size() + " for function quote");
        }

        AstNode content = contents.get(0);

        if (content instanceof AstNode.Name) {
            return new LispObject.Symbol(((AstNode.Name) content).value);
        } else if (content instanceof AstNode.Sexp) {
            List<LispObject> processedContents = new ArrayList<>();

            for (AstNode node : ((AstNode.Sexp) content).contents)
                processedContents.add(interpreter.processQuotedObj(node));

            return new LispObject.List(processedContents);
        } else {
            return interpreter.eval(content, new StackFrame(frame));
        }
    }
}
