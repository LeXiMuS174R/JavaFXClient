package javafx.game.client.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.game.client.sockets.SocketClient;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private Button connectionButton;

    @FXML
    private TextField playerNameTextField;

    @FXML
    private Button startGameButton;

    @FXML
    private TextArea serverLogsTextArea;

    @FXML
    private Circle player;

    @FXML
    private Circle enemy;

    @FXML
    private AnchorPane pane;

    @FXML
    private Label playerHP;

    @FXML
    private Label enemyHP;

    private SocketClient socketClient;

    private String playerNumber;



    // событие, которое происходит при нажатии на клавиатуре какой-либо кнопки
    public EventHandler<KeyEvent> keyEventEventHandler = event -> {
        if (event.getCode() == KeyCode.LEFT) {
            socketClient.sendMessage("move LEFT " + playerNumber);
            player.setCenterX(player.getCenterX() - 5);
        } else if (event.getCode() == KeyCode.RIGHT) {
            socketClient.sendMessage("move RIGHT " + playerNumber);
            player.setCenterX(player.getCenterX() + 5);
        } else if (event.getCode() == KeyCode.CONTROL) {
            Circle bullet = new Circle();
            bullet.setRadius(5);
            bullet.setCenterX(player.getCenterX() + player.getLayoutX());
            bullet.setCenterY(player.getCenterY() + player.getLayoutY());
            bullet.setFill(Color.AQUA);
            pane.getChildren().add(bullet);

            // TODO: AtomicBoolean isHit = new AtomicBoolean(false);
            // TODO: как варианта - заканчивать на линии противника
            final boolean[] isHit = {false};

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.005), animation -> {
                if (!isHit[0]) {
                    bullet.setCenterY(bullet.getCenterY() - 1);

                    if (bullet.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                        bullet.setVisible(false);
                        if (this.playerNumber.equals("PLAYER_1")) {
                            socketClient.sendMessage("hit PLAYER_2");
                        } else {
                            socketClient.sendMessage("hit PLAYER_1");
                        }
                        isHit[0] = true;
                    }
                }
            }));

            timeline.setCycleCount(500);
            timeline.play();

            socketClient.sendMessage("shot " + playerNumber);
        }
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serverLogsTextArea.setEditable(false);
        startGameButton.setDisable(true);
        playerNameTextField.setDisable(true);
        connectionButton.setOnAction(event -> {
            socketClient = new SocketClient("localhost", 24758, this);
            connectionButton.setDisable(true);
            startGameButton.setDisable(false);
            playerNameTextField.setDisable(false);
        });

        startGameButton.setOnAction(event -> {
            socketClient.sendMessage("start " + playerNameTextField.getText());
            startGameButton.setDisable(true);
            playerNameTextField.setDisable(true);
            // перемещал фокус на форму
            startGameButton.getScene().getRoot().requestFocus();
        });
    }

    public TextArea getServerLogsTextArea() {
        return serverLogsTextArea;
    }

    public void setPlayerNumber(String playerNumber) {
        this.playerNumber = playerNumber;
    }

    public Circle getPlayer() {
        return player;
    }

    public Circle getEnemy() {
        return enemy;
    }

    public String getPlayerNumber() {
        return playerNumber;
    }

    public AnchorPane getPane() {
        return pane;
    }

    public Label getPlayerHP() {
        return playerHP;
    }

    public Label getEnemyHP() {
        return enemyHP;
    }

    public void setEnemyHP(Label enemyHP) {
        this.enemyHP = enemyHP;
    }
}
