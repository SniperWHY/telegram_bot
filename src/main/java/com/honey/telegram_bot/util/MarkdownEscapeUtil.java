package com.honey.telegram_bot.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MarkdownEscapeUtil {
    public static String escapeMarkdown(String text) {
        Set<Character> specialChars = new HashSet<>(Arrays.asList('\\', '_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '.', '!'));
        StringBuilder escapedText = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (specialChars.contains(c)) {
                escapedText.append('\\');
            }
            escapedText.append(c);
        }

        return escapedText.toString();
    }
}
