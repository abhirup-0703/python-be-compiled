package compiler.lexer;

import java.util.ArrayList;
import java.util.List;

import compiler.lexer.extractors.*;
import compiler.lexer.util.CharReader;
import compiler.lexer.util.IndentationTracker;
import compiler.lexer.util.KeywordTable;
import compiler.util.SymbolTable;
import compiler.util.Token;
import compiler.util.TokenType;

public class Lexer {
    private final CharReader reader;
    private final IndentationTracker indentTracker;
    private final List<TokenExtractor> extractors;
    private final SymbolTable symbolTable;

    public Lexer(String source) {
        this.reader = new CharReader(source);
        this.indentTracker = new IndentationTracker();
        this.extractors = new ArrayList<>();
        this.symbolTable = new SymbolTable();
        
        // Initialize the Keyword dictionary
        KeywordTable keywordTable = new KeywordTable();

        // Register Strategies (Order is important! Symbol fallback goes last)
        // NOTE: Instantiating these extractors automatically generates their NFA HTML visualizers
        extractors.add(new StringExtractor());
        extractors.add(new IdentifierExtractor(keywordTable));
        extractors.add(new NumberExtractor());
        extractors.add(new SymbolExtractor()); 
    }

    /**
     * Retrieves the populated Symbol Table containing all unique identifiers.
     */
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    /**
     * Executes the lexical analysis, returning a complete stream of tokens.
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        boolean isAtLineStart = true;

        while (!reader.isAtEnd()) {
            char c = reader.peek();
            int startLine = reader.getLine();
            int startCol = reader.getColumn();

            // 1. Handle Indentation (Pythonic Scoping)
            if (isAtLineStart) {
                int spaces = 0;
                while (reader.peek() == ' ') {
                    spaces++;
                    reader.advance();
                }
                if (reader.peek() == '\n' || reader.isAtEnd()) {
                    // Ignore blank lines for indentation purposes
                    if(!reader.isAtEnd()) reader.advance();
                    continue;
                }
                tokens.addAll(indentTracker.processIndentation(spaces, startLine, 1));
                isAtLineStart = false;
                continue; // Re-evaluate the current character after skipping spaces
            }

            // 2. Skip remaining inline whitespace
            if (c == ' ' || c == '\r' || c == '\t') {
                reader.advance();
                continue;
            }

            // 3. Handle explicit newlines
            if (c == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\\n", startLine, startCol));
                reader.advance();
                isAtLineStart = true;
                continue;
            }

            // 4. Strategy Pattern Token Extraction (NFA Maximal Munch)
            boolean matched = false;
            for (TokenExtractor extractor : extractors) {
                if (extractor.canHandle(c)) {
                    Token token = extractor.extract(reader, startLine, startCol);
                    tokens.add(token);
                    
                    // If the token is a user-defined variable/identifier, drop it in the Symbol Table!
                    if (token.type() == TokenType.IDENTIFIER) {
                        symbolTable.addSymbol(token.lexeme(), token.line(), token.column());
                    }
                    
                    matched = true;
                    break;
                }
            }

            // Failsafe in case of a completely unrecognized character
            if (!matched) {
                tokens.add(new Token(TokenType.UNKNOWN, String.valueOf(reader.advance()), startLine, startCol));
            }
        }

        // Flush any remaining unclosed indentation blocks at the end of the file
        tokens.addAll(indentTracker.flushRemaining(reader.getLine(), reader.getColumn()));
        tokens.add(new Token(TokenType.EOF, "", reader.getLine(), reader.getColumn()));
        
        return tokens;
    }
}