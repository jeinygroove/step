package com.google.sps.data;

/**
 * Represents frequencies of some words from music album.
 * @author Olga Shimanskaia <olgashimanskaia@gmail.com>
 */
public class WordsFrequencies {
    /**
     * Represents a word with its frequency in some text.
     */
    private final class WordFrequency {
        private String word;
        private int frequency;
    }
    private String musician;
    private String albumTitle;
    private WordFrequency[] words;
}
