package org.majora320.tealisp.evaluator;

import java.util.List;

public class LispObject {
    public static class LispInteger extends LispObject {
        public int value;

        public LispInteger(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static class LispSymbol extends LispObject {
        public String value;

        public LispSymbol(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class LispList extends LispObject {
        public List<LispObject> elements;


        public LispList(List<LispObject> elements) {
            this.elements = elements;
        }

        @Override
        public String toString() {
            return realToString(false);
        }

        /**
         * If `nested` is true, then we don't need to print the opening '
         */
        private String realToString(boolean nested) {
            StringBuilder res = new StringBuilder();
            if (!nested)
                res.append("'");

            for (LispObject obj : elements) {
                if (obj instanceof LispList) {
                    res.append(((LispList) obj).realToString(true));
                } else {
                    res.append(obj.toString());
                }
            }

            return res.toString();
        }
    }

    public static class LispVoid extends LispObject {
        @Override
        public String toString() {
            return "";
        }
    }
}
