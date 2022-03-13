/**
 * @Authors
 * Thomas BECHET
 * Helloïs BARBOSA
 */

package com.sma;

public class AgentDropPickError extends Agent {

    public final static float ERROR_RATE = 0.1f;

    @Override
    protected float computeFruitFrequency(Fruit f) {
        int validCount = 0;
        int invalidCount = 0;
        for (int i = 0; i < MEMORY_SIZE; i++) {
            if (this.memory[i] != null) {
                // Check same type
                if (this.memory[i].getClass().equals(f.getClass())) {
                    validCount++;
                } else {
                    invalidCount++;
                }
            }
        }
        return (((float)validCount + ((float)invalidCount * ERROR_RATE)) / (float)MEMORY_SIZE);
    }
}