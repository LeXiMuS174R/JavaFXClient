package javafx.game.client.sockets;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.game.client.controller.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class SocketClient {

    private Controller controller;

    private Socket client;

    private PrintWriter toServer; // поток, в который мы пишем сообщения для сервера
    private BufferedReader fromServer; // поток, из которого приходят сообщения с сервера

    public SocketClient(String host, int port, Controller controller) {
        try {
            this.controller = controller;
            client = new Socket(host, port); // подключаемся к серверу
            toServer = new PrintWriter(client.getOutputStream(), true);
            fromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));
            // запускаем побочный поток, который будет читать сообщения от сервера
            new Thread(receiverMessagesTask).start();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void sendMessage(String message) {
        toServer.println(message);
    }

    // задача для побочного потока, чтобы в асинхронном режиме читать сообщения с сервера
    private Runnable receiverMessagesTask = () -> {
        while (true) {
            try {
                String messageFromServer = fromServer.readLine();
                if (messageFromServer != null) {
                    if (messageFromServer.startsWith("PLAYER")) {
                        controller.setPlayerNumber(messageFromServer);
                    }
                    if (messageFromServer.contains("move")) {
                        String[] parsedMessage = messageFromServer.split(" ");
                        if (!parsedMessage[2].equals(controller.getPlayerNumber())) {
                            if (parsedMessage[1].equals("RIGHT")) {
                                controller.getEnemy().setCenterX(controller.getEnemy().getCenterX() + 5);
                            } else {
                                controller.getEnemy().setCenterX(controller.getEnemy().getCenterX() - 5);
                            }
                        }
                    }
                    if (messageFromServer.contains("shot")) {
                        String[] parsedMessage = messageFromServer.split(" ");
                        // если выстрел врага
                        if (!parsedMessage[1].equals(controller.getPlayerNumber())) {
                            Platform.runLater(() -> {
                                Circle bullet = new Circle();
                                bullet.setRadius(5);
                                bullet.setCenterX(controller.getEnemy().getCenterX() + controller.getEnemy().getLayoutX());
                                bullet.setCenterY(controller.getEnemy().getCenterY() + controller.getEnemy().getLayoutY());
                                bullet.setFill(Color.ORANGE);
                                controller.getPane().getChildren().add(bullet);

                                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.005), animation -> {
                                    bullet.setCenterY(bullet.getCenterY() + 1);

                                    if (bullet.getBoundsInParent().intersects(controller.getPlayer().getBoundsInParent())) {
                                        bullet.setVisible(false);
                                        controller.getPane().getChildren().remove(bullet);
                                    }
                                }));

                                timeline.setCycleCount(500);
                                timeline.play();
                            });
                        }
                    }
                    if (messageFromServer.contains("hit")) {
                        String[] parsedMessage = messageFromServer.split(" ");
                        String player = parsedMessage[1];
                        if (controller.getPlayerNumber().equals(player)) {
                            Platform.runLater(() ->
                                    controller.getPlayerHP().setText(String.valueOf(Integer.parseInt(controller.getPlayerHP().getText()) - 4))
                            );
                        } else {
                            Platform.runLater(() ->
                                    controller.getEnemyHP().setText(String.valueOf(Integer.parseInt(controller.getEnemyHP().getText()) - 4)));
                        }
                        if (controller.getEnemyHP().getText().equals("0")) {
                            sendMessage("game over " + controller.getPlayerNumber());
                            client.close();
                        }
                    }
                    controller.getServerLogsTextArea().appendText(messageFromServer + "\n");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    };
}
