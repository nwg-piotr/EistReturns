package game;

import javafx.application.Application;
import javafx.application.Platform;


public class Start {

    public static void main(String[] args) {

        /*
         * Workaround for the JavaFX for linux bug
         * see: https://stackoverflow.com/questions/45812036/javafx-60-fps-frame-rate-cap-doesnt-work/45827990#45827990
         */
        System.setProperty("quantum.multithreaded", "false");
        System.out.println("Setting \'quantum.multithreaded\' \'true\' (see: https://stackoverflow.com/a/45827990/4040598)");
        System.out.println("eist-returns {-E --edit} runs the Level Editor");

        if(args.length > 0) {
            String arg = args[0].trim().toUpperCase();
            if (arg.equals("-E") || arg.equals("--EDIT")) {
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