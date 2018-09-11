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
        public AstNode function;
        public List<AstNode> arguments;

        public FunctionApplication(AstNode function, List<AstNode> arguments) {
            this.function = function;
            this.arguments = arguments;
        }
    }

    public static class Name extends AstNode {
        public String value;

        public Name(String value) {
            this.value = value;
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
        public String value;

        public Symbol(String value) {
            this.value = value;
        }
    }

    public static class Integer extends Primitive {
        public int value;

        public Integer(int value) {
            this.value = value;
        }
    }
}
