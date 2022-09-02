package XMini;

/*
 * An interpreter for the XMini programming language described in the assignment.
 * Language specification:
 *    1. `text` statement - prints the text to the console. The text can be a single word or a quoted string.
 *    2. `output` statement - prints the value of the expression to the console. The expression is in the prefix expression format,
 *    which is like: `+ 1 2` or more complicated: `+ * x x * y y `.
 *    3. `var` statement - declares a variable with the given name and value. Example: `var x 1` or `var x + 1 2`.
 *    4. `set` statement - sets the value of the variable to the value of the expression. Example: `set x + 1 2`.
 *
 * Note: Line breaks don't really matter to XMini, and it allows comments between statements (but not within statements).
 * Note: negative numbers are represented as ~ followed by the positive number (with a space between).
 */

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class XMini {
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: java XMini.XMini [input file]");
            System.exit(1);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runConsole();
        }
    }

    private static void runFile(String fileName) {
        try {
            String input = Files.readString(Paths.get(fileName), Charset.defaultCharset());
            Interpreter interpreter = new Interpreter();

            interpret(interpreter, input);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void runConsole() {
        System.out.println("XMini 0.1.0 (Aug 28 2022 05:12:00)");
        Scanner scanner = new Scanner(System.in);
        Interpreter interpreter = new Interpreter();
        while (true) {
            System.out.print(">>> ");
            String input = scanner.nextLine();
            if (input == null || input.equals("quit")) {
                break;
            }
            interpret(interpreter, input);
        }
    }

    private static void interpret(Interpreter interpreter, String input) {
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.getTokens();
        Parser parser = new Parser(tokens);
        List<Expr.Statement> statements = parser.parse();
        interpreter.interpret(statements);
    }

//    public static void error(int line, int pos, String msg) {
//        System.err.println("Error on line " + line + " at position " + pos + ": " + msg);
//        System.exit(1);
//    }
//
//    public static void error(String msg) {
//        System.err.println(msg);
//        System.exit(1);
//    }
}

