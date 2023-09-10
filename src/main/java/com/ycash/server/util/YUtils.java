package com.ycash.server.util;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class YUtils {

    public static String formatName(String name) {
        List<String> prepositions = Arrays.asList("de", "da", "do", "das", "dos");

        return Arrays.stream(name.trim().split("\\s+"))
//                .map(YUtils::normalizeAndRemoveSpecialCharacters)
                .map(YUtils::capitalizeFirstLetterAndLowerRest)
                .map(part -> (prepositions.contains(part.toLowerCase()) ? part.toLowerCase() : part))
                .collect(Collectors.joining(" "));
    }

//    private static String normalizeAndRemoveSpecialCharacters(String part) {
//        return Normalizer.normalize(part, Normalizer.Form.NFD)
//                .replaceAll("[^\\p{IsAlphabetic}\\s]", "");
//    }

    private static String capitalizeFirstLetterAndLowerRest(String part) {
        if (part.isEmpty()) {
            return part;
        }

        return part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase();
    }
}
