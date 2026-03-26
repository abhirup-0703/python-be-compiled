package compiler.parser.grammar;

import compiler.lexer.util.TokenType;

public record Terminal(TokenType type) implements Symbol {
    @Override
    public String getName() {
        return type.name();
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public String toString() {
        return type.name();
    }
}