/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 * Object for counting in {@link computations.ClassicWordCounter}
 *
 * @deprecated Replaced be Hashmap in new {@link computations.WordCounter}
 * @author Karsten Brandt
 * @author Martin Stoffers
 */
public class ObjectCounter {

    /**
     * Contains a String
     */
    public final String name;

    /**
     * Contains the current count
     */
    public long count;

    /**
     * Constructs a new ObjectCounter object
     * {@link model.ObjectCounter#count} is set to 1
     *
     * @param name Word from sentence
     */
    public ObjectCounter(String name){
        this.name=name;
        this.count=1L;
    }
}
