package game;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;

import game.Sprites.Player;
import game.Sprites.Arrow;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

abstract class Utils extends Application {

    double mSceneWidth;
    double mSceneHeight;
    double walkSpeedPerSecond;
    /**
     * The Frame is a rectangular part of the game board of width of 2 columns and height of 2 rows.
     * It corresponds to the width of the path on which Eist's sprite moves.
     */
    double mFrameDimension;
    double mGridDimension;

    /**
     * Eists movement directions; diagonal directions to be used while falling down only.
     */
    final int DIR_RIGHT = 0;
    final int DIR_DOWN = 1;
    final int DIR_LEFT = 2;
    final int DIR_UP = 3;

    final int TURNING_NOT = 0;
    final int TURNING_RIGHT = 1;
    final int TURNING_LEFT = 2;
    final int TURNING_BACK = 3;

    /**
     * Sprites and other game objects come here.
     */
    Player eist;

    /**
     * To place the game board content (arrows, artifacts, teleports etc), we'll divide it into rows and columns.
     * The column is half the width of the Frame.
     * The row is half the height of the Frame.
     */
    double[] rows = new double[19];
    double[] columns = new double[33];

    void setBoardDimensions() {

        /*
         * The dimensions of the Scene and the game board inside will derive from the users' screen width.
         * Source graphics has been drawn as fullHD (1920 x 1080).
         */
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        mSceneWidth = primaryScreenBounds.getWidth() / 2;
        mSceneHeight = (mSceneWidth / 1920) * 1080;
        mFrameDimension = (mSceneWidth / 1920) * 120;

        mGridDimension = mFrameDimension / 2;

        walkSpeedPerSecond = mSceneWidth / 12d;

        /*
         * Board grid coordinates
         */
        for (int i = 0; i < rows.length; i++) {
            rows[i] = (mGridDimension) * i;
        }
        for (int i = 0; i < columns.length; i++) {
            columns[i] = (mGridDimension) * i;
        }
    }

    /**
     * FPS calculation after https://stackoverflow.com/a/28288527/4040598 by @brian
     */
    static long lastUpdate = 0;
    static int index = 0;
    static double[] frameRates = new double[500];

    /*
     * Returns the instantaneous FPS for the last frame rendered.
     */
    //static double getInstantFPS() {
    //    return frameRates[index % frameRates.length];
    //}

    /**
     * Returns the average FPS for the last frameRates.length frames rendered.
     * (Original for loop replaced with foreach).
     */

    static double getAverageFPS() {
        double total = 0.0d;

        for (double value : frameRates) {
            total += value;
        }
        return total / frameRates.length;
    }

    void handleMouseEvents(MouseEvent event) {
        if (eist.getArea().contains(new Point2D(event.getSceneX(), event.getSceneY()))) {

            /*
             * (Temporarily) turn 90 degrees right if the sprite clicked.
             */
            switch (eist.getDirection()) {
                case DIR_RIGHT:
                    eist.setDirection(DIR_DOWN);
                    break;

                case DIR_DOWN:
                    eist.setDirection(DIR_LEFT);
                    break;

                case DIR_LEFT:
                    eist.setDirection(DIR_UP);
                    break;

                case DIR_UP:
                    eist.setDirection(DIR_RIGHT);
                    break;

                default:
                    break;
            }

        } else {
            /*
             * (Temporarily) start / stop moving if anything else clicked.
             */
            eist.isMoving = !eist.isMoving;
        }
    }

    Image mBoard;

    Image mEistImage;

    Image mEistRight;
    Image mEistDown;
    Image mEistLeft;
    Image mEistUp;

    Image mArrowRight;
    Image mArrowDown;
    Image mArrowLeft;
    Image mArrowUp;

    /**
     * Some graphics will look the same way on all levels. Let's load it here.
     */
    void loadCommonGraphics() {

        mEistRight = new Image("images/sprites/eist_right.png", mFrameDimension * 8, mFrameDimension, false, true);
        mEistDown = new Image("images/sprites/eist_down.png", mFrameDimension * 8, mFrameDimension, false, true);
        mEistLeft = new Image("images/sprites/eist_left.png", mFrameDimension * 8, mFrameDimension, false, true);
        mEistUp = new Image("images/sprites/eist_up.png", mFrameDimension * 8, mFrameDimension, false, true);

        mArrowRight = new Image("images/sprites/arrow_right.png");
        mArrowDown = new Image("images/sprites/arrow_down.png");
        mArrowLeft = new Image("images/sprites/arrow_left.png");
        mArrowUp = new Image("images/sprites/arrow_up.png");
    }

    List<Arrow> mArrows;

    void loadLevel(int level) {

        String lvlNumberToString = (level < 10) ? "0" + String.valueOf(level) : String.valueOf(level);
        String url = "/res/" + lvlNumberToString;

        mBoard = new Image(url + "board.png", mSceneWidth, mSceneHeight, true, true, true);

        String dataString;
        /*
         * Load arrows
         */
        dataString = datToString(getClass().getResource(url + "arrows.dat").getPath());
        if (dataString != null) {

            System.out.println(dataString);

            mArrows = new ArrayList<>();

            String[] arrows = dataString.split(":");

            for (String single_entry : arrows) {

                String[] positions = single_entry.split(",");

                int posX = Integer.valueOf(positions[0]);
                int posY = Integer.valueOf(positions[1]);

                Arrow arrow = new Arrow();
                arrow.setPosX(columns[posX]);
                arrow.setPosY(rows[posY]);

                Rectangle2D area = new Rectangle2D(posX, posY, mFrameDimension, mFrameDimension);

                arrow.setArea(area);

                arrow.setDirection(Integer.valueOf(positions[2]));
                mArrows.add(arrow);
            }
            System.out.println("Arrows: " + mArrows.size());
        }

    }

    private String datToString(String url) {

        System.out.println("URL: " + url);

        File file = new File(url).getAbsoluteFile();

        Path path = file.toPath().toAbsolutePath();

        System.out.println("path: " + path.toString());

        String content = null;

        try {
            content = new String(Files.readAllBytes(path));
        } catch (IOException e) {
            content = null;
            e.printStackTrace();
        }

        if (content != null && content.isEmpty()) {
            content = null;
        }
        return content;

    }

    /*
    private String datToString(String path) {

        System.out.println("PATH: |" + path + "|");

        File file = new File(path);

        BufferedReader reader = null;
        String data = "";

        try {
            reader =  new BufferedReader(new FileReader(file));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                data += mLine;
            }
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (data.equals("")) {
            data = null;
        }

        return data;
    }
    */
}
