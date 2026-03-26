package compiler.parser.util.statement;

import compiler.parser.util.expression.Expression;

public record IfStatement(
    Expression condition, 
    java.util.List<Statement> trueBlock, 
    java.util.List<Statement> elseBlock
) implements Statement {}