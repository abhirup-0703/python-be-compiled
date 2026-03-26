package compiler.lexer;

import java.io.IOException; // <-- NEW IMPORT
import java.util.List;

import compiler.util.SymbolTable;
import compiler.util.Token;
import compiler.util.TokenType;

public class LexerAPI {
    private final List<Token> tokens;
    private final SymbolTable symbolTable;
    private int position;

    // <-- UPDATED: Now takes filePath and throws IOException
    public LexerAPI(String filePath) throws IOException {
        Lexer internalLexer = new Lexer(filePath);
        
        this.tokens = internalLexer.tokenize();
        this.symbolTable = internalLexer.getSymbolTable();
        this.position = 0;
    }

    public SymbolTable getSymbolTable() { return symbolTable; }
    
    public Token getNextToken() {
        if (position < tokens.size()) return tokens.get(position++);
        return tokens.get(tokens.size() - 1);
    }

    public Token peekToken() {
        if (position < tokens.size()) return tokens.get(position);
        return tokens.get(tokens.size() - 1);
    }

    public Token peekAhead(int offset) {
        if (position + offset < tokens.size()) return tokens.get(position + offset);
        return tokens.get(tokens.size() - 1);
    }

    public boolean hasNext() {
        return position < tokens.size() && tokens.get(position).type() != TokenType.EOF;
    }
}