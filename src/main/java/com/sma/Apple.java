package com.sma;

import javafx.scene.paint.Color;

public class Apple extends Fruit {
    @Override
    public Color getColor() {
        return Color.RED;
    }

    @Override
    public int requiredAgentCount() {
        return 1;
    }
}
