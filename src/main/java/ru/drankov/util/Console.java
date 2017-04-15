package ru.drankov.util;

import javafx.scene.control.TextArea;


public class Console implements Consolable {

    public volatile TextArea view = new TextArea();

    private StringBuilder stringBuilder = new StringBuilder("");

    int cout = 0;

    public Console() {
        view.textProperty().addListener((observable, oldValue, newValue) -> {
            view.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
        });
        view.setEditable(false);
    }


    /**
     * This method print something to console
     *
     * @param st String that should be printed
     */
    public void cout(String st) {

            stringBuilder.append(st).append("\n");
            cout++;
            viewConsole();

    }

    private void viewConsole() {

            view.setPrefColumnCount(cout);
            view.setText(stringBuilder.toString());
            //trigger scroll
            view.appendText("");

    }


}
