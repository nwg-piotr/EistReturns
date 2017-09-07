package game;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;

import java.io.File;

import game.Sprites.Player;
import game.Sprites.Arrow;
import game.Sprites.Artifact;
import game.Sprites.Teleport;
import game.Sprites.Door;
import game.Sprites.Key;
import game.Sprites.Slot;
import game.Sprites.Ladder;
import game.Sprites.Exit;
import game.Sprites.Pad;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

abstract class Utils extends Application {

    double mSceneWidth;
    double mSceneHeight;
    double walkingSpeedPerSecond;

    int mCurrentLevel = 0;
    int mSelectedLevel = 1;
    int mAchievedLevel = 0;
    final int MAX_LEVEL = 40;

    double mDimensionDivider;
    double rem;
    double cs;

    int mCurrentEistFrame = 0;
    int mCurrentArtifactFrame = 0;
    Integer mCurrentFallingFrame = null;

    boolean turnRight;

    boolean mDisableDoorReaction = false;

    int mTurnsCounter;
    int mTurnsBest;

    PixelReader pixelReader;

    private Rectangle2D mButtonLevelUp;
    private Rectangle2D mButtonLevelDown;
    private Rectangle2D mButtonPlay;
    private Rectangle2D mButtonMenu;

    private Rectangle2D mButtonAbout;
    private Rectangle2D mButtonMuteMusic;
    private Rectangle2D mButtonMuteSound;

    private MediaPlayer trackMainPlayer;
    private MediaPlayer trackLevelPlayer;

    MediaPlayer fxBounce;
    MediaPlayer fxArtifact;
    MediaPlayer fxKey;
    MediaPlayer fxDoor;
    MediaPlayer fxExit;
    MediaPlayer fxLevelLost;
    MediaPlayer fxLevelUp;
    private MediaPlayer fxLadder;
    MediaPlayer fxTeleport;


    private Media bounceMedia;
    private Media artifactMedia;
    private Media keyMedia;
    private Media doorOpenMedia;
    private Media exitOpenMedia;
    private Media levelLostMedia;
    private Media levelUpMedia;
    private Media ladderMedia;
    private Media teleportMedia;

    /**
     * The Frame is a rectangular part of the game board of width of 2 columns and height of 2 rows.
     * It corresponds to the width of the path on which Eist's sprite moves.
     */
    double mFrameDimension;
    double mGridDimension;
    double mHalfGridDimension;
    int mDetectionOffset;
    double mRotationRadius;

    boolean mMuteMusic;
    boolean mMuteSound;

    /**
     * Eists movement directions; diagonal directions to be used while falling down only.
     */
    static final int DIR_RIGHT = 0;
    static final int DIR_DOWN = 1;
    static final int DIR_LEFT = 2;
    static final int DIR_UP = 3;
    static final int DIR_CLEAR = 4;

    static final int TURNING_NOT = 0;
    static final int TURNING_RIGHT = 1;
    static final int TURNING_LEFT = 2;
    static final int TURNING_BACK = 3;

    static final int ORIENTATION_HORIZONTAL = 0;
    private static final int ORIENTATION_VERTICAL = 1;

    Preferences prefs;

    /**
     * Sprites and other game objects come here.
     */
    Player eist;
    Ladder ladder;
    Exit exit;
    Pad pad;

    private Alert mErrorAlert;

    /**
     * To place the game board content (arrows, artifacts, teleports etc), we'll divide it into rows and columns grid.
     * The column is half the width of the Frame. The row is half the height of the Frame.
     */
    double[] rows = new double[19];
    double[] columns = new double[33];

