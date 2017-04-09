package ru.drankov.util;

import javafx.scene.control.TextArea;


public class Console implements Consolable {

    public volatile TextArea view = new TextArea();

    private StringBuilder stringBuilder = new StringBuilder("");

    int cout = 0;

    boolean guimode = false;

    public Console(boolean guimode) {
        view.textProperty().addListener((observable, oldValue, newValue) -> {
            view.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
        });
        view.setEditable(false);
        this.guimode = guimode;
    }

    public Console() {
        view.textProperty().addListener((observable, oldValue, newValue) -> {
            view.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
        });
        view.setEditable(false);
    }


    /**
     * This method print something to consolearray
     *
     * @param st String that should be printed
     */
    public void cout(String st) {
        if(!guimode) {
            stringBuilder.append(st).append("\n");
            cout++;
            viewConsole();
        }
        else {
            System.out.println(st);
        }
    }

    private void viewConsole() {

        if (!guimode) {
            view.setPrefColumnCount(cout);
            view.setText(stringBuilder.toString());
            //trigger scroll
            view.appendText("");
        }
    }


}
