package compiler.lexer.nfa;

import java.util.function.Predicate;

public class Transition {
    public static final Predicate<Character> EPSILON = c -> c == '\0';

    public final Predicate<Character> condition;
    private final State targetState;

    public Transition(Predicate<Character> condition, State targetState) {
        this.condition = condition;
        this.targetState = targetState;
    }

    public boolean matches(char c) {
        return condition.test(c);
    }

    public State getTargetState() {
        return targetState;
    }
}