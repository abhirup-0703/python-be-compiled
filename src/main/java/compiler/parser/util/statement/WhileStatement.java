package compiler.parser.util.statement;

import compiler.parser.util.expression.Expression;

public record WhileStatement(
    Expression condition,
    java.util.List<Statement> body
) implements Statement {}