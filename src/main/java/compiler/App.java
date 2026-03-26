package compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import compiler.lexer.LexerAPI;
import compiler.parser.CLRParser;
import compiler.parser.grammar.Grammar;
import compiler.parser.table.CLRTableGenerator;
import compiler.parser.table.ParsingTable;
import compiler.parser.util.GrammarLoader;

public class App {
    
    private static final String NON_TERMINALS_FILE = "config/nonterminals.config";
    private static final String GRAMMAR_FILE = "config/grammar.config";
    private static final String SOURCE_FILE = "input.py"; // Or test_script.spy

    public static void main(String[] args) {
        try {
            System.out.println("--- 1. Loading Configuration ---");
            // Automatically extracts terminals from TokenType enum!
            Grammar grammar = GrammarLoader.load(NON_TERMINALS_FILE, GRAMMAR_FILE);
            System.out.println("Grammar loaded with " + grammar.getProductions().size() + " productions.");

            System.out.println("\n--- 2. Generating CLR(1) Parsing Table ---");
            CLRTableGenerator generator = new CLRTableGenerator(grammar);
            ParsingTable table = generator.generate();
            System.out.println("Parsing Table generated successfully.");

            System.out.println("\n--- 3. Reading Source Code ---");
            String sourceCode = Files.readString(Path.of(SOURCE_FILE));
            System.out.println(sourceCode);

            System.out.println("\n--- 4. Lexical & Syntax Analysis ---");
            LexerAPI lexer = new LexerAPI(sourceCode);
            CLRParser parser = new CLRParser(table);
            
            boolean success = parser.parse(lexer);
            if (success) {
                System.out.println("Compilation successful: The source code is syntactically valid.");
            }

        } catch (IOException e) {
            System.err.println("File IO Error: Make sure configuration files and source file exist.");
            System.err.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Configuration Error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Compilation Error: " + e.getMessage());
        }
    }
}