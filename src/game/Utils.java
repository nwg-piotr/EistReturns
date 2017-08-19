package game;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;

import game.Sprites.Player;

abstract class Utils extends Application {

    double mSceneWidth;
    double mSceneHeight;
    double walkSpeedPerSecond;
    /**
     * The Frame is a rectangular part of the game board of width of 2 columns and height of 2 rows.
     * It corresponds to the width of the path on which Eist's sprite moves.
     */
    double mFrameDimension;

    /**
     * Eists movement directions; diagonal directions to be used while falling down only.
     */
    final int DIR_RIGHT = 1;
    final int DIR_RIGHT_DOWN = 2;
    final int DIR_DOWN = 3;
    final int DIR_LEFT_DOWN = 4;
    final int DIR_LEFT = 5;
    final int DIR_LEFT_UP = 6;
    final int DIR_UP = 7;
    final int DIR_RIGHT_UP = 8;

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

        walkSpeedPerSecond = mSceneWidth / 12d;

        /*
         * Board grid coordinates
         */
        double gridDimension = mFrameDimension / 2;
        for (int i = 0; i < rows.length; i++) {
            rows[i] = (gridDimension) * i;
        }
        for (int i = 0; i < columns.length; i++) {
            columns[i] = (gridDimension) * i;
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

    void loadCommonGraphics() {

        mEistRight = new Image("images/sprites/eist_right.png", mFrameDimension * 8, mFrameDimension, false, true);
        mEistDown = new Image("images/sprites/eist_down.png", mFrameDimension * 8, mFrameDimension, false, true);
        mEistLeft = new Image("images/sprites/eist_left.png", mFrameDimension * 8, mFrameDimension, false, true);
        mEistUp = new Image("images/sprites/eist_up.png", mFrameDimension * 8, mFrameDimension, false, true);
    }

    void loadLevel(int level) {

        String lvlNumberToString = (level < 10) ? "0" + String.valueOf(level) : String.valueOf(level);
        mBoard = new Image("levels/" + lvlNumberToString + "board.png", mSceneWidth, mSceneHeight, true, true, true);
    }
}