    void setBoard() {

        /*
         * Saved preferences
         */
        prefs = Preferences.userNodeForPackage(Main.class);
        mSelectedLevel = prefs.getInt("level", 1);
        mAchievedLevel = prefs.getInt("achieved", 0);
        mMuteSound = prefs.getBoolean("msound", false);
        mMuteMusic = prefs.getBoolean("mmusic", false);
        mDimensionDivider = prefs.getDouble("divider", 1.5);

        /*
         * The dimensions of the Scene and the game board inside will derive from the users' screen width.
         * Source graphics has been drawn as fullHD (1920 x 1080).
         */
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        mSceneWidth = primaryScreenBounds.getWidth() / mDimensionDivider;
        mSceneHeight = (mSceneWidth / 1920) * 1080;
        mFrameDimension = (mSceneWidth / 1920) * 120;
        mGridDimension = mFrameDimension / 2;
        mHalfGridDimension = mGridDimension / 2;
        mDetectionOffset = (int) mFrameDimension / 6;
        mRotationRadius = mFrameDimension / 4;
        walkingSpeedPerSecond = mSceneWidth / 14d;
        rem = javafx.scene.text.Font.getDefault().getSize() / 13;

        System.out.println("mSceneWidth = " + mSceneWidth);
        System.out.println("mSceneHeight = " + mSceneHeight);
        System.out.println("mFrameDimension = " + mFrameDimension);
        System.out.println("mGridDimension = " + mGridDimension);
        System.out.println("walkingSpeedPerSecond = " + walkingSpeedPerSecond);

        /*
         * For future use - create the folder which user can upload custom levels data to.
         */
        File mEistFolder = new File(System.getProperty("user.home") + "/.EistReturns");
        if (mEistFolder.mkdir()) {
            System.out.println("EistReturns folder created");
        }

        /*
         * Calculate board grid coordinates
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
    /*
    static double getInstantFPS() {
        return frameRates[index % frameRates.length];
    }
    */

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

    void handleMouseEvent(MouseEvent event) {

        Point2D pointClicked = new Point2D(event.getSceneX(), event.getSceneY());

        if (mButtonMuteMusic.contains(pointClicked)) {
            mMuteMusic = !mMuteMusic;
            prefs.putBoolean("mmusic", mMuteMusic);
            if (trackMainPlayer != null) {
                trackMainPlayer.setMute(mMuteMusic);
            }
            if (trackLevelPlayer != null) {
                trackLevelPlayer.setMute(mMuteMusic);
            }
        }
        if (mButtonMuteSound.contains(pointClicked)) {
            mMuteSound = !mMuteSound;
            prefs.putBoolean("msound", mMuteSound);
        }
        if(mButtonAbout.contains(pointClicked) && mCurrentLevel == 0){
            displayAboutAlert();
        }

        if (mCurrentLevel != 0) {

            /*
             * Check whether menu or board clicked
             */
            if (pointClicked.getX() > columns[26]) {

                /*
                 * Menu clicked. Check which part.
                 */
                if (pointClicked.getY() < rows[11]) {

                    /*
                     * Pad clicked
                     */
                    if (pad.getButtonLeft().contains(pointClicked)) {
                        pad.setSelection(DIR_LEFT);
                        eist.isMoving = true;
                    } else if (pad.getButtonRight().contains(pointClicked)) {
                        pad.setSelection(DIR_RIGHT);
                        eist.isMoving = true;
                    } else if (pad.getButtonUp().contains(pointClicked)) {
                        pad.setSelection(DIR_UP);
                        eist.isMoving = true;
                    } else if (pad.getButtonDown().contains(pointClicked)) {
                        pad.setSelection(DIR_DOWN);
                        eist.isMoving = true;
                    } else if (pad.getButtonClear().contains(pointClicked)) {
                        pad.setSelection(DIR_CLEAR);
                        eist.isMoving = true;
                    }
                }

            } else {

                /*
                 * Board clicked
                 */
                eist.isMoving = true;

                for (Slot slot : mSlots) {

                    if (slot.getArea().contains(pointClicked)) {

                        int clickedSlotIdx = mSlots.indexOf(slot);

                        if (ladder.getSlotIdx() == null) {

                            if (!mMuteSound) {
                                fxLadder.play();
                            }
                            ladder.setSlotIdx(clickedSlotIdx);

                        } else {

                            if (clickedSlotIdx == ladder.getSlotIdx()) {

                                if (!mMuteSound) {
                                    fxLadder.play();
                                }
                                ladder.setSlotIdx(null);
                            }
                        }

                    }
                }

                if (pad.getSelection() != null && pixelReader.getArgb((int) pointClicked.getX(), (int) pointClicked.getY()) != -16777216) {

                    Rectangle2D pressedSquare = nearestSquare(pointClicked.getX(), pointClicked.getY());

                    if (pressedSquare != null) {

                        if (pad.getSelection() != DIR_CLEAR) {

                            if (arrowAllowed(new Point2D(pressedSquare.getMinX() + mGridDimension, pressedSquare.getMinY() + mGridDimension))) {
                                placeArrow(pressedSquare.getMinX(), pressedSquare.getMinY());
                            }

                        } else {

                            removeArrow(pressedSquare);
                        }
                    }
                }
            }

            if (mButtonMenu.contains(pointClicked)) {
                mSelectedLevel = mCurrentLevel;
                mCurrentLevel = 0;
                loadLevel(mCurrentLevel);
            }

        } else {
            /*
             * Handle intro menu clicks
             */
            if (mButtonLevelUp.contains(pointClicked)) {
                if (mSelectedLevel < mAchievedLevel && mSelectedLevel < MAX_LEVEL) {
                    mSelectedLevel++;
                    prefs.putInt("level", mSelectedLevel);
                }
            } else if (mButtonLevelDown.contains(pointClicked)) {
                if (mSelectedLevel > 1) {
                    mSelectedLevel--;
                    prefs.putInt("level", mSelectedLevel);
                }
            } else if (mButtonPlay.contains(pointClicked)) {
                mCurrentLevel = mSelectedLevel;
                loadLevel(mCurrentLevel);
            } else if (mButtonMenu.contains(pointClicked)) {

                //displayAboutAlert();
                displaySizeDialog();
            }
        }
    }

    /**
     * Some graphics will look the same way on all levels. Let's load it here.
     */
    Image mBoardImg;

    Image mEistImg;
    Image mEistRightImg;
    Image mEistDownImg;
    Image mEistLeftImg;
    Image mEistUpImg;

    Image mEistFallingRightImg;
    Image mEistFallingDownImg;
    Image mEistFallingLeftImg;
    Image mEistFallingUpImg;

    Image mArrowRightImg;
    Image mArrowDownImg;
    Image mArrowLeftImg;
    Image mArrowUpImg;

    Image mSelRightImg;
    Image mSelDownImg;
    Image mSelLeftImg;
    Image mSelUpImg;
    Image mSelClearImg;

    Image mArtifactImg;
    Image mTeleportImg;
    Image mKeyImg;
    Image mDoorHImg;
    Image mDoorVImg;

    Image mLadderImg;
    Image mLadderHImg;
    Image mLadderVImg;

    Image mExitClosedImg;
    Image mExitOpenImg;

    Image mIntro01;
    Image mIntro02;
    Image mIntro03;
    Image mIntro04;

    Image mMutedMusicImg;
    Image mMutedSoundImg;

    void loadCommonGraphics() {

        /*
         * Load media
         */
        try {
            Media sound;
            sound = new Media(ClassLoader.getSystemResource("sounds/eist.mp3").toExternalForm());
            trackMainPlayer = new MediaPlayer(sound);
            trackMainPlayer.setVolume(0.4);
            trackMainPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            trackMainPlayer.setMute(mMuteMusic);

            sound = new Media(ClassLoader.getSystemResource("sounds/eist_ingame.mp3").toExternalForm());
            trackLevelPlayer = new MediaPlayer(sound);
            trackLevelPlayer.setVolume(0.3);
            trackLevelPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            trackMainPlayer.setMute(mMuteMusic);

        } catch (MediaException e) {

            displayExceptionAlert("Media player error (missing ffmpeg codec?)", e);
        }

        try {
            bounceMedia = new Media(ClassLoader.getSystemResource("sounds/bounce.wav").toExternalForm());
            fxBounce = new MediaPlayer(bounceMedia);
            fxBounce.setVolume(1);
            fxBounce.setMute(mMuteSound);
            fxBounce.setOnEndOfMedia(() -> fxBounce.stop());

            artifactMedia = new Media(ClassLoader.getSystemResource("sounds/amulet.wav").toExternalForm());
            fxArtifact = new MediaPlayer(artifactMedia);
            fxArtifact.setVolume(1);
            fxArtifact.setMute(mMuteSound);
            fxArtifact.setOnEndOfMedia(() -> fxArtifact.stop());

            keyMedia = new Media(ClassLoader.getSystemResource("sounds/key.wav").toExternalForm());
            fxKey = new MediaPlayer(keyMedia);
            fxKey.setVolume(1);
            fxKey.setMute(mMuteSound);
            fxKey.setOnEndOfMedia(() -> fxKey.stop());

            doorOpenMedia = new Media(ClassLoader.getSystemResource("sounds/door_open.wav").toExternalForm());
            fxDoor = new MediaPlayer(doorOpenMedia);
            fxDoor.setVolume(1);
            fxDoor.setMute(mMuteSound);
            fxDoor.setOnEndOfMedia(() -> fxDoor.stop());

            exitOpenMedia = new Media(ClassLoader.getSystemResource("sounds/exit.wav").toExternalForm());
            fxExit = new MediaPlayer(exitOpenMedia);
            fxExit.setVolume(1);
            fxExit.setMute(mMuteSound);
            fxExit.setOnEndOfMedia(() -> fxExit.stop());

            levelLostMedia = new Media(ClassLoader.getSystemResource("sounds/level_lost.wav").toExternalForm());
            fxLevelLost = new MediaPlayer(levelLostMedia);
            fxLevelLost.setVolume(1);
            fxLevelLost.setMute(mMuteSound);
            fxLevelLost.setOnEndOfMedia(() -> fxLevelLost.stop());

            levelUpMedia = new Media(ClassLoader.getSystemResource("sounds/level.wav").toExternalForm());
            fxLevelUp = new MediaPlayer(levelUpMedia);
            fxLevelUp.setVolume(1);
            fxLevelUp.setMute(mMuteSound);
            fxLevelUp.setOnEndOfMedia(() -> fxLevelUp.stop());

            ladderMedia = new Media(ClassLoader.getSystemResource("sounds/ladder.wav").toExternalForm());
            fxLadder = new MediaPlayer(ladderMedia);
            fxLadder.setVolume(1);
            fxLadder.setMute(mMuteSound);
            fxLadder.setOnEndOfMedia(() -> fxLadder.stop());

            teleportMedia = new Media(ClassLoader.getSystemResource("sounds/teleport.wav").toExternalForm());
            fxTeleport = new MediaPlayer(teleportMedia);
            fxTeleport.setVolume(1);
            fxTeleport.setMute(mMuteSound);
            fxTeleport.setOnEndOfMedia(() -> fxTeleport.stop());

        } catch (Exception e) {
            displayExceptionAlert("Media *.wav file found", e);
        }

        mEistRightImg = new Image(ClassLoader.getSystemResource("images/sprites/eist_right.png").toExternalForm());
        mEistDownImg = new Image(ClassLoader.getSystemResource("images/sprites/eist_down.png").toExternalForm());
        mEistLeftImg = new Image(ClassLoader.getSystemResource("images/sprites/eist_left.png").toExternalForm());
        mEistUpImg = new Image(ClassLoader.getSystemResource("images/sprites/eist_up.png").toExternalForm());

        mEistFallingRightImg = new Image(ClassLoader.getSystemResource("images/sprites/falldown_right.png").toExternalForm());
        mEistFallingDownImg = new Image(ClassLoader.getSystemResource("images/sprites/falldown_down.png").toExternalForm());
        mEistFallingLeftImg = new Image(ClassLoader.getSystemResource("images/sprites/falldown_left.png").toExternalForm());
        mEistFallingUpImg = new Image(ClassLoader.getSystemResource("images/sprites/falldown_up.png").toExternalForm());

        mArrowRightImg = new Image(ClassLoader.getSystemResource("images/sprites/arrow_right.png").toExternalForm());
        mArrowDownImg = new Image(ClassLoader.getSystemResource("images/sprites/arrow_down.png").toExternalForm());
        mArrowLeftImg = new Image(ClassLoader.getSystemResource("images/sprites/arrow_left.png").toExternalForm());
        mArrowUpImg = new Image(ClassLoader.getSystemResource("images/sprites/arrow_up.png").toExternalForm());

        mTeleportImg = new Image("images/sprites/teleport.png");

        mIntro01 = new Image(ClassLoader.getSystemResource("images/common/intro01.png").toExternalForm());
        mIntro02 = new Image(ClassLoader.getSystemResource("images/common/intro02.png").toExternalForm());
        mIntro03 = new Image(ClassLoader.getSystemResource("images/common/intro03.png").toExternalForm());
        mIntro04 = new Image(ClassLoader.getSystemResource("images/common/intro04.png").toExternalForm());

        mMutedMusicImg = new Image(ClassLoader.getSystemResource("images/common/muted_music.png").toExternalForm());
        mMutedSoundImg = new Image(ClassLoader.getSystemResource("images/common/muted_sound.png").toExternalForm());

        /*
         * Initialize pad buttons
         */
        mSelRightImg = new Image(ClassLoader.getSystemResource("images/sprites/button_arrow_right_selected.png").toExternalForm());
        mSelLeftImg = new Image(ClassLoader.getSystemResource("images/sprites/button_arrow_left_selected.png").toExternalForm());
        mSelUpImg = new Image(ClassLoader.getSystemResource("images/sprites/button_arrow_up_selected.png").toExternalForm());
        mSelDownImg = new Image(ClassLoader.getSystemResource("images/sprites/button_arrow_down_selected.png").toExternalForm());
        mSelClearImg = new Image(ClassLoader.getSystemResource("images/sprites/button_erase_selected.png").toExternalForm());

        pad.setSelection(null);
        pad.setButtonUp(new Rectangle2D(columns[28], rows[1], mGridDimension * 3, mFrameDimension));
        pad.setButtonDown(new Rectangle2D(columns[28], rows[5], mGridDimension * 3, mFrameDimension));
        pad.setButtonLeft(new Rectangle2D(columns[27], rows[3], mFrameDimension, mFrameDimension));
        pad.setButtonRight(new Rectangle2D(columns[30], rows[3], mFrameDimension, mFrameDimension));
        pad.setButtonClear(new Rectangle2D(columns[28], rows[8], mGridDimension * 3, mFrameDimension));

        /*
         * Initialize menu buttons
         */
        mButtonLevelDown = new Rectangle2D(columns[27], rows[3], mFrameDimension, mFrameDimension);
        mButtonLevelUp = new Rectangle2D(columns[29], rows[3], mFrameDimension, mFrameDimension);
        mButtonPlay = new Rectangle2D(columns[27], rows[6], mFrameDimension * 2, mFrameDimension);
        mButtonMenu = new Rectangle2D(columns[30], rows[16], mFrameDimension, mFrameDimension);
        mButtonAbout = new Rectangle2D(columns[27], rows[9], mFrameDimension * 2, mGridDimension);
        mButtonMuteMusic = new Rectangle2D(columns[30], rows[11], mFrameDimension, mFrameDimension);
        mButtonMuteSound = new Rectangle2D(columns[30], rows[13], mFrameDimension, mFrameDimension);
    }

    /**
     * Create game objects on the basis of .dat files
     */
    List<Arrow> mArrows;
    List<Artifact> mArtifacts;
    List<Teleport> mTeleports;
    List<Key> mKeys;
    List<Door> mDoors;
    List<Slot> mSlots;

    void loadLevel(int level) {

        mCurrentFallingFrame = null;
        eist.isMoving = false;
        eist.setKeys(0);
        pad.setSelection(null);

        String lvlNumberToString = (level < 10) ? "0" + String.valueOf(level) : String.valueOf(level);

        mTurnsCounter = 0;
        /*
         * load saved level best score, if any.
         */
        mTurnsBest = prefs.getInt(lvlNumberToString + "best", 0);

        String urlString = "levels/" + lvlNumberToString;

        /*
         * Load board bitmap
         */
        mBoardImg = new Image(ClassLoader.getSystemResource(urlString + "board.png").toExternalForm(), mSceneWidth, mSceneHeight, true, true, false);

        pixelReader = mBoardImg.getPixelReader();

        String dataString;
        /*
         * Load arrows
         */
        mArrows = new ArrayList<>();
        dataString = datToString(urlString + "arrows.dat");
        if (dataString != null && !dataString.isEmpty()) {

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
        }

        /*
         * Load artifacts (called "amulets" in resources due to historical reasons ;)
         */
        mArtifactImg = new Image(urlString + "amulet.png");
        mArtifacts = new ArrayList<>();
        dataString = datToString(urlString + "amulets.dat");
        if (dataString != null) {

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
        }

        /*
         * Load teleports
         */
        mTeleports = new ArrayList<>();
        dataString = datToString(urlString + "teleports.dat");
        if (dataString != null) {

            String[] teleports = dataString.split(":");

            for (String single_entry : teleports) {

                String[] positions = single_entry.split(",");

                int posX = Integer.valueOf(positions[0]);
                int posY = Integer.valueOf(positions[1]);

                Teleport teleport = new Teleport();
                teleport.setPosX(columns[posX]);
                teleport.setPosY(rows[posY]);

                teleport.setArea(innerRect(columns[posX], rows[posY]));
                mTeleports.add(teleport);
            }
        }

        /*
         * Load keys
         */
        mKeyImg = new Image(urlString + "key.png");
        mKeys = new ArrayList<>();
        dataString = datToString(urlString + "keys.dat");
        if (dataString != null) {

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
        }

        /*
         * Load doors
         */
        mDoorHImg = new Image(urlString + "door_h.png");
        mDoorVImg = new Image(urlString + "door_v.png");
        mDoors = new ArrayList<>();
        dataString = datToString(urlString + "doors.dat");
        if (dataString != null) {

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
        }

        /*
         * Load ladder slots
         */
        mSlots = new ArrayList<>();
        dataString = datToString(urlString + "slots.dat");
        if (dataString != null) {

            String[] slots = dataString.split(":");

            for (String single_entry : slots) {

                String[] positions = single_entry.split(",");

                int posX = Integer.valueOf(positions[0]);
                int posY = Integer.valueOf(positions[1]);
                int orientation = Integer.valueOf(positions[2]);

                Slot slot = new Slot();
                slot.setPosX(columns[posX]);
                slot.setPosY(rows[posY]);
                slot.setOrientation(orientation);

                if (slot.getOrientation() == ORIENTATION_VERTICAL) {
                    slot.setArea(new Rectangle2D(columns[posX], rows[posY], mFrameDimension, mGridDimension));
                } else {
                    slot.setArea(new Rectangle2D(columns[posX], rows[posY], mGridDimension, mFrameDimension));
                }
                mSlots.add(slot);
            }
        }

        /*
         * Load ladder bitmaps
         */
        mLadderHImg = new Image(urlString + "ladder_h.png", mFrameDimension, mFrameDimension, true, true, true);
        mLadderVImg = new Image(urlString + "ladder_v.png", mFrameDimension, mFrameDimension, true, true, true);

        /*
         * Load exit bitmaps
         */
        mExitClosedImg = new Image(urlString + "exit_closed.png", mFrameDimension, mFrameDimension, true, true, true);
        mExitOpenImg = new Image(urlString + "exit_open.png", mFrameDimension, mFrameDimension, true, true, true);

        /*
         * Load level data
         */
        dataString = datToString(urlString + "level.dat");
        if (dataString != null) {

            String[] data = dataString.split(",");

            eist.x = columns[Integer.valueOf(data[0])];
            eist.y = rows[Integer.valueOf(data[1])];
            eist.setDirection(Integer.valueOf(data[2]));
            exit.setPosX(columns[Integer.valueOf(data[3])]);
            exit.setPosY(rows[Integer.valueOf(data[4])]);
            exit.setArea(new Rectangle2D(exit.getPosX(), exit.getPosY(), mFrameDimension, mFrameDimension));
            ladder.setSlotIdx(Integer.valueOf(data[5]));
        }

        if (level == 0) {

            if (trackLevelPlayer != null) {
                trackLevelPlayer.stop();
            }
            Task<Void> sleeper = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        System.out.println(e.toString());
                    }
                    return null;
                }
            };
            sleeper.setOnSucceeded(event -> {
                eist.isMoving = true;
                if (trackMainPlayer != null) {
                    trackMainPlayer.play();
                }
            });
            new Thread(sleeper).start();
        } else {
            if (trackMainPlayer != null) {
                trackMainPlayer.stop();
            }
            if (trackLevelPlayer != null) {
                trackLevelPlayer.play();
                trackLevelPlayer.setMute(mMuteMusic);
            }
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

    private String datToString(String urlString) {

        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream(urlString);
        String output = null;

        if (inputStream == null) {
            return null;
        } else {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                output = stringBuilder.toString().trim();

            } catch (IOException e) {
                displayExceptionAlert("Error reading InputStream", e);
            }
            if(output != null && output.equals("")){
                output =  null;
            }
            return output;
        }
    }

    static boolean getRandomBoolean() {
        return Math.random() < 0.5;
    }

    /**
     * On the basis of clicked point coordinates, we need to calculate the place to put the arrow in.
     * Disallowed locations: off the board and on the margin of the board.
     *
     * @param touch_x Clicked point X
     * @param touch_y Clicked point Y
     * @return Adjusted rectangle coordinates.
     */
    private Rectangle2D nearestSquare(double touch_x, double touch_y) {

        double nearest_left = ((int) (touch_x / mGridDimension)) * mGridDimension;
        double nearest_top = ((int) (touch_y / mGridDimension)) * mGridDimension;

        nearest_left = nearest_left - mGridDimension;
        nearest_top = nearest_top - mGridDimension;

        try {

            Rectangle2D adjustedSquare = new Rectangle2D(nearest_left, nearest_top, mFrameDimension, mFrameDimension);

            int top_left_color = pixelReader.getArgb((int) adjustedSquare.getMinX() + 15, (int) adjustedSquare.getMinY() + 15);
            int top_right_color = pixelReader.getArgb((int) adjustedSquare.getMaxX() - 15, (int) adjustedSquare.getMinY() + 15);
            int bottom_left_color = pixelReader.getArgb((int) adjustedSquare.getMinX() + 15, (int) adjustedSquare.getMaxY() - 15);
            int bottom_right_color = pixelReader.getArgb((int) adjustedSquare.getMaxX() - 15, (int) adjustedSquare.getMaxY() - 15);


            // both right stick out -> move LEFT
            if (top_right_color == -16777216 && bottom_right_color == -16777216) {
                adjustedSquare = new Rectangle2D(adjustedSquare.getMinX() - mGridDimension, adjustedSquare.getMinY(), mFrameDimension, mFrameDimension);
            }
            // both bottom stick out -> MOVE UP
            if (bottom_left_color == -16777216 && bottom_right_color == -16777216) {
                adjustedSquare = new Rectangle2D(adjustedSquare.getMinX(), adjustedSquare.getMinY() - mGridDimension, mFrameDimension, mFrameDimension);
            }
            // both left stick out -> MOVE RIGHT
            if (top_left_color == -16777216 && bottom_left_color == -16777216 && top_right_color != -16777216) {
                adjustedSquare = new Rectangle2D(adjustedSquare.getMinX() + mGridDimension, adjustedSquare.getMinY(), mFrameDimension, mFrameDimension);
            }
            // both top stick out -> MOVE DOWN
            if (top_left_color == -16777216 && top_right_color == -16777216 && bottom_left_color != -16777216) {
                adjustedSquare = new Rectangle2D(adjustedSquare.getMinX(), adjustedSquare.getMinY() + mGridDimension, mFrameDimension, mFrameDimension);
            }

            // three corners stick out -> MOVE BOTTOM RIGHT
            if (top_left_color == -16777216 && top_right_color == -16777216 && bottom_left_color == -16777216) {
                adjustedSquare = new Rectangle2D(adjustedSquare.getMinX() + mGridDimension, adjustedSquare.getMinY() + mGridDimension, mFrameDimension, mFrameDimension);
            }

            // just top left corner sticks out -> MOVE BOTTOM RIGHT
            if (top_left_color == -16777216 && top_right_color != -16777216 && bottom_left_color != -16777216) {
                adjustedSquare = new Rectangle2D(adjustedSquare.getMinX() + mGridDimension, adjustedSquare.getMinY() + mGridDimension, mFrameDimension, mFrameDimension);
            }

            // just bottom left corner sticks out -> MOVE TOP RIGHT
            if (bottom_left_color == -16777216 && top_left_color != -16777216 && bottom_right_color != -16777216) {
                adjustedSquare = new Rectangle2D(adjustedSquare.getMinX() + mGridDimension, adjustedSquare.getMinY() - mGridDimension, mFrameDimension, mFrameDimension);
            }

            // just top right corner sticks out -> MOVE BOTTOM LEFT
            if (top_right_color == -16777216 && top_left_color != -16777216 && bottom_right_color != -16777216) {
                adjustedSquare = new Rectangle2D(adjustedSquare.getMinX() - mGridDimension, adjustedSquare.getMinY() + mGridDimension, mFrameDimension, mFrameDimension);
            }

            // Shouldn't happen, but happens: all 4 corners out of the board (WTF?)
            if (top_right_color == -16777216 && top_left_color != -16777216 && bottom_right_color != -16777216 && bottom_left_color != -16777216) {
                adjustedSquare = null;
            }


            return (adjustedSquare);

        } catch (Exception e) {

            System.out.println("Couldn't get square: " + e);
            return (null);
        }
    }

    private void placeArrow(double x, double y) {

        Arrow arrow = new Arrow();
        arrow.setPosX(x);
        arrow.setPosY(y);

        arrow.setArea(innerRect(x, y));
        arrow.setDirection(pad.getSelection());

        mArrows.add(arrow);
    }

    private boolean arrowAllowed(Point2D squareCenter) {

        for (Arrow arrow : mArrows) {
            if (arrow.getArea().contains(squareCenter)) {
                return false;
            }
        }
        for (Artifact artifact : mArtifacts) {
            if (artifact.getArea().contains(squareCenter)) {
                return false;
            }
        }
        for (Key key : mKeys) {
            if (key.getArea().contains(squareCenter)) {
                return false;
            }
        }
        for (Door door : mDoors) {
            if (door.getArea().contains(squareCenter)) {
                return false;
            }
        }
        for (Teleport teleport : mTeleports) {
            if (teleport.getArea().contains(squareCenter)) {
                return false;
            }
        }
        if (exit.getArea().contains(squareCenter)) {
            return false;
        }
        return true;
    }

    private void removeArrow(Rectangle2D pressedSquare) {

        for (Arrow arrow : mArrows) {
            if (arrow.getArea().intersects(pressedSquare)) {
                mArrows.remove(arrow);
                break;
            }
        }
    }

    void displayExceptionAlert(String header, Exception e) {
        if (mErrorAlert != null && mErrorAlert.isShowing()) {
            return;
        }
        mErrorAlert = new Alert(Alert.AlertType.ERROR);
        mErrorAlert.setResizable(true);
        mErrorAlert.setTitle("Error");
        if (header != null) {
            mErrorAlert.setHeaderText(header);
        }

        /*
         * After http://code.makery.ch/blog/javafx-dialogs-official
         */
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Stacktrace:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        mErrorAlert.getDialogPane().setExpandableContent(expContent);

        mErrorAlert.showAndWait();
    }

    private void displayExceptionAlert(Exception e) {
        displayExceptionAlert(null, e);
    }

    private void displayAboutAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About the game");
        alert.setHeaderText("Eist returns\narcade-puzzle game\n\u00a91992-2017 nwg (Piotr Miller)");
        alert.setResizable(true);

        String message = "This program is free software; you can redistribute it and/or modify it under the terms of the GNU " +
                "General Public License version 3.0, as published by the Free Software Foundation." +
                "\n\nhttps://github.com/nwg-piotr/EistReturns\n\nrem = "+ rem;

        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane content = new GridPane();
        content.setMaxWidth(Double.MAX_VALUE);
        content.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(content);

        alert.show();
    }

    void displayNewBestAlert(int oldBest, int newBest) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Congrats!");
        alert.setHeaderText("New best score!");
        alert.setContentText("Old best: " + oldBest + ", yours: " + newBest);
        alert.setResizable(true);

        alert.show();
    }

    private void displaySizeDialog() {
        List<String> choices = new ArrayList<>();
        choices.add("Small");
        choices.add("Medium");
        choices.add("Full screen");

        ChoiceDialog<String> dialog;

        if(mDimensionDivider == 1) {
            dialog = new ChoiceDialog<>("Full screen", choices);
        } else if (mDimensionDivider == 1.5) {
            dialog = new ChoiceDialog<>("Medium", choices);
        } else {
            dialog = new ChoiceDialog<>("Small", choices);
        }
        dialog.setTitle("Setting the game window size");
        dialog.setHeaderText("Select size and restart the game");
        dialog.setContentText("Choose the window size:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            System.out.println("Your choice: " + result.get());
            switch (result.get()) {
                case "Small":
                    prefs.putDouble("divider", 2.0);
                    break;
                case "Medium":
                    prefs.putDouble("divider", 1.5);
                    break;
                case "Full screen":
                    prefs.putDouble("divider", 1.0);
                    break;
                default:
                    break;
            }
            Platform.exit();
        }
    }
}
