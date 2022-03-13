/**
 * @Authors
 * Thomas BECHET
 * Helloïs BARBOSA
 */

package com.sma;

import javafx.scene.paint.Color;

public class Banana extends Fruit {
    @Override
    public Color getColor() {
        return Color.YELLOW;
    }

    @Override
    public int requiredAgentCount() {
        return 1;
    }
}