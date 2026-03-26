package compiler.lexer;

import java.util.ArrayList;
import java.util.List;

import compiler.lexer.extractors.*;
import compiler.lexer.nfa.LexerNFAVisualizer;
import compiler.lexer.util.CharReader;
import compiler.lexer.util.IndentationTracker;
import compiler.lexer.util.KeywordTable;
import compiler.util.Token;
import compiler.util.TokenType;

public class Lexer {
    private final CharReader reader;
    private final IndentationTracker indentTracker;
    private final List<TokenExtractor> extractors;

    public Lexer(String source) {
        this.reader = new CharReader(source);
        this.indentTracker = new IndentationTracker();
        this.extractors = new ArrayList<>();
        
        // Initialize dependencies
        KeywordTable keywordTable = new KeywordTable();

        // Register Strategies (Order is important! Symbol fallback goes last)
        extractors.add(new StringExtractor());
        extractors.add(new IdentifierExtractor(keywordTable));
        extractors.add(new NumberExtractor());
        extractors.add(new SymbolExtractor()); 
        
        // Generate NFAs
        LexerNFAVisualizer.buildAndVisualizeNFAs();
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        boolean isAtLineStart = true;

        while (!reader.isAtEnd()) {
            char c = reader.peek();
            int startLine = reader.getLine();
            int startCol = reader.getColumn();

            // Handle Indentation
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

            // Skip whitespace
            if (c == ' ' || c == '\r' || c == '\t') {
                reader.advance();
                continue;
            }

            // Handle explicit newlines
            if (c == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\\n", startLine, startCol));
                reader.advance();
                isAtLineStart = true;
                continue;
            }

            // Strategy pattern delegation
            for (TokenExtractor extractor : extractors) {
                if (extractor.canHandle(c)) {
                    tokens.add(extractor.extract(reader, startLine, startCol));
                    break;
                }
            }
        }

        tokens.addAll(indentTracker.flushRemaining(reader.getLine(), reader.getColumn()));
        tokens.add(new Token(TokenType.EOF, "", reader.getLine(), reader.getColumn()));
        return tokens;
    }
}