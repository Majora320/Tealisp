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
    protected StackFrame globalFrame = new StackFrame();
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
    private Runtime runtime;
    private LispObject globalResult;

    public Interpreter() {
        this(JavaRegistry.getGlobalRegistry());
    }

    public Interpreter(JavaRegistry registry) {
        runtime = new Runtime(this, registry);
    }

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
        this(registry);
        run(program);
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public LispObject getGlobalResult() {
        return globalResult;
    }

    public StackFrame getGlobalFrame() {
        return globalFrame;
    }

    public LispObject run(Reader reader) throws ParseException, LexException, IOException, LispException {
        return run(Parser.parse(reader));
    }

    public LispObject run(AstNode.RootNode program) throws LispException {
        for (AstNode child : program.children)
            globalResult = eval(child, globalFrame);

        return globalResult;
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
            return SpecialForms.handleSpecials(name, arguments, frame, this);

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