package XMini;

/*
 * We define an Expr class to represent expressions, and we use visitor pattern to visit each expression.
 * Grammar:
 *
 * program : statement*
 * statement : "output" prefix_expression | "text" string_literal | "var" var_name prefix_expression | "set" var_name prefix_expression
 * prefix_expression : ("+" | "-" | "*" | "/" | "%" | "&&" | "||" | "==" | "!=" | "<" | "<=" | ">" | ">=") prefix_expression prefix_expression | unary_expression | number | var_name
 * unary_expression : ("~" | "!") prefix_expression
 * number : [0-9]+
 * var_name : [a-zA-Z_][a-zA-Z0-9_]*
 * string_literal : ".*" | [\S]+
 *
 */
enum ExprType {
    BINARY,
    UNARY,
    LITERAL,
    VARIABLE
}
abstract class Expr {
    abstract <R> R accept(Visitor<R> visitor);

    // Technically, a statement is not an expression, but for simplicity in implementation, we treat it as an expression.
    static class Statement extends Expr {
        final Token keyword;
        final Token var_name;
        final Expr expr;
        Statement(Token keyword, Token var_name, Expr expr) {
            this.keyword = keyword;
            this.var_name = var_name;
            this.expr = expr;
        }

        <R> R accept(Visitor<R> visitor) {
            visitor.visitStatement(this);
            return null;
        }
    }

    // for ~ and !, right is null
    static class Arithmetic extends Expr {
        final Token operator;
        final Expr left;
        final Expr right;
        Arithmetic(Token operator, Expr left, Expr right) {
            this.operator = operator;
            this.left = left;
            this.right = right;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitArithmetic(this);
        }
    }

    static class Literal extends Expr {
        final Token value;  // String or Integer
        Literal(Token value) {
            this.value = value;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteral(this);
        }
    }
    
    static class Variable extends Expr {
        final Token var_name;
        Variable(Token var_name) {
            this.var_name = var_name;
        }
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariable(this);
        }
    }

    interface Visitor<T> {
        void visitStatement(Statement stmt);
        T visitArithmetic(Arithmetic expr);
        T visitLiteral(Literal expr);

        T visitVariable(Variable variable);
    }

}

