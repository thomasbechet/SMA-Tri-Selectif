package com.sma;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Environment {

    public final static int INVALID_POSITION = -1;
    public final static float EVAPORATION_RATE = 0.0005f;

    public final static int SIZE_X = 30;
    public final static int SIZE_Y = 30;
    public final static int BANANA_COUNT = 50;
    public final static int APPLE_COUNT = 50;
    public final static int KIWI_COUNT = 100;
    public final static int AGENT_COUNT = 20;

//    public final static int SIZE_X = 4;
//    public final static int SIZE_Y = 4;
//    public final static int BANANA_COUNT = 0;
//    public final static int APPLE_COUNT = 0;
//    public final static int KIWI_COUNT = 3;
//    public final static int AGENT_COUNT = 2;

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
            this.cases[i] = new Case(i);
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
        for (int i = 0; i < KIWI_COUNT; i++) {
            int p = positions.remove(random.nextInt(positions.size()));
            Fruit fruit = new Kiwi();
            this.fruits.add(fruit);
            this.cases[p].fruit = fruit;
        }
        // Place agents
        for (int i = 0; i < AGENT_COUNT; i++) {
            int p = positions.remove(random.nextInt(positions.size()));
            // Select which type of agent to spawn
            Agent agent = new Agent();
//            Agent agent = new AgentDropPickError();
            this.agents.add(agent);
            this.cases[p].agents.add(agent);
        }
    }

    public void updateView() {
        for (int i = 0; i < (SIZE_X * SIZE_Y); i++) {
            this.view.setCellRedColor(Environment.toX(i), Environment.toY(i), Math.min(this.cases[i].signal, 1.0f));
            if (this.cases[i].fruit != null || !this.cases[i].agents.isEmpty()) {
                if (!this.cases[i].agents.isEmpty()) {
                    if (this.cases[i].agents.size() == 1) {
                        this.view.setCellColor(Environment.toX(i), Environment.toY(i), Color.BLUE);
                    } else {
                        this.view.setCellColor(Environment.toX(i), Environment.toY(i), Color.PURPLE);
                    }
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

    private void moveAgentSolo(Agent agent, Case src, Case dst) {
        dst.agents.add(agent);
        src.agents.remove(agent);
    }
    private void moveAgentGroup(Agent agent, Case src, Case dst) {
        for (Agent a : src.agents) {
            if (a.getFruit() == agent.getFruit()) {
                dst.agents.add(a);
            }
        }
        for (Agent a : dst.agents) {
            if (a.getFruit() == agent.getFruit()) {
                src.agents.remove(a);
            }
        }
    }

    public void moveAgent(Agent agent, Case src, Case dst) {

        // [  ] -> empty case
        // [X  ] -> case with fruit
        // [ A ] -> case with agent
        // [XA ] -> case with agent and fruit
        // [XA+] -> case with group agent and a fruit

        // [ A ] -> [  ] : Move (None)
        // [ A ] -> [X ] : Move (None)
        // [ A ] -> [ A] : Not Move
        // [ A ] -> [XA] : Move (if dst.agents.size() + 1 == dst.fruit.requiredAgentCount())
        // [XA ] -> [  ] : Move (None)
        // [XA ] -> [X ] : Move
        // [XA ] -> [ A] : Not Move
        // [XA ] -> [XA] : Not Move
        // [XA+] -> [  ] : Move (None)
        // [XA+] -> [X ] : Move (None)
        // [XA+] -> [ A] : Not Move
        // [XA+] -> [XA] : Not Move

        // TODO: check manhattan distance to ensure the agent is not cheating

        // dst has agent
        if (dst.agents.size() > 0) {
            // dst has fruit
            if (dst.fruit != null) {
                // src is solo
                if (src.agents.size() == 1) {
                    // has enough space
                    if (dst.agents.size() + 1 <= dst.fruit.requiredAgentCount()) {
                        // Move agent
                        moveAgentSolo(agent, src, dst);
                    }
                // src is group
                } else {
                    assert false; // Impossible
                }
            // dst has no fruit
            } else {
                assert false; // Impossible
            }
        // dst has no agent
        } else {
            // dst has fruit
            if (dst.fruit != null) {
                moveAgentGroup(agent, src, dst);
            // dst has no fruit
            } else {
                // src is solo
                if (src.agents.size() == 1) {
                    // Move agent
                    moveAgentSolo(agent, src, dst);
                // src is group
                } else {
                    // group has a fruit
                    if (src.agents.get(0).getFruit() != null) {
                        // Move the group or solo
                        moveAgentGroup(agent, src, dst);
                    // no group, when the group has dropped the fruit
                    } else {
                        // Move agent
                        moveAgentSolo(agent, src, dst);
                    }
                }
            }
        }
    }

    public void pickFruit(Case c) {
        if (c.fruit != null) {
            if (c.fruit.requiredAgentCount() == c.agents.size()) {
                for (Agent a : c.agents) {
                    a.setFruit(c.fruit);
                }
                c.fruit = null;
            }
        }
    }

    public List<Case> findViewCases(int p, int viewDistance) {
        List<Case> cases = new ArrayList<>();
        int cx = Environment.toX(p);
        int cy = Environment.toY(p);
        for (int dir = -1; dir <= 1; dir += 2) {
            {
                int y = cy + viewDistance * dir;
                for (int x = (cx - viewDistance); x <= (cx + viewDistance); x++) {
                    if (Environment.isValidPosition(x, y)) {
                        Case c = getCase(Environment.to1D(x, y));
                        if (c != null) {
                            cases.add(c);
                        }
                    }
                }
            }
            {
                int x = cx + viewDistance * dir;
                for (int y = (cy - viewDistance + 1); y <= (cy + viewDistance - 1); y++) {
                    if (Environment.isValidPosition(x, y)) {
                        Case c = getCase(Environment.to1D(x, y));
                        if (c != null) {
                            cases.add(c);
                        }
                    }
                }
            }
        }
        return cases;
    }

    public void emitSignal(Case src, float value, int distance) {
        for (int i = 1; i < distance+1; i++) {
            for (Case c : findViewCases(src.position, i)) {
                c.signal += value - (value / (float)distance);
            }
        }
    }

    public void dropFruit(Case dst, Fruit fruit) {
        if (dst.fruit == null) {
            for (Agent a : dst.agents) {
                a.setFruit(null);
            }
            dst.fruit = fruit;
        }
    }

    public void evaporation() {
        for (int i = 0; i < (SIZE_X * SIZE_Y); i++) {
            this.cases[i].signal -= this.cases[i].signal * EVAPORATION_RATE;
        }
    }

    private int it = 0;

    private int agentId = 0;
    public void update() {

        // Evaporation step
        evaporation();

        // Pick random agent
//        Random random = new Random();
//        Agent a = this.agents.get(random.nextInt(this.agents.size()));

        Agent a = this.agents.get(this.agentId);
        this.agentId = (this.agentId + 1) % this.agents.size();

        // Update agent
        Agent.Perception perception = a.perception(this);
        a.action(perception, this);

        if ((it++ % 10000) == 0) {
            System.out.println("Iteration: " + it);
        }
    }
}