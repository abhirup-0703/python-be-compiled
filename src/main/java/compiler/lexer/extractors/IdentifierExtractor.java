package compiler.lexer.extractors;

import compiler.lexer.nfa.NFA;
import compiler.lexer.nfa.State;
import compiler.lexer.nfa.Transition;
import compiler.lexer.nfa.visualizer.NFAVisualizer;
import compiler.lexer.util.CharReader;
import compiler.lexer.util.KeywordTable;
import compiler.util.Token;
import compiler.util.TokenType;

public class IdentifierExtractor implements TokenExtractor {
    private final KeywordTable keywordTable;
    private final NFA nfa;

    public IdentifierExtractor(KeywordTable keywordTable) {
        this.keywordTable = keywordTable;
        
        // 1. Define the NFA natively
        State s0 = new State(false);
        State s1 = new State(true);
        s0.addTransition(new Transition(c -> Character.isLetter(c) || c == '_', s1, "[a-zA-Z_]"));
        s1.addTransition(new Transition(c -> Character.isLetterOrDigit(c) || c == '_', s1, "[a-zA-Z0-9_]"));
        this.nfa = new NFA(s0);
        
        // 2. Export Visualization
        NFAVisualizer.generateHTML(nfa, "identifier_nfa_graph.html");
    }

    @Override
    public boolean canHandle(char c) {
        return Character.isLetter(c) || c == '_';
    }

    @Override
    public Token extract(CharReader reader, int startLine, int startCol) {
        StringBuilder sb = new StringBuilder();
        
        // NFA Maximal Munch Loop
        while (!reader.isAtEnd()) {
            sb.append(reader.peek());
            if (nfa.isAlive(sb.toString())) {
                reader.advance();
            } else {
                sb.deleteCharAt(sb.length() - 1); // Remove the char that killed the NFA
                break;
            }
        }
        
        String text = sb.toString();
        if (!nfa.matches(text)) {
            throw new RuntimeException("Lexical Error: Invalid Identifier at line " + startLine);
        }
        
        return new Token(keywordTable.getTokenType(text), text, startLine, startCol);
    }
}