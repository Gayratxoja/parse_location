package com.example.andrea.myapplication;

/**
 * Created by Andrea on 26.09.2015.
 */
public class Places {

    public Double getLongi() {
        return longi;
    }

    public Double getMag() {
        return mag;
    }

    public Integer getNumberOfvisits() {
        return numberOfvisits;
    }

    public String getName() {
        return name;
    }

    private Double longi, mag;
    Integer numberOfvisits;
    private String name;
    public Places(String name, Double longi, Double mag, Integer numberOfvisits) {
        this.longi = longi;
        this.mag = mag;
        this.name = name;
        this.numberOfvisits = numberOfvisits;

    }
    }


