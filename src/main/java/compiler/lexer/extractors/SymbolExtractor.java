package compiler.lexer.extractors;

import compiler.lexer.util.CharReader;
import compiler.util.Token;
import compiler.util.TokenType;

public class SymbolExtractor implements TokenExtractor {
    @Override
    public boolean canHandle(char c) {
        return true; // Fallback for all other characters
    }

    @Override
    public Token extract(CharReader reader, int startLine, int startCol) {
        char c = reader.advance();
        char next = reader.peek();

        switch (c) {
            case '+' -> { return new Token(TokenType.PLUS, "+", startLine, startCol); }
            case '-' -> { return new Token(TokenType.MINUS, "-", startLine, startCol); }
            case '(' -> { return new Token(TokenType.LPAREN, "(", startLine, startCol); }
            case ')' -> { return new Token(TokenType.RPAREN, ")", startLine, startCol); }
            case '[' -> { return new Token(TokenType.LBRACKET, "[", startLine, startCol); }
            case ']' -> { return new Token(TokenType.RBRACKET, "]", startLine, startCol); }
            case ':' -> { return new Token(TokenType.COLON, ":", startLine, startCol); }
            case ',' -> { return new Token(TokenType.COMMA, ",", startLine, startCol); }
            case '=' -> {
                if (next == '=') {
                    reader.advance();
                    return new Token(TokenType.EQUALITY, "==", startLine, startCol);
                }
                return new Token(TokenType.ASSIGN, "=", startLine, startCol);
            }
            case '>' -> {
                if (next == '=') {
                    reader.advance();
                    return new Token(TokenType.GREATER_EQUALS, ">=", startLine, startCol);
                }
                return new Token(TokenType.GREATER_THAN, ">", startLine, startCol);
            }
            case '<' -> {
                if (next == '=') {
                    reader.advance();
                    return new Token(TokenType.LESS_EQUALS, "<=", startLine, startCol);
                }
                return new Token(TokenType.LESS_THAN, "<", startLine, startCol);
            }
            case '!' -> {
                if (next == '=') {
                    reader.advance();
                    return new Token(TokenType.NOT_EQUALS, "!=", startLine, startCol);
                }
                return new Token(TokenType.UNKNOWN, "!", startLine, startCol);
            }
            case '*' -> {
                if (next == '*') {
                    reader.advance();
                    return new Token(TokenType.DOUBLE_STAR, "**", startLine, startCol);
                }
                return new Token(TokenType.STAR, "*", startLine, startCol);
            }
            case '/' -> {
                if (next == '/') {
                    reader.advance();
                    return new Token(TokenType.DOUBLE_SLASH, "//", startLine, startCol);
                }
                return new Token(TokenType.SLASH, "/", startLine, startCol);
            }
            default -> { return new Token(TokenType.UNKNOWN, String.valueOf(c), startLine, startCol); }
        }
    }
}