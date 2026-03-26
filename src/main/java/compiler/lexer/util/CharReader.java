package compiler.lexer.util;

public class CharReader {
    private final String source;
    private int position = 0;
    private int line = 1;
    private int column = 1;

    public CharReader(String source) {
        this.source = source;
    }

    public char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(position);
    }

    public char peekNext() {
        if (position + 1 >= source.length()) return '\0';
        return source.charAt(position + 1);
    }

    public char advance() {
        if (isAtEnd()) return '\0';
        char c = source.charAt(position++);
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    public boolean isAtEnd() {
        return position >= source.length();
    }

    public int getLine() { return line; }
    public int getColumn() { return column; }
    public int getPosition() { return position; }
}