import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.majora320.tealisp.evaluator.Evaluator;
import org.majora320.tealisp.evaluator.LispException;
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
        String string = "123 \"foo bar baz\" #t #f (and \"f\" \"z\" #f)";
        TokenStream stream = new TokenStream(new StringReader(string));

        try {
            //printTokenStream(stream);
            AstNode.RootNode parsed = Parser.parse(stream);
            //printSyntaxTree(parsed);
            System.out.println(new Evaluator(parsed).getGlobalResult());
        } catch (IOException | LexException | ParseException | LispException e) {
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
        } else if (node instanceof AstNode.String) {
            System.out.print("\"" + ((AstNode.String) node).value + "\"");
        } else if (node instanceof AstNode.Boolean) {
            if (((AstNode.Boolean) node).value)
                System.out.print("#t");
            else
                System.out.print("#f");
        } else if (node instanceof AstNode.Sexp) {
            System.out.print("(");
            boolean once = false;

            for (AstNode child : ((AstNode.Sexp) node).contents) {
                if (once)
                    System.out.print(" ");
                else
                    once = true;

                printSyntaxTree(child);
            }

            System.out.print(")");
        }
    }
}
