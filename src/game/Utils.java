package game;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.media.AudioClip;
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
import game.Sprites.Ornament;
import game.Sprites.Pad;
import game.Sprites.Toolbar;
import javafx.stage.Stage;

import java.io.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

abstract class Utils extends Application {

    double mSceneWidth;
    double mSceneHeight;
    double mCenterX;
    double walkingSpeedPerSecond;

    double mSplashWidth;
    double mSplashHeight;
    double mSplashCenterX;

    int mCurrentLevel = 0;
    int mSelectedLevel = 1;
    int mAchievedLevel = 0;
    /**
     * This was left as final int, since in the future may be replaced with a value stored in prefs.
     */
    final int MAX_LEVEL = 40;
    boolean mGameFinished = false;

    double mDimensionDivider;
    double rem;

    int mCurrentEistFrame = 0;
    int mCurrentArtifactFrame = 0;
    Integer mCurrentFallingFrame = null;

    boolean mEditor = false;
    boolean mTesting = false;

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

    MediaPlayer trackMainPlayer;
    MediaPlayer trackLevelPlayer;

    AudioClip fxBounce;
    AudioClip fxArtifact;
    AudioClip fxKey;
    AudioClip fxDoor;
    AudioClip fxExit;
    AudioClip fxLevelLost;
    AudioClip fxLevelUp;
    private AudioClip fxLadder;
    AudioClip fxTeleport;

    File mUserFolder;
    File mUserLevelsFolder;
    File mEditorFolder;

    boolean mLoadUserLevel;

    Stage mEditorStage;

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
    static final int ORIENTATION_VERTICAL = 1;

    static final int SELECTION_DOOR = 0;
    static final int SELECTION_SLOT = 1;
    static final int SELECTION_ARTIFACT = 2;
    static final int SELECTION_KEY = 3;
    static final int SELECTION_TELEPORT = 4;
    static final int SELECTION_EXIT = 5;
    static final int SELECTION_ARROW = 6;
    static final int SELECTION_ORNAMENT = 7;
    static final int SELECTION_CLEAR = 8;
    static final int SELECTION_EIST = 9;

    String message = "";
    Rectangle2D detectRect;

    Preferences prefs;

    /**
     * Sprites and other game objects come here.
     */
    Player eist;
    Ladder ladder;
    Exit exit;
    Pad pad;
    Toolbar toolbar;

    private Alert mErrorAlert;

    /**
     * To place the game board content (arrows, artifacts, teleports etc), we'll divide it into rows and columns grid.
     * The column is half the width of the Frame. The row is half the height of the Frame.
     */
    double[] rows = new double[19];
    double[] columns = new double[33];

    void setSplash() {
        prefs = Preferences.userNodeForPackage(Main.class);
        mDimensionDivider = prefs.getDouble("divider", 1.5);
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        mSceneWidth = primaryScreenBounds.getWidth() / mDimensionDivider;
        mSplashWidth = mSceneWidth / 2;
        mSplashHeight = (mSplashWidth / 8) * 5;
        mSplashCenterX = mSplashWidth / 2;

    }

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

        /*
         * Create the folder which user can upload custom levels data to.
         */
        mUserFolder = new File(System.getProperty("user.home") + "/.EistReturns");
        if (mUserFolder.mkdir()) {
            System.out.println("\nEistReturns folder created");
        }
        mUserLevelsFolder = new File(System.getProperty("user.home") + "/.EistReturns/levels");
        if (mUserLevelsFolder.mkdir()) {
            System.out.println("Levels folder created");
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
        /*
         * Horizontal center of the game board. Needed to set balance while playing FX sounds.
         */
        mCenterX = columns[13] + mGridDimension / 2;
    }

    /**
     * FPS calculation after https://stackoverflow.com/a/28288527/4040598 by @brian
     */
    static long lastUpdate = 0;
    static int index = 0;
    static double[] frameRates = new double[5];

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

