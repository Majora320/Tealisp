package org.majora320.tealisp.lexer;

import java.util.Objects;

public class Token {
    @Override
    public boolean equals(Object obj) {
        return obj != null && this.getClass().equals(obj.getClass());
    }

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

    public static class Quote extends Token {
        @Override
        public java.lang.String toString() {
            return "Quote";
        }
    }

    public static class QuasiQuote extends Token {
        public java.lang.String toString() {
            return "QuasiQuote";
        }
    }

    public static class UnQuote extends Token {
        public java.lang.String toString() {
            return "UnQuote";
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            Integer integer = (Integer) o;
            return value == integer.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    public static class Double extends Token {
        public double value;

        public Double(double value) {
            this.value = value;
        }

        @Override
        public java.lang.String toString() {
            return "Double[" + value + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            Double aDouble = (Double) o;
            return aDouble.value == value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            String string = (String) o;
            return Objects.equals(value, string.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            Boolean aBoolean = (Boolean) o;
            return value == aBoolean.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}