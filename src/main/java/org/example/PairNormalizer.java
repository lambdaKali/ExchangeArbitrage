package org.example;

public class PairNormalizer {
    public static String normalizePair(String pair) {
        return pair.toUpperCase().replace("-", "").replace("_", "");
    }
}
