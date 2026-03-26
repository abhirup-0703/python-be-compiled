package compiler;

import java.io.IOException;

import compiler.lexer.LexerAPI;
import compiler.util.Token;
import compiler.util.TokenType;

public class App {
    public static void main(String[] args) {
        try {
            String filePath = "test_script.spy";
            
            System.out.println("--- Initializing LexerAPI with file: " + filePath + " ---");
            // <-- UPDATED: Passing the file path directly to the Lexer
            LexerAPI lexerAPI = new LexerAPI(filePath);

            System.out.println("\n--- Lexical Analysis via LexerAPI ---");

            Token currentToken = lexerAPI.getNextToken();
            while (currentToken.type() != TokenType.EOF) {
                System.out.println(currentToken);
                currentToken = lexerAPI.getNextToken();
            }
            
            System.out.println(currentToken);

            System.out.println("\n--- Generated Symbol Table ---");
            lexerAPI.getSymbolTable().printTable();

        } catch (IOException e) {
            System.err.println("Could not process file: " + e.getMessage());
        }
    }
}