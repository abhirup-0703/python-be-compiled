package compiler.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import compiler.lexer.util.CharReader;
import compiler.lexer.util.IndentationTracker;
import compiler.util.Token;
import compiler.util.TokenType;

// Imports for NFA Generation
import compiler.lexer.nfa.NFA;
import compiler.lexer.nfa.State;
import compiler.lexer.nfa.Transition;
import compiler.lexer.nfa.visualizer.NFAVisualizer;

public class Lexer {
    private final CharReader reader;
    private final IndentationTracker indentTracker;
    private final Map<String, TokenType> keywords;

    public Lexer(String source) {
        this.reader = new CharReader(source);
        this.indentTracker = new IndentationTracker();
        this.keywords = new HashMap<>();
        setupKeywords();
        
        // Generate the NFAs visually right as the lexer starts up
        buildAndVisualizeNFAs();
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
        keywords.put("True", TokenType.IDENTIFIER); // Alternatively add BOOLEAN_LIT to TokenType
        keywords.put("False", TokenType.IDENTIFIER);
    }

    /**
     * Constructs the theoretical NFAs defining our Lexical Rules 
     * and exports them to HTML files for visualization.
     */
    private void buildAndVisualizeNFAs() {
        // 1. Identifier NFA [a-zA-Z_][a-zA-Z0-9_]*
        State idS0 = new State(false);
        State idS1 = new State(true);
        idS0.addTransition(new Transition(c -> Character.isLetter(c) || c == '_', idS1, "[a-zA-Z_]"));
        idS1.addTransition(new Transition(c -> Character.isLetterOrDigit(c) || c == '_', idS1, "[a-zA-Z0-9_]"));
        NFA identifierNFA = new NFA(idS0);
        NFAVisualizer.generateHTML(identifierNFA, "identifier_nfa_graph.html");

        // 2. Number NFA (Handles both Integer and Float)
        State numS0 = new State(false);
        State numS1 = new State(true);  // Accept Int
        State numS2 = new State(false); // Decimal point seen
        State numS3 = new State(true);  // Accept Float
        
        numS0.addTransition(new Transition(Character::isDigit, numS1, "[0-9]"));
        numS1.addTransition(new Transition(Character::isDigit, numS1, "[0-9]"));
        numS1.addTransition(new Transition(c -> c == '.', numS2, "."));
        numS2.addTransition(new Transition(Character::isDigit, numS3, "[0-9]"));
        numS3.addTransition(new Transition(Character::isDigit, numS3, "[0-9]"));
        
        NFA numberNFA = new NFA(numS0);
        NFAVisualizer.generateHTML(numberNFA, "number_nfa_graph.html");

        // 3. String Literal NFA
        State strS0 = new State(false);
        State strS1 = new State(false);
        State strS2 = new State(true);
        strS0.addTransition(new Transition(c -> c == '"', strS1, "\\\""));
        strS1.addTransition(new Transition(c -> c != '"', strS1, "[^\\\"]"));
        strS1.addTransition(new Transition(c -> c == '"', strS2, "\\\""));
        NFA stringNFA = new NFA(strS0);
        NFAVisualizer.generateHTML(stringNFA, "string_nfa_graph.html");
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

            if (Character.isLetter(c) || c == '_') {
                tokens.add(lexIdentifierOrKeyword());
            } else if (Character.isDigit(c)) {
                tokens.add(lexNumber());
            } else if (c == '"') {
                tokens.add(lexString()); // <--- Now properly handles strings!
            } else {
                tokens.add(lexSymbol());
            }
        }

        tokens.addAll(indentTracker.flushRemaining(reader.getLine(), reader.getColumn()));
        tokens.add(new Token(TokenType.EOF, "", reader.getLine(), reader.getColumn()));
        return tokens;
    }

    private Token lexString() {
        int startCol = reader.getColumn();
        reader.advance(); // consume opening quote
        StringBuilder sb = new StringBuilder();
        
        while (!reader.isAtEnd() && reader.peek() != '"') {
            sb.append(reader.advance());
        }
        
        if (!reader.isAtEnd()) {
            reader.advance(); // consume closing quote
        }
        
        return new Token(TokenType.STRING, "\"" + sb.toString() + "\"", reader.getLine(), startCol);
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
            sb.append(reader.advance()); 
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
            case '+' -> { return new Token(TokenType.PLUS, "+", reader.getLine(), startCol); }
            case '-' -> { return new Token(TokenType.MINUS, "-", reader.getLine(), startCol); }
            case '(' -> { return new Token(TokenType.LPAREN, "(", reader.getLine(), startCol); }
            case ')' -> { return new Token(TokenType.RPAREN, ")", reader.getLine(), startCol); }
            case '[' -> { return new Token(TokenType.LBRACKET, "[", reader.getLine(), startCol); }
            case ']' -> { return new Token(TokenType.RBRACKET, "]", reader.getLine(), startCol); }
            case ':' -> { return new Token(TokenType.COLON, ":", reader.getLine(), startCol); }
            case ',' -> { return new Token(TokenType.COMMA, ",", reader.getLine(), startCol); }
            case '=' -> {
                if (next == '=') {
                    reader.advance();
                    return new Token(TokenType.EQUALITY, "==", reader.getLine(), startCol);
                }
                return new Token(TokenType.ASSIGN, "=", reader.getLine(), startCol);
            }
            case '>' -> {
                if (next == '=') {
                    reader.advance();
                    return new Token(TokenType.GREATER_EQUALS, ">=", reader.getLine(), startCol);
                }
                return new Token(TokenType.GREATER_THAN, ">", reader.getLine(), startCol);
            }
            case '<' -> {
                if (next == '=') {
                    reader.advance();
                    return new Token(TokenType.LESS_EQUALS, "<=", reader.getLine(), startCol);
                }
                return new Token(TokenType.LESS_THAN, "<", reader.getLine(), startCol);
            }
            case '!' -> {
                if (next == '=') {
                    reader.advance();
                    return new Token(TokenType.NOT_EQUALS, "!=", reader.getLine(), startCol);
                }
                return new Token(TokenType.UNKNOWN, "!", reader.getLine(), startCol);
            }
            case '*' -> {
                if (next == '*') {
                    reader.advance();
                    return new Token(TokenType.DOUBLE_STAR, "**", reader.getLine(), startCol);
                }
                return new Token(TokenType.STAR, "*", reader.getLine(), startCol);
            }
            case '/' -> {
                if (next == '/') {
                    reader.advance();
                    return new Token(TokenType.DOUBLE_SLASH, "//", reader.getLine(), startCol);
                }
                return new Token(TokenType.SLASH, "/", reader.getLine(), startCol);
            }
            default -> { return new Token(TokenType.UNKNOWN, String.valueOf(c), reader.getLine(), startCol); }
        }
    }
}