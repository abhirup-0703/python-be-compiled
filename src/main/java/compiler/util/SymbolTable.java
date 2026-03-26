package compiler.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {
    public record SymbolRecord(String lexeme, String type, int line, int column) {}

    // Using LinkedHashMap to preserve insertion order (first appearance)
    private final Map<String, SymbolRecord> table = new LinkedHashMap<>();

    public void addSymbol(String lexeme, int line, int column) {
        if (!table.containsKey(lexeme)) {
            table.put(lexeme, new SymbolRecord(lexeme, "IDENTIFIER", line, column));
        }
    }

    public void printTable() {
        System.out.println("\n=================================================================");
        System.out.println(String.format("|| %-20s | %-15s | %-6s | %-6s ||", "Lexeme / Symbol", "Token Type", "Line", "Col"));
        System.out.println("=================================================================");
        for (SymbolRecord record : table.values()) {
            System.out.println(String.format("|| %-20s | %-15s | %-6d | %-6d ||", 
                record.lexeme(), record.type(), record.line(), record.column()));
        }
        System.out.println("=================================================================\n");
    }
}