package XMini;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Interpreter implements Expr.Visitor<Object> {
    private final Map<String, Object> symbols = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(Interpreter.class.getName());

    public Interpreter() {
        LOGGER.setLevel(Level.WARNING);
    }

    Object get(String name) {
        if (!symbols.containsKey(name)) {
            throw new RuntimeException("Undefined variable " + name);
        }
        return symbols.get(name);
    }

    void set(String name, Object value) {
        symbols.put(name, value);
    }
    void interpret(List<Expr.Statement> statements) {
        for (Expr.Statement statement : statements) {
            execute(statement);
        }
    }
    @Override
    public void visitStatement(Expr.Statement statement) {
        if (statement.keyword.type() == TokenType.TEXT || statement.keyword.type() == TokenType.OUTPUT) {
            System.out.println(evaluate(statement.expr));
        } else if (statement.keyword.type() == TokenType.VAR) {
            if (symbols.containsKey(statement.var_name.text())) {
//                throw new RuntimeException("Variable " + statement.var_name.text() + " already defined");
                LOGGER.warning("Variable " + statement.var_name.text() + " already defined");
            }
            set(statement.var_name.text(), evaluate(statement.expr));
        } else if (statement.keyword.type() == TokenType.SET) {
            if (!symbols.containsKey(statement.var_name.text())) {
                throw new RuntimeException("Variable " + statement.var_name.text() + " not defined");
            }
            set(statement.var_name.text(), evaluate(statement.expr));
        }
        else {
            throw new RuntimeException("Unexpected token " + statement.keyword);
        }
    }

    @Override
    public Object visitArithmetic(Expr.Arithmetic expr) {
        var left = evaluate(expr.left);
        var right = evaluate(expr.right);
        switch (expr.operator.type()) {
            case TILDE -> {
                assert right == null;
                return -(Integer)left;
            }
            case BANG -> {
                assert right == null;
                return (Integer)left == 0? 1: 0;  // because we are asked to evaluate true as 1, false as 0
            }
            case PLUS -> {
                assert left != null && right != null;
                return (Integer)left + (Integer)right;
            }
            case MINUS -> {
                assert left != null && right != null;
                return (Integer)left - (Integer)right;
            }
            case MUL -> {
                assert left != null && right != null;
                return (Integer)left * (Integer)right;
            }
            case DIV -> {
                assert left != null && right != null;
                return (Integer)left / (Integer)right;
            }
            case MOD -> {
                assert left != null && right != null;
                return (Integer)left % (Integer)right;
            }
            case AND -> {
                assert left != null && right != null;
                return ((Integer)left != 0 && (Integer)right != 0)? 1 : 0;
            }
            case OR -> {
                return ((Integer)left != 0 || (Integer)right != 0)? 1 : 0;
            }
            case EQ -> {
                assert left != null && right != null;
                return left.equals(right) ? 1 : 0;
            }
            case NEQ -> {
                assert left != null && right != null;
                return !left.equals(right) ? 1 : 0;
            }
            case GT -> {
                assert left != null && right != null;
                return (Integer)left > (Integer)right? 1 : 0;
            }
            case LT -> {
                assert left != null && right != null;
                return (Integer)left < (Integer)right? 1 : 0;
            }
            case GTE -> {
                assert left != null && right != null;
                return (Integer)left >= (Integer)right? 1 : 0;
            }
            case LTE -> {
                assert left != null && right != null;
                return (Integer)left <= (Integer)right? 1 : 0;
            }
            default -> {
                throw new RuntimeException("Unknown operator: " + expr.operator.type());
            }
        }
    }

    @Override
    public Object visitLiteral(Expr.Literal expr) {
        if (expr.value.type() == TokenType.NUMBER) {
            return Integer.parseInt(expr.value.text());
        } else if (expr.value.type() == TokenType.STRING) {
            return expr.value.text();
        } else {
            throw new RuntimeException("Unknown literal type: " + expr.value.type());
        }
    }

    @Override
    public Object visitVariable(Expr.Variable variable) {
        return get(variable.var_name.text());
    }

    private Object evaluate(Expr expr) {
        if (expr == null) {
            return null;
        }
        return expr.accept(this);
    }

    private void execute(Expr.Statement statement) {
        statement.accept(this);
    }
}

