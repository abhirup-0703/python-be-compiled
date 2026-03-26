package compiler.lexer.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CharReader implements AutoCloseable {
    private final BufferedReader reader;
    private int currentChar = -1;
    private int nextChar = -1;
    private int position = 0;
    private int line = 1;
    private int column = 1;

    public CharReader(String filePath) throws IOException {
        this.reader = new BufferedReader(new FileReader(filePath));
        
        // Initialize the 2-character sliding window for lookahead
        this.currentChar = reader.read();
        if (this.currentChar != -1) {
            this.nextChar = reader.read();
        }
    }

    public char peek() {
        if (isAtEnd()) return '\0';
        return (char) currentChar;
    }

    public char peekNext() {
        if (nextChar == -1) return '\0';
        return (char) nextChar;
    }

    public char advance() {
        if (isAtEnd()) return '\0';
        char c = (char) currentChar;
        
        // Advance the sliding window
        currentChar = nextChar;
        try {
            if (currentChar != -1) {
                nextChar = reader.read();
            }
        } catch (IOException e) {
            nextChar = -1; 
            throw new RuntimeException("Error reading file during lexical analysis", e);
        }

        position++;
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    public boolean isAtEnd() {
        return currentChar == -1;
    }

    public int getLine() { return line; }
    public int getColumn() { return column; }
    public int getPosition() { return position; }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}