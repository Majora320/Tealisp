package org.majora320.tealisp.lexer;

public class Token {
    public static class LeftParen extends Token {
        @Override
        public String toString() {
            return "LeftParen";
        }
    };

    public static class RightParen extends Token {
        @Override
        public String toString() {
            return "RightParen";
        }
    };

    public static class SymbolMark extends Token {
        @Override
        public String toString() {
            return "SymbolMark";
        }
    };

    public static class Name extends Token {
        public String value;

        public Name(String name) {
            this.value = name;
        }

        @Override
        public String toString() {
            return "Name[" + value + "]";
        }
    };

    public static class Integer extends Token {
        public int value;

        public Integer(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Integer[" + value + "]";
        }
    }
}
