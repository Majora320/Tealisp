package org.majora320.tealisp.parser;

import org.majora320.tealisp.lexer.LexException;
import org.majora320.tealisp.lexer.Token;
import org.majora320.tealisp.lexer.TokenStream;
import org.majora320.tealisp.parser.util.CheckedBiFunction;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class Parser {
    public static AstNode.RootNode parse(Reader input) throws IOException, LexException, ParseException {
        return parse(new TokenStream(input));
    }

    public static AstNode.RootNode parse(TokenStream tokens) throws IOException, LexException, ParseException {
        AstNode.RootNode res = new AstNode.RootNode(new ArrayList<AstNode>());

        Token token = tokens.nextToken();

        while (token != null) {
            res.children.add(parseNode(token, tokens));

            token = tokens.nextToken();
        }

        return res;
    }

    private static AstNode parseNode(Token token, TokenStream tokens) throws ParseException, IOException, LexException {
        if (token instanceof Token.LeftParen) {
            return parseFunctionApplication(tokens);
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
        } else if (token instanceof Token.SymbolMark) {
            return parseSymbolOrList(tokens);
        }

        throw new ParseException("This should never happen. If it does, contact Majora320 immediately with error code 451");
    }

    private static AstNode parseSymbolOrList(TokenStream tokens) throws IOException, LexException, ParseException {
        Token next = tokens.nextToken();

        if (next instanceof Token.Name) {
            return new AstNode.Symbol(((Token.Name) next).value);
        } else if (next instanceof Token.LeftParen) {
            return parseListElement(next, tokens);
        } else {
            throw new ParseException("Expected symbol or list, got something else after '");
        }
    }

    private static AstNode.Primitive parseListElement(Token token, TokenStream tokens) throws IOException, LexException, ParseException {
        if (token instanceof Token.Integer) {
            return new AstNode.Integer(((Token.Integer) token).value);
        } else if (token instanceof Token.Name) {
            return new AstNode.Symbol(((Token.Name) token).value);
        } else if (token instanceof Token.LeftParen) {
            return mapReduceTokens(
                    tokens,
                    new AstNode.List(new ArrayList<>()),
                    Parser::parseListElement,
                    (node, list) -> list.children.add(node)
            );
        } else if (token instanceof Token.SymbolMark) {
            throw new ParseException("To put a symbol inside of a list, just use the name without the tick mark.");
        } else if (token instanceof Token.RightParen) {
            throw new ParseException("Extra right parenthesis.");
        }

        throw new ParseException("This should never happen. If it does, contact Majora320 immediately with error code 452");
    }

    private static AstNode.FunctionApplication parseFunctionApplication(TokenStream tokens) throws IOException, LexException, ParseException {
        // Function
        Token function = tokens.nextToken();
        AstNode functionNode;

        if (function instanceof Token.Name) {
            functionNode = new AstNode.Name(((Token.Name) function).value);
        } else if (function instanceof Token.LeftParen) {
            functionNode = parseFunctionApplication(tokens);
        } else if (function instanceof Token.RightParen) {
            throw new ParseException("Extra right parenthesis.");
        } else {
            throw new ParseException("Expected function or expression evaluating to function, got something else.");
        }

        java.util.List arguments = mapReduceTokens(
                tokens,
                new ArrayList<>(),
                Parser::parseNode,
                (node, list) -> list.add(node)
        );

        return new AstNode.FunctionApplication(functionNode, arguments);
    }

    /**
     * Iterates through tokens until a closing paren is found.
     */
    private static <T, N extends AstNode> T mapReduceTokens(
            TokenStream tokens, T emptyT,
            CheckedBiFunction<Token, TokenStream, N> map,
            BiConsumer<N, T> reduce)
            throws IOException, LexException, ParseException {
        T res = emptyT;
        Token token = tokens.nextToken();

        while (!(token instanceof Token.RightParen)) {
            reduce.accept(map.apply(token, tokens), res);
            token = tokens.nextToken();
        }

        return res;
    }
}
