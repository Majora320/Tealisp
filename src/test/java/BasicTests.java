import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.majora320.tealisp.lexer.LexException;
import org.majora320.tealisp.lexer.Token;
import org.majora320.tealisp.lexer.TokenStream;
import org.majora320.tealisp.parser.AstNode;
import org.majora320.tealisp.parser.ParseException;
import org.majora320.tealisp.parser.Parser;

import java.io.IOException;
import java.io.StringReader;

class BasicTests {
    @Test
    void testLexer() {
        String string = "(hello world 123 '(4 5 6) 'foo \"foo bar baz\")";
        TokenStream stream = new TokenStream(new StringReader(string));

        try {
            //printTokenStream(stream);
            printSyntaxTree(Parser.parse(stream));
        } catch (IOException | LexException | ParseException e) {
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

    void printSyntaxTree(AstNode node) {
        if (node instanceof AstNode.RootNode) {
            for (AstNode child : ((AstNode.RootNode) node).children) {
                printSyntaxTree(child);
                System.out.println();
            }
        } else if (node instanceof AstNode.Integer) {
            System.out.print(((AstNode.Integer) node).value);
        } else if (node instanceof AstNode.Name) {
            System.out.print(((AstNode.Name) node).value);
        } else if (node instanceof AstNode.Symbol) {
            System.out.print("'" + ((AstNode.Symbol) node).value);
        } else if (node instanceof AstNode.String) {
            System.out.print("\"" + ((AstNode.String) node).value + "\"");
        } else if (node instanceof AstNode.List) {
            System.out.print("'(");

            boolean once = false;

            for (AstNode child : ((AstNode.List) node).children) {
                if (once)
                    System.out.print(" ");
                else
                    once = true;

                printSyntaxTree(child);
            }

            System.out.print(")");
        } else if (node instanceof AstNode.FunctionApplication) {
            System.out.print("(");

            printSyntaxTree(((AstNode.FunctionApplication) node).function);

            for (AstNode child : ((AstNode.FunctionApplication) node).arguments) {
                System.out.print(" ");
                printSyntaxTree(child);
            }

            System.out.print(")");
        }
    }
}
