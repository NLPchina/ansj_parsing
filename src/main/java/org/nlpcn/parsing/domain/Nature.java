package org.nlpcn.parsing.domain;

import java.io.Serializable;

/**
 * Created by Ansj on 29/03/2017.
 */
public class Nature implements Serializable,Cloneable{

    private String name ;

    private double freq ;


    public Nature(String name, double freq ) {
        this.name = name;
        this.freq = freq;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getFreq() {
        return freq;
    }

    public void setFreq(double freq) {
        this.freq = freq;
    }

    @Override
    public String toString() {
        return name;
    }

}
