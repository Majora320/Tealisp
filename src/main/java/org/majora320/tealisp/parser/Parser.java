package org.majora320.tealisp.parser;

import org.majora320.tealisp.lexer.LexException;
import org.majora320.tealisp.lexer.Token;
import org.majora320.tealisp.lexer.TokenStream;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    public static AstNode.RootNode parse(Reader input) throws IOException, LexException, ParseException {
        return parse(new TokenStream(input));
    }

    public static AstNode.RootNode parse(TokenStream tokens) throws IOException, LexException, ParseException {
        AstNode.RootNode res = new AstNode.RootNode(new ArrayList<>());

        Token token = tokens.nextToken();

        while (token != null) {
            res.children.add(parseNode(token, tokens));

            token = tokens.nextToken();
        }

        return res;
    }

    private static AstNode parseNode(Token token, TokenStream tokens) throws ParseException, IOException, LexException {
        if (token instanceof Token.LeftParen) {
            return parseSexp(tokens);
        } else if (token instanceof Token.RightParen) {
            throw new ParseException("Extra right parenthesis.");
        } else if (token instanceof Token.Integer) {
            return new AstNode.Integer(((Token.Integer) token).value);
        } else if (token instanceof Token.Name) {
            return new AstNode.Name(((Token.Name) token).value);
        } else if (token instanceof Token.String) {
            return new AstNode.String(((Token.String) token).value);
        } else if (token instanceof Token.Boolean) {
            return new AstNode.Boolean(((Token.Boolean) token).value);
        } else if (token instanceof Token.Quote
                || token instanceof Token.UnQuote
                || token instanceof Token.QuasiQuote) {
            Token nextToken = tokens.nextToken();
            if (nextToken == null)
                throw new ParseException("Expected something after '");

            AstNode nextNode = parseNode(nextToken, tokens);

            List<AstNode> sexpContents = new ArrayList<>();

            if (token instanceof Token.Quote)
                sexpContents.add(new AstNode.Name("quote"));
            else if (token instanceof Token.UnQuote)
                sexpContents.add(new AstNode.Name("unquote"));
            else if (token instanceof Token.QuasiQuote)
                sexpContents.add(new AstNode.Name("quasiquote"));

            sexpContents.add(nextNode);
            return new AstNode.Sexp(sexpContents);
        } else if (token == null) {
            throw new ParseException("Expected something, got nothing at ???");
        }

        throw new ParseException("This should never happen. If it does, contact Majora320 immediately with error code 451");
    }

    private static AstNode.Sexp parseSexp(TokenStream tokens) throws IOException, LexException, ParseException {
        AstNode.Sexp res = new AstNode.Sexp(new ArrayList<>());
        Token token = tokens.nextToken();

        while (!(token instanceof Token.RightParen)) {
            res.contents.add(parseNode(token, tokens));
            token = tokens.nextToken();
        }

        return res;
    }
}
