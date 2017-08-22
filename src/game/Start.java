package game;

import javafx.application.Application;

public class Start {
    public static void main(String[] args) {
        System.setProperty("quantum.multithreaded", "false");
        Application.launch(Main.class, args);
    }
}
