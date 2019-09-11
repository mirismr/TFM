/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmr.application;

import java.util.ArrayList;

/**
 *
 * @author mirismr
 */
public class LabelGroup {
    private ArrayList<String> labels;
    
    public LabelGroup(ArrayList<String> labels) {
        this.labels = labels;
    }
    
    public ArrayList<String> getLabels() {
        return this.labels;
    }
    
    public String toString() {
        String result = "[";
        for(String s: this.labels){
            result += s+", ";
        }
        result = result.substring(0, result.length()-2);
        result += "]";
        return result;
    }
}
