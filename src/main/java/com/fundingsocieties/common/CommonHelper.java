package com.fundingsocieties.common;

import lombok.SneakyThrows;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonHelper {
    /**
     * Get match group
     *
     * @param input    Input text
     * @param regex    Regex
     * @param groupIdx 0 mean the whole test, 1 is first group, 2 is second group and so on
     * @return matched group
     */
    public static String getMatchGroup(final String input, final String regex, final int groupIdx) {
        String result = null;
        final Matcher m = getMatcher(input, regex);
        if (m.find()) {
            result = m.group(groupIdx);
        }
        return result;
    }

    private static Matcher getMatcher(final String input, final String regex) {
        final Pattern r = Pattern.compile(regex);
        return r.matcher(input);
    }

    @SneakyThrows
    public static void sleep(final int timeInSeconds) {
        Thread.sleep(timeInSeconds * 1000L);
    }

    /**
     * Convert hex to RGB
     *
     * @param colorStr e.g. "#FFFFFF"
     * @return
     */
    public static Color hex2Rgb(final String colorStr) {
        return new Color(
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
    }

    /**
     * Convert String rgb to Color
     *
     * @param colorStr e.g. "rgb(255,181,13)"
     * @return
     */
    public static Color rgbFromString(final String colorStr) {
        final String regex = "rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)";
        final Matcher m = getMatcher(colorStr, regex);
        if (m.matches()) {
            return new Color(Integer.parseInt(m.group(1)),  // r
                    Integer.parseInt(m.group(2)),  // g
                    Integer.parseInt(m.group(3))); // b
        }
        return null;
    }
}
