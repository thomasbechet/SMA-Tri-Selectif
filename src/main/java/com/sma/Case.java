package com.sma;

import java.util.ArrayList;
import java.util.List;

public class Case {
    public Fruit fruit;
    public List<Agent> agents;
    public int position;
    public float signal;

    public Case(int p) {
        this.agents = new ArrayList<>();
        this.fruit = null;
        this.position = p;
        this.signal = 0.0f;
    }
}