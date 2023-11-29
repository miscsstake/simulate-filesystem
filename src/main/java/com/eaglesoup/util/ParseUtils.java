package com.eaglesoup.util;

import java.util.ArrayList;
import java.util.List;

public class ParseUtils {
    public static String[] parseCommand(String command) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        String insideQuotes = null;
        boolean escaped = false;

        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);

            if (escaped) {
                if (c == 'n') {
                    currentToken.append('\n');
                } else if (c == 't') {
                    currentToken.append('\t');
                } else {
                    currentToken.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"' || c == '\'') {
                if (insideQuotes != null && insideQuotes.equals(String.valueOf(c))) {
                    insideQuotes = null;
                } else if (insideQuotes == null) {
                    insideQuotes = String.valueOf(c);
                } else {
                    currentToken.append(c);
                }
            } else if (insideQuotes == null && (c == ' ' || c == '\n' || c == '>' || c == '|' || c == '<')) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                if (c != ' ') {
                    if (i + 1 < command.length() && command.charAt(i + 1) == '>') {
                        tokens.add(">>");
                        i++; // Skip the next character
                    } else {
                        tokens.add(String.valueOf(c));
                    }
                }
            } else {
                currentToken.append(c);
            }
        }

        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens.toArray(new String[0]);
    }

    public static List<String[]> pipeCommand(String[] args) {
        List<String[]> commands = new ArrayList<>();
        List<String> command = new ArrayList<>();
        for (String p : args) {
            if (p.equals("|")) {
                if (command.size() > 0) {
                    commands.add(command.toArray(new String[0]));
                    command.clear();
                }
            } else {
                command.add(p);
            }
        }
        commands.add(command.toArray(new String[0]));
        return commands;
    }

    public static String byte2Str(byte[] name) {
        StringBuilder sb = new StringBuilder();
        for (byte b : name) {
            if (b == 0) {
                break;
            }
            sb.append((char) (b & 0xFF));
        }
        return sb.toString();
    }
}
