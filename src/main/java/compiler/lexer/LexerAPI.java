package compiler.lexer;

import compiler.util.TokenType;
import compiler.util.Token;
import compiler.util.SymbolTable;
import java.util.List;

public class LexerAPI {
    private final List<Token> tokens;
    private final SymbolTable symbolTable;
    private int position;

    public LexerAPI(String sourceCode) {
        // The internal lexer does the heavy lifting, completely hidden from the user of LexerAPI
        Lexer internalLexer = new Lexer(sourceCode);
        
        // Tokenize the file and immediately grab the resulting data structures
        this.tokens = internalLexer.tokenize();
        this.symbolTable = internalLexer.getSymbolTable();
        this.position = 0;
    }

    /**
     * Retrieves the populated Symbol Table containing all unique identifiers.
     */
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    /**
     * Consumes and returns the current token, advancing the internal cursor.
     */
    public Token getNextToken() {
        if (position < tokens.size()) {
            return tokens.get(position++);
        }
        // Safely return the EOF token if the parser asks for too many tokens
        return tokens.get(tokens.size() - 1);
    }

    /**
     * Looks at the current token without consuming it. Crucial for parser lookahead.
     */
    public Token peekToken() {
        if (position < tokens.size()) {
            return tokens.get(position);
        }
        return tokens.get(tokens.size() - 1);
    }

    /**
     * Looks ahead 'n' steps without consuming. (e.g., peekAhead(1) is the next token).
     */
    public Token peekAhead(int offset) {
        if (position + offset < tokens.size()) {
            return tokens.get(position + offset);
        }
        return tokens.get(tokens.size() - 1);
    }

    /**
     * Checks if there are more functional tokens left (ignoring the final EOF).
     */
    public boolean hasNext() {
        return position < tokens.size() && tokens.get(position).type() != TokenType.EOF;
    }
}