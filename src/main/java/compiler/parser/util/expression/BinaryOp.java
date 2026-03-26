package compiler.parser.util.expression;

import compiler.lexer.util.Token;

public record BinaryOp(
    Expression left, 
    Token operator,
    Expression right
) implements Expression {}