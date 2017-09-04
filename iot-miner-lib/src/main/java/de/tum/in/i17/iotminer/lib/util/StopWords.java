package de.tum.in.i17.iotminer.lib.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class StopWords {
    private String[] defaultStopWords = {"i", "a", "about", "an",
            "are", "as", "at", "be", "by", "com", "for", "from", "how",
            "in", "is", "it", "of", "on", "or", "that", "the", "this",
            "to", "was", "what", "when", "where", "who", "will", "with"};
    private static HashSet stopWords = new HashSet();

    public StopWords() {
        stopWords.addAll(Arrays.asList(defaultStopWords));
    }

    public String[] removeStopWords(String[] words) {
        ArrayList<String> tokens =
                new ArrayList<String>(Arrays.asList(words));
        for (int i = 0; i < tokens.size(); i++) {
            if (stopWords.contains(tokens.get(i))) {
                tokens.remove(i);
            }
        }
        return (String[]) tokens.toArray(
                new String[tokens.size()]);
    }

}
