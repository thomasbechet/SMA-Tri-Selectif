package com.sma;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Agent {

    public enum State {
        MOVING,
        DROPPING,
        PICKING
    }

    public class Perception {
        public List<Case> view;
        public Case current;
        public Perception() {
            this.view = new ArrayList<>();
        }
    }

    public final static int MEMORY_SIZE = 10;
    public final static int VIEW_DISTANCE = 2;
    public final static float KP = 0.1f;
    public final static float KM = 0.3f;

    private State state;
    private Fruit[] memory;
    private int currentMemoryIndex;
    private Fruit fruit;

    public Agent() {
        this.state = State.MOVING;
        this.memory = new Fruit[MEMORY_SIZE];
        this.currentMemoryIndex = 0;
        this.fruit = null;
    }

    private void addMemory(Fruit fruit) {
        int index = this.currentMemoryIndex;
        this.memory[index] = fruit;
        this.currentMemoryIndex = (index + 1) % MEMORY_SIZE;
    }

    private float computeFruitFrequency(Fruit f) {
        int count = 0;
        for (int i = 0; i < MEMORY_SIZE; i++) {
            if (this.memory[i] != null) {
                // Check same type
                if (this.memory[i].getClass().equals(f.getClass())) {
                    count++;
                }
            }
        }
        return ((float)count / (float)MEMORY_SIZE);
    }

    public Perception perception(Environment env) {

        Perception perception = new Perception();

        // Find current position
        for (int i = 0; i < (Environment.SIZE_X * Environment.SIZE_Y); i++) {
            if (env.getCase(i).agent == this) {
                perception.current = env.getCase(i);
            }
        }

        // Find view cases
        int cx = Environment.toX(perception.current.position);
        int cy = Environment.toY(perception.current.position);
        for (int dir = -1; dir <= 1; dir += 2) {
            {
                int y = cy + VIEW_DISTANCE * dir;
                for (int x = (cx - VIEW_DISTANCE); x <= (cx + VIEW_DISTANCE); x++) {
                    if (Environment.isValidPosition(x, y)) {
                        Case c = env.getCase(Environment.to1D(x, y));
                        if (c != null) {
                            perception.view.add(c);
                        }
                    }
                }
            }
            {
                int x = cx + VIEW_DISTANCE * dir;
                for (int y = (cy - VIEW_DISTANCE + 1); y <= (cy + VIEW_DISTANCE - 1); y++) {
                    if (Environment.isValidPosition(x, y)) {
                        Case c = env.getCase(Environment.to1D(x, y));
                        if (c != null) {
                            perception.view.add(c);
                        }
                    }
                }
            }
        }

        return perception;
    }

    public void action(Perception perception, Environment env) {

        // State machine
        switch (this.state) {
            case MOVING -> {

                // Save memory
                addMemory(perception.current.fruit);

                // Filter case with agents
                List<Case> withoutAgents = perception.view.stream()
                        .filter(x -> x.agent == null).collect(Collectors.toList());
                Random random = new Random();
                Case randomCase = withoutAgents.get(random.nextInt(withoutAgents.size()));
                env.moveAgent(perception.current, randomCase);

                // Change state
                if (this.fruit == null) {
                    this.state = State.PICKING;
                } else {
                    this.state = State.DROPPING;
                }
            }
            case DROPPING -> {

                // Check it has object
                if (perception.current.fruit == null) {
                    // Compute probability
                    final float f = computeFruitFrequency(this.fruit);
                    float p = (f / ((float)KM + f));
                    p *= p;
                    Random random = new Random();
                    // Check random
                    if (random.nextFloat() <= p) {
                        // Drop the fruit
                        env.dropFruit(perception.current, this.fruit);
                        this.fruit = null;
                    }
                }

                // Reset state
                this.state = State.MOVING;
            }
            case PICKING -> {

                // Check if it has no object
                if (perception.current.fruit != null) {
                    // Compute probability
                    float p = (KP / (KP + computeFruitFrequency(perception.current.fruit)));
                    p *= p;
                    Random random = new Random();
                    // Check random
                    if (random.nextFloat() <= p) {
                        // Add to memory
                        this.fruit = perception.current.fruit;
                        // Pick fruit from environment
                        env.pickFruit(perception.current);
                    }
                }

                // Reset state
                this.state = State.MOVING;
            }
        }
    }
}
