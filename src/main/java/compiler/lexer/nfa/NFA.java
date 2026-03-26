package compiler.lexer.nfa;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class NFA {
    private final State startState;

    public NFA(State startState) {
        this.startState = startState;
    }

    public State getStartState() {
        return startState;
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

    /**
     * Checks if the given input string is a valid prefix.
     * Returns true if the NFA has not hit a dead end.
     */
    public boolean isAlive(String input) {
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
            if (currentStates.isEmpty()) return false; // The NFA died
        }
        return true;
    }

    /**
     * Runs the input string through the NFA and returns the set of 
     * accepting states the machine landed on.
     */
    public Set<State> getAcceptedStates(String input) {
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
            if (currentStates.isEmpty()) return new HashSet<>();
        }

        Set<State> accepted = new HashSet<>();
        for (State state : currentStates) {
            if (state.isAccepting()) accepted.add(state);
        }
        return accepted;
    }
}