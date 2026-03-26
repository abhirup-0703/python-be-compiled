package compiler;

import compiler.lexer.LexerAPI;
import compiler.util.Token;
import compiler.util.TokenType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class App {
    public static void main(String[] args) {
        try {
            String sourceCode = Files.readString(Path.of("test_script.spy"));
            
            System.out.println("--- Source Code ---");
            System.out.println(sourceCode);
            System.out.println("\n--- Lexical Analysis via LexerAPI ---");

            LexerAPI lexerAPI = new LexerAPI(sourceCode);

            // Simulating how a parser will consume the tokens
            Token currentToken = lexerAPI.getNextToken();
            while (currentToken.type() != TokenType.EOF) {
                System.out.println(currentToken);
                currentToken = lexerAPI.getNextToken();
            }
            
            // Print the EOF token at the end
            System.out.println(currentToken);

            // <-- NEW: Print the Symbol Table!
            System.out.println("\n--- Generated Symbol Table ---");
            lexerAPI.getSymbolTable().printTable();

        } catch (IOException e) {
            System.err.println("Could not read test_script.spy: " + e.getMessage());
        }
    }
}