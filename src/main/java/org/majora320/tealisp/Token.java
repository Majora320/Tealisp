package org.majora320.tealisp;

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

    public static class ListStartMark extends Token {
        @Override
        public String toString() {
            return "ListStartMark";
        }
    };

    public static class Name extends Token {
        String name;

        public Name(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Name[" + name + "]";
        }
    };

    public static class Integer extends Token {
        int i;

        public Integer(int i) {
            this.i = i;
        }

        @Override
        public String toString() {
            return "Integer[" + i + "]";
        }
    }
}
