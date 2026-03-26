package compiler.lexer.extractors;

import compiler.lexer.nfa.NFA;
import compiler.lexer.nfa.State;
import compiler.lexer.nfa.Transition;
import compiler.lexer.nfa.visualizer.NFAVisualizer;
import compiler.lexer.util.CharReader;
import compiler.lexer.util.KeywordTable;
import compiler.util.Token;

public class IdentifierExtractor implements TokenExtractor {
    private final KeywordTable keywordTable;
    private final NFA nfa;

    public IdentifierExtractor(KeywordTable keywordTable) {
        this.keywordTable = keywordTable;
        
        State s0 = new State(false);
        State s1 = new State(true);
        s0.addTransition(new Transition(c -> Character.isLetter(c) || c == '_', s1, "[a-zA-Z_]"));
        s1.addTransition(new Transition(c -> Character.isLetterOrDigit(c) || c == '_', s1, "[a-zA-Z0-9_]"));
        this.nfa = new NFA(s0);
        
        NFAVisualizer.generateHTML(nfa, "identifier_nfa_graph.html");
    }

    @Override
    public boolean canHandle(char c) {
        // PURE NFA ROUTING: Ask the NFA if it survives the first character
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
        if (!nfa.matches(text)) {
            throw new RuntimeException("Lexical Error: Invalid Identifier at line " + startLine);
        }
        
        return new Token(keywordTable.getTokenType(text), text, startLine, startCol);
    }
}