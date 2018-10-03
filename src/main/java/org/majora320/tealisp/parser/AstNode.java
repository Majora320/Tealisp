package org.majora320.tealisp.parser;

import java.util.List;

public class AstNode {
    private static java.lang.String astNodesToString(List<AstNode> children) {
        StringBuilder res = new StringBuilder("[");
        boolean once = false;

        for (AstNode child : children) {
            if (once)
                res.append(", ");
            else
                once = true;

            res.append(child);
        }

        res.append("]");
        return res.toString();
    }

    public static class RootNode extends AstNode {
        public List<AstNode> children;

        public RootNode(List<AstNode> children) {
            this.children = children;
        }

        @Override
        public java.lang.String toString() {
            return "RootNode" + AstNode.astNodesToString(children);
        }
    }

    public static class Sexp extends AstNode {
        public List<AstNode> contents;

        public Sexp(List<AstNode> contents) {
            this.contents = contents;
        }

        @Override
        public java.lang.String toString() {
            return "Sexp" + AstNode.astNodesToString(contents);
        }
    }

    public static class Name extends AstNode {
        public java.lang.String value;

        public Name(java.lang.String value) {
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return "Name[" + value + "]";
        }
    }

    public static class Integer extends AstNode {
        public int value;

        public Integer(int value) {
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return "Integer[" + value + "]";
        }
    }

    public static class Double extends AstNode {
        public double value;

        public Double(double value) {
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return "Double[" + value + "]";
        }
    }


    public static class String extends AstNode {
        public java.lang.String value;

        public String(java.lang.String value) {
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return "String[" + value + "]";
        }
    }

    public static class Boolean extends AstNode {
        public boolean value;

        public Boolean(boolean value) {
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return "Boolean[" + value + "]";
        }
    }
}
