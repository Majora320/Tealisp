package org.majora320.tealisp.parser;

public class AstNode {
    public static class RootNode extends AstNode {
        public java.util.List<AstNode> children;

        public RootNode(java.util.List<AstNode> children) {
            this.children = children;
        }
    }

    public static class FunctionApplication extends AstNode {
        public AstNode function;
        public java.util.List<AstNode> arguments;

        public FunctionApplication(AstNode function, java.util.List<AstNode> arguments) {
            this.function = function;
            this.arguments = arguments;
        }
    }

    public static class Name extends AstNode {
        public java.lang.String value;

        public Name(java.lang.String value) {
            this.value = value;
        }
    }

    public static class Primitive extends AstNode {

    }

    public static class List extends Primitive {
        public java.util.List<Primitive> children;

        public List(java.util.List<Primitive> children) {
            this.children = children;
        }
    }

    public static class Symbol extends Primitive {
        public java.lang.String value;

        public Symbol(java.lang.String value) {
            this.value = value;
        }
    }

    public static class Integer extends Primitive {
        public int value;

        public Integer(int value) {
            this.value = value;
        }
    }

    public static class String extends Primitive {
        public java.lang.String value;

        public String(java.lang.String value) {
            this.value = value;
        }
    }

    public static class Boolean extends Primitive {
        public boolean value;

        public Boolean(boolean value) {
            this.value = value;
        }
    }
}