        if (event.getButton() == MouseButton.PRIMARY) {

            if (mButtonMuteMusic.contains(pointClicked)) {

                if (!mEditor || mTesting) {

                    mMuteMusic = !mMuteMusic;
                    if (mCurrentLevel == 0) {
                        if (trackMainPlayer != null) {
                            if (trackMainPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)) {
                                trackMainPlayer.stop();
                            } else {
                                trackMainPlayer.play();
                            }
                        }
                    } else {
                        if (trackLevelPlayer != null) {
                            if (trackLevelPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)) {
                                trackLevelPlayer.stop();
                            } else {
                                trackLevelPlayer.play();
                            }
                        }
                    }
                    prefs.putBoolean("mmusic", mMuteMusic);
                }
            }
            if (!mEditor || mTesting) {
                if (mButtonMuteSound.contains(pointClicked)) {
                    mMuteSound = !mMuteSound;
                    prefs.putBoolean("msound", mMuteSound);
                }
            }
            if (mButtonAbout.contains(pointClicked) && mCurrentLevel == 0) {
                displayAboutAlert();
            }

            if (mCurrentLevel != 0) {

                /*
                 * Check whether menu or board clicked
                 */
                if (pointClicked.getX() > columns[26]) {

                    if (!mEditor || mTesting) {
                        /*
                         * In-game Menu clicked. Check which part.
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

                        message = "";

                        if(toolbar.getOpenArea().contains(pointClicked)){
                            displayLevelChoiceDialog();
                        }

                        if (toolbar.getDoorArea().contains(pointClicked)) {
                            toolbar.setSelection(SELECTION_DOOR);
                            message = "Click path to add\nRight click rotates";
                        } else if (toolbar.getSlotArea().contains(pointClicked)) {
                            toolbar.setSelection(SELECTION_SLOT);
                            message = "Click black to add\nRight click rotates";
                        } else if (toolbar.getArtifactArea().contains(pointClicked)) {
                            toolbar.setSelection(SELECTION_ARTIFACT);
                            message = "Click path to add";
                        } else if (toolbar.getKeyArea().contains(pointClicked)) {
                            toolbar.setSelection(SELECTION_KEY);
                            message = "Click path to add";
                        } else if (toolbar.getTeleportArea().contains(pointClicked)) {
                            if(mTeleports.size() < 2) {
                                toolbar.setSelection(SELECTION_TELEPORT);
                                message = "Click path to add";
                            } else {
                                message = "2 teleports allowed";
                            }
                        } else if (toolbar.getExitArea().contains(pointClicked)) {
                            toolbar.setSelection(SELECTION_EXIT);
                            message = "Click path to move";
                        } else if (toolbar.getArrowArea().contains(pointClicked)) {
                            toolbar.setSelection(SELECTION_ARROW);
                            message = "Click path to add\nRight click rotates";
                        } else if (toolbar.getOrnamentArea().contains(pointClicked)) {
                            toolbar.setSelection(SELECTION_ORNAMENT);
                            message = "Click anywhere to add";
                        } else if (toolbar.getClearArea().contains(pointClicked)) {
                            toolbar.setSelection(SELECTION_CLEAR);
                            message = "Click object to delete";
                        } else if (toolbar.getEistArea().contains(pointClicked)) {
                            toolbar.setSelection(SELECTION_EIST);
                            message = "Click path to move\nRight click rotates";
                        } else {
                            toolbar.setSelection(null);
                        }
                    }

                } else {

                    /*
                     * Board clicked
                     */
                    if (!mEditor) {
                        eist.isMoving = true;
                    } else {
                        if (mTesting) {
                            eist.isMoving = true;
                        }
                    }

                    for (Slot slot : mSlots) {

                        if (slot.getArea().contains(pointClicked)) {

                            int clickedSlotIdx = mSlots.indexOf(slot);

                            if (ladder.getSlotIdx() == null) {

                                if (!mMuteSound) {
                                    fxLadder.setBalance(calculateBalance(mSlots.get(clickedSlotIdx).getPosX()));
                                    fxLadder.play();
                                }
                                ladder.setSlotIdx(clickedSlotIdx);

                            } else {

                                if (clickedSlotIdx == ladder.getSlotIdx()) {

                                    if (!mMuteSound) {
                                        fxLadder.setBalance(calculateBalance(mSlots.get(clickedSlotIdx).getPosX()));
                                        fxLadder.play();
                                    }
                                    ladder.setSlotIdx(null);
                                }
                            }

                        }
                    }

                    /*
                     * In-game: place or remove arrows if selected
                     */
                    if (pad.getSelection() != null && pixelReader.getArgb((int) pointClicked.getX(), (int) pointClicked.getY()) != -16777216) {

                        Rectangle2D pressedSquare = nearestAdjustedSquare(pointClicked.getX(), pointClicked.getY());

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

                    /*
                     * In editor: place / remove selected item
                     */
                    if (toolbar != null && toolbar.getSelection() != null) {
                        /*
                        If path clicked
                         */
                        if (pixelReader.getArgb((int) pointClicked.getX(), (int) pointClicked.getY()) != -16777216) {

                            Rectangle2D pressedSquare = nearestAdjustedSquare(pointClicked.getX(), pointClicked.getY());
                            Point2D checkPoint = null;

                            if (pressedSquare != null) {
                                checkPoint = new Point2D(pressedSquare.getMinX() + mGridDimension, pressedSquare.getMinY() + mGridDimension);

                                switch (toolbar.getSelection()) {
                                    case SELECTION_DOOR:
                                        if (arrowAllowed(checkPoint)) {
                                            placeDoor(pressedSquare.getMinX(), pressedSquare.getMinY());
                                        }
                                        break;
                                    case SELECTION_ARTIFACT:
                                        if (arrowAllowed(checkPoint)) {
                                            placeArtifact(pressedSquare.getMinX(), pressedSquare.getMinY());
                                        }
                                        break;
                                    case SELECTION_KEY:
                                        if (arrowAllowed(checkPoint)) {
                                            placeKey(pressedSquare.getMinX(), pressedSquare.getMinY());
                                        }
                                        break;
                                    case SELECTION_TELEPORT:
                                        if (arrowAllowed(checkPoint)) {
                                            placeTeleport(pressedSquare.getMinX(), pressedSquare.getMinY());
                                        }
                                        break;
                                    case SELECTION_ARROW:
                                        if (arrowAllowed(checkPoint)) {
                                            placeArrowEditor(pressedSquare.getMinX(), pressedSquare.getMinY());
                                        }
                                        break;
                                    case SELECTION_EXIT:
                                        if (arrowAllowed(checkPoint)) {
                                            placeExit(pressedSquare.getMinX(), pressedSquare.getMinY());
                                        }
                                        break;
                                    case SELECTION_EIST:
                                        if (arrowAllowed(checkPoint)) {
                                            placeEist(pressedSquare.getMinX(), pressedSquare.getMinY());
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                        if (toolbar.getSelection() == SELECTION_ORNAMENT) {
                            Rectangle2D rectangle2D = nearestSquare(pointClicked.getX(), pointClicked.getY());
                            placeOrnament(rectangle2D.getMinX(), rectangle2D.getMinY());
                        }
                        if(toolbar.getSelection() == SELECTION_SLOT &&
                                pixelReader.getArgb((int) pointClicked.getX(), (int) pointClicked.getY()) == -16777216){
                            Rectangle2D area = nearestSlot(pointClicked.getX(), pointClicked.getY(), toolbar.getSlotOrientation());
                            placeSlot(area, toolbar.getSlotOrientation());
                        }
                        if (toolbar.getSelection() == SELECTION_CLEAR) {
                            deleteIfFound(pointClicked);
                        }
                    }

                }

                if (mButtonMenu.contains(pointClicked)) {

                    if (!mEditor) {
                        mSelectedLevel = mCurrentLevel;
                        mCurrentLevel = 0;
                        loadLevel(mCurrentLevel);
                    } else {
                        mTesting = !mTesting;
                    }
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

                    displaySizeDialog();
                }
            }
        } else if (event.getButton() == MouseButton.SECONDARY) {

            if (mEditor && !mTesting) {

                message = "";

                if (toolbar.getDoorArea().contains(pointClicked)) {
                    toolbar.setSelection(SELECTION_DOOR);
                    message = "Click path to add\nRight click rotates";
                    if (toolbar.getDoorOrientation() == ORIENTATION_HORIZONTAL) {
                        toolbar.setDoorOrientation(ORIENTATION_VERTICAL);
                    } else {
                        toolbar.setDoorOrientation(ORIENTATION_HORIZONTAL);
                    }
                }
                if (toolbar.getSlotArea().contains(pointClicked)) {
                    toolbar.setSelection(SELECTION_SLOT);
                    message = "Click black to add\nRight click rotates";
                    if (toolbar.getSlotOrientation() == ORIENTATION_HORIZONTAL) {
                        toolbar.setSlotOrientation(ORIENTATION_VERTICAL);
                    } else {
                        toolbar.setSlotOrientation(ORIENTATION_HORIZONTAL);
                    }
                }
                if (toolbar.getArrowArea().contains(pointClicked)) {
                    toolbar.setSelection(SELECTION_ARROW);
                    message = "Click path to add\nRight click rotates";
                    switch (toolbar.getArrowDirection()) {
                        case DIR_RIGHT:
                            toolbar.setArrowDirection(DIR_DOWN);
                            break;
                        case DIR_DOWN:
                            toolbar.setArrowDirection(DIR_LEFT);
                            break;
                        case DIR_LEFT:
                            toolbar.setArrowDirection(DIR_UP);
                            break;
                        case DIR_UP:
                            toolbar.setArrowDirection(DIR_RIGHT);
                            break;
                    }
                }
                if (toolbar.getEistArea().contains(pointClicked)){
                    toolbar.setSelection(SELECTION_EIST);
                    message = "Click path to move\nRight click rotates";
                    switch (eist.getDirection()){
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
                    }
                }
                if (toolbar.getArtifactArea().contains(pointClicked)) {
                    toolbar.setSelection(SELECTION_ARTIFACT);
                    message = "Click path to add";
                }
                if (toolbar.getKeyArea().contains(pointClicked)) {
                    toolbar.setSelection(SELECTION_KEY);
                    message = "Click path to add";
                }
                if (toolbar.getTeleportArea().contains(pointClicked)) {
                    if (mTeleports.size() < 2) {
                        toolbar.setSelection(SELECTION_TELEPORT);
                        message = "Click path to add";
                    } else {
                        message = "2 teleports allowed";
                    }
                }
                if (toolbar.getExitArea().contains(pointClicked)) {
                    toolbar.setSelection(SELECTION_EXIT);
                    message = "Click path to move";
                }
                if (toolbar.getOrnamentArea().contains(pointClicked)) {
                    toolbar.setSelection(SELECTION_ORNAMENT);
                    message = "Click anywhere to add";
                }
                if (toolbar.getClearArea().contains(pointClicked)) {
                    toolbar.setSelection(SELECTION_CLEAR);
                    message = "Click object to delete";
                }
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

    Image mSlotHImg;
    Image mSlotHToolbarImg;
    Image mSlotVImg;
    Image mSlotVToolbarImg;
    Image mToolbarEraseImg;

    Image mSelRightImg;
    Image mSelDownImg;
    Image mSelLeftImg;
    Image mSelUpImg;
    Image mSelClearImg;

    Image mArtifactImg;
    Image mOrnamentImg;
    Image mTeleportImg;
    Image mKeyImg;
    Image mDoorHImg;
    Image mDoorVImg;

    Image mToolbarMenuImg;

    Image mLadderImg;
    Image mLadderHImg;
    Image mLadderVImg;

    Image mExitClosedImg;
    Image mExitOpenImg;

    Image mIntro01;
    Image mIntro02;
    Image mIntro03;
    Image mIntro04;
    Image mIntroFinished;

    Image mMutedMusicImg;
    Image mMutedSoundImg;

    void loadCommonGraphics() {

        /*
         * Load media
         */

        try {
            trackMainPlayer = new MediaPlayer(new Media(ClassLoader.getSystemResource("sounds/eist.mp3").toExternalForm()));
            trackMainPlayer.setVolume(0.4);
            trackMainPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            trackLevelPlayer = new MediaPlayer(new Media(ClassLoader.getSystemResource("sounds/eist_ingame.mp3").toExternalForm()));
            trackLevelPlayer.setVolume(0.3);
            trackLevelPlayer.setCycleCount(MediaPlayer.INDEFINITE);

        } catch (MediaException e) {

            displayExceptionAlert("Media player error (missing ffmpeg codec?)", e);
        }

        try {
            fxBounce = new AudioClip(ClassLoader.getSystemResource("sounds/bounce.wav").toExternalForm());
            fxBounce.setVolume(1);

            fxArtifact = new AudioClip(ClassLoader.getSystemResource("sounds/amulet.wav").toExternalForm());
            fxArtifact.setVolume(1);

            fxKey = new AudioClip(ClassLoader.getSystemResource("sounds/key.wav").toExternalForm());
            fxKey.setVolume(1);

            fxDoor = new AudioClip(ClassLoader.getSystemResource("sounds/door_open.wav").toExternalForm());
            fxDoor.setVolume(1);

            fxExit = new AudioClip(ClassLoader.getSystemResource("sounds/exit.wav").toExternalForm());
            fxExit.setVolume(1);

            fxLevelLost = new AudioClip(ClassLoader.getSystemResource("sounds/level_lost.wav").toExternalForm());
            fxLevelLost.setVolume(1);

            fxLevelUp = new AudioClip(ClassLoader.getSystemResource("sounds/level.wav").toExternalForm());
            fxLevelUp.setVolume(1);

            fxLadder = new AudioClip(ClassLoader.getSystemResource("sounds/ladder.wav").toExternalForm());
            fxLadder.setVolume(1);

            fxTeleport = new AudioClip(ClassLoader.getSystemResource("sounds/teleport.wav").toExternalForm());
            fxTeleport.setVolume(1);

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

        mSlotHImg = new Image(ClassLoader.getSystemResource("images/sprites/slot_h.png").toExternalForm());
        mSlotHToolbarImg = new Image(ClassLoader.getSystemResource("images/sprites/slot_h_toolbar.png").toExternalForm());
        mSlotVImg = new Image(ClassLoader.getSystemResource("images/sprites/slot_v.png").toExternalForm());
        mSlotVToolbarImg = new Image(ClassLoader.getSystemResource("images/sprites/slot_v_toolbar.png").toExternalForm());
        mToolbarEraseImg = new Image(ClassLoader.getSystemResource("images/sprites/toolbar_erase.png").toExternalForm());
        mToolbarMenuImg = new Image(ClassLoader.getSystemResource("images/sprites/toolbar_menu.png").toExternalForm());

        mTeleportImg = new Image("images/sprites/teleport.png");

        mIntro01 = new Image(ClassLoader.getSystemResource("images/common/intro01.png").toExternalForm());
        mIntro02 = new Image(ClassLoader.getSystemResource("images/common/intro02.png").toExternalForm());
        mIntro03 = new Image(ClassLoader.getSystemResource("images/common/intro03.png").toExternalForm());
        mIntro04 = new Image(ClassLoader.getSystemResource("images/common/intro04.png").toExternalForm());
        mIntroFinished = new Image(ClassLoader.getSystemResource("images/common/you_won.png").toExternalForm());

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
    List<Ornament> mOrnaments;

    void loadLevel(int level) {

        System.out.println("\nLOADING LEVEL " + level + "\n");

        mEditor = false;

        mCurrentFallingFrame = null;
        eist.isMoving = false;
        eist.setKeys(0);
        pad.setSelection(null);

        mAchievedLevel = prefs.getInt("achieved", 0);

        if (mGameFinished) {
            mIntro01 = mIntroFinished;
            fxLevelUp.play();
        }

        mTurnsCounter = 0;

        String urlString;

        String lvlNumberToString = (level < 10) ? "0" + String.valueOf(level) : String.valueOf(level);
            /*
             * load saved level best score, if any.
             */
        mTurnsBest = prefs.getInt(lvlNumberToString + "best", 0);

        urlString = "levels/" + lvlNumberToString + "/";

        /*
         * Check if user-defined folder exists
         */
        if (level > 0) {

            File userLevel = new File(System.getProperty("user.home") + "/.EistReturns/" + urlString);

            if (userLevel.exists()) {

                mLoadUserLevel = true;

                List<String> userLevelLoadingReport = new ArrayList<>();

                System.out.println("Found user-defined level: " + userLevel.toString() + "\n");
                    /*
                     * Let's check if all the necessary data exist in the user-defined folder.
                     * Skip arrows, teleports and ornaments, as the don't have to exist on each level.
                     */
                System.out.println("Checking integrity:");

                File checkMe = new File(userLevel + "/board.png");
                if (checkMe.exists()) {
                    System.out.println("+Board image found.");
                } else {
                    System.out.println("-Board image not found.");
                    userLevelLoadingReport.add("Missing " + userLevel + "/board.png");
                    mLoadUserLevel = false;
                }

                checkMe = new File(userLevel + "/amulet.png");
                if (checkMe.exists()) {
                    System.out.println("+Artifact image found.");
                } else {
                    System.out.println("-Artifact image not found.");
                    userLevelLoadingReport.add("Missing " + userLevel + "/amulet.png");
                    mLoadUserLevel = false;
                }

                checkMe = new File(userLevel + "/amulets.dat");
                if (checkMe.exists()) {
                    System.out.println("+Artifacts data found.");
                } else {
                    System.out.println("-Artifacts data not found.");
                    userLevelLoadingReport.add("Missing " + userLevel + "/amulets.dat");
                    mLoadUserLevel = false;
                }

                checkMe = new File(userLevel + "/door_h.png");
                if (checkMe.exists()) {
                    System.out.println("+Horizontal door image found.");
                } else {
                    System.out.println("-Horizontal door image not found.");
                    userLevelLoadingReport.add("Missing " + userLevel + "/door_h.png");
                    mLoadUserLevel = false;
                }

                checkMe = new File(userLevel + "/door_v.png");
                if (checkMe.exists()) {
                    System.out.println("+Vertical door image found.");
                } else {
                    System.out.println("-Vertical door image not found.");
                    userLevelLoadingReport.add("Missing " + userLevel + "/door_v.png");
                    mLoadUserLevel = false;
                }

                checkMe = new File(userLevel + "/doors.dat");
                if (checkMe.exists()) {
                    System.out.println("+Doors data found.");
                } else {
                    System.out.println("-Doors data not found.");
                    userLevelLoadingReport.add("Missing " + userLevel + "/doors.dat");
                    mLoadUserLevel = false;
                }

                checkMe = new File(userLevel + "/exit_closed.png");
                if (checkMe.exists()) {
                    System.out.println("+Closed exit image found.");
                } else {
                    System.out.println("-Closed exit image not found.");
                    userLevelLoadingReport.add("Missing " + userLevel + "/exit_closed.png");
                    mLoadUserLevel = false;
                }

                checkMe = new File(userLevel + "/exit_open.png");
                if (checkMe.exists()) {
                    System.out.println("+Open exit image found.");
                } else {
                    System.out.println("-Open exit image not found.");
                    userLevelLoadingReport.add("Missing " + userLevel + "/exit_open.png");
                    mLoadUserLevel = false;
                }

                checkMe = new File(userLevel + "/key.png");
                if (checkMe.exists()) {
                    System.out.println("+Key image found.");
                } else {
                    System.out.println("-Key image not found.");
                    userLevelLoadingReport.add("Missing " + userLevel + "/key.png");
                    mLoadUserLevel = false;
                }

                checkMe = new File(userLevel + "/keys.dat");
                if (checkMe.exists()) {
                    System.out.println("+Keys data found.");
                } else {
                    System.out.println("-Keys data not found.");
                    userLevelLoadingReport.add("Missing " + userLevel + "/keys.dat");
                    mLoadUserLevel = false;
                }

                checkMe = new File(userLevel + "/ladder_h.png");
                if (checkMe.exists()) {
                    System.out.println("+Horizontal ladder image found.");
                } else {
                    System.out.println("-Horizontal ladder image not found.");
                    userLevelLoadingReport.add("Missing " + userLevel + "/ladder_h.png");
                    mLoadUserLevel = false;
                }

                checkMe = new File(userLevel + "/ladder_v.png");
                if (checkMe.exists()) {
                    System.out.println("+Vertical ladder image found.");
                } else {
                    System.out.println("-Vertical ladder image not found.");
                    userLevelLoadingReport.add("Missing " + userLevel + "/ladder_v.png");
                    mLoadUserLevel = false;
                }

                checkMe = new File(userLevel + "/slots.dat");
                if (checkMe.exists()) {
                    System.out.println("+Ladder slots data found.");
                } else {
                    System.out.println("-Ladder slots data not found.");
                    userLevelLoadingReport.add("Missing " + userLevel + "/slots.dat");
                    mLoadUserLevel = false;
                }

                checkMe = new File(userLevel + "/level.dat");
                if (checkMe.exists()) {
                    System.out.println("+level.dat file found.");
                } else {
                    System.out.println("-level.dat file not found.");
                    userLevelLoadingReport.add("Missing " + userLevel + "/level.dat");
                    mLoadUserLevel = false;
                }

                if (mLoadUserLevel) {
                    urlString = userLevel.toURI().toString();
                    System.out.println("\nAll necessary level data present. Loading user-defined level.");

                } else {

                    System.out.println("\nEssential level file(s) missing. Loading default level.\n");
                    StringBuilder missingFilesList = new StringBuilder();
                    for (String string : userLevelLoadingReport) {
                        missingFilesList.append(string);
                        missingFilesList.append("\n");
                    }
                    displayMissingUserFiles(missingFilesList.toString());
                }

            } else {
                mLoadUserLevel = false;
                System.out.println("User-defined level not found. Loading from resources.\n");
            }

        } else {
            mLoadUserLevel = false;
        }

        /*
         * Load board bitmap
         */
        System.out.println("Loading board from: " + urlString + "board.png");
        mBoardImg = new Image(urlString + "board.png", mSceneWidth, mSceneHeight, true, true, false);

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
         * Load ornaments
         */
        mOrnamentImg = new Image(urlString + "ornament.png");
        mOrnaments = new ArrayList<>();
        dataString = datToString(urlString + "ornaments.dat");
        if (dataString != null) {

            String[] ornaments = dataString.split(":");

            for (String single_entry : ornaments) {

                String[] positions = single_entry.split(",");

                int posX = Integer.valueOf(positions[0]);
                int posY = Integer.valueOf(positions[1]);

                Ornament ornament = new Ornament();
                ornament.setPosX(columns[posX]);
                ornament.setPosY(rows[posY]);

                mOrnaments.add(ornament);
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

            if (trackLevelPlayer != null && trackLevelPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)) {
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
                if (trackMainPlayer != null && !mMuteMusic && !trackMainPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)) {
                    trackMainPlayer.play();
                }
            });
            new Thread(sleeper).start();

        } else {

            if (trackMainPlayer != null && trackMainPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)) {
                trackMainPlayer.stop();
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
                if (trackLevelPlayer != null && !mMuteMusic && !trackLevelPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)
                        && !mEditor) {
                    trackLevelPlayer.play();
                }
            });
            new Thread(sleeper).start();
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

    /**
     * Loading files for Editor varies quite much. Let's use a separate method
     */
    void loadEditor() {

        System.out.println("\nLOADING EDITOR\n");
        mEditor = true;
        mTesting = false;

        mCurrentFallingFrame = null;
        eist.isMoving = false;
        eist.setKeys(0);
        pad.setSelection(null);

        mTurnsCounter = 0;

        String urlString;

        File userLevel = new File(System.getProperty("user.home") + "/.EistReturns/levels/editor-data/");
        urlString = userLevel.toURI().toString();

        /*
         * Load board bitmap
         */
        System.out.println("Loading board from: " + urlString + "board.png");
        mBoardImg = new Image(urlString + "board.png", mSceneWidth, mSceneHeight, true, true, false);

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
         * Load ornaments
         */
        mOrnamentImg = new Image(urlString + "ornament.png");
        mOrnaments = new ArrayList<>();
        dataString = datToString(urlString + "ornaments.dat");
        if (dataString != null) {

            String[] ornaments = dataString.split(":");

            for (String single_entry : ornaments) {

                String[] positions = single_entry.split(",");

                int posX = Integer.valueOf(positions[0]);
                int posY = Integer.valueOf(positions[1]);

                Ornament ornament = new Ornament();
                ornament.setPosX(columns[posX]);
                ornament.setPosY(rows[posY]);

                mOrnaments.add(ornament);
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

        } else {
            // error loading file, set replacement data
            eist.x = columns[1];
            eist.y = rows[1];
            eist.setDirection(2);
            exit.setPosX(columns[24]);
            exit.setPosY(rows[15]);
            exit.setArea(new Rectangle2D(exit.getPosX(), exit.getPosY(), mFrameDimension, mFrameDimension));
            ladder.setSlotIdx(0);

        }
        /*
         * Just in case the slots.dat file not found:
         */
        if(mSlots == null || mSlots.size() == 0){
            ladder.setSlotIdx(null);
        }

        if (trackMainPlayer != null && trackMainPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)) {
            trackMainPlayer.stop();
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
            if (trackLevelPlayer != null && !mMuteMusic && !trackLevelPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)) {
                trackLevelPlayer.play();
            }
        });
        new Thread(sleeper).start();
    }

    private String datToString(String urlString) {

        System.out.println("Loading " + urlString);

        StringBuilder stringBuilder = new StringBuilder();

        InputStream inputStream;

        if (!mLoadUserLevel && !mEditor) {
            inputStream = Utils.class.getClassLoader().getResourceAsStream(urlString);
        } else {
            try {
                // This should be done better, still I don't know how...
                inputStream = new FileInputStream(urlString.substring(5));
            } catch (FileNotFoundException e) {
                inputStream = null;
            }
        }
        String output = null;

        if (inputStream == null) {
            System.out.println("inputStream = null, couldn't read");
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
            if (output != null && output.equals("")) {
                output = null;
            }
            return output;
        }
    }

    static boolean getRandomBoolean() {
        return Math.random() < 0.5;
    }

    private Rectangle2D nearestSquare(double touch_x, double touch_y) {

        double nearest_left = ((int) (touch_x / mGridDimension)) * mGridDimension;
        double nearest_top = ((int) (touch_y / mGridDimension)) * mGridDimension;

        nearest_left = nearest_left - mGridDimension;
        nearest_top = nearest_top - mGridDimension;

        return new Rectangle2D(nearest_left, nearest_top, mFrameDimension, mFrameDimension);
    }

    private Rectangle2D nearestSlot(double touch_x, double touch_y, int orientation) {

        double nearest_left = ((int) (touch_x / mGridDimension)) * mGridDimension;
        double nearest_top = ((int) (touch_y / mGridDimension)) * mGridDimension;

        if(orientation == ORIENTATION_VERTICAL) {
            return new Rectangle2D(nearest_left, nearest_top, mFrameDimension, mGridDimension);
        } else {
            return new Rectangle2D(nearest_left, nearest_top, mGridDimension, mFrameDimension);
        }
    }

    /**
     * On the basis of clicked point coordinates, we need to calculate the place to put the arrow in.
     * Disallowed locations: off the board and on the margin of the board.
     *
     * @param touch_x Clicked point X
     * @param touch_y Clicked point Y
     * @return Adjusted rectangle coordinates.
     */
    private Rectangle2D nearestAdjustedSquare(double touch_x, double touch_y) {

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


            // both right corner sticks out -> move LEFT
            if (top_right_color == -16777216 && bottom_right_color == -16777216) {
                adjustedSquare = new Rectangle2D(adjustedSquare.getMinX() - mGridDimension, adjustedSquare.getMinY(), mFrameDimension, mFrameDimension);
            }
            // both bottom corners stick out -> MOVE UP
            if (bottom_left_color == -16777216 && bottom_right_color == -16777216) {
                adjustedSquare = new Rectangle2D(adjustedSquare.getMinX(), adjustedSquare.getMinY() - mGridDimension, mFrameDimension, mFrameDimension);
            }
            // both left corner sticks out -> MOVE RIGHT
            if (top_left_color == -16777216 && bottom_left_color == -16777216 && top_right_color != -16777216) {
                adjustedSquare = new Rectangle2D(adjustedSquare.getMinX() + mGridDimension, adjustedSquare.getMinY(), mFrameDimension, mFrameDimension);
            }
            // both top corners stick out -> MOVE DOWN
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
            return null;
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

    private void placeDoor(double x, double y) {

        Door door = new Door();
        door.setPosX(x);
        door.setPosY(y);
        door.setArea(innerRect(x, y));
        door.setOrientation(toolbar.getDoorOrientation());
        mDoors.add(door);
    }

    /*
     * Placing ladder spots will demand different method to check if the spot is allowed here.
     * Skipping now...
     */

    private void placeArtifact(double x, double y) {

        Artifact artifact = new Artifact();
        artifact.setPosX(x);
        artifact.setPosY(y);
        artifact.setArea(innerRect(x, y));
        mArtifacts.add(artifact);
    }

    private void placeKey(double x, double y) {

        Key key = new Key();
        key.setPosX(x);
        key.setPosY(y);
        key.setArea(innerRect(x, y));
        mKeys.add(key);
    }

    private void placeTeleport(double x, double y) {

        if(mTeleports.size() < 2) {
            Teleport teleport = new Teleport();
            teleport.setPosX(x);
            teleport.setPosY(y);
            teleport.setArea(innerRect(x, y));
            mTeleports.add(teleport);
        }
    }

    private void placeArrowEditor(double x, double y) {

        Arrow arrow = new Arrow();
        arrow.setPosX(x);
        arrow.setPosY(y);
        arrow.setArea(innerRect(x, y));
        arrow.setDirection(toolbar.getArrowDirection());
        mArrows.add(arrow);
    }

    private void placeOrnament(double x, double y) {

        Ornament ornament = new Ornament();
        ornament.setPosX(x);
        ornament.setPosY(y);
        mOrnaments.add(ornament);
    }

    private void placeSlot(Rectangle2D area, int orientation) {

        Slot slot = new Slot();
        slot.setPosX(area.getMinX());
        slot.setPosY(area.getMinY());
        slot.setArea(area);
        slot.setOrientation(orientation);

        mSlots.add(slot);
        if(mSlots.size() == 1){
            ladder.setSlotIdx(0);
        }
    }

    private void placeExit(double x, double y) {

        exit.setPosX(x);
        exit.setPosY(y);
        exit.setArea(new Rectangle2D(exit.getPosX(), exit.getPosY(), mFrameDimension, mFrameDimension));
    }

    private void placeEist(double x, double y) {

        eist.x = x;
        eist.y = y;
        eist.setArea(new Rectangle2D(exit.getPosX(), exit.getPosY(), mFrameDimension, mFrameDimension));
    }

    private void deleteIfFound(Point2D checkPoint) {

        for(Door door : mDoors){
            detectRect = new Rectangle2D(door.getPosX(), door.getPosY(), mFrameDimension, mFrameDimension);
            if (detectRect.contains(checkPoint)){
                mDoors.remove(door);
                break;
            }
        }

        for(Artifact artifact : mArtifacts){
            detectRect = new Rectangle2D(artifact.getPosX(), artifact.getPosY(), mFrameDimension, mFrameDimension);
            if (detectRect.contains(checkPoint)){
                mArtifacts.remove(artifact);
                break;
            }
        }

        for(Key key : mKeys){
            detectRect = new Rectangle2D(key.getPosX(), key.getPosY(), mFrameDimension, mFrameDimension);
            if (detectRect.contains(checkPoint)){
                mKeys.remove(key);
                break;
            }
        }

        for(Teleport teleport : mTeleports){
            detectRect = new Rectangle2D(teleport.getPosX(), teleport.getPosY(), mFrameDimension, mFrameDimension);
            if (detectRect.contains(checkPoint)){
                mTeleports.remove(teleport);
                break;
            }
        }

        for(Arrow arrow : mArrows){
            detectRect = new Rectangle2D(arrow.getPosX(), arrow.getPosY(), mFrameDimension, mFrameDimension);
            if (detectRect.contains(checkPoint)){
                mArrows.remove(arrow);
                break;
            }
        }

        for(Ornament ornament : mOrnaments){
            detectRect = new Rectangle2D(ornament.getPosX(), ornament.getPosY(), mFrameDimension, mFrameDimension);
            if (detectRect.contains(checkPoint)){
                mOrnaments.remove(ornament);
                break;
            }
        }

        for (Slot slot : mSlots){
            if(slot.getArea().contains(checkPoint)){
                mSlots.remove(slot);

                if(mSlots.size() > 0){
                    ladder.setSlotIdx(0);
                } else {
                    ladder.setSlotIdx(null);
                }
                break;
            }
        }
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

    /**
     * Calculates stereo balance for AudioClip to play the sound FX
     *
     * @param eistX - current Eists X coordinate will be compared to horizontal center of the game board
     * @return double in range -1.0 to 1.0
     */
    double calculateBalance(double eistX) {
        if (eistX < mCenterX) {
            return -(mCenterX - eistX) / mCenterX;
        } else if (eistX > mCenterX) {
            return (eistX - mCenterX) / mCenterX;
        } else {
            return 0.0;
        }
    }

    private void displayExceptionAlert(String header, Exception e) {
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

    private void displayMissingUserFiles(String content) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setResizable(true);
        alert.setTitle("Error loading data");
        alert.setHeaderText("User defined level misses required files");

        Label label = new Label("Missing files:");

        TextArea textArea = new TextArea(content);
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

        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    private void displayAboutAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About the game");
        alert.setHeaderText("Eist returns\narcade-puzzle game\n\u00a91992-2017 nwg (Piotr Miller)");
        alert.setResizable(true);

        String message = "This program is free software; you can redistribute it and/or modify it under the terms of the GNU " +
                "General Public License version 3.0, as published by the Free Software Foundation. Find the source code at" +
                " https://github.com/nwg-piotr/EistReturns" +
                "\n\nThe game was created with the following software:" +
                "\nGraphics: Inkscape, GIMP" +
                "\nSounds: LMMS, Audacity" +
                "\nIDE: IntelliJ IDEA Community Edition" +
                "\nSystem: Manjaro Linux, Antergos Linux";

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

        if (mDimensionDivider == 1) {
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

    void displayLevelChoiceDialog(){
        List<String> levels = new ArrayList<>();
        for(int i = 1; i < MAX_LEVEL +1; i++) {
            levels.add(String.valueOf(i));
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>("1", levels);
        dialog.setTitle("Importing");
        dialog.setHeaderText("Select level to import");
        dialog.setContentText("Level:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            setEditorFiles(Integer.valueOf(result.get()));
            loadEditor();
        }

    }

    private void setEditorFiles(Integer level) {

        mEditorFolder = new File(System.getProperty("user.home") + "/.EistReturns/levels/editor-data");
        if (mEditorFolder.mkdir()) {

            /*
             * Editor folder not found. Let's create it and import sample level 03 data
             */
            System.out.println("Editor folder created, importing sample data...");

            importEditorFiles(3);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sample data created");
            alert.setHeaderText(null);
            alert.setContentText("First run - sample Level 3 data imported");

            alert.showAndWait();
        }

        if(level != null){

            /*
             * Importing predefined level on demand
             */
            importEditorFiles(level);

        } else {

            /*
             * Opening last editor state. Let's check if user didn't remove required files.
             * If so, substitute them with ones taken from the sample Level 03.
             */
            String missingFiles = "";

            if(!isFilePresent("board.png")){
                missingFiles = "\n- board.png";
            }
            if(!isFilePresent("door_h.png")){
                missingFiles += "\n- door_h.png";
            }
            if(!isFilePresent("door_v.png")){
                missingFiles += "\n- door_v.png";
            }
            if(!isFilePresent("exit_closed.png")){
                missingFiles += "\n- exit_closed.png";
            }
            if(!isFilePresent("exit_open.png")){
                missingFiles += "\n- exit_open.png";
            }
            if(!isFilePresent("key.png")){
                missingFiles += "\n- key.png";
            }
            if(!isFilePresent("ladder_h.png")){
                missingFiles += "\n- ladder_h.png";
            }
            if(!isFilePresent("ladder_v.png")){
                missingFiles += "\n- ladder_v.png";
            }
            if(!isFilePresent("ornament.png")){
                missingFiles += "\n- ornament.png";
            }
            if(!isFilePresent("ornament.png")){
                missingFiles += "\n- ornament.png";
            }
            if(!isFilePresent("amulets.dat")){
                missingFiles += "\n- amulets.dat";
            }
            if(!isFilePresent("arrows.dat")){
                missingFiles += "\n- arrows.dat";
            }
            if(!isFilePresent("doors.dat")){
                missingFiles += "\n- doors.dat";
            }
            if(!isFilePresent("keys.dat")){
                missingFiles += "\n- keys.dat";
            }
            if(!isFilePresent("level.dat")){
                missingFiles += "\n- level.dat";
            }
            if(!isFilePresent("slots.dat")){
                missingFiles += "\n- slots.dat";
            }
            if(!isFilePresent("teleports.dat")){
                missingFiles += "\n- teleports.dat";
            }

            if(!missingFiles.isEmpty()){
                missingFiles = "Missing file(s) substituted:" + missingFiles;
            }



            if(!missingFiles.isEmpty()){

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Data not found");
                alert.setHeaderText(null);
                alert.setContentText(missingFiles);

                alert.showAndWait();
            }
        }

    }

    void setEditorFiles() {

        setEditorFiles(null);
    }

    private boolean isFilePresent(String name){

        File checkMe = new File(System.getProperty("user.home") + "/.EistReturns/levels/editor-data/" + name);
        if(!checkMe.exists()){

            File replacement = new File(ClassLoader.getSystemResource("levels/03/" + name).toExternalForm().substring(5));
            try {
                copyFile(replacement, checkMe);
            }catch (IOException e){
                System.out.println("Error copying board.png:" + e);
            }
        return false;

        } else {
            return true;
        }
    }

    private void importEditorFiles(Integer level){

        String lvlNumberToString;
        if(level != null) {

            lvlNumberToString = (level < 10) ? "0" + String.valueOf(level) : String.valueOf(level);
            mEditorStage.setTitle("Imported level: " + lvlNumberToString);

        } else {

            lvlNumberToString = "03";
            mEditorStage.setTitle("Imported sample level: " + lvlNumberToString);
        }

        File sourceFolder = new File(ClassLoader.getSystemResource("levels/" + lvlNumberToString).toExternalForm().substring(5));
        File[] sourceFiles = sourceFolder.listFiles();

        if (sourceFiles != null) {
            for (File file : sourceFiles) {

                Path source = Paths.get(file.toString());
                Path destination = Paths.get(System.getProperty("user.home") + "/.EistReturns/levels/editor-data", source.getFileName().toString());

                try {
                    copyFile(source.toFile(), destination.toFile());
                    System.out.println("Copying: " + file.toString());
                } catch(IOException e) {
                    System.out.println("Copying error: " + e);
                }
            }
        }

    }

    private static void copyFile(File source, File dest) throws IOException {
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }
}
