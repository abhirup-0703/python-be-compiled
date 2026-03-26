package compiler.parser.grammar;

import java.util.List;

public class Production {
    private final NonTerminal leftHandSide;
    private final List<Symbol> rightHandSide;

    public Production(NonTerminal leftHandSide, List<Symbol> rightHandSide) {
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
    }

    public NonTerminal getLeftHandSide() {
        return leftHandSide;
    }

    public List<Symbol> getRightHandSide() {
        return rightHandSide;
    }

    public boolean isEpsilon() {
        return rightHandSide.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(leftHandSide.getName()).append(" -> ");
        if (rightHandSide.isEmpty()) {
            sb.append("ε");
        } else {
            for (Symbol s : rightHandSide) {
                sb.append(s.getName()).append(" ");
            }
        }
        return sb.toString().trim();
    }
}