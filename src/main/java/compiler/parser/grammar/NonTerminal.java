package compiler.parser.grammar;

public record NonTerminal(String name) implements Symbol {
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}