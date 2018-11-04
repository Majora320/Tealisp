package org.majora320.tealisp.evaluator;

import org.majora320.tealisp.lexer.LexException;
import org.majora320.tealisp.parser.AstNode;
import org.majora320.tealisp.parser.ParseException;
import org.majora320.tealisp.parser.Parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Interpreter {
    // Weird syntax to add stuff to a container inline
    // The first { creates an anonymous class subclassing from HashSet
    // And the second { creates a static initialization block within that class
    private Set<String> reservedKeywords = new HashSet<String>() {{
        add("define");
        add("lambda");
        add("let");
        add("let*");
        add("set!");
        add("if");
        add("when");
        add("unless");
        add("cond");
        add("and");
        add("or");
        add("quote");
        add("unquote");
        add("quasiquote");
    }};

    protected StackFrame globalFrame = new StackFrame();
    private Runtime runtime;

    public Runtime getRuntime() {
        return runtime;
    }

    private LispObject globalResult;

    public Interpreter(Reader reader) throws LispException, ParseException, LexException, IOException {
        this(Parser.parse(reader));
    }

    public Interpreter(Reader reader, JavaRegistry registry) throws LispException, ParseException, LexException, IOException {
        this(Parser.parse(reader), registry);
    }

    public Interpreter(AstNode.RootNode program) throws LispException {
        this(program, JavaRegistry.getGlobalRegistry());
    }

    public Interpreter(AstNode.RootNode program, JavaRegistry registry) throws LispException {
        runtime = new Runtime(this, registry);

        for (AstNode child : program.children)
            globalResult = eval(child, globalFrame);
    }

    public LispObject getGlobalResult() {
        return globalResult;
    }

    public StackFrame getGlobalFrame() {
        return globalFrame;
    }

    public LispObject eval(AstNode node, StackFrame frame) throws LispException {
        if (node instanceof AstNode.RootNode) {
            throw new LispException("Only one root node is allowed, and it must be the actual root node.");
        } else if (node instanceof AstNode.Boolean) {
            return new LispObject.Boolean(((AstNode.Boolean) node).value);
        } else if (node instanceof AstNode.String) {
            return new LispObject.String(((AstNode.String) node).value);
        } else if (node instanceof AstNode.Integer) {
            return new LispObject.Integer(((AstNode.Integer) node).value);
        } else if (node instanceof AstNode.Double) {
            return new LispObject.Double(((AstNode.Double) node).value);
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
            return apply(name, contents.subList(1, contents.size()), frame);
        } else if (first instanceof AstNode.Sexp) {
            LispObject result = eval(first, new StackFrame(frame));

            return apply(result, contents.subList(1, contents.size()), frame);
        }

        throw new LispException("This should never happen. If it does, contact Majora320 immediately with error code 453");
    }

    // Bit of code duplication here. Feel free to clean up.
    protected LispObject apply(String name, List<AstNode> arguments, StackFrame frame) throws LispException {
        if (reservedKeywords.contains(name))
            return handleSpecials(name, arguments, frame);

        List<LispObject> lispObjs = new ArrayList<>(arguments.size());

        for (AstNode node : arguments) {
            lispObjs.add(eval(node, frame));
        }

        return applyNoSpecials(name, lispObjs, frame);
    }

    protected LispObject apply(LispObject value, List<AstNode> arguments, StackFrame frame) throws LispException {
        List<LispObject> lispObjs = new ArrayList<>(arguments.size());

        for (AstNode node : arguments) {
            lispObjs.add(eval(node, frame));
        }

        return apply2(value, lispObjs, frame);
    }

    protected LispObject applyNoSpecials(String name, List<LispObject> arguments, StackFrame frame) throws LispException {
        LispObject value = null;

        LispObject.JavaFunction javaFunction = runtime.registry.lookupFunction(name);
        if (javaFunction != null)
            value = javaFunction;

        LispObject obj = frame.lookupBinding(name);
        if (obj != null)
            value = obj;

        if (value == null)
            throw new LispException("Undefined variable: " + name);

        return apply2(value, arguments, frame);
    }

    // The apply2 is to work around "method has same erasure" error
    // Kind of hacky, if you find a better solution go ahead

    protected LispObject apply2(LispObject value, List<LispObject> arguments, StackFrame frame) throws LispException {
        if (!(value instanceof LispObject.Function || value instanceof LispObject.JavaFunction))
            throw new LispException("Not a function: " + value);

        if (value instanceof LispObject.JavaFunction)
            return applyJavaFunction((LispObject.JavaFunction) value, arguments, frame);

        LispObject.Function function = (LispObject.Function) value;

        if (!(arguments.size() == function.paramNames.size()))
            throw new LispException("Expected " + function.paramNames.size() + " arguments, got " + arguments.size());


        StackFrame newFrame = new StackFrame(frame);

        for (int i = 0; i < arguments.size(); ++i) {
            newFrame.storeBinding(function.paramNames.get(i), arguments.get(i));
        }

        LispObject res = null;
        for (AstNode node : function.body) {
            res = eval(node, newFrame);
        }

        return res;
    }

    private LispObject applyJavaFunction(LispObject.JavaFunction function, List<LispObject> arguments, StackFrame frame) throws LispException {
        return function.iface.runFunction(function.name, arguments.toArray(new LispObject[]{}), frame);
    }

    private LispObject handleSpecials(String special, List<AstNode> contents, StackFrame frame) throws LispException {
        switch (special) {
            case "define":
                if (contents.size() < 2)
                    throw new LispException("Define missing body");

                AstNode defineSpec = contents.get(0);
                if (!(defineSpec instanceof AstNode.Name || defineSpec instanceof AstNode.Sexp))
                    throw new LispException("Expected variable or procedure definition, got something else: " + defineSpec);

                if (defineSpec instanceof AstNode.Name) {
                    if (contents.size() != 2)
                        throw new LispException("Extra terms for variable definition");

                    frame.storeBinding(((AstNode.Name) defineSpec).value, eval(contents.get(1), new StackFrame(frame)));
                    return new LispObject.Void();
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
                    return new LispObject.Void();
                }
            case "lambda":
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
            case "set!":
                if (contents.size() != 2)
                    throw new LispException("set! has the wrong number of arguments");

                AstNode rawName = contents.get(0);

                if (!(rawName instanceof AstNode.Name))
                    throw new LispException("Expected identifier passed to set!");

                LispObject value = eval(contents.get(1), frame);
                frame.modifyBinding(((AstNode.Name) rawName).value, value);

                return new LispObject.Void();
            case "if":
                if (contents.size() != 3)
                    throw new LispException("If expression must have exactly 3 arguments");

                LispObject ifCondition = eval(contents.get(0), frame);

                if ((ifCondition instanceof LispObject.Boolean) && ((LispObject.Boolean) ifCondition).getValue() == false)
                    return eval(contents.get(2), frame);
                else
                    return eval(contents.get(1), frame);
            case "when":
            case "unless":
                if (contents.size() < 2)
                    throw new LispException((special.equals("when") ? "When" : "Unless") + " expression must have two or more arguments");

                newFrame = new StackFrame(frame);

                LispObject condition = eval(contents.get(0), frame);

                if (condition instanceof LispObject.Boolean && ((LispObject.Boolean) condition).getValue() == false) {
                    if (special.equals("when")) {
                        return new LispObject.Void();
                    } else {
                        res = null; // must always be one statement, so this can never return null

                        for (int i = 1; i < contents.size(); ++i)
                            res = eval(contents.get(i), newFrame);
                        return res;
                    }
                } else {
                    if (special.equals("when")) {
                        res = null; // must always be one statement, so this can never return null
                        for (int i = 1; i < contents.size(); ++i)
                            res = eval(contents.get(i), newFrame);
                        return res;
                    } else {
                        return new LispObject.Void();
                    }
                }
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
                            && !((LispObject.Boolean) condCondition).getValue())) {
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
                    if (andRes instanceof LispObject.Boolean && !((LispObject.Boolean) andRes).getValue())
                        return andRes;
                }

                return andRes;
            case "or":
                for (AstNode node : contents) {
                    LispObject orRes = eval(node, frame);
                    if (orRes instanceof LispObject.Boolean && !((LispObject.Boolean) orRes).getValue())
                        continue;

                    return orRes;
                }

                return new LispObject.Boolean(false);
            case "quote":
                if (contents.size() != 1) {
                    throw new LispException("Arity mismatch: expected 1 argument, got " + contents.size() + " for function quote");
                }

                AstNode content = contents.get(0);

                if (!(content instanceof AstNode.Name) && !(content instanceof AstNode.Sexp))
                    throw new LispException("Quote not followed by a symbol or list");

                if (content instanceof AstNode.Name) {
                    return new LispObject.Symbol(((AstNode.Name) content).value);
                } else if (content instanceof AstNode.Sexp) {
                    List<LispObject> processedContents = new ArrayList<>();

                    for (AstNode node : ((AstNode.Sexp) content).contents)
                        processedContents.add(processQuotedObj(node));

                    return new LispObject.List(processedContents);
                }
        }

        throw new LispException("Something has gone horribly wrong. Please consult a local moose to update this.");
    }

    LispObject processQuotedObj(AstNode node) throws LispException {
        if (!(node instanceof AstNode.Name) && !(node instanceof AstNode.Sexp) && !(node instanceof AstNode.Integer)
                && !(node instanceof AstNode.String) && !(node instanceof AstNode.Boolean))
            throw new LispException("Not quotable: " + node);

        if (node instanceof AstNode.Name) {
            return new LispObject.Symbol(((AstNode.Name) node).value);
        } else if (node instanceof AstNode.Sexp) {
            List<LispObject> processedContents = new ArrayList<>();

            for (AstNode subNode : ((AstNode.Sexp) node).contents)
                processedContents.add(processQuotedObj(subNode));

            return new LispObject.List(processedContents);
        } else if (node instanceof AstNode.Integer) {
            return new LispObject.Integer(((AstNode.Integer) node).value);
        } else if (node instanceof AstNode.String) {
            return new LispObject.String(((AstNode.String) node).value);
        } else if (node instanceof AstNode.Boolean) {
            return new LispObject.Boolean(((AstNode.Boolean) node).value);
        }

        throw new LispException("ERROR BEGIN PLEASE CONTACT LOCAL CODING MONKEY TO UPDATE QUOTE TABLE END ERROR");
    }
}