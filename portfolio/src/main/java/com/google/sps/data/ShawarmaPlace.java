package com.google.sps.data;

/**
 * Represents a shawarma place (it's location, name and description about its shawarmas).
 * This class is only used by GSON with the help of reflection.
 * @author Olga Shimanskaia <olgashimanskaia@gmail.com>
 */
public class ShawarmaPlace {
    private double latitude;
    private double longitude;
    private String name;
    private String description;
}