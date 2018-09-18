package org.majora320.tealisp.evaluator;

import org.majora320.tealisp.parser.AstNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Evaluator {
    // Weird syntax to add stuff to a container inline
    // The first { creates an anonymous class subclassing from HashSet
    // And the second { creates a static initialization block within that class
    private Set<String> reservedKeywords = new HashSet<String>() {{
        add("define");
        add("lambda");
        add("let");
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
            LispObject result = eval(first, frame);

            return apply(result, contents.subList(1, contents.size()), frame);
        }

        throw new LispException("This should never happen. If it does, contact Majora320 immediately with error code 453");
    }

    private LispObject apply(LispObject value, List<AstNode> arguments, StackFrame frame) throws LispException {
        if (!(value instanceof LispObject.Function))
            throw new LispException("Not a function"); // TODO better error reporting

        LispObject.Function function = (LispObject.Function) value;

        if (!(arguments.size() == function.paramNames.size()))
            throw new LispException("Expected " + function.paramNames.size() + " arguments, got " + arguments.size());


        StackFrame newFrame = new StackFrame(frame);

        for (int i = 0; i < arguments.size(); ++i) {
            newFrame.storeBinding(function.paramNames.get(i).value, eval(arguments.get(i), frame));
        }

        LispObject res = null;
        for (AstNode node : function.body) {
            res = eval(node, newFrame);
        }

        return res;
    }

    private LispObject handleSpecials(String name, List<AstNode> contents, StackFrame frame) throws LispException {
        switch (name) {
            case "define":
                break;
            case "lambda":
                break;
            case "let":
                break;
            case "if":
                if (contents.size() != 3)
                    throw new LispException("If expression must have exactly 3 arguments");

                LispObject condition = eval(contents.get(0), frame);

                if ((condition instanceof LispObject.Boolean) && ((LispObject.Boolean) condition).value == false)
                    return eval(contents.get(2), frame);
                else
                    return eval(contents.get(1), frame);
            case "cond":

                break;
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