package compiler.lexer.extractors;

import compiler.lexer.util.CharReader;
import compiler.lexer.util.KeywordTable;
import compiler.util.Token;
import compiler.util.TokenType;

public class IdentifierExtractor implements TokenExtractor {
    private final KeywordTable keywordTable;

    public IdentifierExtractor(KeywordTable keywordTable) {
        this.keywordTable = keywordTable;
    }

    @Override
    public boolean canHandle(char c) {
        return Character.isLetter(c) || c == '_';
    }

    @Override
    public Token extract(CharReader reader, int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        
        while (Character.isLetterOrDigit(reader.peek()) || reader.peek() == '_') {
            sb.append(reader.advance());
        }
        
        String text = sb.toString();
        TokenType type = keywordTable.getTokenType(text);
        return new Token(type, text, startLine, startCol);
    }
}