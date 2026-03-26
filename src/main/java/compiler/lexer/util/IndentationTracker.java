package compiler.lexer.util;

import compiler.util.Token;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import compiler.util.TokenType;

public class IndentationTracker {
    private final Stack<Integer> indentStack = new Stack<>();

    public IndentationTracker() {
        indentStack.push(0); // Base indentation level
    }

    public List<Token> processIndentation(int currentIndentLevel, int line, int column) {
        List<Token> tokens = new ArrayList<>();
        
        if (currentIndentLevel > indentStack.peek()) {
            indentStack.push(currentIndentLevel);
            tokens.add(new Token(TokenType.INDENT, "", line, column));
        } else {
            while (currentIndentLevel < indentStack.peek()) {
                indentStack.pop();
                tokens.add(new Token(TokenType.DEDENT, "", line, column));
            }
        }
        return tokens;
    }

    public List<Token> flushRemaining(int line, int column) {
        List<Token> tokens = new ArrayList<>();
        while (indentStack.size() > 1) {
            indentStack.pop();
            tokens.add(new Token(TokenType.DEDENT, "", line, column));
        }
        return tokens;
    }
}