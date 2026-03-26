package compiler.parser.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import compiler.parser.grammar.NonTerminal;
import compiler.parser.grammar.Production;
import compiler.parser.grammar.Terminal;
import compiler.parser.table.Action;
import compiler.parser.table.ParsingTable;

public class ParsingTableFormatter {

    public static void writeTableToFile(ParsingTable table, String filename) throws IOException {
        Set<Terminal> terminals = new TreeSet<>(Comparator.comparing(Terminal::getName));
        Set<NonTerminal> nonTerminals = new TreeSet<>(Comparator.comparing(NonTerminal::getName));
        int maxState = 0;

        // Extract all symbols and find max state
        for (Map.Entry<Integer, Map<Terminal, Action>> entry : table.getActionTable().entrySet()) {
            maxState = Math.max(maxState, entry.getKey());
            terminals.addAll(entry.getValue().keySet());
        }
        for (Map.Entry<Integer, Map<NonTerminal, Integer>> entry : table.getGotoTable().entrySet()) {
            maxState = Math.max(maxState, entry.getKey());
            nonTerminals.addAll(entry.getValue().keySet());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Write HTML Head and CSS
            writer.write("<!DOCTYPE html>\n<html>\n<head>\n");
            writer.write("<meta charset=\"UTF-8\">\n");
            writer.write("<title>CLR(1) Parsing Table</title>\n");
            writer.write("<style>\n");
            writer.write("  body { font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8f9fa; color: #333; margin: 20px; }\n");
            writer.write("  h1 { text-align: center; color: #2c3e50; }\n");
            writer.write("  .table-container { overflow: auto; max-height: 85vh; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); }\n");
            writer.write("  table { border-collapse: collapse; width: max-content; min-width: 100%; background: #fff; }\n");
            writer.write("  th, td { padding: 10px 15px; border: 1px solid #e0e0e0; text-align: center; }\n");
            writer.write("  th { background-color: #2c3e50; color: #fff; position: sticky; top: 0; z-index: 10; }\n");
            writer.write("  th.header-top { top: 0; border-bottom: 2px solid #fff; }\n");
            writer.write("  th.header-bottom { top: 41px; box-shadow: 0 2px 2px -1px rgba(0,0,0,0.4); }\n");
            writer.write("  .header-action { background-color: #34495e; }\n");
            writer.write("  .header-goto { background-color: #2980b9; }\n");
            writer.write("  tr:nth-child(even) { background-color: #fbfcfc; }\n");
            writer.write("  tr:hover { background-color: #eaf2f8; }\n");
            writer.write("  .state-col { font-weight: bold; background-color: #ecf0f1 !important; position: sticky; left: 0; z-index: 15; color: #2c3e50; border-right: 2px solid #bdc3c7; }\n");
            writer.write("  th.state-col { z-index: 20; background-color: #1a252f !important; border-right: none; }\n");
            writer.write("  .action-shift { color: #2980b9; font-weight: 600; }\n");
            writer.write("  .action-reduce { color: #c0392b; font-weight: 600; }\n");
            writer.write("  .action-accept { color: #27ae60; font-weight: bold; }\n");
            writer.write("  .goto-cell { color: #8e44ad; font-weight: 600; }\n");
            writer.write("  .divider { border-left: 2px solid #2c3e50; }\n");
            writer.write("</style>\n</head>\n<body>\n");

            writer.write("<h1>CLR(1) Parsing Table</h1>\n");
            writer.write("<div class=\"table-container\">\n");
            writer.write("<table>\n");

            // --- Header Row 1 (Groups) ---
            writer.write("<thead>\n");
            writer.write("<tr>\n");
            writer.write("  <th class=\"state-col header-top\" rowspan=\"2\">State</th>\n");
            writer.write("  <th class=\"header-action header-top\" colspan=\"" + terminals.size() + "\">ACTION</th>\n");
            writer.write("  <th class=\"header-goto header-top divider\" colspan=\"" + nonTerminals.size() + "\">GOTO</th>\n");
            writer.write("</tr>\n");

            // --- Header Row 2 (Symbols) ---
            writer.write("<tr>\n");
            for (Terminal t : terminals) {
                writer.write("  <th class=\"header-bottom\">" + t.getName() + "</th>\n");
            }
            boolean firstGoto = true;
            for (NonTerminal nt : nonTerminals) {
                writer.write("  <th class=\"header-bottom" + (firstGoto ? " divider" : "") + "\">" + nt.getName() + "</th>\n");
                firstGoto = false;
            }
            writer.write("</tr>\n");
            writer.write("</thead>\n");

            // --- Body (States) ---
            writer.write("<tbody>\n");
            for (int i = 0; i <= maxState; i++) {
                if (!table.getActionTable().containsKey(i) && !table.getGotoTable().containsKey(i)) continue;

                writer.write("<tr>\n");
                writer.write("  <td class=\"state-col\">" + i + "</td>\n");

                // Print Actions
                for (Terminal t : terminals) {
                    Action action = table.getAction(i, t);
                    writer.write("  <td class=\"" + getActionCssClass(action) + "\">" + formatAction(action) + "</td>\n");
                }

                // Print Gotos
                firstGoto = true;
                for (NonTerminal nt : nonTerminals) {
                    Integer gotoState = table.getGoto(i, nt);
                    String gotoStr = (gotoState != null) ? String.valueOf(gotoState) : "";
                    writer.write("  <td class=\"goto-cell" + (firstGoto ? " divider" : "") + "\">" + gotoStr + "</td>\n");
                    firstGoto = false;
                }

                writer.write("</tr>\n");
            }
            writer.write("</tbody>\n");
            
            // Close HTML
            writer.write("</table>\n</div>\n</body>\n</html>");
        }
    }

    private static String getActionCssClass(Action action) {
        if (action == null || action.getType() == null) return "";
        return switch (action.getType()) {
            case SHIFT -> "action-shift";
            case REDUCE -> "action-reduce";
            case ACCEPT -> "action-accept";
            default -> "";
        };
    }

    private static String formatAction(Action action) {
        if (action != null && action.getType() != null) {
            return switch (action.getType()) {
                case SHIFT -> "s" + action.getTargetState();
                case REDUCE -> {
                    Production p = action.getRule();
                    yield "r(" + p.getLeftHandSide().getName() + " &rarr; " + (p.isEpsilon() ? "&epsilon;" : "...") + ")";
                }
                case ACCEPT -> "acc";
                default -> "";
            };
        }
        return "";
    }
}