package com.sma;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Agent {

    public enum State {
        MOVING,
        DROPPING,
        PICKING,
        EMITTING
    }

    public class Perception {
        public List<Case> view;
        public Case current;
        public Perception() {
            this.view = new ArrayList<>();
        }
    }

    public final static int MEMORY_SIZE = 10;
    public final static int VIEW_DISTANCE = 1;
    public final static float KP = 0.1f;
    public final static float KM = 0.3f;
    public final static int GIVEUP_EMITTING_TICK = 50;

    private State state;
    protected Fruit[] memory;
    private int currentMemoryIndex;
    private Fruit fruit;
    private int tickCount;

    public Agent() {
        this.state = State.MOVING;
        this.memory = new Fruit[MEMORY_SIZE];
        this.currentMemoryIndex = 0;
        this.fruit = null;
        this.tickCount = 0;
    }

    public State getState() {
        return this.state;
    }

    public Fruit getFruit() {
        return this.fruit;
    }
    public void setFruit(Fruit fruit) {
        this.fruit = fruit;
    }

    public void notifyPicking() {
        this.state = State.PICKING;
    }

    private void addMemory(Fruit fruit) {
        int index = this.currentMemoryIndex;
        this.memory[index] = fruit;
        this.currentMemoryIndex = (index + 1) % MEMORY_SIZE;
    }

    protected float computeFruitFrequency(Fruit f) {
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
            if (env.getCase(i).agents.contains(this)) {
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

                // Filter cases
                List<Case> validCases = perception.view.stream()
                        .filter(x -> {
                            // dst has agent
                            if (x.agents.size() > 0) {
                                // dst has fruit
                                if (x.fruit != null) {
                                    // src is solo
                                    if (perception.current.agents.size() == 1) {
                                        // has enough space
                                        if (x.agents.size() + 1 <= x.fruit.requiredAgentCount()) {
                                            return true;
                                        }
                                    // src is group
                                    } else {
                                        return false;
                                    }
                                // dst has no fruit
                                } else {
                                    return false;
                                }
                            // dst has no agent
                            } else {
                                // dst has fruit
                                if (x.fruit != null) {
                                    // src is solo
                                    if (perception.current.agents.size() == 1) {
                                        return true;
                                    // src is group
                                    } else {
                                        return false;
                                    }
                                // dst has no fruit
                                } else {
                                    // src is solo
                                    if (x.agents.size() == 1) {
                                        return true;
                                    // src is group
                                    } else {
                                        return true;
                                    }
                                }
                            }

                            return false;
                        })
                        .collect(Collectors.toList());

                // It has possible movement
                if (!validCases.isEmpty()) {

                    // Pick a random case
                    Random random = new Random();
                    Case randomCase = validCases.get(random.nextInt(validCases.size()));
                    env.moveAgent(this, perception.current, randomCase);

                    // Check if it should start to emit
                    if (this.fruit == null) {
                        // Check emitting only if a group is required
                        if (randomCase.fruit != null && randomCase.fruit.requiredAgentCount() > 1) {
                            this.state = State.EMITTING;
                        }
                    }

                    // Check if it should start to pick
                    if (randomCase.fruit != null && this.fruit == null) {
                        this.state = State.PICKING;
                    } else if (this.fruit != null) {
                        this.state = State.DROPPING;
                    }

                }
            }
            case DROPPING -> {

                // Case has no fruit
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
                        // Pick fruit from environment
                        env.pickFruit(perception.current);
                    }
                }

                // Reset state
                this.state = State.MOVING;
            }
            case EMITTING -> {

                // The agent has no fruit
                if (this.fruit == null) {

                    // The group is completed, pick the fruit
                    if (perception.current.agents.size() == perception.current.fruit.requiredAgentCount()) {
                        // Pick the fruit
                        env.pickFruit(perception.current);
                        // Change state of all group
                        for (Agent a : perception.current.agents) {
                            a.state = State.MOVING;
                        }

                    // Continue to emit or give up
                    } else {

                        // Emit
                        if (this.tickCount < GIVEUP_EMITTING_TICK) {
                            if (this.tickCount % 5 == 0) {
                                // Emit signal
                                env.emitSignal(perception.current);
                            }
                            this.tickCount++;

                        // Give up
                        } else {
                            this.tickCount = 0;
                            this.state = State.MOVING;
                        }
                    }
                }
            }
        }
    }
}
