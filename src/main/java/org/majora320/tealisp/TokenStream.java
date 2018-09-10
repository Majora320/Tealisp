package org.majora320.tealisp;

import com.sun.istack.internal.Nullable;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

public class TokenStream {
    private PushbackReader input;

    public TokenStream(Reader input) {
        this.input = new PushbackReader(input);
    }

    @Nullable
    public Token nextToken() throws IOException, LexException {
        int in = input.read();

        while (in != -1 && Character.isWhitespace(in))
            in = input.read();

        if (in == -1)
            return null;


        switch ((char)in) {
            case '(': case '[': case '{':
                return new Token.LeftParen();
            case ')': case ']': case '}':
                return new Token.RightParen();
            case '\'':
                return new Token.ListStartMark();
            case '"':
                throw new LexException("Strings not yet supported");
        }

        if (in >= '0' && in <= '9') {
            return parseInteger((char)in);
        }

        return parseName((char)in);
    }

    private Token parseInteger(char firstChar) throws IOException, LexException {
        int res = 0;

        int in = firstChar;
        while (in != -1 && Character.isDigit(in)) {
            res *= 10;
            res += Character.getNumericValue(in);
            in = input.read();
        }

        if (Character.isLetter(in)) {
            throw new LexException("Expected space between integer and name.");
        }

        input.unread(in);
        return new Token.Integer(res);
    }

    private Token parseName(char firstChar) throws IOException {
        StringBuilder res = new StringBuilder();

        int in = firstChar;
        while (in != 1 && Character.isLetterOrDigit(in)) {
            res.append((char)in);
            in = input.read();
        }

        input.unread(in);
        return new Token.Name(res.toString());
    }
}
