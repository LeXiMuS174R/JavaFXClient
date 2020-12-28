package javafx.game.client.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.game.client.controller.Controller;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        String fxmlFile = "/fxml/Main.fxml";
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResourceAsStream(fxmlFile));

        stage.setTitle("Inno Game");
        stage.setScene(new Scene(root));
        stage.setResizable(false);

        Scene scene = stage.getScene();
        Controller controller = loader.getController();
        scene.setOnKeyPressed(controller.keyEventEventHandler);

        stage.show();
    }
}
