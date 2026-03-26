package compiler.lexer.extractors;

import compiler.lexer.util.CharReader;
import compiler.util.Token;
import compiler.util.TokenType;

public class NumberExtractor implements TokenExtractor {
    @Override
    public boolean canHandle(char c) {
        return Character.isDigit(c);
    }

    @Override
    public Token extract(CharReader reader, int startLine, int startCol) {
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

        return new Token(isFloat ? TokenType.FLOAT : TokenType.INTEGER, sb.toString(), startLine, startCol);
    }
}