package org.majora320.tealisp.parser;

public class AstNode {
    public static class RootNode extends AstNode {
        public java.util.List<AstNode> children;

        public RootNode(java.util.List<AstNode> children) {
            this.children = children;
        }
    }

    public static class Sexp extends AstNode {
        public java.util.List<AstNode> contents;

        public Sexp(java.util.List<AstNode> contents) {
            this.contents = contents;
        }
    }

    public static class Name extends AstNode {
        public java.lang.String value;

        public Name(java.lang.String value) {
            this.value = value;
        }
    }

    public static class Quote extends AstNode {
        public AstNode contents;

        public Quote(AstNode contents) {
            this.contents = contents;
        }
    }

    public static class QuasiQuote extends AstNode {
        public AstNode contents;

        public QuasiQuote(AstNode contents) {
            this.contents = contents;
        }
    }

    public static class UnQuote extends AstNode {
        public AstNode contents;

        public UnQuote(AstNode contents) {
            this.contents = contents;
        }
    }

    public static class Integer extends AstNode {
        public int value;

        public Integer(int value) {
            this.value = value;
        }
    }

    public static class String extends AstNode {
        public java.lang.String value;

        public String(java.lang.String value) {
            this.value = value;
        }
    }

    public static class Boolean extends AstNode {
        public boolean value;

        public Boolean(boolean value) {
            this.value = value;
        }
    }
}
