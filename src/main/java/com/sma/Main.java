package com.sma;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {

        // Create scene
        VisualBoard board = new VisualBoard();
        Scene scene = new Scene(board.getGridPane(), 500, 500);
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
            environment.update();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.playFromStart();
    }

    public static void main(String[] args) {
        launch();
    }
}