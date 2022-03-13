/**
 * @Authors
 * Thomas BECHET
 * Helloïs BARBOSA
 */

package com.sma;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;

public class Main extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {

        // Create scene
        VisualBoard board = new VisualBoard();
        Scene scene = new Scene(board.getGridPane(), 1000, 1000);
        stage.setScene(scene);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                Platform.exit();
                System.exit(0);
            }
        });
        stage.show();

        // Create environment and update
        Environment environment = new Environment(board);
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1), (event) -> {
            for (int i = 0; i < 50; i++) {
                environment.update();
            }
            environment.updateView();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.playFromStart();
    }

    public static void main(String[] args) {
        launch();
    }
}