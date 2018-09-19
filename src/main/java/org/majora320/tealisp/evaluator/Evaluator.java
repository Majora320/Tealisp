package org.majora320.tealisp.evaluator;

import org.majora320.tealisp.parser.AstNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Evaluator {
    // Weird syntax to add stuff to a container inline
    // The first { creates an anonymous class subclassing from HashSet
    // And the second { creates a static initialization block within that class
    private Set<String> reservedKeywords = new HashSet<String>() {{
        add("define");
        add("lambda");
        add("let");
        add("let*");
        add("if");
        add("cond");
        add("and");
        add("or");
    }};

    private Runtime runtime = new Runtime();

    public Runtime getRuntime() {
        return runtime;
    }

    private LispObject globalResult;

    public Evaluator(AstNode.RootNode program) throws LispException {
        for (AstNode child : program.children)
            globalResult = eval(child, runtime.frame);
    }

    public LispObject getGlobalResult() {
        return globalResult;
    }

    public LispObject eval(AstNode node, StackFrame parentFrame) throws LispException {
        StackFrame frame = new StackFrame(parentFrame);

        // great big loop thru all the node types
        if (node instanceof AstNode.RootNode) {
            throw new LispException("Only one root node is allowed, and it must be the actual root node.");
        } else if (node instanceof AstNode.Boolean) {
            return new LispObject.Boolean(((AstNode.Boolean) node).value);
        } else if (node instanceof AstNode.String) {
            return new LispObject.String(((AstNode.String) node).value);
        } else if (node instanceof AstNode.Integer) {
            return new LispObject.Integer(((AstNode.Integer) node).value);
        } else if (node instanceof AstNode.Name) {
            String name = ((AstNode.Name) node).value;

            if (reservedKeywords.contains(name))
                throw new LispException("Cannot use keyword as variable name: " + name);

            LispObject value = frame.lookupBinding(name);

            if (value == null)
                throw new LispException("Undefined variable: " + name);
            return value;
        } else if (node instanceof AstNode.Sexp) {
            return evalSexp((AstNode.Sexp) node, frame);
        }

        throw new LispException("This should never happen. If it does, contact Majora320 immediately with error code 452");
    }

    private LispObject evalSexp(AstNode.Sexp node, StackFrame frame) throws LispException {
        List<AstNode> contents = node.contents;

        if (contents.size() == 0)
            throw new LispException("Empty parenthesis.");

        AstNode first = contents.get(0);

        if (!(first instanceof AstNode.Name || first instanceof AstNode.Sexp))
            throw new LispException("Not a function: " + first);

        if (first instanceof AstNode.Name) {
            String name = ((AstNode.Name) first).value;

            if (reservedKeywords.contains(name))
                return handleSpecials(name, contents.subList(1, contents.size()), frame);

            LispObject value = frame.lookupBinding(name);

            if (value == null)
                throw new LispException("Undefined variable: " + name);

            return apply(value, contents.subList(1, contents.size()), frame);
        } else if (first instanceof AstNode.Sexp) {
            LispObject result = eval(first, new StackFrame(frame));

            return apply(result, contents.subList(1, contents.size()), frame);
        }

        throw new LispException("This should never happen. If it does, contact Majora320 immediately with error code 453");
    }

    private LispObject apply(LispObject value, List<AstNode> arguments, StackFrame frame) throws LispException {
        if (!(value instanceof LispObject.Function))
            throw new LispException("Not a function: " + value);

        LispObject.Function function = (LispObject.Function) value;

        if (!(arguments.size() == function.paramNames.size()))
            throw new LispException("Expected " + function.paramNames.size() + " arguments, got " + arguments.size());


        StackFrame newFrame = new StackFrame(frame);

        for (int i = 0; i < arguments.size(); ++i) {
            newFrame.storeBinding(function.paramNames.get(i), eval(arguments.get(i), frame));
        }

        LispObject res = null;
        for (AstNode node : function.body) {
            res = eval(node, newFrame);
        }

        return res;
    }

    private LispObject handleSpecials(String special, List<AstNode> contents, StackFrame frame) throws LispException {
        switch (special) {
            case "define":
                if (contents.size() < 2)
                    throw new LispException("Define missing body");

                AstNode spec = contents.get(0);
                if (!(spec instanceof AstNode.Name || spec instanceof AstNode.Sexp))
                    throw new LispException("Expected variable or procedure definition, got something else: " + spec);

                if (spec instanceof AstNode.Name) {
                    if (contents.size() != 2)
                        throw new LispException("Extra terms for variable definition");

                    frame.storeBinding(((AstNode.Name) spec).value, eval(contents.get(1), new StackFrame(frame)));
                    return new LispObject.Void();
                } else if (spec instanceof AstNode.Sexp) {
                    List<AstNode> functionSpec = ((AstNode.Sexp) spec).contents;

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
                    return new LispObject.Void();
                }
            case "lambda":
                break;
            case "let":
            case "let*":
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

                    AstNode.Name name = (AstNode.Name) rawName;

                    if (special.equals("let"))
                        newFrame.storeBinding(name.value, eval(pair.contents.get(1), new StackFrame(frame)));
                    else // let*
                        newFrame.storeBinding(name.value, eval(pair.contents.get(1), new StackFrame(newFrame)));
                }

                LispObject res = null; // must always be one statement, so this can never return null
                for (int i = 1; i < contents.size(); ++i)
                    res = eval(contents.get(i), newFrame);

                return res;
            case "if":
                if (contents.size() != 3)
                    throw new LispException("If expression must have exactly 3 arguments");

                LispObject ifCondition = eval(contents.get(0), frame);

                if ((ifCondition instanceof LispObject.Boolean) && ((LispObject.Boolean) ifCondition).value == false)
                    return eval(contents.get(2), frame);
                else
                    return eval(contents.get(1), frame);
            case "cond":
                LispObject result = new LispObject.Void();
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
                            result = eval(clause.get(j), frame);
                        }

                        if (i != contents.size() - 1)
                            throw new LispException("Else must be the last clause in a cond block");

                        return result;
                    }


                    LispObject condCondition = eval(clause.get(0), frame);
                    if (!(condCondition instanceof LispObject.Boolean
                            && ((LispObject.Boolean) condCondition).value == false)) {
                        result = condCondition;

                        for (int j = 1; j < clause.size(); j++) {
                            result = eval(clause.get(j), frame);
                        }

                        return result;
                    }
                }

                return result;
            case "and":
                LispObject andRes = new LispObject.Boolean(true);
                for (AstNode node : contents) {
                    andRes = eval(node, frame);
                    if (andRes instanceof LispObject.Boolean && ((LispObject.Boolean) andRes).value == false)
                        return andRes;
                }

                return andRes;
            case "or":
                for (AstNode node : contents) {
                    LispObject orRes = eval(node, frame);
                    if (orRes instanceof LispObject.Boolean && ((LispObject.Boolean) orRes).value == false)
                        continue;

                    return orRes;
                }
                return new LispObject.Boolean(false);
        }

        throw new LispException("Something has gone horribly wrong. Please consult a local moose to update this.");
    }
}