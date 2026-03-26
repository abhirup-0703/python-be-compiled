package compiler;

import compiler.lexer.Lexer;
import compiler.lexer.Token;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.List;

public class App {
    public static void main(String[] args) {
        try {
            // Read the script from the project root
            String sourceCode = Files.readString(Path.of("test_script.py"));
            
            System.out.println("--- Source Code ---");
            System.out.println(sourceCode);
            System.out.println("\n--- Lexical Analysis ---");

            Lexer lexer = new Lexer(sourceCode);
            List<Token> tokens = lexer.tokenize();

            for (Token token : tokens) {
                System.out.println(token);
            }
        } catch (IOException e) {
            System.err.println("Could not read test_script.py: " + e.getMessage());
        }
    }
}