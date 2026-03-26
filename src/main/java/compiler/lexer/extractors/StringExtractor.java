package compiler.lexer.extractors;

import compiler.lexer.nfa.NFA;
import compiler.lexer.nfa.State;
import compiler.lexer.nfa.Transition;
import compiler.lexer.nfa.visualizer.NFAVisualizer;
import compiler.lexer.util.CharReader;
import compiler.util.Token;
import compiler.util.TokenType;

public class StringExtractor implements TokenExtractor {
    private final NFA nfa;

    public StringExtractor() {
        State strS0 = new State(false);
        State strS1 = new State(false);
        State strS2 = new State(true);
        
        strS0.addTransition(new Transition(c -> c == '"', strS1, "\\\""));
        strS1.addTransition(new Transition(c -> c != '"', strS1, "[^\\\"]"));
        strS1.addTransition(new Transition(c -> c == '"', strS2, "\\\""));
        
        this.nfa = new NFA(strS0);
        NFAVisualizer.generateHTML(nfa, "string_nfa_graph.html");
    }

    @Override
    public boolean canHandle(char c) {
        return c == '"';
    }

    @Override
    public Token extract(CharReader reader, int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        
        while (!reader.isAtEnd()) {
            sb.append(reader.peek());
            if (nfa.isAlive(sb.toString())) {
                reader.advance();
            } else {
                sb.deleteCharAt(sb.length() - 1);
                break;
            }
        }
        
        String text = sb.toString();
        if (!nfa.matches(text)) {
            throw new RuntimeException("Lexical Error: Unterminated string at line " + startLine);
        }
        
        return new Token(TokenType.STRING, text, startLine, startCol);
    }
}