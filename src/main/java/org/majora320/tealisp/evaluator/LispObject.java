package org.majora320.tealisp.evaluator;

import org.majora320.tealisp.parser.AstNode;

public class LispObject {
    public static class Integer extends LispObject {
        public int value;

        public Integer(int value) {
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return java.lang.String.valueOf(value);
        }
    }

    public static class Symbol extends LispObject {
        public java.lang.String value;

        public Symbol(java.lang.String value) {
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return "'" + value;
        }
    }

    public static class String extends LispObject {
        public java.lang.String value;

        public String(java.lang.String value) {
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return "\"" + value + "\"";
        }
    }

    public static class Boolean extends LispObject {
        public boolean value;

        public Boolean(boolean value) {
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            if (value)
                return "#t";
            else
                return "#f";
        }
    }

    public static class List extends LispObject {
        public java.util.List<LispObject> elements;


        public List(java.util.List<LispObject> elements) {
            this.elements = elements;
        }

        @Override
        public java.lang.String toString() {
            return realToString(false);
        }

        /**
         * If `nested` is true, then we don't need to print the opening '
         */
        private java.lang.String realToString(boolean nested) {
            StringBuilder res = new StringBuilder();
            if (!nested)
                res.append("'");

            for (LispObject obj : elements) {
                if (obj instanceof List) {
                    res.append(((List) obj).realToString(true));
                } else {
                    res.append(obj.toString());
                }
            }

            return res.toString();
        }
    }

    public static class Void extends LispObject {
        @Override
        public java.lang.String toString() {
            return "";
        }
    }

    public static class Function extends LispObject {
        public java.lang.String name;
        public java.util.List<java.lang.String> paramNames;
        public java.util.List<AstNode> body;

        /**
         * name *can* be null. If so, this is a lambda (anonymous function).
         */
        public Function(java.lang.String name, java.util.List<java.lang.String> paramNames, java.util.List<AstNode> body) {
            this.name = name;
            this.paramNames = paramNames;
            this.body = body;
        }

        @Override
        public java.lang.String toString() {
            if (name == null)
                return "#<procedure>";
            else
                return "#<procedure:" + name + ">";
        }
    }

    public static class JavaFunction extends LispObject {
        public Class<?> function;
        public String name;
        public int numParams;

        /**
         * All parameters of `function` must be `LispObject`s. It should return a `LispObject`.
         * If the function does not have a value to return, it should return `LispVoid`, not `null`.
         */
        public JavaFunction(Class<?> function, String name, int numParams) {
            this.function = function;
            this.name = name;
            this.numParams = numParams;
        }
    }
}
