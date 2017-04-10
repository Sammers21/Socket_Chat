package ru.drankov.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ru.drankov.util.Console;

import java.io.IOException;

public class ClientGUI extends Application {

    private Console clientConsole = new Console();

    private Client client;

    String login = null;

    String chatName = "default";

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("ClientGUI");
        client = new Client(clientConsole);
        GridPane gridPane = initGridPane();

        Scene scene = new Scene(gridPane, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        help();

        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> Platform.exit());
    }

    private void help() {
        clientConsole.cout("type \"help\" if you need a help");
    }

    private GridPane initGridPane() {

        GridPane gridPane = new GridPane();
        //
        TextField faddress = new TextField();
        faddress.setPromptText("Enter server adders");
        faddress.setOnAction(s -> client.makeConnection(faddress.getText()));
        //text field init
        TextField textField = new TextField();

        textField.setPromptText("Enter command");
        textField.setOnAction(s -> {
            process(textField.getText());
            textField.setText("");
        });

        //btn
        Button button = new Button("Connect");
        button.setOnAction(s -> client.makeConnection(faddress.getText()));
        gridPane.add(button, 0, 0);
        gridPane.add(faddress, 1, 0, 1, 1);
        gridPane.add(clientConsole.view, 0, 1, 2, 1);
        gridPane.add(textField, 0, 2, 2, 1);

        gridPane.setAlignment(Pos.CENTER);


        //grid settings
        gridPane.setHgap(10);
        gridPane.setVgap(10);


        return gridPane;
    }

    private void process(String text) {
        if (text.equals("help")) {
            System.out.println("help");
            clientConsole.cout("help:");
            clientConsole.cout("\t1. login <username>");
            return;
        }
        String[] split = text.split(" ");

        if (split.length == 1 && login == null) {
            System.out.println("login before sending message");
            clientConsole.cout("login before sending message");
            help();
        } else if (split.length == 2 && split[0].equals("login")) {
            login = split[1];
            clientConsole.cout("you login like a " + split[1]);
        } else if (split.length >= 1 && login != null) {
            try {

                    StringBuilder sb = new StringBuilder("");
                    for (int i = 0; i < split.length; i++) {
                        sb.append(split[i]+" ");
                    }

                client.send("msg " + login + " " + chatName + " " + sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
                clientConsole.cout("can't send message");
            }
        }

    }
}
