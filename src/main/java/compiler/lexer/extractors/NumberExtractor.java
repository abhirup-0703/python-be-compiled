package compiler.lexer.extractors;

import compiler.lexer.nfa.NFA;
import compiler.lexer.nfa.State;
import compiler.lexer.nfa.Transition;
import compiler.lexer.nfa.visualizer.NFAVisualizer;
import compiler.lexer.util.CharReader;
import compiler.util.Token;
import compiler.util.TokenType;

public class NumberExtractor implements TokenExtractor {
    private final NFA nfa;

    public NumberExtractor() {
        State numS0 = new State(false);
        State numS1 = new State(true);  // Accept Int
        State numS2 = new State(false); // Decimal point seen
        State numS3 = new State(true);  // Accept Float
        
        numS0.addTransition(new Transition(Character::isDigit, numS1, "[0-9]"));
        numS1.addTransition(new Transition(Character::isDigit, numS1, "[0-9]"));
        numS1.addTransition(new Transition(c -> c == '.', numS2, "."));
        numS2.addTransition(new Transition(Character::isDigit, numS3, "[0-9]"));
        numS3.addTransition(new Transition(Character::isDigit, numS3, "[0-9]"));
        
        this.nfa = new NFA(numS0);
        NFAVisualizer.generateHTML(nfa, "number_nfa_graph.html");
    }

    @Override
    public boolean canHandle(char c) {
        return Character.isDigit(c);
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
            throw new RuntimeException("Lexical Error: Malformed number '" + text + "' at line " + startLine);
        }
        
        return new Token(text.contains(".") ? TokenType.FLOAT : TokenType.INTEGER, text, startLine, startCol);
    }
}