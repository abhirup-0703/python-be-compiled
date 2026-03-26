package compiler.lexer.extractors;

import compiler.lexer.util.CharReader;
import compiler.util.Token;
import compiler.util.TokenType;

public class StringExtractor implements TokenExtractor {
    @Override
    public boolean canHandle(char c) {
        return c == '"';
    }

    @Override
    public Token extract(CharReader reader, int startLine, int startCol) {
        reader.advance(); // consume opening quote
        StringBuilder sb = new StringBuilder();
        
        while (!reader.isAtEnd() && reader.peek() != '"') {
            sb.append(reader.advance());
        }
        
        if (!reader.isAtEnd()) {
            reader.advance(); // consume closing quote
        }
        
        return new Token(TokenType.STRING, "\"" + sb.toString() + "\"", startLine, startCol);
    }
}