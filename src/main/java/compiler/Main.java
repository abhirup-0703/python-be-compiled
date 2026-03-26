package compiler;

import java.io.IOException;

import compiler.lexer.LexerAPI;
import compiler.parser.CLRParser;
import compiler.parser.grammar.Grammar;
import compiler.parser.table.CLRTableGenerator;
import compiler.parser.table.ParsingTable;
import compiler.parser.util.GrammarLoader;
import compiler.parser.util.ParsingTableFormatter;

public class Main {
    
    private static final String GRAMMAR_FILE = "config/grammar.config";
    private static final String TABLE_OUTPUT_FILE = "parsing_table.html";

    public static void main(String[] args) {
        try {
            // --- 1. Load Grammar & Generate Parser Table ---
            System.out.println("--- 1. Loading Configuration ---");
            Grammar grammar = GrammarLoader.load(GRAMMAR_FILE);
            System.out.println("Grammar loaded with " + grammar.getProductions().size() + " productions.");

            System.out.println("\n--- 2. Generating CLR(1) Parsing Table ---");
            CLRTableGenerator generator = new CLRTableGenerator(grammar);
            ParsingTable table = generator.generate();
            
            // Output table to text file
            ParsingTableFormatter.writeTableToFile(table, TABLE_OUTPUT_FILE);
            System.out.println("Parsing Table generated and saved to: " + TABLE_OUTPUT_FILE);

            // --- 3. Lexical Analysis (File path directly to Lexer) ---
            String filePath = "test_script.spy";
            System.out.println("\n--- 3. Initializing LexerAPI with file: " + filePath + " ---");
            LexerAPI lexerAPI = new LexerAPI(filePath);

            System.out.println("\n--- Generated Symbol Table ---");

            lexerAPI.getSymbolTable().printTable();

            // --- 4. Syntax Analysis (Parsing) ---
            System.out.println("\n--- 4. Syntax Analysis (Parsing) ---");
            CLRParser parser = new CLRParser(table);
            
            // The parser consumes the tokens from the lexer
            boolean success = parser.parse(lexerAPI);
            if (success) {
                System.out.println("Compilation successful: The source code is syntactically valid.");
            }

        } catch (IOException e) {
            System.err.println("File IO Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Configuration Error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Compilation Error: " + e.getMessage());
        }
    }
}