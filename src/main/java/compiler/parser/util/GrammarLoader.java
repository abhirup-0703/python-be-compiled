package compiler.parser.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import compiler.parser.grammar.Grammar;
import compiler.parser.grammar.NonTerminal;
import compiler.parser.grammar.Production;
import compiler.parser.grammar.Symbol;
import compiler.parser.grammar.Terminal;
import compiler.util.TokenType;

public class GrammarLoader {

    /**
     * Orchestrates the loading of the entire grammar from configuration files.
     * Terminals are automatically loaded from the TokenType enum.
     */
    public static Grammar load(String nonTerminalsFile, String grammarFile) throws IOException {
        Map<String, Terminal> terminals = loadTerminalsFromEnum();
        Map<String, NonTerminal> nonTerminals = loadNonTerminals(Path.of(nonTerminalsFile));

        // Assume the first non-terminal in the file is the Start Symbol
        String startSymbolName = Files.readAllLines(Path.of(nonTerminalsFile)).stream()
                .filter(line -> !line.trim().isEmpty() && !line.startsWith("#"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Non-terminals config is empty"));

        Grammar grammar = new Grammar(nonTerminals.get(startSymbolName));
        loadProductions(Path.of(grammarFile), grammar, terminals, nonTerminals);

        return grammar;
    }

    /**
     * Automatically maps every TokenType defined in the enum to a Terminal object.
     */
    private static Map<String, Terminal> loadTerminalsFromEnum() {
        Map<String, Terminal> map = new HashMap<>();
        for (TokenType type : TokenType.values()) {
            map.put(type.name(), new Terminal(type));
        }
        return map;
    }

    private static Map<String, NonTerminal> loadNonTerminals(Path path) throws IOException {
        Map<String, NonTerminal> map = new HashMap<>();
        if (!Files.exists(path)) return map;

        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("//")) continue;
            map.put(line, new NonTerminal(line));
        }
        return map;
    }

    private static void loadProductions(Path path, Grammar grammar, Map<String, Terminal> terminals, Map<String, NonTerminal> nonTerminals) throws IOException {
        if (!Files.exists(path)) return;

        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("//")) continue;

            String[] parts = line.split("->");
            if (parts.length < 1 || parts.length > 2) {
                throw new IllegalArgumentException("Invalid production format (must contain '->'): " + line);
            }

            String lhsName = parts[0].trim();
            NonTerminal lhs = nonTerminals.get(lhsName);
            if (lhs == null) throw new IllegalArgumentException("Unknown LHS non-terminal in grammar: " + lhsName);

            // Handle empty RHS (epsilon production where nothing follows '->')
            String rhsString = parts.length == 2 ? parts[1] : "";

            // Split multiple right-hand sides by '|'
            String[] rhsOptions = rhsString.split("\\|");

            for (String rhsOption : rhsOptions) {
                String[] rhsNames = rhsOption.trim().split("\\s+");
                List<Symbol> rhs = new ArrayList<>();

                // Check for Epsilon (empty string, "ε", or "epsilon")
                if (rhsNames.length == 1 && (rhsNames[0].isEmpty() || rhsNames[0].equals("ε") || rhsNames[0].equalsIgnoreCase("epsilon"))) {
                    // Epsilon production: leave the RHS list empty
                } else {
                    for (String symName : rhsNames) {
                        if (symName.isEmpty()) continue;

                        if (terminals.containsKey(symName)) {
                            rhs.add(terminals.get(symName));
                        } else if (nonTerminals.containsKey(symName)) {
                            rhs.add(nonTerminals.get(symName));
                        } else {
                            throw new IllegalArgumentException("Unknown symbol in production RHS: '" + symName + "' in rule: " + line);
                        }
                    }
                }

                // Add each variation as its own discrete production to the Grammar
                grammar.addProduction(new Production(lhs, rhs));
            }
        }
    }
}