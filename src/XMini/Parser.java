package XMini;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static class ParserException extends RuntimeException {
        ParserException(String message) {
            super(message);
        }
    }
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }


    private Token peek() {
        return tokens.get(pos);
    }

    private Token advance() {
        if (peek().type() == TokenType.EOF) {
            return peek();
        }
        return tokens.get(pos++);
    }

    private boolean match(TokenType... type) {
        for (TokenType t : type) {
            if (peek().type() == t) {
//                advance();
                return true;
            }
        }
        return false;
    }

    public List<Expr.Statement> parse() {
        List<Expr.Statement> statements = new ArrayList<>();
        while (!match(TokenType.EOF)) {
            statements.add(parseStatement());
        }
        return statements;
    }

    private Expr.Statement parseStatement() {
        if (match(TokenType.TEXT, TokenType.OUTPUT)) {
            var keyword = advance();
            var expr = parseExpression();
            return new Expr.Statement(keyword,null, expr);
        } else if (match(TokenType.VAR, TokenType.SET)) {
            var keyword = advance();
            var name = advance();
            var expr = parseExpression();
            return new Expr.Statement(keyword, name, expr);
        } else {
            throw new ParserException("Unexpected token " + peek());
        }
    }

    // parse prefix expression
    private Expr parseExpression() {
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(advance());
        } else if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(advance());
        }
        else if (match(TokenType.TILDE, TokenType.BANG)) {
            var op = advance();
            var expr = parseExpression();
            return new Expr.Arithmetic(op, expr, null);
        } else if (!match(TokenType.TEXT, TokenType.OUTPUT, TokenType.VAR, TokenType.SET, TokenType.EOF)) {
            // binary expression
            var op = advance();
            var left = parseExpression();
            var right = parseExpression();
            return new Expr.Arithmetic(op, left, right);
        } else {
            throw new ParserException("Unexpected token " + peek());
        }
    }
}