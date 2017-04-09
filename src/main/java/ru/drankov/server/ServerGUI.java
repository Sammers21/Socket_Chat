package ru.drankov.server;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ServerGUI extends Application {

    private ServerConsole serverConsole=new ServerConsole();

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("ServerGUI");
        GridPane gridPane = initGridPane();

        Scene scene = new Scene(gridPane, 240, 300);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private GridPane initGridPane() {
        //root
        GridPane gridPane = new GridPane();

        //button
        Button button = new Button("Start");
        button.setOnAction(event -> serverConsole.cout("kek"));

        TextField textField = new TextField();
        textField.setPromptText("Enter Server port");

        //grid fill
        gridPane.add(button, 0, 0);
        gridPane.add(serverConsole.view, 0, 1,2,1);
        gridPane.add(textField, 1, 0);

        //grid settings
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        return gridPane;
    }

    public static void main(String[] args) {
        launch(ServerGUI.class);
    }


}
