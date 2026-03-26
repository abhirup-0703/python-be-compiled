package compiler.parser.util.statement;

import compiler.parser.util.expression.Expression;

public record Assignment(
    String variableName, 
    Expression value
) implements Statement {}