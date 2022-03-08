package com.sma;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Environment {

    public final static int INVALID_POSITION = -1;
    public final static int SIZE_X = 50;
    public final static int SIZE_Y = 50;
    public final static int BANANA_COUNT = 200;
    public final static int APPLE_COUNT = 200;
    public final static int AGENT_COUNT = 20;

    public static int toX(int p) {
        return p % SIZE_X;
    }
    public static int toY(int p) {
        return p / SIZE_X;
    }
    public static boolean isValidPosition(int x, int y) {
        if (x < 0 || x >= SIZE_X || y < 0 || y >= SIZE_Y) return false;
        return true;
    }
    public static int to1D(int x, int y) {
        if (!isValidPosition(x, y)) return INVALID_POSITION;
        return y * SIZE_X + x;
    }

    private VisualBoard view;
    private Case[] cases;
    private List<Fruit> fruits;
    private List<Agent> agents;

    public Environment(VisualBoard visualBoard) {
        this.view = visualBoard;
        this.fruits = new ArrayList<>();
        this.agents = new ArrayList<>();

        // Initialize board
        this.cases = new Case[SIZE_X * SIZE_Y];
        for (int i = 0; i < (SIZE_X * SIZE_Y); i++) {
            this.cases[i] = new Case();
            this.cases[i].agent = null;
            this.cases[i].fruit = null;
            this.cases[i].position = i;
        }

        // Generate free positions
        List<Integer> positions = new LinkedList<>();
        for (int i = 0; i < (SIZE_X * SIZE_Y); i++) {
            positions.add(i);
        }
        Random random = new Random();

        // Place random fruits
        for (int i = 0; i < APPLE_COUNT; i++) {
            int p = positions.remove(random.nextInt(positions.size()));
            Fruit fruit = new Apple();
            this.fruits.add(fruit);
            this.cases[p].fruit = fruit;
        }
        for (int i = 0; i < BANANA_COUNT; i++) {
            int p = positions.remove(random.nextInt(positions.size()));
            Fruit fruit = new Banana();
            this.fruits.add(fruit);
            this.cases[p].fruit = fruit;
        }
        // Place agents
        for (int i = 0; i < AGENT_COUNT; i++) {
            int p = positions.remove(random.nextInt(positions.size()));
            Agent agent = new Agent();
            this.agents.add(agent);
            this.cases[p].agent = agent;
        }
    }

    private void updateView() {
        for (int i = 0; i < (SIZE_X * SIZE_Y); i++) {
            if (this.cases[i].fruit != null || this.cases[i].agent != null) {
                if (this.cases[i].agent != null) {
                    this.view.setCellColor(Environment.toX(i), Environment.toY(i), Color.BLUE);
                } else if (this.cases[i].fruit != null) {
                    this.view.setCellColor(Environment.toX(i), Environment.toY(i), this.cases[i].fruit.getColor());
                }
            } else {
                this.view.setCellColor(Environment.toX(i), Environment.toY(i), Color.WHITE);
            }
        }
    }

    public Case getCase(int position) {
        int x = toX(position);
        int y = toY(position);
        if (!isValidPosition(x, y)) return null;
        return this.cases[position];
    }

    public void moveAgent(Case src, Case dst) {
        if (src != dst && src.agent != null && dst.agent == null) {
            // TODO: check manhattan distance to ensure the agent is not cheating
            dst.agent = src.agent;
            src.agent = null;
        }
    }

    public void pickFruit(Case c) {
        if (c.fruit != null) {
            c.fruit = null;
        }
    }

    public void dropFruit(Case dst, Fruit fruit) {
        if (dst.fruit == null) {
            dst.fruit = fruit;
        }
    }

    private int it = 0;

    public void update() {
        for (int i = 0; i < AGENT_COUNT; i++) {
            // Pick random agent
//            Random random = new Random();
//            Agent a = this.agents.get(random.nextInt(this.agents.size()));

            Agent a = this.agents.get(i);

            // Update agent
            Agent.Perception perception = a.perception(this);
            a.action(perception, this);
        }

        if ((it++ % 100) == 0) {
            System.out.println("Iteration: " + it);
        }
        // Update board view
        updateView();
    }
}
