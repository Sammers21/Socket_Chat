package ru.drankov.server;

import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerConsole {

    public TextArea view = new TextArea();

    private List<String> console = new ArrayList<>();

    public ServerConsole() {
        view.textProperty().addListener((observable, oldValue, newValue) -> {
            view.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
        });
        view.setEditable(false);
    }

    /**
     * This method print something to console
     * @param st String that should be printed
     */
    public void cout(String st) {
        console.add(st);
        viewConsole();
    }

    private void viewConsole() {
        String st = console.stream()
                .collect(Collectors.joining("\n"));
        view.setPrefColumnCount(console.size());
        view.setText(st);
        //trigger scroll
        view.appendText("");
    }


}
