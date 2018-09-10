import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.majora320.tealisp.lexer.LexException;
import org.majora320.tealisp.lexer.Token;
import org.majora320.tealisp.lexer.TokenStream;

import java.io.IOException;
import java.io.StringReader;

class BasicTests {
    @Test
    void testLexer() {
        String string = "'(hello world 61 61 6999)";
        TokenStream stream = new TokenStream(new StringReader(string));
        try {
            printTokenStream(stream);
        } catch (IOException | LexException e) {
            Assertions.fail(e);
        }
    }

    void printTokenStream(TokenStream stream) throws IOException, LexException {
        Token tok = stream.nextToken();

        while (tok != null) {
            System.out.println(tok);
            tok = stream.nextToken();
        }
    }
}
