package org.majora320.tealisp;

public class LexException extends Exception {
    public LexException() {
        super();
    }

    public LexException(String message) {
        super(message);
    }

    public LexException(Throwable cause) {
        super(cause);
    }

    public LexException(String message, Throwable cause) {
        super(message, cause);
    }
}
