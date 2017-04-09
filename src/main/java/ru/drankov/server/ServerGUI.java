package ru.drankov.server;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ru.drankov.util.Console;

import java.io.IOException;

public class ServerGUI extends Application {

    private Console serverConsole = new Console();

    private Server server;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("ServerGUI");
        GridPane gridPane = initGridPane();

        //server
        server = new Server(serverConsole);

        Scene scene = new Scene(gridPane, 240, 300);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private GridPane initGridPane() {
        //root
        GridPane gridPane = new GridPane();

        //text field init
        TextField textField = new TextField();
        textField.setPromptText("Enter Server port");

        //button and event handler
        Button button = new Button("Start");
        button.setOnAction(event -> {
            int i = Integer.parseInt(textField.getText());
            if (i > 1 && i < 65000) {
                try {
                    server.initServer(i);
                    serverConsole.cout("server succeed in starting");
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    serverConsole.cout("server failed to start");
                }
            }

        });


        //grid fill
        gridPane.add(button, 0, 0);
        gridPane.add(serverConsole.view, 0, 1, 2, 1);
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
