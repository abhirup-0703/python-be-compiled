package compiler.parser.util.statement;

import compiler.parser.util.expression.Expression;

public record ForStatement(
    String iteratorVariable,
    Expression iterable,
    java.util.List<Statement> body
) implements Statement {}