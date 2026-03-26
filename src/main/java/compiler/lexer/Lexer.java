package compiler.lexer;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import compiler.util.Token;
import compiler.util.TokenType;

class Lexer {
    private final String source;
    private int position;
    private char currentChar;
    
    private int line;
    private int column;

    private final Stack<Integer> indentStack;
    private final Queue<Token> pendingTokens;

    public Lexer(String source) {
        this.source = source;
        this.position = 0;
        this.line = 1;
        this.column = 1;

        this.indentStack = new Stack<>();
        this.indentStack.push(0);
        
        this.pendingTokens = new LinkedList<>();
        
        if (source.length() > 0) {
            this.currentChar = source.charAt(0);
        } else {
            this.currentChar = '\0';
        }
    }
    
    private void advance() {
        position++;
        
        if (position >= source.length()) {
            currentChar = '\0';
        } else {
            if (currentChar == '\n') {
                line++;
                column = 0; 
            }
            
            currentChar = source.charAt(position);
            column++;
        }
    }

    private char peek() {
        if (position + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(position + 1);
    }

    private void skipWhitespace() {
        while (currentChar == ' ' || currentChar == '\t' || currentChar == '\r') {
            advance();
        }
    }

    private Token consumeNumber() {
        int startColumn = column;
        StringBuilder result = new StringBuilder();
        boolean hasDecimal = false;
        
        while (currentChar != '\0' && (Character.isDigit(currentChar) || currentChar == '.')) {
            if (currentChar == '.') {
                if (hasDecimal) {
                    throw new RuntimeException(
                        String.format("Lexer Error: Multiple decimal points at line %d, col %d", line, column)
                    );
                }
                hasDecimal = true;
            }
            result.append(currentChar);
            advance();
        }
        
        TokenType type = hasDecimal ? TokenType.FLOAT : TokenType.INTEGER;
        return new Token(type, result.toString(), line, startColumn);
    }

    private Token consumeString(char quoteType) {
        int startColumn = column;
        advance();
        
        StringBuilder result = new StringBuilder();
        
        while (currentChar != '\0' && currentChar != quoteType) {
            result.append(currentChar);
            advance();
        }
        
        if (currentChar == '\0') {
            throw new RuntimeException("Lexer Error: Unterminated string starting at line " + line);
        }
        
        advance();
        return new Token(TokenType.STRING, result.toString(), line, startColumn);
    }
    
    private Token consumeIdentifier() {
        int startColumn = column;
        StringBuilder result = new StringBuilder();
        
        while (currentChar != '\0' && 
              (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
            result.append(currentChar);
            advance();
        }
        
        String value = result.toString();
        
        TokenType type = switch (value) {
            case "if" -> TokenType.IF;
            case "else" -> TokenType.ELSE;
            case "print" -> TokenType.PRINT;
            case "for" -> TokenType.FOR;
            case "in" -> TokenType.IN;
            case "while" -> TokenType.WHILE;
            case "and" -> TokenType.AND;
            case "or" -> TokenType.OR;
            case "not" -> TokenType.NOT;
            default -> TokenType.IDENTIFIER;
        };
        
        return new Token(type, value, line, startColumn);
    }

    private void handleNewline() {
        int startLine = line;
        int startCol = column;
        
        advance();
        pendingTokens.add(new Token(TokenType.NEWLINE, "\\n", startLine, startCol));
        
        int spaceCount = 0;
        while (currentChar == ' ') {
            spaceCount++;
            advance();
        }
        
        if (currentChar == '\n' || currentChar == '\0' || currentChar == '\r') {
            return; 
        }
        
        int currentIndent = indentStack.peek();
        
        if (spaceCount > currentIndent) {
            indentStack.push(spaceCount);
            pendingTokens.add(new Token(TokenType.INDENT, "", line, column));
            
        } else if (spaceCount < currentIndent) {
            while (spaceCount < indentStack.peek()) {
                indentStack.pop();
                pendingTokens.add(new Token(TokenType.DEDENT, "", line, column));
            }
            
            if (spaceCount != indentStack.peek()) {
                throw new RuntimeException(
                    "IndentationError: unindent does not match any outer indentation level at line " + line
                );
            }
        }
    }
    
    public Token getNextToken() {
        if (!pendingTokens.isEmpty()) {
            return pendingTokens.poll();
        }

        while (currentChar != '\0') {
            
            if (currentChar == ' ' || currentChar == '\t' || currentChar == '\r') {
                skipWhitespace();
                continue;
            }
            
            if (Character.isLetter(currentChar) || currentChar == '_') {
                return consumeIdentifier();
            }
            
            if (Character.isDigit(currentChar)) {
                return consumeNumber();
            }

            if (currentChar == '"' || currentChar == '\'') {
                return consumeString(currentChar);
            }
            
            int startColumn = column;
            
            switch (currentChar) {
                case '[' -> {
                    advance();
                    return new Token(TokenType.LBRACKET, "[", line, startColumn);
                }
                case ']' -> {
                    advance();
                    return new Token(TokenType.RBRACKET, "]", line, startColumn);
                }
                case ',' -> {
                    advance();
                    return new Token(TokenType.COMMA, ",", line, startColumn);
                }
                case '=' -> {
                    if (peek() == '=') {
                        advance();
                        advance();
                        return new Token(TokenType.EQUALITY, "==", line, startColumn);
                    } else {
                        advance();
                        return new Token(TokenType.ASSIGN, "=", line, startColumn);
                    }
                }
                case '+' -> {
                    advance();
                    return new Token(TokenType.PLUS, "+", line, startColumn);
                }
                case '-' -> {
                    advance();
                    return new Token(TokenType.MINUS, "-", line, startColumn);
                }
                case '*' -> {
                    if (peek() == '*') {
                        advance(); advance();
                        return new Token(TokenType.DOUBLE_STAR, "**", line, startColumn);
                    }
                    advance();
                    return new Token(TokenType.STAR, "*", line, startColumn);
                }
                case '/' -> {
                    if (peek() == '/') {
                        advance(); advance();
                        return new Token(TokenType.DOUBLE_SLASH, "//", line, startColumn);
                    }
                    advance();
                    return new Token(TokenType.SLASH, "/", line, startColumn);
                }
                case '%' -> {
                    advance();
                    return new Token(TokenType.MODULO, "%", line, startColumn);
                }
                case '<' -> {
                    if (peek() == '=') {
                        advance(); advance();
                        return new Token(TokenType.LESS_EQUALS, "<=", line, startColumn);
                    }
                    advance();
                    return new Token(TokenType.LESS_THAN, "<", line, startColumn);
                }
                case '>' -> {
                    if (peek() == '=') {
                        advance(); advance();
                        return new Token(TokenType.GREATER_EQUALS, ">=", line, startColumn);
                    }
                    advance();
                    return new Token(TokenType.GREATER_THAN, ">", line, startColumn);
                }
                case '!' -> {
                    if (peek() == '=') {
                        advance(); advance();
                        return new Token(TokenType.NOT_EQUALS, "!=", line, startColumn);
                    }
                    throw new RuntimeException("Lexer Error: Unexpected character '!' at line " + line);
                }
                case '(' -> {
                    advance();
                    return new Token(TokenType.LPAREN, "(", line, startColumn);
                }
                case ')' -> {
                    advance();
                    return new Token(TokenType.RPAREN, ")", line, startColumn);
                }
                case ':' -> {
                    advance();
                    return new Token(TokenType.COLON, ":", line, startColumn);
                }
                case '\n' -> {
                    handleNewline();
                }
                default -> 
                    throw new RuntimeException(
                        String.format("Lexer Error: Unexpected character '%c' at line %d, col %d", currentChar, line, startColumn)
                    );
            }
        }
        
        while (indentStack.peek() > 0) {
            indentStack.pop();
            pendingTokens.add(new Token(TokenType.DEDENT, "", line, column));
        }
        
        pendingTokens.add(new Token(TokenType.EOF, "EOF", line, column));
        return pendingTokens.poll();
    }
}