package org.majora320.tealisp.evaluator;

import org.majora320.tealisp.parser.AstNode;

import java.util.stream.Collectors;

public abstract class LispObject {
    public static LispObject fromJavaObject(Object obj) throws ClassNotFoundException {
        if (obj instanceof java.lang.Integer) {
            return new Integer((java.lang.Integer) obj);
        } else if (obj instanceof java.lang.Double) {
            return new Double((java.lang.Double) obj);
        } else if (obj instanceof java.lang.String) {
            return new String((java.lang.String) obj);
        } else if (obj instanceof java.lang.Boolean) {
            return new Boolean((java.lang.Boolean) obj);
        } else if (obj instanceof java.util.List) {
            java.util.List<LispObject> converted;

            try {
                converted =
                        ((java.util.List<Object>) obj)
                                .stream()
                                .map(o -> {
                                    try {
                                        return LispObject.fromJavaObject(o);
                                    } catch (ClassNotFoundException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                                .collect(Collectors.toList());
            } catch (RuntimeException e) {
                if (e.getCause() instanceof ClassNotFoundException)
                    throw (ClassNotFoundException) e.getCause();
                else
                    throw e;
            }

            return new List(converted);
        }

        throw new ClassNotFoundException("Matching Tealisp type not found for class " + obj.getClass());
    }

    public static abstract class Number extends LispObject {

    }

    public static class Integer extends Number {
        public int value;

        public Integer(int value) {
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return java.lang.String.valueOf(value);
        }
    }

    public static class Double extends Number {
        public double value;

        public Double(double value) {
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
            return "'" + realToString();
        }

        /**
         * If `nested` is true, then we don't need to print the opening '
         */
        private java.lang.String realToString() {
            StringBuilder res = new StringBuilder("(");

            boolean once = false;

            for (LispObject obj : elements) {
                if (once)
                    res.append(" ");
                else
                    once = true;

                if (obj instanceof List) {
                    res.append(((List) obj).realToString());
                } else {
                    res.append(obj.toString());
                }
            }

            res.append(")");
            return res.toString();
        }
    }

    public static class Void extends LispObject {
        public Void() {

        }

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
        public java.lang.String name;
        public JavaInterface iface;

        /**
         * All parameters of `function` must be `LispObject`s. It should return a `LispObject`.
         * If the function does not have a value to return, it should return `LispVoid`, not `null`.
         */
        public JavaFunction(java.lang.String name, JavaInterface iface) {
            this.name = name;
            this.iface = iface;
        }
    }

    /**
     * Opaque (to Tealisp) Java object meant to be passed to and from Java functions
     */
    public static class JavaObject extends LispObject {
        public Object value;

        public JavaObject(Object value) {
            this.value = value;
        }
    }
}
