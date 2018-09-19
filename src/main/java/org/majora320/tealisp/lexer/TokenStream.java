package org.majora320.tealisp.lexer;


import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

public class TokenStream {
    // Weird syntax to add stuff to a container inline
    // The first { creates an anonymous class subclassing from HashSet
    // And the second { creates a static initialization block within that class
    private Set<Character> allowedNamePunctuation = new HashSet<Character>() {{
       add('-');
       add('+');
       add('=');
       add('>');
       add('<');
       add('?');
    }};

    private PushbackReader input;

    public TokenStream(Reader input) {
        this.input = new PushbackReader(input);
    }

    /**
     * Tries to parse a token from the input stream. If it fails,
     * throws LexException; if there are no tokens left, it returns
     * null.
     *
     * @return A token when there are tokens left, otherwise null.
     * @throws IOException
     * @throws LexException
     */
    public Token nextToken() throws IOException, LexException {
        int in = input.read();

        while (in != -1 && Character.isWhitespace(in))
            in = input.read();

        if (in == -1)
            return null;


        switch ((char) in) {
            case '(':
            case '[':
            case '{':
                return new Token.LeftParen();
            case ')':
            case ']':
            case '}':
                return new Token.RightParen();
            case '\'':
                return new Token.Quote();
            case '`':
                return new Token.QuasiQuote();
            case ',':
                return new Token.UnQuote();
            case '"':
                return parseString();
            case '#':
                int next = input.read();

                if (next == 't')
                    return new Token.Boolean(true);
                else if (next == 'f')
                    return new Token.Boolean(false);
                else
                    throw new LexException("Only legal boolean values are #t and #f.");

        }

        if (in >= '0' && in <= '9') {
            return parseInteger((char) in);
        }

        return parseName((char) in);
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

    private Token parseName(char firstChar) throws IOException, LexException {
        StringBuilder res = new StringBuilder();

        int in = firstChar;
        while (in != 1 && (Character.isLetterOrDigit(in) || allowedNamePunctuation.contains((char)in))) {
            res.append((char) in);
            in = input.read();
        }

        input.unread(in);
        if (res.length() == 0)
            throw new LexException("Illegal character: " + firstChar);
        return new Token.Name(res.toString());
    }

    private Token parseString() throws IOException, LexException {
        int in = input.read();
        StringBuilder res = new StringBuilder();

        while (in != '"') {
            validateCharInString(in);

            if (in == '\\') {
                int next = input.read();
                validateCharInString(in);

                switch (next) {
                    case '\\':
                        res.append('\\');
                        break;
                    case 'n':
                        res.append('\n');
                        break;
                    case 't':
                        res.append('\t');
                        break;
                    case '"':
                        res.append('"');
                    default:
                        throw new LexException("Invalid character '" + (char) next + "' after escape.");
                }
            } else {
                res.append((char) in);
            }

            in = input.read();
        }

        return new Token.String(res.toString());
    }

    private void validateCharInString(int c) throws LexException {
        if (c == -1)
            throw new LexException("String missing closing quote.");

        if (c == '\n')
            throw new LexException("Newlines in strings are not supported except by \\n");
    }
}
