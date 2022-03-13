/**
 * @Authors
 * Thomas BECHET
 * Helloïs BARBOSA
 */

package com.sma;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class VisualBoard {

    private StackPane[] panes;
    private GridPane gridPane;

    public VisualBoard() {

        // Initialize view gridpane
        this.gridPane = new GridPane();
        List<StackPane> stackPanes = new ArrayList<StackPane>();
        this.panes = new StackPane[Environment.SIZE_X * Environment.SIZE_Y];
        for(int y = 0; y < Environment.SIZE_Y ; y++) {
            for(int x = 0; x < Environment.SIZE_X ; x++) {
                StackPane stackPane = new StackPane();
                float cellSize = 20.0f;
                stackPane.setMaxSize(cellSize, cellSize);
                stackPane.setMinSize(cellSize, cellSize);
                this.gridPane.add(stackPane, x, y);
                this.panes[y * Environment.SIZE_X + x] = stackPane;
                stackPanes.add(stackPane);
            }
        }
        this.gridPane.setGridLinesVisible(false);
        this.gridPane.autosize();
    }

    public void setCellColor(int x, int y, Color color) {
        this.panes[y * Environment.SIZE_X + x].setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
    }
    public void setCellRedColor(int x, int y, float v) {
        this.panes[y * Environment.SIZE_X + x].setBorder(new Border(new BorderStroke(Color.color(1.0, 0.0f, 0.0f, Math.max(0.1f, v)), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

    public GridPane getGridPane() {
        return this.gridPane;
    }
}