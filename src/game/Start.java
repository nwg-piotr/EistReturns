package game;

import javafx.application.Application;
import javafx.application.Platform;


public class Start {

    public static void main(String[] args) {
        System.out.println("eist-returns.jar {-E --edit} runs the Level Editor");
        if(args.length > 0) {
            if (args[0].trim().equals("-E") || args[0].trim().equals("--edit")) {
                Application.launch(Editor.class, args);
            } else {
                Application.launch(Main.class, args);
            }
        } else {
            Application.launch(Main.class, args);
        }
        Platform.exit();
    }
}