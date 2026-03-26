package compiler.lexer.util;

import compiler.util.TokenType;
import java.util.HashMap;
import java.util.Map;

public class KeywordTable {
    private final Map<String, TokenType> keywords = new HashMap<>();

    public KeywordTable() {
        setupKeywords();
    }

    private void setupKeywords() {
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("print", TokenType.PRINT);
        // keywords.put("for", TokenType.FOR);
        // keywords.put("in", TokenType.IN);
        // keywords.put("while", TokenType.WHILE);
        keywords.put("input", TokenType.INPUT);
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
        keywords.put("not", TokenType.NOT);
        keywords.put("True", TokenType.IDENTIFIER); 
        keywords.put("False", TokenType.IDENTIFIER);
    }

    public TokenType getTokenType(String text) {
        return keywords.getOrDefault(text, TokenType.IDENTIFIER);
    }
}