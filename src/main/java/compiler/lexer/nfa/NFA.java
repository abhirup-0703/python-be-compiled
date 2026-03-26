package compiler.lexer.nfa;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class NFA {
    private final State startState;

    public NFA(State startState) {
        this.startState = startState;
    }

    // Simulates the NFA against an input string
    public boolean matches(String input) {
        Set<State> currentStates = getEpsilonClosure(Set.of(startState));

        for (char c : input.toCharArray()) {
            Set<State> nextStates = new HashSet<>();
            for (State state : currentStates) {
                for (Transition t : state.getTransitions()) {
                    if (t.matches(c) && t.condition != Transition.EPSILON) {
                        nextStates.add(t.getTargetState());
                    }
                }
            }
            currentStates = getEpsilonClosure(nextStates);
            if (currentStates.isEmpty()) return false;
        }

        return currentStates.stream().anyMatch(State::isAccepting);
    }

    private Set<State> getEpsilonClosure(Set<State> states) {
        Set<State> closure = new HashSet<>(states);
        Stack<State> stack = new Stack<>();
        stack.addAll(states);

        while (!stack.isEmpty()) {
            State state = stack.pop();
            for (Transition t : state.getTransitions()) {
                if (t.condition == Transition.EPSILON && !closure.contains(t.getTargetState())) {
                    closure.add(t.getTargetState());
                    stack.push(t.getTargetState());
                }
            }
        }
        return closure;
    }
}