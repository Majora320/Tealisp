package org.majora320.tealisp.evaluator;

public class LispException extends Exception {
    public LispException() {
        super();
    }

    public LispException(String message) {
        super(message);
    }

    public LispException(Throwable cause) {
        super(cause);
    }

    public LispException(String message, Throwable cause) {
        super(message, cause);
    }
}
