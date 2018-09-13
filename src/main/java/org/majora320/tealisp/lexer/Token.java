package org.majora320.tealisp.lexer;

public class Token {
    public static class LeftParen extends Token {
        @Override
        public java.lang.String toString() {
            return "LeftParen";
        }
    }

    public static class RightParen extends Token {
        @Override
        public java.lang.String toString() {
            return "RightParen";
        }
    }

    public static class SymbolMark extends Token {
        @Override
        public java.lang.String toString() {
            return "SymbolMark";
        }
    }

    public static class Name extends Token {
        public java.lang.String value;

        public Name(java.lang.String name) {
            this.value = name;
        }

        @Override
        public java.lang.String toString() {
            return "Name[" + value + "]";
        }
    }

    public static class Integer extends Token {
        public int value;

        public Integer(int value) {
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return "Integer[" + value + "]";
        }
    }

    public static class String extends Token {
        public java.lang.String value;

        public String(java.lang.String value) {
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return "String[" + value + "]";
        }
    }

    public static class Boolean extends Token {
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
