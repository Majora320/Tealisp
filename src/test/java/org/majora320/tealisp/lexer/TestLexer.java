package org.majora320.tealisp.lexer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

class TestLexer {
    @Test
    void testBasic() throws IOException, LexException {
        assertLexEquals("()", new Token[]{new Token.LeftParen(), new Token.RightParen()});
        assertLexEquals("(\"hello world\" 'foo 123 -123 456.789 -1.2 .5 -.5 ` , ')", new Token[]{
                new Token.LeftParen(),
                new Token.String("hello world"),
                new Token.Quote(),
                new Token.Name("foo"),
                new Token.Integer(123),
                new Token.Integer(-123),
                new Token.Double(456.789),
                new Token.Double(-1.2),
                new Token.Double(.5),
                new Token.Double(-.5),
                new Token.QuasiQuote(),
                new Token.UnQuote(),
                new Token.Quote(),
                new Token.RightParen()
        });
    }

    @Test
    void testSpecialCases() throws IOException, LexException {
        assertLexEquals("", new Token[]{});
        assertLexEquals(" ", new Token[]{});
        assertLexEquals("\n", new Token[]{});
    }

    @Test
    void testIntegerParsings() throws IOException, LexException {
        Assertions.assertThrows(LexException.class, () -> assertLexEquals(".", new Token[]{}));
        Assertions.assertThrows(LexException.class, () -> assertLexEquals("..", new Token[]{}));
        Assertions.assertThrows(LexException.class, () -> assertLexEquals("..5", new Token[]{}));
        Assertions.assertThrows(LexException.class, () -> assertLexEquals(".5.", new Token[]{new Token.Double(.5)}));
        Assertions.assertThrows(LexException.class, () -> assertLexEquals("-.", new Token[]{}));
        Assertions.assertThrows(LexException.class, () -> assertLexEquals("-.5.", new Token[]{new Token.Double(-.5)}));
        Assertions.assertThrows(LexException.class, () -> assertLexEquals("-..5", new Token[]{}));
        Assertions.assertThrows(LexException.class, () -> assertLexEquals("-", new Token[]{}));

        assertLexEquals("- 5", new Token[]{new Token.Name("-"), new Token.Integer(5)});
        assertLexEquals("- .5", new Token[]{new Token.Name("-"), new Token.Double(.5)});
    }

    @Test
    void testInvalidParsings() {
        Assertions.assertThrows(LexException.class, () -> assertLexEquals("\\", new Token[]{}));
        Assertions.assertThrows(LexException.class, () -> assertLexEquals("\"", new Token[]{}));
        Assertions.assertThrows(LexException.class, () -> assertLexEquals("\"foo", new Token[]{}));
        Assertions.assertThrows(LexException.class, () -> assertLexEquals("foo\"", new Token[]{new Token.Name("foo")}));

    }

    private void assertLexEquals(String input, Token[] expectedOutput) throws IOException, LexException {
        TokenStream stream = new TokenStream(new StringReader(input));

        int i = 0;
        for (Token tok = stream.nextToken(); tok != null; tok = stream.nextToken()) {
            if (i >= expectedOutput.length)
                Assertions.fail("Too many tokens returned: " + input);

            Assertions.assertEquals(expectedOutput[i++], tok, "Input: " + input);
        }

        if (i != expectedOutput.length)
            Assertions.fail("Not enough tokens returned: " + input);
    }
}