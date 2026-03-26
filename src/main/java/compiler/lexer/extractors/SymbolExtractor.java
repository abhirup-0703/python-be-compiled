package compiler.lexer.extractors;

import compiler.lexer.nfa.NFA;
import compiler.lexer.nfa.State;
import compiler.lexer.nfa.Transition;
import compiler.lexer.nfa.visualizer.NFAVisualizer;
import compiler.lexer.util.CharReader;
import compiler.util.Token;
import compiler.util.TokenType;

import java.util.HashMap;
import java.util.Map;

public class SymbolExtractor implements TokenExtractor {
    private final NFA nfa;
    private final Map<String, TokenType> symbolMap = new HashMap<>();

    public SymbolExtractor() {
        setupSymbolMap();
        
        State s0 = new State(false);
        State accept = new State(true);

        // Single character boundaries
        s0.addTransition(new Transition(c -> "+-()[]:,".indexOf(c) != -1, accept, "+-()[]:,"));

        // Multi-character logic (e.g. =, ==, !, !=, <, <=, >, >=)
        State eqBase = new State(true);
        s0.addTransition(new Transition(c -> "=<!>".indexOf(c) != -1, eqBase, "=< >!"));
        eqBase.addTransition(new Transition(c -> c == '=', accept, "="));

        // Math multi-characters (*, **, /, //)
        State starBase = new State(true);
        s0.addTransition(new Transition(c -> c == '*', starBase, "*"));
        starBase.addTransition(new Transition(c -> c == '*', accept, "*"));

        State slashBase = new State(true);
        s0.addTransition(new Transition(c -> c == '/', slashBase, "/"));
        slashBase.addTransition(new Transition(c -> c == '/', accept, "/"));

        this.nfa = new NFA(s0);
        NFAVisualizer.generateHTML(nfa, "symbol_nfa_graph.html");
    }

    private void setupSymbolMap() {
        symbolMap.put("+", TokenType.PLUS); symbolMap.put("-", TokenType.MINUS);
        symbolMap.put("(", TokenType.LPAREN); symbolMap.put(")", TokenType.RPAREN);
        symbolMap.put("[", TokenType.LBRACKET); symbolMap.put("]", TokenType.RBRACKET);
        symbolMap.put(":", TokenType.COLON); symbolMap.put(",", TokenType.COMMA);
        symbolMap.put("=", TokenType.ASSIGN); symbolMap.put("==", TokenType.EQUALITY);
        symbolMap.put(">", TokenType.GREATER_THAN); symbolMap.put(">=", TokenType.GREATER_EQUALS);
        symbolMap.put("<", TokenType.LESS_THAN); symbolMap.put("<=", TokenType.LESS_EQUALS);
        symbolMap.put("!", TokenType.UNKNOWN); symbolMap.put("!=", TokenType.NOT_EQUALS);
        symbolMap.put("*", TokenType.STAR); symbolMap.put("**", TokenType.DOUBLE_STAR);
        symbolMap.put("/", TokenType.SLASH); symbolMap.put("//", TokenType.DOUBLE_SLASH);
    }

    @Override
    public boolean canHandle(char c) {
        return true; // Catch-all for remaining symbols
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
        if (!nfa.matches(text) || !symbolMap.containsKey(text)) {
            // Failsafe for complete junk characters
            return new Token(TokenType.UNKNOWN, String.valueOf(reader.advance()), startLine, startCol);
        }
        
        return new Token(symbolMap.get(text), text, startLine, startCol);
    }
}