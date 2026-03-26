package compiler.lexer.nfa;

import java.util.ArrayList;
import java.util.List;

public class State {
    private final int id;
    private boolean isAccepting;
    private final List<Transition> transitions;

    private static int idCounter = 0;

    public State(boolean isAccepting) {
        this.id = idCounter++;
        this.isAccepting = isAccepting;
        this.transitions = new ArrayList<>();
    }

    public void addTransition(Transition t) {
        this.transitions.add(t);
    }

    public List<Transition> getTransitions() { return transitions; }
    public boolean isAccepting() { return isAccepting; }
    public void setAccepting(boolean accepting) { isAccepting = accepting; }
    public int getId() { return id; }
}