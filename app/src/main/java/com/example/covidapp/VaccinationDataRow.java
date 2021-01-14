package com.example.covidapp;

public class VaccinationDataRow implements Comparable<VaccinationDataRow> {
    private String country;
    private double value;

    public VaccinationDataRow(String country, double value) {
        this.country = country;
        this.value = value;
    }

    public double getValue(){
        return value;
    }

    public String getCountry(){
        return country;
    }

    @Override
    public int compareTo(VaccinationDataRow o) {
        double value = (double) o.getValue();
        /* For Ascending order*/
        double thisv = this.value;
        return (int) value * 1000 - (int) thisv * 1000;
    }

}
