package compiler.parser.grammar;

import java.util.ArrayList;
import java.util.List;

public class Grammar {
    private final List<Production> productions;
    private final NonTerminal startSymbol;
    
    private final NonTerminal augmentedStart;

    public Grammar(NonTerminal startSymbol) {
        this.productions = new ArrayList<>();
        this.startSymbol = startSymbol;
        
        this.augmentedStart = new NonTerminal(startSymbol.getName() + "'");
    }

    public void addProduction(Production production) {
        productions.add(production);
    }

    public List<Production> getProductions() {
        return productions;
    }

    public NonTerminal getStartSymbol() {
        return startSymbol;
    }

    public NonTerminal getAugmentedStart() {
        return augmentedStart;
    }
    
    public List<Production> getProductionsFor(NonTerminal nonTerminal) {
        List<Production> matching = new ArrayList<>();
        for (Production p : productions) {
            if (p.getLeftHandSide().equals(nonTerminal)) {
                matching.add(p);
            }
        }
        return matching;
    }
}