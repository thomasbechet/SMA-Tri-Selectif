package com.sma;

import javafx.scene.paint.Color;

public class Kiwi extends Fruit {
    @Override
    public Color getColor() {
        return Color.GREEN;
    }

    @Override
    public int requiredAgentCount() {
        return 2;
    }
}
