package com.yoshtec.owl.testclasses.brain;

import com.yoshtec.owl.annotations.OwlClass;
import com.yoshtec.owl.annotations.OwlDataProperty;


@OwlClass
public class Brain {

    @OwlDataProperty
    protected Float weight;

    public Float getWeight() {
        return this.weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }
    
}
