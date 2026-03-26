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
import compiler.parser.util.ParsingTableFormatter;

public class Main {
    
    private static final String GRAMMAR_FILE = "config/grammar.config";
    private static final String TABLE_OUTPUT_FILE = "parsing_table.txt";

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

            // --- 2. Read Source Code ---
            System.out.println("\n--- 3. Reading Source Code ---");
            String sourceCode = Files.readString(Path.of("test_script.spy"));
            System.out.println(sourceCode);

            // --- 3. Lexical Analysis ---
            System.out.println("\n--- 4. Lexical Analysis via LexerAPI ---");
            LexerAPI lexerAPI = new LexerAPI(sourceCode);

            System.out.println("\n--- Generated Symbol Table ---");
            lexerAPI.getSymbolTable().printTable();

            // --- 4. Syntax Analysis (Parsing) ---
            System.out.println("\n--- 5. Syntax Analysis (Parsing) ---");
            CLRParser parser = new CLRParser(table);
            
            // The parser now consumes the tokens from the lexer
            boolean success = parser.parse(lexerAPI);
            if (success) {
                System.out.println("Compilation successful: The source code is syntactically valid.");
            }

        } catch (IOException e) {
            System.err.println("File IO Error: Make sure " + GRAMMAR_FILE + " and test_script.spy exist.");
            System.err.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Configuration Error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Compilation Error: " + e.getMessage());
        }
    }
}