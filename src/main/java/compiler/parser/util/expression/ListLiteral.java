package compiler.parser.util.expression;

public record ListLiteral(java.util.List<Expression> elements) implements Expression {}