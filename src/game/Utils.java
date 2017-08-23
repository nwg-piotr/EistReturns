package game;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;

import game.Sprites.Player;
import game.Sprites.Arrow;
import game.Sprites.Artifact;
import game.Sprites.Door;
import game.Sprites.Key;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

abstract class Utils extends Application {

    double mSceneWidth;
    double mSceneHeight;
    double walkingSpeedPerSecond;

    boolean turnRight;

    boolean mDisableDoorReaction = false;

    /**
     * The Frame is a rectangular part of the game board of width of 2 columns and height of 2 rows.
     * It corresponds to the width of the path on which Eist's sprite moves.
     */
    double mFrameDimension;
    double mGridDimension;
    double mRotationRadius;

    /**
     * Eists movement directions; diagonal directions to be used while falling down only.
     */
    static final int DIR_RIGHT = 0;
    static final int DIR_DOWN = 1;
    static final int DIR_LEFT = 2;
    static final int DIR_UP = 3;

    static final int TURNING_NOT = 0;
    static final int TURNING_RIGHT = 1;
    static final int TURNING_LEFT = 2;
    static final int TURNING_BACK = 3;
    static final int TURNING_DOOR = 4;

    static final int ORIENTATION_HORIZONTAL = 0;
    static final int ORIENTATION_VERTICAL = 1;

    /**
     * Sprites and other game objects come here.
     */
    Player eist;

    /**
     * To place the game board content (arrows, artifacts, teleports etc), we'll divide it into rows and columns grid.
     * The column is half the width of the Frame. The row is half the height of the Frame.
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
        System.out.println("mFrameDimension = " + mFrameDimension);

        mGridDimension = mFrameDimension / 2;

        mRotationRadius = mFrameDimension / 4;

        walkingSpeedPerSecond = mSceneWidth / 12d;
        System.out.println("walkingSpeedPerSecond = " + walkingSpeedPerSecond);

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
    static double[] frameRates = new double[5];

    /*
     * Returns the instantaneous FPS for the last frame rendered.
     */
    static double getInstantFPS() {
        return frameRates[index % frameRates.length];
    }

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

    /**
     * Some graphics will look the same way on all levels. Let's load it here.
     */
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

    Image mArtifact;
    Image mKey;
    Image mDoorH;
    Image mDoorV;

    void loadCommonGraphics() {

        mEistRight = new Image("images/sprites/eist_right.png");
        mEistDown = new Image("images/sprites/eist_down.png");
        mEistLeft = new Image("images/sprites/eist_left.png");
        mEistUp = new Image("images/sprites/eist_up.png");

        mArrowRight = new Image("images/sprites/arrow_right.png");
        mArrowDown = new Image("images/sprites/arrow_down.png");
        mArrowLeft = new Image("images/sprites/arrow_left.png");
        mArrowUp = new Image("images/sprites/arrow_up.png");
    }

    /**
     * Create game objects on the basis of .dat files
     */
    List<Arrow> mArrows;
    List<Artifact> mArtifacts;
    List<Key> mKeys;
    List<Door> mDoors;

    void loadLevel(int level) {

        String lvlNumberToString = (level < 10) ? "0" + String.valueOf(level) : String.valueOf(level);
        String url = "/res/" + lvlNumberToString;

        mBoard = new Image(url + "board.png", mSceneWidth, mSceneHeight, true, true, true);

        /*
         * Load arrows
         */
        String dataString;

        dataString = datToString(getClass().getResource(url + "arrows.dat").getPath());
        if (dataString != null && !dataString.isEmpty()) {

            mArrows = new ArrayList<>();

            String[] arrows = dataString.split(":");

            for (String single_entry : arrows) {

                String[] positions = single_entry.split(",");

                int posX = Integer.valueOf(positions[0]);
                int posY = Integer.valueOf(positions[1]);

                Arrow arrow = new Arrow();
                arrow.setPosX(columns[posX]);
                arrow.setPosY(rows[posY]);

                arrow.setArea(innerRect(columns[posX], rows[posY]));

                arrow.setDirection(Integer.valueOf(positions[2]));
                mArrows.add(arrow);
            }
            System.out.println("Loaded arrows: " + mArrows.size());
        }
        /*
         * Load artifacts (called "amulets" in resources due to historical reasons ;)
         */
        mArtifact = new Image(url + "amulet.png");

        dataString = datToString(getClass().getResource(url + "amulets.dat").getPath());
        if (dataString != null) {

            mArtifacts = new ArrayList<>();

            String[] artifacts = dataString.split(":");

            for (String single_entry : artifacts) {

                String[] positions = single_entry.split(",");

                int posX = Integer.valueOf(positions[0]);
                int posY = Integer.valueOf(positions[1]);

                Artifact artifact = new Artifact();
                artifact.setPosX(columns[posX]);
                artifact.setPosY(rows[posY]);

                artifact.setArea(innerRect(columns[posX], rows[posY]));
                mArtifacts.add(artifact);
            }
            System.out.println("Loaded artifacts: " + mArtifacts.size());
        }

        /*
         * Load keys
         */
        mKey = new Image(url + "key.png");

        dataString = datToString(getClass().getResource(url + "keys.dat").getPath());
        if (dataString != null) {

            mKeys = new ArrayList<>();

            String[] keys = dataString.split(":");

            for (String single_entry : keys) {

                String[] positions = single_entry.split(",");

                int posX = Integer.valueOf(positions[0]);
                int posY = Integer.valueOf(positions[1]);

                Key key = new Key();
                key.setPosX(columns[posX]);
                key.setPosY(rows[posY]);

                key.setArea(innerRect(columns[posX], rows[posY]));
                mKeys.add(key);
            }
            System.out.println("Loaded keys: " + mArtifacts.size());
        }

        /*
         * Load doors
         */
        mDoorH = new Image(url + "door_h.png");
        mDoorV = new Image(url + "door_v.png");

        dataString = datToString(getClass().getResource(url + "doors.dat").getPath());
        if (dataString != null) {

            mDoors = new ArrayList<>();

            String[] artifacts = dataString.split(":");

            for (String single_entry : artifacts) {

                String[] positions = single_entry.split(",");

                int posX = Integer.valueOf(positions[0]);
                int posY = Integer.valueOf(positions[1]);
                int orientation = Integer.valueOf(positions[2]);

                Door door = new Door();
                door.setPosX(columns[posX]);
                door.setPosY(rows[posY]);
                door.setOrientation(orientation);

                door.setArea(innerRect(columns[posX], rows[posY]));
                mDoors.add(door);
            }
            System.out.println("Loaded doors: " + mDoors.size());
        }
    }

    /**
     * Object detection area must not fill all the frame. Let's center a rectangle of the grid size inside the frame.
     *
     * @param outerX Source frame X
     * @param outerY Source frame Y
     * @return Centered smaller rectangle
     */
    private Rectangle2D innerRect(double outerX, double outerY) {
        double x = outerX + mGridDimension / 2;
        double y = outerY + mGridDimension / 2;

        return new Rectangle2D(x, y, mGridDimension, mGridDimension);
    }

    private String datToString(String url) {

        File file = new File(url).getAbsoluteFile();

        Path path = file.toPath().toAbsolutePath();

        String content;

        try {
            content = new String(Files.readAllBytes(path)).trim();
        } catch (IOException e) {
            content = null;
            e.printStackTrace();
        }

        if (content != null && content.isEmpty()) {
            content = null;
        }
        return content;
    }

    static boolean getRandomBoolean() {
        return Math.random() < 0.5;
    }
}
