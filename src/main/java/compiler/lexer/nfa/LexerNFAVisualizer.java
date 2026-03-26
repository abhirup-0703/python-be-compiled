package compiler.lexer.nfa;

import compiler.lexer.nfa.visualizer.NFAVisualizer;

public class LexerNFAVisualizer {
    
    /**
     * Constructs the theoretical NFAs defining our Lexical Rules 
     * and exports them to HTML files for visualization.
     */
    public static void buildAndVisualizeNFAs() {
        // 1. Identifier NFA [a-zA-Z_][a-zA-Z0-9_]*
        State idS0 = new State(false);
        State idS1 = new State(true);
        idS0.addTransition(new Transition(c -> Character.isLetter(c) || c == '_', idS1, "[a-zA-Z_]"));
        idS1.addTransition(new Transition(c -> Character.isLetterOrDigit(c) || c == '_', idS1, "[a-zA-Z0-9_]"));
        NFA identifierNFA = new NFA(idS0);
        NFAVisualizer.generateHTML(identifierNFA, "identifier_nfa_graph.html");

        // 2. Number NFA (Handles both Integer and Float)
        State numS0 = new State(false);
        State numS1 = new State(true);  // Accept Int
        State numS2 = new State(false); // Decimal point seen
        State numS3 = new State(true);  // Accept Float
        
        numS0.addTransition(new Transition(Character::isDigit, numS1, "[0-9]"));
        numS1.addTransition(new Transition(Character::isDigit, numS1, "[0-9]"));
        numS1.addTransition(new Transition(c -> c == '.', numS2, "."));
        numS2.addTransition(new Transition(Character::isDigit, numS3, "[0-9]"));
        numS3.addTransition(new Transition(Character::isDigit, numS3, "[0-9]"));
        
        NFA numberNFA = new NFA(numS0);
        NFAVisualizer.generateHTML(numberNFA, "number_nfa_graph.html");

        // 3. String Literal NFA
        State strS0 = new State(false);
        State strS1 = new State(false);
        State strS2 = new State(true);
        strS0.addTransition(new Transition(c -> c == '"', strS1, "\\\""));
        strS1.addTransition(new Transition(c -> c != '"', strS1, "[^\\\"]"));
        strS1.addTransition(new Transition(c -> c == '"', strS2, "\\\""));
        NFA stringNFA = new NFA(strS0);
        NFAVisualizer.generateHTML(stringNFA, "string_nfa_graph.html");
    }
}