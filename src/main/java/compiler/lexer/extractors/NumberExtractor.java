package compiler.lexer.extractors;

import java.util.Set;

import compiler.lexer.nfa.NFA;
import compiler.lexer.nfa.State;
import compiler.lexer.nfa.Transition;
import compiler.lexer.nfa.visualizer.NFAVisualizer;
import compiler.lexer.util.CharReader;
import compiler.util.Token;
import compiler.util.TokenType;

public class NumberExtractor implements TokenExtractor {
    private final NFA nfa;
    private final State intAcceptState;
    private final State floatAcceptState;

    public NumberExtractor() {
        State numS0 = new State(false);
        this.intAcceptState = new State(true);  
        State numS2 = new State(false);         
        this.floatAcceptState = new State(true);
        
        numS0.addTransition(new Transition(Character::isDigit, intAcceptState, "[0-9]"));
        intAcceptState.addTransition(new Transition(Character::isDigit, intAcceptState, "[0-9]"));
        intAcceptState.addTransition(new Transition(c -> c == '.', numS2, "."));
        numS2.addTransition(new Transition(Character::isDigit, floatAcceptState, "[0-9]"));
        floatAcceptState.addTransition(new Transition(Character::isDigit, floatAcceptState, "[0-9]"));
        
        this.nfa = new NFA(numS0);
        NFAVisualizer.generateHTML(nfa, "number_nfa_graph.html");
    }

    @Override
    public boolean canHandle(char c) {
        return nfa.isAlive(String.valueOf(c));
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
        Set<State> finalStates = nfa.getAcceptedStates(text);
        
        if (finalStates.isEmpty()) {
            throw new RuntimeException("Lexical Error: Malformed number '" + text + "' at line " + startLine);
        }
        
        TokenType type = finalStates.contains(floatAcceptState) ? TokenType.FLOAT : TokenType.INTEGER;
        return new Token(type, text, startLine, startCol);
    }
}