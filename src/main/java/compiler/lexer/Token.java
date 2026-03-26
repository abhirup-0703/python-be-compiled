package compiler.lexer;

import compiler.lexer.util.TokenType;

public record Token(TokenType type, String lexeme, int line, int column) {
    @Override
    public String toString() {
        return String.format("Token{%-15s '%s' [Line: %d, Col: %d]}", type, lexeme, line, column);
    }
}