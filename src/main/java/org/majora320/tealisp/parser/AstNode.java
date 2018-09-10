package org.majora320.tealisp.parser;

import java.util.List;

public class AstNode {
    public static class RootNode extends AstNode {
        public List<AstNode> children;

        public RootNode(List<AstNode> children) {
            this.children = children;
        }
    }

    public static class FunctionApplication extends AstNode {
        public Name functionName;
        public List<AstNode> arguments;

        public FunctionApplication(Name functionName, List<AstNode> arguments) {
            this.functionName = functionName;
            this.arguments = arguments;
        }
    }

    public static class Name extends AstNode {
        public String name;

        public Name(String name) {
            this.name = name;
        }
    }

    public static class Primitive extends AstNode {

    }

    public static class LispList extends Primitive {
        public List<Primitive> children;

        public LispList(List<Primitive> children) {
            this.children = children;
        }
    }

    public static class Symbol extends Primitive {
        public String symbol;

        public Symbol(String symbol) {
            this.symbol = symbol;
        }
    }

    public static class Integer extends Primitive {
        public int value;

        public Integer(int value) {
            this.value = value;
        }
    }
}
