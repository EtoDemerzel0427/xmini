package XMini;

import java.util.ArrayList;
import java.util.List;

/* Operators:
 *    Binary math operators: +, -, *, /, %
 *    Unary math operators: !, ~
 *    Binary boolean operators: &&, ||
 *    Comparison operators: ==, !=, <, >, <=, >=
 */
enum TokenType {
    // keywords
    TEXT,
    OUTPUT,
    VAR,
    SET,

    // operators
    PLUS,
    MINUS,
    MUL,
    DIV,
    MOD,
    BANG,
    TILDE,
    EQ,
    NEQ,
    LT,
    GT,
    LTE,
    GTE,
    AND,
    OR,

    // literals
    NUMBER,
    STRING,

    // Identifiers
    IDENTIFIER,

    EOF
}

record Token(TokenType type, String text) {
    @Override
    public String toString() {
        return "Token(" + type + ", " + text + ")";
    }
}


public class Lexer {
    private final String input;
    private int line;
    private int linePos;
    private int pos;
    private char currentChar;
    private final List<Token> tokens = new ArrayList<>();

    public Lexer(String input) {
        this.input = input;
        this.line = 1;
        this.linePos = 0;
        this.pos = 0;
        this.currentChar = input.charAt(0);
    }

    public List<Token> getTokens() {
        while (currentChar != '\0') {
            if (Character.isWhitespace(currentChar)) {
                skipWhitespace();
            } else if (Character.isDigit(currentChar)) {
                tokens.add(new Token(TokenType.NUMBER, readNumber()));
            } else if (currentChar == '"') {
                tokens.add(new Token(TokenType.STRING, readString(true)));
            } else if (tokens.size() > 0 && tokens.get(tokens.size() - 1).type() == TokenType.TEXT) {
                tokens.add(new Token(TokenType.STRING, readString(false)));
            } else if (currentChar == '+') {
                tokens.add(new Token(TokenType.PLUS, "+"));
                nextChar();
            } else if (currentChar == '-') {
                tokens.add(new Token(TokenType.MINUS, "-"));
                nextChar();
            } else if (currentChar == '*') {
                tokens.add(new Token(TokenType.MUL, "*"));
                nextChar();
            } else if (currentChar == '/') {
                var tokenString = readString(false);
                if (tokenString.equals("/")) {
                    tokens.add(new Token(TokenType.DIV, "/"));
                    nextChar();
                } else if (tokenString.startsWith("//")) {
                    skipComment();
                }
            } else if (currentChar == '%') {
                tokens.add(new Token(TokenType.MOD, "%"));
                nextChar();
            } else if (currentChar == '~') {
                tokens.add(new Token(TokenType.TILDE, "~"));
                nextChar();
            } else if (currentChar == '&') {
                // &&
                var tokenString = readString(false);
                if (tokenString.equals("&&")) {
                    tokens.add(new Token(TokenType.AND, "&&"));
                } else {
                    error(line, linePos, "Unexpected token: " + tokenString);
                }
            } else if (currentChar == '|') {
                // ||
                var tokenString = readString(false);
                if (tokenString.equals("||")) {
                    tokens.add(new Token(TokenType.OR, "||"));
                } else {
                    error(line, linePos, "Unexpected token: " + tokenString);
                }
            } else if (currentChar == '=') {
                // ==
                var tokenString = readString(false);
                if (tokenString.equals("==")) {
                    tokens.add(new Token(TokenType.EQ, "=="));
                } else {
                    error(line, linePos, "Unexpected token: " + tokenString);
                }
            } else if (currentChar == '!') {
                // !, !=
                var tokenString = readString(false);
                if (tokenString.equals("!")) {
                    tokens.add(new Token(TokenType.BANG, "!"));
                } else if (tokenString.equals("!=")) {
                    tokens.add(new Token(TokenType.NEQ, "!="));
                } else {
                    error(line, linePos, "Unexpected token: " + tokenString);
                }
            } else if (currentChar == '<') {
                // <, <=
                var tokenString = readString(false);
                if (tokenString.equals("<")) {
                    tokens.add(new Token(TokenType.LT, "<"));
                } else if (tokenString.equals("<=")) {
                    tokens.add(new Token(TokenType.LTE, "<="));
                } else {
                    error(line, linePos, "Unexpected token: " + tokenString);
                }
            } else if (currentChar == '>') {
                // >, >=
                var tokenString = readString(false);
                if (tokenString.equals(">")) {
                    tokens.add(new Token(TokenType.GT, ">"));
                } else if (tokenString.equals(">=")) {
                    tokens.add(new Token(TokenType.GTE, ">="));
                } else {
                    error(line, linePos, "Unexpected token: " + tokenString);
                }
            } else if (Character.isLetter(currentChar)) {
                var tokenString = readString(false);
                if (tokenString.equals("output")) {
                    tokens.add(new Token(TokenType.OUTPUT, "output"));
                } else if (tokenString.equals("var")) {
                    tokens.add(new Token(TokenType.VAR, "var"));
                } else if (tokenString.equals("set")) {
                    tokens.add(new Token(TokenType.SET, "set"));
                }  else if (tokenString.equals("text")) {
                    tokens.add(new Token(TokenType.TEXT, "text"));
                } else if (isIdentifier(tokenString)) {
                    tokens.add(new Token(TokenType.IDENTIFIER, tokenString));
                } else {
                    error(line, linePos, "Unexpected token: " + tokenString);
                }
            }
            else {
                error(line, linePos, "Unexpected token: " + currentChar);
            }
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private void nextChar() {
        if (currentChar == '\n') {
            line++;
            linePos = 0;
        } else {
            linePos++;
        }
        pos++;
        currentChar = pos == input.length() ? '\0' : input.charAt(pos);
    }

    private void skipWhitespace() {
        while (Character.isWhitespace(currentChar) && currentChar != '\0') {
            nextChar();
        }
    }

    private String readNumber() {
        StringBuilder sb = new StringBuilder();
        while (Character.isDigit(currentChar)) {
            sb.append(currentChar);
            nextChar();
        }
        return sb.toString();
    }

    private String readString(boolean quoted) {
        StringBuilder sb = new StringBuilder();
        if (quoted) {
            nextChar();
            while (currentChar != '"' && currentChar != '\0') {
               readOneChar(sb);
            }
            if (currentChar == '\0') {
                error(line, linePos, "Unexpected end of input");
            }
            nextChar();
        } else {
            while (!Character.isWhitespace(currentChar) && currentChar != '\0') {
                // handle escaped characters
                readOneChar(sb);
            }
        }

        return sb.toString();
    }

    private void readOneChar(StringBuilder sb) {
        if (currentChar == '\\') {
            nextChar();
            switch (currentChar) {
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 't' -> sb.append('\t');
                case 'r' -> sb.append('\r');
                case 'n' -> sb.append('\n');
                case '\'' -> sb.append('\'');
                case '"' -> sb.append('"');
                case '\\' -> sb.append('\\');
                default -> error(line, linePos, "Unexpected escape character: " + currentChar);
            }
        } else {
            sb.append(currentChar);
        }

        nextChar();
    }

    private void skipComment() {
        if (currentChar == '\0'  || linePos == 0) {
            // if the comment has been eaten by the previous readString
            return;
        }
        nextChar();
        while (currentChar != '\n' && currentChar != '\0') {
            nextChar();
        }
    }

    private boolean isIdentifier(String s) {
        return s.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    private static void error(int line, int pos, String msg) {
        System.err.println("Error on line " + line + " at position " + pos + ": " + msg);
        System.exit(1);
    }

    private static void error(String msg) {
        System.err.println(msg);
        System.exit(1);
    }
}
