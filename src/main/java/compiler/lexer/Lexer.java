package compiler.lexer;

import java.io.IOException;
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

    // <-- UPDATED: Now takes filePath and throws IOException
    public Lexer(String filePath) throws IOException {
        this.reader = new CharReader(filePath);
        this.indentTracker = new IndentationTracker();
        this.extractors = new ArrayList<>();
        this.symbolTable = new SymbolTable();
        
        KeywordTable keywordTable = new KeywordTable();

        extractors.add(new StringExtractor());
        extractors.add(new IdentifierExtractor(keywordTable));
        extractors.add(new NumberExtractor());
        extractors.add(new SymbolExtractor()); 
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        boolean isAtLineStart = true;

        // <-- UPDATED: Wrapped in try-finally to close the file stream
        try {
            while (!reader.isAtEnd()) {
                char c = reader.peek();
                int startLine = reader.getLine();
                int startCol = reader.getColumn();

                if (isAtLineStart) {
                    int spaces = 0;
                    while (reader.peek() == ' ') {
                        spaces++;
                        reader.advance();
                    }
                    if (reader.peek() == '\n' || reader.isAtEnd()) {
                        if(!reader.isAtEnd()) reader.advance();
                        continue;
                    }
                    tokens.addAll(indentTracker.processIndentation(spaces, startLine, 1));
                    isAtLineStart = false;
                    continue;
                }

                if (c == ' ' || c == '\r' || c == '\t') {
                    reader.advance();
                    continue;
                }

                if (c == '\n') {
                    tokens.add(new Token(TokenType.NEWLINE, "\\n", startLine, startCol));
                    reader.advance();
                    isAtLineStart = true;
                    continue;
                }

                boolean matched = false;
                for (TokenExtractor extractor : extractors) {
                    if (extractor.canHandle(c)) {
                        Token token = extractor.extract(reader, startLine, startCol);
                        tokens.add(token);
                        
                        if (token.type() == TokenType.IDENTIFIER) {
                            symbolTable.addSymbol(token.lexeme(), token.line(), token.column());
                        }
                        matched = true;
                        break;
                    }
                }

                if (!matched) {
                    tokens.add(new Token(TokenType.UNKNOWN, String.valueOf(reader.advance()), startLine, startCol));
                }
            }

            tokens.addAll(indentTracker.flushRemaining(reader.getLine(), reader.getColumn()));
            tokens.add(new Token(TokenType.EOF, "", reader.getLine(), reader.getColumn()));
            
        } finally {
            // Guarantee the file lock is released
            try {
                reader.close();
            } catch (IOException e) {
                System.err.println("Warning: Failed to close source file - " + e.getMessage());
            }
        }
        
        return tokens;
    }
}