package de.corey.challenges.utils;

import java.util.function.Consumer;

public class Utils {

    private static String fill(boolean left, char filler, int length, String string) {
        StringBuilder stringBuilder = new StringBuilder(string);
        while (stringBuilder.length() < length) {
            if (left) {
                stringBuilder.insert(0, filler);
            } else {
                stringBuilder.append(filler);
            }
        }
        return stringBuilder.toString();
    }

    public static String fillLeft(char filler, int length, String string) {
        return fill(true, filler, length, string);
    }

    public static String fillRight(char filler, int length, String string) {
        return fill(false, filler, length, string);
    }
}
