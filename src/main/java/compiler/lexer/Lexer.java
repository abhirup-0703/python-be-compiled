package compiler.lexer;

import compiler.lexer.util.CharReader;
import compiler.lexer.util.IndentationTracker;
import compiler.util.TokenType;
import compiler.util.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final CharReader reader;
    private final IndentationTracker indentTracker;
    private final Map<String, TokenType> keywords;

    public Lexer(String source) {
        this.reader = new CharReader(source);
        this.indentTracker = new IndentationTracker();
        this.keywords = new HashMap<>();
        setupKeywords();
    }

    private void setupKeywords() {
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("print", TokenType.PRINT);
        keywords.put("for", TokenType.FOR);
        keywords.put("in", TokenType.IN);
        keywords.put("while", TokenType.WHILE);
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
        keywords.put("not", TokenType.NOT);
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        boolean isAtLineStart = true;

        while (!reader.isAtEnd()) {
            char c = reader.peek();
            int startLine = reader.getLine();
            int startCol = reader.getColumn();

            // Handle Indentation at the start of a line
            if (isAtLineStart) {
                int spaces = 0;
                while (reader.peek() == ' ') {
                    spaces++;
                    reader.advance();
                }
                if (reader.peek() == '\n' || reader.isAtEnd()) {
                    // Blank line, ignore indentation
                    if(!reader.isAtEnd()) reader.advance();
                    continue;
                }
                tokens.addAll(indentTracker.processIndentation(spaces, startLine, 1));
                isAtLineStart = false;
                continue; // Re-evaluate the current character after spaces
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

            if (Character.isLetter(c) || c == '_') {
                tokens.add(lexIdentifierOrKeyword());
            } else if (Character.isDigit(c)) {
                tokens.add(lexNumber());
            } else {
                tokens.add(lexSymbol());
            }
        }

        tokens.addAll(indentTracker.flushRemaining(reader.getLine(), reader.getColumn()));
        tokens.add(new Token(TokenType.EOF, "", reader.getLine(), reader.getColumn()));
        return tokens;
    }

    private Token lexIdentifierOrKeyword() {
        int startCol = reader.getColumn();
        StringBuilder sb = new StringBuilder();
        
        while (Character.isLetterOrDigit(reader.peek()) || reader.peek() == '_') {
            sb.append(reader.advance());
        }
        
        String text = sb.toString();
        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
        return new Token(type, text, reader.getLine(), startCol);
    }

    private Token lexNumber() {
        int startCol = reader.getColumn();
        StringBuilder sb = new StringBuilder();
        boolean isFloat = false;

        while (Character.isDigit(reader.peek())) {
            sb.append(reader.advance());
        }

        if (reader.peek() == '.' && Character.isDigit(reader.peekNext())) {
            isFloat = true;
            sb.append(reader.advance()); // Consume '.'
            while (Character.isDigit(reader.peek())) {
                sb.append(reader.advance());
            }
        }

        return new Token(isFloat ? TokenType.FLOAT : TokenType.INTEGER, sb.toString(), reader.getLine(), startCol);
    }

    private Token lexSymbol() {
        int startCol = reader.getColumn();
        char c = reader.advance();
        char next = reader.peek();

        switch (c) {
            case '+': return new Token(TokenType.PLUS, "+", reader.getLine(), startCol);
            case '-': return new Token(TokenType.MINUS, "-", reader.getLine(), startCol);
            case '(': return new Token(TokenType.LPAREN, "(", reader.getLine(), startCol);
            case ')': return new Token(TokenType.RPAREN, ")", reader.getLine(), startCol);
            case '[': return new Token(TokenType.LBRACKET, "[", reader.getLine(), startCol);
            case ']': return new Token(TokenType.RBRACKET, "]", reader.getLine(), startCol);
            case ':': return new Token(TokenType.COLON, ":", reader.getLine(), startCol);
            case ',': return new Token(TokenType.COMMA, ",", reader.getLine(), startCol);
            
            case '=':
                if (next == '=') {
                    reader.advance();
                    return new Token(TokenType.EQUALITY, "==", reader.getLine(), startCol);
                }
                return new Token(TokenType.ASSIGN, "=", reader.getLine(), startCol);
                
            case '*':
                if (next == '*') {
                    reader.advance();
                    return new Token(TokenType.DOUBLE_STAR, "**", reader.getLine(), startCol);
                }
                return new Token(TokenType.STAR, "*", reader.getLine(), startCol);
                
            case '/':
                if (next == '/') {
                    reader.advance();
                    return new Token(TokenType.DOUBLE_SLASH, "//", reader.getLine(), startCol);
                }
                return new Token(TokenType.SLASH, "/", reader.getLine(), startCol);
                
            default:
                return new Token(TokenType.UNKNOWN, String.valueOf(c), reader.getLine(), startCol);
        }
    }
}