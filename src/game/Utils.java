package game;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.*;

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
import javafx.util.Duration;
import javafx.scene.control.ButtonBar.ButtonData;

import game.ZipUtils.*;
import javafx.util.Pair;

import java.io.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static javafx.scene.layout.BackgroundSize.AUTO;

abstract class Utils extends Application {

    boolean mDevMode = false;

    double mSceneWidth;
    double mSceneHeight;
    private double mCenterX;
    double walkingSpeedPerSecond;

    int mCurrentLevel = 0;
    int mSelectedLevel = 1;
    int mAchievedLevel = 0;

    double mFrameDurationEist = 90000000;
    /**
     * This was left as final int, since in the future may be replaced with a value stored in prefs.
     */
    final int MAX_LEVEL = 40;
    boolean mGameFinished = false;

    double mDimensionDivider;

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
    private Rectangle2D mButtonHall;

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
    private File mEditorFolder;

    private boolean mLoadUserLevel;

    private Button mImportButton;
    private Button mOpenButton;
    private Button mSaveButton;
    private Button mSaveAsButton;
    private Button mToolsButton;
    private Button mDoorButton;
    private Button mSlotButton;
    private Button mArtifactButton;
    private Button mKeyButton;
    private Button mTeleportButton;
    private Button mOrnamentButton;
    private Button mExitButton;
    private Button mArrowButton;
    private Button mEistButton;
    private Button mClearButton;

    Stage mGameStage;
    Stage mEditorStage;
    Scene mEditorScene;

    Font infoFont;
    Font messageFont;
    Font turnsFont;
    Font playerFont;
    private Font menuFont;

    private String mHttpResponse;
    private boolean mNewPlayer;
    private boolean mHofInProgress;
    boolean mResultsChanged = false;

    /**
     * The Frame is a rectangular part of the game board of width of 2 columns and height of 2 rows.
     * It corresponds to the width of the path on which Eist's sprite moves.
     */
    double mFrameDimension;
    double mGridDimension;
    double mHalfGridDimension;
    private double mButtonWidth;
    private double mButtonWidthNarrow;
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

    private static final int TOAST_LENGTH_LONG = 3000;
    private static final int TOAST_LENGTH_SHORT = 1000;

    boolean mFullSpeed;

    String mMenuHint = "";

    String mPlayer = "";
    String mPass = "";

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

        mPlayer = prefs.get("user", "");
        mPass = prefs.get("pass", "");

        /*
         * The dimensions of the Scene and the game board inside will derive from the users' screen width.
         * Source graphics has been drawn as fullHD (1920 x 1080).
         */
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        mSceneWidth = primaryScreenBounds.getWidth() / mDimensionDivider;
        mSceneHeight = (mSceneWidth / 1920) * 1080;
        mFrameDimension = (mSceneWidth / 1920) * 120;
        mGridDimension = mFrameDimension / 2;
        mButtonWidth = mGridDimension * 5;
        mButtonWidthNarrow = mGridDimension * 3;
        mHalfGridDimension = mGridDimension / 2;
        mDetectionOffset = (int) mFrameDimension / 6;
        mRotationRadius = mFrameDimension / 4;
        walkingSpeedPerSecond = mSceneWidth / 14d;

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

    /*
     * FPS calculation after https://stackoverflow.com/a/28288527/4040598 by @brian
     */

    /*static long lastUpdate = 0;
    static int index = 0;
    static double[] frameRates = new double[5];
    */
    /*
     * Returns the average FPS for the last frameRates.length frames rendered.
     * (Original for loop replaced with foreach).
     */
    /*
    static double getAverageFPS() {
        double total = 0.0d;

        for (double value : frameRates) {
            total += value;
        }
        return total / frameRates.length;
    }
    */

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

                        if (toolbar.getSelection() != null) {
                            prefs.putInt("sel", toolbar.getSelection());
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
                    if (!mTesting && toolbar != null && toolbar.getSelection() != null) {
                        /*
                        If path clicked
                         */
                        if (pixelReader.getArgb((int) pointClicked.getX(), (int) pointClicked.getY()) != -16777216) {

                            Rectangle2D pressedSquare = nearestAdjustedSquare(pointClicked.getX(), pointClicked.getY());
                            Point2D checkPoint;

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
                        if (toolbar.getSelection() == SELECTION_SLOT &&
                                pixelReader.getArgb((int) pointClicked.getX(), (int) pointClicked.getY()) == -16777216) {
                            Rectangle2D area = nearestSlot(pointClicked.getX(), pointClicked.getY(), toolbar.getSlotOrientation());
                            if (area != null) {
                                placeSlot(area, toolbar.getSlotOrientation());
                            }
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

                        if (!mTesting) {
                            saveEditor();
                            disableButtons(true);
                            if (toolbar.getSelection() != null) {
                                prefs.putInt("sel", toolbar.getSelection());
                            }
                            mTesting = true;
                        } else {
                            mTesting = false;
                            disableButtons(false);
                            loadEditor();
                        }
                    }
                }

            } else {
                /*
                 * Handle intro menu clicks
                 */
                if (mButtonLevelUp.contains(pointClicked)) {
                    if (mSelectedLevel < mAchievedLevel || mDevMode) {
                        if (mSelectedLevel < MAX_LEVEL) {
                            mSelectedLevel++;
                            prefs.putInt("level", mSelectedLevel);
                        }
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

                    displaySettings();

                } else if (mButtonHall.contains(pointClicked)) {
                    if(!mHofInProgress) {
                        displayHallOfFame();
                    } else {
                        Toast.makeText(mGameStage, "Already asking the cloud, wait...", TOAST_LENGTH_SHORT);
                    }
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
    private Image mIntroFinished;

    private Background mButtonBackground;
    private Background mButtonBackgroundNarrow;

    Image mMutedMusicImg;
    Image mMutedSoundImg;

    void initializeFonts() {

        /*
        infoFont = Font.loadFont(ClassLoader.getSystemResource("Orbitron-Regular.ttf").toExternalForm(), 50 / mDimensionDivider * rem);
        levelFont = Font.loadFont(ClassLoader.getSystemResource("Orbitron-Regular.ttf").toExternalForm(), 44 / mDimensionDivider * rem);
        turnsFont = Font.loadFont(ClassLoader.getSystemResource("Orbitron-Regular.ttf").toExternalForm(), 36 / mDimensionDivider * rem);
        playerFont = Font.font("Helvetica", FontWeight.NORMAL, 26 / mDimensionDivider * rem);
        messageFont = Font.loadFont(ClassLoader.getSystemResource("Orbitron-Regular.ttf").toExternalForm(), 20 / mDimensionDivider * rem);
        menuFont = Font.loadFont(ClassLoader.getSystemResource("Orbitron-Regular.ttf").toExternalForm(), 20 / mDimensionDivider * rem);
        */

        infoFont = Font.loadFont(ClassLoader.getSystemResource("Orbitron-Regular.ttf").toExternalForm(), mGridDimension * 0.75);
        turnsFont = Font.loadFont(ClassLoader.getSystemResource("Orbitron-Regular.ttf").toExternalForm(), mGridDimension * 0.6);
        playerFont = Font.font("Helvetica", FontWeight.NORMAL, mGridDimension * 0.45);
        messageFont = Font.loadFont(ClassLoader.getSystemResource("Orbitron-Regular.ttf").toExternalForm(), mGridDimension * 0.35);
        menuFont = Font.loadFont(ClassLoader.getSystemResource("Orbitron-Regular.ttf").toExternalForm(), mGridDimension * 0.35);
    }

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

        BackgroundImage mButtonImage = new BackgroundImage(new Image(ClassLoader.getSystemResource("images/common/menu_button.png").toExternalForm()),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                new BackgroundSize(AUTO, AUTO, true, true, true, false));
        mButtonBackground = new Background(mButtonImage);

        BackgroundImage mButtonImageNarrow = new BackgroundImage(new Image(ClassLoader.getSystemResource("images/common/menu_button_narrow.png").toExternalForm()),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                new BackgroundSize(AUTO, AUTO, true, true, true, false));
        mButtonBackgroundNarrow = new Background(mButtonImageNarrow);

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
        mButtonHall = new Rectangle2D(columns[27], rows[1], mFrameDimension * 2, mFrameDimension);
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

        System.out.println("Loading level " + level);

        setSpeed(level);

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
                System.out.println("User-defined level not found. Loading from resources.");
            }

        } else {
            mLoadUserLevel = false;
        }

        /*
         * Load board bitmap
         */
        mBoardImg = new Image(urlString + "board.png", mSceneWidth, mSceneHeight, true, true, false);

        pixelReader = mBoardImg.getPixelReader();

        String info = infoString(System.getProperty("user.home") + "/.EistReturns/levels/info.txt");
        if (info == null && mLoadUserLevel) {
            info = "unnamed set";
        }

        if (info != null) {
            if (mGameStage != null) {
                mGameStage.setTitle("Eist returns: " + info);
            }
        } else {
            if (mGameStage != null) {
                mGameStage.setTitle("Eist returns");
            }
        }

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

    void setSpeed(int level){

        mFullSpeed = prefs.getBoolean("fspeed", false);

        walkingSpeedPerSecond = mSceneWidth / 14d;
        mFrameDurationEist = 70000000;

        if(!mFullSpeed && level > 0){
            double delay = (1.1 / mDimensionDivider) * (MAX_LEVEL - level);
            walkingSpeedPerSecond = walkingSpeedPerSecond - delay;
            mFrameDurationEist = mFrameDurationEist + ((MAX_LEVEL - level) * 500000);
        }

        System.out.println("walkingSpeedPerSecond = " + walkingSpeedPerSecond);
        System.out.println("mFrameDurationEist = " + mFrameDurationEist);
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

        setUpButtons();
        toolbar.setSelection(prefs.getInt("sel", SELECTION_KEY));

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
        if (mSlots == null || mSlots.size() == 0) {
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

        disableButtons(false);
    }

    private String datToString(String urlString) {

        StringBuilder stringBuilder = new StringBuilder();

        InputStream inputStream;

        if (!mLoadUserLevel && !mEditor) {
            inputStream = getClass().getClassLoader().getResourceAsStream(urlString);
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
            System.out.println("Couldn't read " + urlString);
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

    String infoString(String urlString) {

        StringBuilder stringBuilder = new StringBuilder();

        InputStream inputStream;

        try {
            inputStream = new FileInputStream(urlString);
        } catch (FileNotFoundException e) {
            inputStream = null;
        }
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

        Rectangle2D slotRect;
        if (orientation == ORIENTATION_VERTICAL) {

            slotRect = new Rectangle2D(nearest_left - mGridDimension, nearest_top, mFrameDimension, mGridDimension);

        } else {

            slotRect = new Rectangle2D(nearest_left, nearest_top - mGridDimension, mGridDimension, mFrameDimension);
        }
        return isSlotAllowed(slotRect) ? slotRect : null;
    }

    private boolean isSlotAllowed(Rectangle2D nearestSquare) {

        int top_left_color = pixelReader.getArgb((int) nearestSquare.getMinX(), (int) nearestSquare.getMinY());
        int top_right_color = pixelReader.getArgb((int) nearestSquare.getMaxX(), (int) nearestSquare.getMinY());
        int bottom_left_color = pixelReader.getArgb((int) nearestSquare.getMinX(), (int) nearestSquare.getMaxY());
        int bottom_right_color = pixelReader.getArgb((int) nearestSquare.getMaxX(), (int) nearestSquare.getMaxY());

        return top_left_color != -16777216 && top_right_color != -16777216 &&
                bottom_left_color != -16777216 && bottom_right_color != -16777216;
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

        if (touch_x - nearest_left < mHalfGridDimension) {
            nearest_left = nearest_left - mGridDimension;
        }
        if (touch_y - nearest_top < mHalfGridDimension) {
            nearest_top = nearest_top - mGridDimension;
        }

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

        if (mTeleports.size() < 2) {
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
        if (mSlots.size() == 1) {
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

        Rectangle2D detectRect;

        for (Door door : mDoors) {
            detectRect = new Rectangle2D(door.getPosX(), door.getPosY(), mFrameDimension, mFrameDimension);
            if (detectRect.contains(checkPoint)) {
                mDoors.remove(door);
                break;
            }
        }

        for (Artifact artifact : mArtifacts) {
            detectRect = new Rectangle2D(artifact.getPosX(), artifact.getPosY(), mFrameDimension, mFrameDimension);
            if (detectRect.contains(checkPoint)) {
                mArtifacts.remove(artifact);
                break;
            }
        }

        for (Key key : mKeys) {
            detectRect = new Rectangle2D(key.getPosX(), key.getPosY(), mFrameDimension, mFrameDimension);
            if (detectRect.contains(checkPoint)) {
                mKeys.remove(key);
                break;
            }
        }

        for (Teleport teleport : mTeleports) {
            detectRect = new Rectangle2D(teleport.getPosX(), teleport.getPosY(), mFrameDimension, mFrameDimension);
            if (detectRect.contains(checkPoint)) {
                mTeleports.remove(teleport);
                break;
            }
        }

        for (Arrow arrow : mArrows) {
            detectRect = new Rectangle2D(arrow.getPosX(), arrow.getPosY(), mFrameDimension, mFrameDimension);
            if (detectRect.contains(checkPoint)) {
                mArrows.remove(arrow);
                break;
            }
        }

        for (Ornament ornament : mOrnaments) {
            detectRect = new Rectangle2D(ornament.getPosX(), ornament.getPosY(), mFrameDimension, mFrameDimension);
            if (detectRect.contains(checkPoint)) {
                mOrnaments.remove(ornament);
                break;
            }
        }

        for (Slot slot : mSlots) {
            if (slot.getArea().contains(checkPoint)) {
                mSlots.remove(slot);

                if (mSlots.size() > 0) {
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

    void displayFirstRunAlert(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setGraphic(new ImageView(ClassLoader.getSystemResource("images/common/cloud.png").toExternalForm()));
        alert.setTitle("Hall of Fame cloud service");
        alert.setHeaderText("Create or login to a Hall of Fame account");
        alert.setContentText("Online account lets you sync the game state\nbetween devices and compare results\nto other players.\n\n");
        alert.initOwner(mGameStage);

        ButtonType buttonTypeOK = new ButtonType("Create/Login", ButtonData.OK_DONE);
        ButtonType buttonTypeDontAsk = new ButtonType("Don't ask again", ButtonData.NO);
        ButtonType buttonTypeLater = new ButtonType("Ask later", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeOK, buttonTypeDontAsk, buttonTypeLater);

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent()) {
            if (result.get() == buttonTypeOK) {
                prefs.putBoolean("hofAsked", true);
                displayHallOfFame();
            } else if (result.get() == buttonTypeDontAsk) {
                prefs.putBoolean("hofAsked", true);
            }
        }

    }

    private void displayAboutAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About the game");
        alert.setHeaderText("Eist returns\narcade-puzzle game\n\u00a91992-2017 nwg (Piotr Miller)\nhttp://nwg.pl/eist");
        alert.setResizable(true);
        alert.initOwner(mGameStage);

        String message = "This program is free software; you can redistribute it and/or modify it under the terms of the GNU " +
                "General Public License version 3.0, as published by the Free Software Foundation. Find the source code at" +
                " https://github.com/nwg-piotr/EistReturns" +
                "\n\nThe game was created with the following software:" +
                "\nGraphics: Inkscape, GIMP" +
                "\nSounds: LMMS, Audacity" +
                "\nIDE: IntelliJ IDEA Community Edition\n" +
                "\nOrbitron TrueType font, Copyright (c) 2009, Matt McInerney (matt@pixelspread.com), released under the SIL OPEN FONT LICENSE Version 1.1";

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
        dialog.initOwner(mGameStage);

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

    private void displayImportLevelChoiceDialog() {
        List<String> levels = new ArrayList<>();
        for (int i = 1; i < MAX_LEVEL + 1; i++) {
            levels.add(String.valueOf(i));
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>("1", levels);
        dialog.setTitle("Import default level");
        dialog.setHeaderText("Built-in level to import");
        dialog.setContentText("Level:");
        dialog.initOwner(mEditorStage);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            int level = Integer.valueOf(result.get());
            prefs.putInt("editorLvl", level);
            setEditorFiles(level);
            loadEditor();
        }
    }

    private void displayOpenUserLevelDialog() {

        List<String> levels = userLevels();
        if (userLevels().size() > 0) {

            ChoiceDialog<String> dialog = new ChoiceDialog<>(levels.get(0), levels);
            dialog.setTitle("Open user-defined level");
            dialog.setHeaderText("Select level to open");
            dialog.setContentText("Level:");
            dialog.initOwner(mEditorStage);

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                int level = Integer.valueOf(result.get());
                prefs.putInt("editorLvl", level);
                setEditorFilesFromUserLevel(result.get());
                loadEditor();
            }
        } else {
            Toast.makeText(mEditorStage, "No user-defined levels found", TOAST_LENGTH_SHORT);
        }
    }

    private void displayDeleteUserLevelDialog(Stage stage) {

        List<String> levels = userLevels();
        if (userLevels().size() > 0) {

            ChoiceDialog<String> dialog = new ChoiceDialog<>(levels.get(0), levels);
            dialog.setTitle("Delete user-defined level");
            dialog.setHeaderText("User level to delete");
            dialog.setContentText("Level:");
            dialog.initOwner(stage);

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {

                File level = new File(System.getProperty("user.home") + "/.EistReturns/levels/" + result.get());
                if (deleteFolder(level)) {
                    Toast.makeText(mEditorStage, "User level " + result.get() + " deleted", TOAST_LENGTH_SHORT);
                } else {
                    Toast.makeText(mEditorStage, "Failed deleting " + level, TOAST_LENGTH_SHORT);
                }
                if (userLevels().size() == 0) {
                    File infoFile = new File(System.getProperty("user.home") + "/.EistReturns/levels/info.txt");
                    if (infoFile.delete()) {
                        System.out.println("Info file deleted");
                    }
                }

            }
        } else {
            Toast.makeText(mEditorStage, "No user-defined levels found", TOAST_LENGTH_SHORT);
        }
    }

    private void displayClearAllUserLevelsDialog(Stage stage) {

        List<String> userLevels = userLevels();
        if (userLevels.size() > 0) {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Clear user levels");
            alert.initOwner(stage);

            StringBuilder names = new StringBuilder();
            names.append("To delete: ");
            for (String levelName : userLevels) {
                names.append(levelName);
                if (userLevels.indexOf(levelName) < userLevels.size() - 1) {
                    names.append(", ");
                }
            }

            alert.setHeaderText("This will delete all user-defined levels:");
            alert.setContentText(names.toString());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == ButtonType.OK) {

                    for (String levelName : userLevels) {
                        File levelFolder = new File(System.getProperty("user.home") + "/.EistReturns/levels/" + levelName);
                        System.out.println("Deleting " + levelFolder);
                        deleteFolder(levelFolder);
                    }
                    File infoFile = new File(System.getProperty("user.home") + "/.EistReturns/levels/info.txt");
                    if (infoFile.delete()) {
                        System.out.println("Info file deleted");
                    }
                    clearScores();
                    Toast.makeText(stage, "User levels cleared", TOAST_LENGTH_SHORT);
                    if (mGameStage != null) {
                        mGameStage.setTitle("Eist returns: default");

                        if (!mPlayer.isEmpty() && !mPass.isEmpty()) {
                            playerLogin(mPlayer, mPass);
                        }
                    }
                    if (mEditorStage != null) {
                        mEditorStage.setTitle("Level editor");
                    }
                }
            }
        } else {
            Toast.makeText(stage, "No user-defined levels found", TOAST_LENGTH_SHORT);
        }

    }

    /**
     * This is for testing purposes, to allow the developer select any level to play
     */
    private void displayDevKeyDialog() {

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Developer mode");
        dialog.setHeaderText("Enter developer key");
        dialog.initOwner(mGameStage);
        dialog.setGraphic(new ImageView(ClassLoader.getSystemResource("images/common/lockpad.png").toExternalForm()));

        ButtonType loginButtonType = new ButtonType("Enter", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 20));

        PasswordField password = new PasswordField();
        password.setPromptText("Key");

        grid.add(new Label("Key:"), 0, 1);
        grid.add(password, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        password.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(password::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return password.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            /*
             * Encode your own key and place below
             */
            String pass = decode("QXJjaA==");

            if (result.get().equals(pass)) {
                mDevMode = true;
                playerLogOut();
            } else {
                mDevMode = false;
                Toast.makeText(mGameStage, "Key invalid", TOAST_LENGTH_SHORT);
            }
        });
    }

    private void displayHighScores() {

        StringBuilder scores = new StringBuilder();

        for (int i = 1; i < MAX_LEVEL + 1; i++) {
            String lvl = lvlToString(i);
            int value = prefs.getInt(lvl + "best", 0);
            if (value != 0) {
                scores.append("Level ");
                scores.append(lvl);
                scores.append(": ");
                scores.append(value);
                scores.append(" turns\n");
            }
        }
        String result = scores.toString();
        if (result.isEmpty()) {
            result = "No saved scores found";
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("High scores");
        alert.setHeaderText("Saved results");
        alert.setContentText(result);
        alert.initOwner(mGameStage);

        alert.showAndWait();
    }

    private void displayHallOfFame() {

        if(!mHofInProgress) {

            if (mPlayer.isEmpty()) {

                displayLogInDialog();

            } else {

                showHallScores();
            }
        }
    }

    private void displayLogInDialog() {

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Player login");
        dialog.setHeaderText("Login to the Hall Of Fame");
        dialog.initOwner(mGameStage);

        dialog.setGraphic(new ImageView(ClassLoader.getSystemResource("images/common/login.png").toExternalForm()));

        ButtonType newButtonType = new ButtonType("New player", ButtonData.OTHER);
        ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(newButtonType, loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Name");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Player name:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        Node newButton = dialog.getDialogPane().lookupButton(newButtonType);
        newButton.setDisable(true);

        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty() || password.getText().isEmpty());
            newButton.setDisable(newValue.trim().isEmpty() || password.getText().isEmpty());
        });

        password.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().length() < 6 || username.getText().isEmpty());
            newButton.setDisable(newValue.trim().length() < 6 || username.getText().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(username::requestFocus);

        mNewPlayer = false;

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            if (dialogButton == newButtonType) {
                mNewPlayer = true;
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {

            if (!mNewPlayer) {
                playerLogin(usernamePassword.getKey(), encode(usernamePassword.getValue()));
            } else {
                playerNew(usernamePassword.getKey(), encode(usernamePassword.getValue()));
            }

        });
    }

    private void displayManagePlayerDialog() {

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Manage players");
        dialog.setHeaderText("Select player account");
        dialog.initOwner(mGameStage);

        dialog.setGraphic(new ImageView(ClassLoader.getSystemResource("images/common/login.png").toExternalForm()));

        ButtonType newButtonType = new ButtonType("New player", ButtonData.OK_DONE);
        ButtonType loginButtonType = new ButtonType("Delete player", ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(newButtonType, loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Name");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Player name:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        Node newButton = dialog.getDialogPane().lookupButton(newButtonType);
        newButton.setDisable(true);

        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty() || password.getText().isEmpty());
            newButton.setDisable(newValue.trim().isEmpty() || password.getText().isEmpty());
        });

        password.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().length() < 6 || username.getText().isEmpty());
            newButton.setDisable(newValue.trim().length() < 6 || username.getText().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(username::requestFocus);

        mNewPlayer = false;

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            if (dialogButton == newButtonType) {
                mNewPlayer = true;
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {

            if (!mNewPlayer) {
                playerDelete(usernamePassword.getKey(), encode(usernamePassword.getValue()));
            } else {
                playerNew(usernamePassword.getKey(), encode(usernamePassword.getValue()));
            }

        });
    }

    void displayClearAllDialog(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear scores and settings");
        alert.setHeaderText("This will clear all the game settings\nand locally stores results!\nYou'll also have to start the game again.");
        alert.setContentText("Are you REALLY sure?");

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            try {
                prefs.clear();
                mPlayer = "";
                mPass = "";
                mAchievedLevel = 1;
                mCurrentLevel = 0;
                mSelectedLevel = 1;
                mDevMode = false;
                loadLevel(0);
                Toast.makeText(mGameStage, "Restoring factory defaults...", TOAST_LENGTH_SHORT);
                Platform.runLater(Platform::exit);
            } catch (BackingStoreException e){
                displayExceptionAlert("Couldn't clear settings", e);
            }

        }
    }

    void playerLogin(String player, String pass) {

        prefs.putBoolean("hofAsked", true);

        mHttpResponse = "";

        Thread thread = new Thread(() -> {
            try {
                mHttpResponse = HttpURLConnection.sendGet("http://nwg.pl/eist/player.php?action=login&uname=" + player + "&upswd=" + pass);
            } catch (Exception e) {
                Platform.runLater(() -> displayExceptionAlert("Internet connection error", e));
            }

            if (!mHttpResponse.isEmpty()) {

                //System.out.println(mHttpResponse);

                if (mHttpResponse.startsWith("login_ok")) {
                    mPlayer = player;
                    mPass = pass;
                    prefs.put("user", mPlayer);
                    prefs.put("pass", mPass);
                    mDevMode = false;
                    Platform.runLater(this::showHallScores);

                    String[] wholeLine = mHttpResponse.split(":");
                    /*
                    Parse user data: name, completed levels, total turns
                     */
                    String[] userData = wholeLine[0].split(",");
                    int completedLevel = Integer.valueOf(userData[1]);
                    mAchievedLevel = completedLevel + 1;
                    mSelectedLevel = mAchievedLevel;
                    prefs.putInt("achieved", mAchievedLevel);

                    /*
                    Parse turns value stored for each level
                     */
                    String[] levelsData = wholeLine[1].split(",");
                    for (int i = 1; i < 41; i++) {
                        int score = Integer.valueOf(levelsData[i - 1]);
                        if(score > 0) {
                            prefs.putInt(lvlToString(i) + "best", score);
                        }
                    }

                } else {

                    switch (mHttpResponse) {
                        case "wrong_pswd":
                            prefs.put("user", "");
                            prefs.put("pass", "");
                            Platform.runLater(() -> Toast.makeText(mGameStage, "Wrong password for player " + player, TOAST_LENGTH_LONG));
                            break;
                        case "no_such_player":
                            prefs.put("user", "");
                            prefs.put("pass", "");
                            Platform.runLater(() -> Toast.makeText(mGameStage, "Player " + player + " does not exist", TOAST_LENGTH_LONG));
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        thread.start();
    }

    void updateHallScore(String player, String pass, boolean exit) {

        if (userLevels().size() == 0) {

            mHttpResponse = "";

            if(exit) {
                Toast.makeText(mGameStage, "Connecting Hall of Fame...", TOAST_LENGTH_LONG);
            }

            Thread thread = new Thread(() -> {
                try {

                    StringBuilder url = new StringBuilder();
                    String part0 = "http://nwg.pl/eist/player.php?action=updateall&uname=" + player + "&upswd=" + pass + "&ulevel=" +
                            (mAchievedLevel - 1) + "&uturns=" + totalTurns();
                    url.append(part0);

                    for (int i = 1; i < MAX_LEVEL + 1; i++) {
                        url.append("&l");
                        url.append(lvlToString(i));
                        url.append("=");
                        url.append(prefs.getInt(lvlToString(i) + "best", 0));
                    }

                    mHttpResponse = HttpURLConnection.sendGet(url.toString());

                } catch (Exception e) {
                    Platform.runLater(() -> Toast.makeText(mGameStage, "Connection error: " + e, TOAST_LENGTH_LONG));
                }

                if (!mHttpResponse.isEmpty()) {

                    //System.out.println(mHttpResponse);

                    if (mHttpResponse.equals("scores_updated")) {
                        Platform.runLater(() -> Toast.makeText(mGameStage, "Hall of Fame updated", TOAST_LENGTH_SHORT));
                        mResultsChanged = false;
                    } else {
                        Platform.runLater(() -> Toast.makeText(mGameStage, "Failed saving results, press 'S' to retry", TOAST_LENGTH_LONG));
                    }
                }
                if (exit) {
                    Platform.runLater(() -> {
                        Platform.runLater(Platform::exit);
                    });

                }
            });
            thread.start();

        }  else {
            Platform.exit();
        }
    }

    private void showHallScores() {

        if (userLevels().size() == 0) {

            mHofInProgress = true;
            mHttpResponse = "";

            Thread thread = new Thread(() -> {
                try {
                    mHttpResponse = HttpURLConnection.sendGet("http://nwg.pl/eist/player.php?action=display&ulimit=20");
                } catch (Exception e) {
                    Platform.runLater(() -> Toast.makeText(mGameStage, "Connection error: " + e, TOAST_LENGTH_LONG));
                }

                //System.out.println(mHttpResponse);
                Platform.runLater(() -> { displayHoF(mHttpResponse);
                    mHofInProgress = false;
                });
            });
            thread.start();

        } else {
            displayHoF("user_set");
            mHofInProgress = false;
        }
    }

    private void playerLogOut() {
        mPlayer = "";
        mPass = "";
        prefs.put("user", mPlayer);
        prefs.put("pass", mPass);
        Toast.makeText(mGameStage, "Hall of Fame offline, scores no longer stored", TOAST_LENGTH_LONG);
    }

    private void playerNew(String player, String pass) {

        mHttpResponse = "";

        Thread thread = new Thread(() -> {
            try {
                mHttpResponse = HttpURLConnection.sendGet("http://nwg.pl/eist/player.php?action=create&uname=" +
                        player + "&upswd=" + pass + "&ulevel=" + (mAchievedLevel - 1) + "&uturns=" + totalTurns());
            } catch (Exception e) {
                Platform.runLater(() -> Toast.makeText(mGameStage, "Connection error: " + e, TOAST_LENGTH_LONG));
            }

            if (!mHttpResponse.isEmpty()) {

                //System.out.println(mHttpResponse);

                switch (mHttpResponse) {
                    case "player_created":
                        mPlayer = player;
                        mPass = pass;
                        prefs.put("user", mPlayer);
                        prefs.put("pass", mPass);
                        clearScores();
                        Platform.runLater(() -> Toast.makeText(mGameStage, "Logged in as new player " + mPlayer, TOAST_LENGTH_SHORT));
                        break;
                    case "failed_creating":
                        prefs.put("user", "");
                        prefs.put("pass", "");
                        Platform.runLater(() -> Toast.makeText(mGameStage, "Couldn't create player " + player, TOAST_LENGTH_LONG));
                        break;
                    case "player_exists":
                        prefs.put("user", "");
                        prefs.put("pass", "");
                        Platform.runLater(() -> Toast.makeText(mGameStage, "Player " + player + " already exists", TOAST_LENGTH_LONG));
                        break;
                    default:
                        break;
                }
            }
        });
        thread.start();
    }

    private void playerDelete(String player, String pass) {

        mHttpResponse = "";

        Thread thread = new Thread(() -> {
            try {
                mHttpResponse = HttpURLConnection.sendGet("http://nwg.pl/eist/player.php?action=delete&uname=" +
                        player + "&upswd=" + pass);
            } catch (Exception e) {
                Platform.runLater(() -> Toast.makeText(mGameStage, "Connection error: " + e, TOAST_LENGTH_LONG));
            }

            if (!mHttpResponse.isEmpty()) {

                //System.out.println(mHttpResponse);

                switch (mHttpResponse) {
                    case "player_deleted":
                        if (player.equals(mPlayer)) {
                            mPlayer = "";
                            mPass = "";
                            prefs.put("user", mPlayer);
                            prefs.put("pass", mPass);
                        }
                        Platform.runLater(() -> Toast.makeText(mGameStage, "Player " + player + " deleted", TOAST_LENGTH_SHORT));
                        break;
                    case "failed_deleting":
                        Platform.runLater(() -> Toast.makeText(mGameStage, "Couldn't delete player " + player, TOAST_LENGTH_LONG));
                        break;
                    case "no_player_wrong_pass":
                        Platform.runLater(() -> Toast.makeText(mGameStage, "Wrong player name or password", TOAST_LENGTH_LONG));
                        break;
                    default:
                        break;
                }
            }
        });
        thread.start();
    }

    private int totalTurns() {
        int totally = 0;
        for (int i = 1; i < MAX_LEVEL + 1; i++) {
            totally += prefs.getInt(lvlToString(i) + "best", 0);
        }
        return totally;
    }

    private String encode(String source) {

        byte[] asBytes = Base64.getEncoder().encode(source.getBytes());
        return new String(asBytes);
    }

    private String decode(String source) {

        byte[] asBytes = Base64.getDecoder().decode(source);
        String pass = "";
        try {
            pass = new String(asBytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return pass;
    }

    private void importLevelsFromZip(Stage stage) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file to import from");
        String initialDir = prefs.get("importPath", "");
        if (!initialDir.isEmpty() && new File(initialDir).exists()) {
            fileChooser.setInitialDirectory(new File(initialDir));
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Zip files", "*.zip"));

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {

            prefs.put("importPath", selectedFile.toString().split(selectedFile.getName())[0]);
            try {
                int dirs = ZipFileUtil.unzip(selectedFile, System.getProperty("user.home") + "/.EistReturns/levels/");
                Toast.makeText(stage, "Imported " + dirs + " folders from " + selectedFile, TOAST_LENGTH_LONG);

                String info = infoString(System.getProperty("user.home") + "/.EistReturns/levels/info.txt");
                if (info == null) {
                    info = "unnamed set";
                }

                if (mGameStage != null) {
                    mGameStage.setTitle("Eist returns: " + info);
                }
                if (mEditorStage != null) {
                    mEditorStage.setTitle("Level editor: " + info);
                }

                clearScores();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(stage, "No file selected", TOAST_LENGTH_SHORT);
        }
    }

    private void displaySetNameDialog(Stage stage) {

        String info = infoString(System.getProperty("user.home") + "/.EistReturns/levels/info.txt");
        if (info == null && mLoadUserLevel) {
            info = "your description here";
        }

        TextInputDialog dialog = new TextInputDialog(info);
        dialog.setTitle("Name your levels");
        dialog.setHeaderText("Give your set a short description");
        dialog.setContentText("Description:");
        dialog.initOwner(stage);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.isEmpty()) {
                try {
                    PrintStream out = new PrintStream(System.getProperty("user.home") + "/.EistReturns/levels/info.txt");
                    out.println(name);
                    Toast.makeText(mEditorStage, "Levels set named: " + name, TOAST_LENGTH_SHORT);
                    mEditorStage.setTitle(name);
                } catch (IOException e) {
                    Toast.makeText(mEditorStage, "Failed saving the name" + name, TOAST_LENGTH_SHORT);
                }
            }
        });
    }

    private void displayExportLevelsDialog(Stage stage) {

        TextInputDialog dialog = new TextInputDialog("unnamed");
        dialog.setTitle("Export user levels");
        dialog.setHeaderText("Exporting user levels to a zip file");
        dialog.setContentText("Filename:");
        dialog.initOwner(stage);

        ButtonType ok = new ButtonType("OK", ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        ButtonType other = new ButtonType("Select file", ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().setAll(ok, cancel, other);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == other) {
                return "select_file";
            } else if (dialogButton == ok) {
                return dialog.getEditor().getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {

            File exportFolder = new File(System.getProperty("user.home") + "/.EistReturns/export");
            if (exportFolder.mkdir()) {
                System.out.println("Export folder created");
            }

            if (name.equals("select_file")) {

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select file to overwrite");
                fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/.EistReturns/export"));
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Zip files", "*.zip"));
                File selectedFile = fileChooser.showOpenDialog(mEditorStage);
                if (selectedFile != null) {
                    System.out.println("Selected: " + selectedFile);
                    try {
                        ZipFileUtil.zipDirectory(new File(System.getProperty("user.home") + "/.EistReturns/levels"),
                                selectedFile);
                        Toast.makeText(mEditorStage, "Saved to " + selectedFile, TOAST_LENGTH_LONG);
                    } catch (IOException ioe) {
                        System.out.println("Couldn't export to a zip file: " + ioe);
                    }
                }

            } else {

                if (name.equals("unnamed")) {
                    DateFormat dateFormat = new SimpleDateFormat("-yy-MM-dd-HH-mm-ss");
                    Calendar cal = Calendar.getInstance();
                    name += dateFormat.format(cal.getTime());
                }

                try {
                    ZipFileUtil.zipDirectory(new File(System.getProperty("user.home") + "/.EistReturns/levels"),
                            new File(System.getProperty("user.home") + "/.EistReturns/export/" + name + ".zip"));
                    Toast.makeText(mEditorStage, "Saved to /.EistReturns/export/" + name + ".zip", TOAST_LENGTH_LONG);
                } catch (IOException ioe) {
                    System.out.println("Couldn't export to a zip file: " + ioe);
                }
            }
        });
    }

    private static boolean deleteFolder(File folder) {
        if (folder.isDirectory()) {
            String[] children = folder.list();
            if (children != null) {
                for (String aChildren : children) {
                    boolean success = deleteFolder(new File(folder, aChildren));

                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return folder.delete();
    }

    private void displaySaveLevelAsChoiceDialog() {
        List<String> levels = new ArrayList<>();
        for (int i = 1; i < MAX_LEVEL + 1; i++) {
            levels.add(String.valueOf(i));
        }
        String level = String.valueOf(prefs.getInt("editorLvl", 1));
        ChoiceDialog<String> dialog = new ChoiceDialog<>(level, levels);
        dialog.setTitle("Save as user level");
        dialog.setHeaderText("Built-in level to override");
        dialog.setContentText("Level:");
        dialog.initOwner(mEditorStage);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(s -> {
            copyEditorToUserLevel(Integer.valueOf(s));
            mEditorStage.setTitle("User level: " + lvlToString(Integer.valueOf(s)));
        });
    }

    private void setEditorFiles(Integer level) {

        mEditorFolder = new File(System.getProperty("user.home") + "/.EistReturns/levels/editor-data");
        if (mEditorFolder.mkdir()) {

            /*
             * Editor folder not found. Let's create it and import sample level 03 data
             */
            System.out.println("Editor folder created, importing sample data...");

            importBuiltInLevel(3);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sample data created");
            alert.setHeaderText(null);
            alert.setContentText("First run - sample Level 3 data imported");

            alert.showAndWait();
        }

        if (level != null) {

            /*
             * Importing predefined level on demand
             */
            importBuiltInLevel(level);

        } else {

            /*
             * Opening last editor state. Let's check if user didn't remove required files.
             * If so, substitute them with ones taken from the sample Level 03.
             */
            String missingFiles = "";

            if (!isFilePresent("board.png")) {
                missingFiles = "\n- board.png";
            }
            if (!isFilePresent("door_h.png")) {
                missingFiles += "\n- door_h.png";
            }
            if (!isFilePresent("door_v.png")) {
                missingFiles += "\n- door_v.png";
            }
            if (!isFilePresent("exit_closed.png")) {
                missingFiles += "\n- exit_closed.png";
            }
            if (!isFilePresent("exit_open.png")) {
                missingFiles += "\n- exit_open.png";
            }
            if (!isFilePresent("key.png")) {
                missingFiles += "\n- key.png";
            }
            if (!isFilePresent("ladder_h.png")) {
                missingFiles += "\n- ladder_h.png";
            }
            if (!isFilePresent("ladder_v.png")) {
                missingFiles += "\n- ladder_v.png";
            }
            if (!isFilePresent("ornament.png")) {
                missingFiles += "\n- ornament.png";
            }
            if (!isFilePresent("ornament.png")) {
                missingFiles += "\n- ornament.png";
            }
            if (!isFilePresent("amulets.dat")) {
                missingFiles += "\n- amulets.dat";
            }
            if (!isFilePresent("arrows.dat")) {
                missingFiles += "\n- arrows.dat";
            }
            if (!isFilePresent("doors.dat")) {
                missingFiles += "\n- doors.dat";
            }
            if (!isFilePresent("keys.dat")) {
                missingFiles += "\n- keys.dat";
            }
            if (!isFilePresent("level.dat")) {
                missingFiles += "\n- level.dat";
            }
            if (!isFilePresent("slots.dat")) {
                missingFiles += "\n- slots.dat";
            }
            if (!isFilePresent("teleports.dat")) {
                missingFiles += "\n- teleports.dat";
            }

            if (!missingFiles.isEmpty()) {
                missingFiles = "Missing file(s) substituted:" + missingFiles;
            }

            if (!missingFiles.isEmpty()) {

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

    private void setEditorFilesFromUserLevel(String folderName) {

        mEditorFolder = new File(System.getProperty("user.home") + "/.EistReturns/levels/editor-data");
        if (mEditorFolder.mkdir()) {

            System.out.println("Created missing editor folder");
        }
        importUserLevelToEditor(folderName);
    }

    private boolean isFilePresent(String name) {

        File checkMe = new File(System.getProperty("user.home") + "/.EistReturns/levels/editor-data/" + name);
        if (!checkMe.exists()) {

            String dst = System.getProperty("user.home") + "/.EistReturns/levels/editor-data";

            try {

                copyStream(getClass().getClassLoader().getResourceAsStream("levels/03/" + name), new File(dst + "/" + name));
                System.out.println("Missing file substituted: " + name);

            } catch (IOException e) {
                System.out.println("Error copying " + name + ": " + e);
            }
            return false;

        } else {
            return true;
        }
    }

    private void importBuiltInLevel(Integer level) {

        String lvl = level != null ? lvlToString(level) : "03";
        mEditorStage.setTitle("Imported default level: " + lvl);

        String dst = System.getProperty("user.home") + "/.EistReturns/levels/editor-data";

        try {
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/amulet.png"), new File(dst + "/amulet.png"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/amulets.dat"), new File(dst + "/amulets.dat"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/arrows.dat"), new File(dst + "/arrows.dat"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/board.png"), new File(dst + "/board.png"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/door_h.png"), new File(dst + "/door_h.png"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/doors.dat"), new File(dst + "/doors.dat"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/door_v.png"), new File(dst + "/door_v.png"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/exit_closed.png"), new File(dst + "/exit_closed.png"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/exit_open.png"), new File(dst + "/exit_open.png"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/key.png"), new File(dst + "/key.png"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/keys.dat"), new File(dst + "/keys.dat"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/ladder_h.png"), new File(dst + "/ladder_h.png"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/ladder_v.png"), new File(dst + "/ladder_v.png"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/level.dat"), new File(dst + "/level.dat"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/ornament.png"), new File(dst + "/ornament.png"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/ornaments.dat"), new File(dst + "/ornaments.dat"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/slots.dat"), new File(dst + "/slots.dat"));
            copyStream(getClass().getClassLoader().getResourceAsStream("levels/" + lvl + "/teleports.dat"), new File(dst + "/teleports.dat"));

        } catch (IOException e) {
            System.out.println("Exception while copying stream: " + e);
        }
    }

    private void importUserLevelToEditor(String levelName) {

        File sourceFolder = new File(System.getProperty("user.home") + "/.EistReturns/levels/" + levelName);

        String missing = missingLevelFiles(sourceFolder);
        if (!missing.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Level integrity error");
            alert.setHeaderText("Missing files detected");
            alert.setContentText(missing);

            alert.showAndWait();
        }

        File[] sourceFiles = sourceFolder.listFiles();

        if (sourceFiles != null) {
            for (File file : sourceFiles) {

                Path source = Paths.get(file.toString());
                Path destination = Paths.get(System.getProperty("user.home") + "/.EistReturns/levels/editor-data", source.getFileName().toString());

                try {
                    copyFile(source.toFile(), destination.toFile());
                } catch (IOException e) {
                    System.out.println("Copying error: " + e);
                }
            }
            mEditorStage.setTitle("User level: " + levelName);
        }
    }

    private String missingLevelFiles(File sourceFolder) {

        StringBuilder missingFiles = new StringBuilder();
        String[] requiredFiles = new String[]{"amulet.png", "amulets.dat", "arrows.dat", "board.png", "door_h.png",
                "doors.dat", "door_v.png", "exit_closed.png", "exit_open.png", "key.png", "keys.dat", "ladder_h.png",
                "ladder_v.png", "level.dat", "ornament.png", "ornaments.dat", "slots.dat", "teleports.dat"};

        for (String filename : requiredFiles) {
            if (!new File(sourceFolder.toString() + "/" + filename).exists()) {
                missingFiles.append(filename);
                missingFiles.append("\n");
            }
        }
        return missingFiles.toString();
    }

    private void copyEditorToUserLevel(int level) {

        String lvlNumberToString = (level < 10) ? "0" + String.valueOf(level) : String.valueOf(level);
        Path destFolder = Paths.get(System.getProperty("user.home") + "/.EistReturns/levels/" + lvlNumberToString);

        if (destFolder.toFile().mkdir()) {
            System.out.println("Created " + destFolder);
        }
        File sourceFolder = new File(System.getProperty("user.home") + "/.EistReturns/levels/editor-data");
        File[] sourceFiles = sourceFolder.listFiles();

        if (sourceFiles != null) {
            for (File file : sourceFiles) {

                Path source = Paths.get(file.toString());
                Path destFile = Paths.get(destFolder.toString() + "/" + source.getFileName().toString());

                try {
                    copyFile(source.toFile(), destFile.toFile());
                } catch (IOException e) {
                    System.out.println("Copying error: " + e);
                }
            }
        }
    }

    private List<String> userLevels() {
        File sourceFolder = new File(System.getProperty("user.home") + "/.EistReturns/levels/");
        File[] sourceFiles = sourceFolder.listFiles();
        List<String> folderNames = new ArrayList<>();
        /*
         * Skip if the folder name is not a 2-digit number
         */
        if (sourceFiles != null) {
            for (File file : sourceFiles) {
                String name = file.getName();
                try {
                    // just to trigger an exception if the folder name couldn't be converted into int
                    int number = Integer.valueOf(name);

                    if (name.length() == 2) {
                        folderNames.add(name);
                    }
                } catch (Exception e) {
                    System.out.println("Skipped not-a-level folder: " + name);
                }
            }
        }
        Collections.sort(folderNames);
        return folderNames;
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

    private static void copyStream(InputStream is, File dest) throws IOException {
        try (OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }

    private void saveEditor(boolean toast) {
        StringBuilder content = new StringBuilder();
        if (mArrows != null && mArrows.size() > 0) {
            for (Arrow arrow : mArrows) {
                content.append(String.valueOf((int) (arrow.getPosX() / mGridDimension)));
                content.append(",");
                content.append(String.valueOf((int) (arrow.getPosY() / mGridDimension)));
                content.append(",");
                content.append(String.valueOf(arrow.getDirection()));
                content.append(":");
            }
            saveToDatFile("arrows.dat", content.toString());
        } else {
            saveToDatFile("arrows.dat", "");
        }

        content = new StringBuilder();
        if (mArtifacts != null && mArtifacts.size() > 0) {
            for (Artifact artifact : mArtifacts) {
                content.append(String.valueOf((int) (artifact.getPosX() / mGridDimension)));
                content.append(",");
                content.append(String.valueOf((int) (artifact.getPosY() / mGridDimension)));
                content.append(":");
            }
            saveToDatFile("amulets.dat", content.toString());
        } else {
            saveToDatFile("amulets.dat", "");
        }

        content = new StringBuilder();
        if (mDoors != null && mDoors.size() > 0) {
            for (Door door : mDoors) {
                content.append(String.valueOf((int) (door.getPosX() / mGridDimension)));
                content.append(",");
                content.append(String.valueOf((int) (door.getPosY() / mGridDimension)));
                content.append(",");
                content.append(String.valueOf(door.getOrientation()));
                content.append(":");
            }
            saveToDatFile("doors.dat", content.toString());
        } else {
            saveToDatFile("doors", "");
        }

        content = new StringBuilder();
        if (mKeys != null && mKeys.size() > 0) {
            for (Key key : mKeys) {
                content.append(String.valueOf((int) (key.getPosX() / mGridDimension)));
                content.append(",");
                content.append(String.valueOf((int) (key.getPosY() / mGridDimension)));
                content.append(":");
            }
            saveToDatFile("keys.dat", content.toString());
        } else {
            saveToDatFile("keys.dat", "");
        }

        content = new StringBuilder();
        if (mOrnaments != null && mOrnaments.size() > 0) {
            for (Ornament ornament : mOrnaments) {
                content.append(String.valueOf((int) (ornament.getPosX() / mGridDimension)));
                content.append(",");
                content.append(String.valueOf((int) (ornament.getPosY() / mGridDimension)));
                content.append(":");
            }
            saveToDatFile("ornaments.dat", content.toString());
        } else {
            saveToDatFile("ornaments.dat", "");
        }

        content = new StringBuilder();
        if (mSlots != null && mSlots.size() > 0) {
            for (Slot slot : mSlots) {
                content.append(String.valueOf((int) (slot.getPosX() / mGridDimension)));
                content.append(",");
                content.append(String.valueOf((int) (slot.getPosY() / mGridDimension)));
                content.append(",");
                content.append(String.valueOf(slot.getOrientation()));
                content.append(":");
            }
            saveToDatFile("slots.dat", content.toString());
        } else {
            saveToDatFile("slots.dat", "");
        }

        content = new StringBuilder();
        content.append(String.valueOf((int) (eist.x / mGridDimension)));
        content.append(",");
        content.append(String.valueOf((int) (eist.y / mGridDimension)));
        content.append(",");
        content.append(String.valueOf(eist.getDirection()));
        content.append(",");
        content.append(String.valueOf((int) (exit.getPosX() / mGridDimension)));
        content.append(",");
        content.append(String.valueOf((int) (exit.getPosY() / mGridDimension)));
        content.append(",");
        content.append(String.valueOf(ladder.getSlotIdx()));

        saveToDatFile("level.dat", content.toString());

        boolean teleportsOK = true;
        content = new StringBuilder();
        if (mTeleports != null && mTeleports.size() == 2) {
            for (Teleport teleport : mTeleports) {
                content.append(String.valueOf((int) (teleport.getPosX() / mGridDimension)));
                content.append(",");
                content.append(String.valueOf((int) (teleport.getPosY() / mGridDimension)));
                content.append(":");
            }
            saveToDatFile("teleports.dat", content.toString());
        } else {
            saveToDatFile("teleports.dat", "");
            if (mTeleports != null && mTeleports.size() == 1) {
                teleportsOK = false;
            }
        }
        if (teleportsOK) {
            if (toast) {
                Toast.makeText(mEditorStage, "Editor saved", TOAST_LENGTH_SHORT);
            }
        } else {
            if (toast) {
                Toast.makeText(mEditorStage, "Editor saved (teleports skipped - pair not found)", TOAST_LENGTH_SHORT);
            }
        }
    }

    private void saveEditor() {
        saveEditor(false);
    }

    private void saveToDatFile(String filename, String content) {
        String pathAndFilename = new File(System.getProperty("user.home") + "/.EistReturns/levels/editor-data/" + filename).toString();
        try {
            PrintStream out = new PrintStream(pathAndFilename);
            out.println(content);
        } catch (IOException e) {
            System.out.println("Failed writing " + filename + ": " + e);
        }
    }

    /*
     * The class Toast after https://stackoverflow.com/a/38373408/4040598 by @alcoolis
     */
    private static final class Toast {

        private static void makeText(Stage ownerStage, String toastMsg, int toastDelay) {
            Stage toastStage = new Stage();
            toastStage.initOwner(ownerStage);
            toastStage.setResizable(false);
            toastStage.initStyle(StageStyle.TRANSPARENT);

            Text text = new Text(toastMsg);
            text.setFont(Font.font("Verdana", 20));
            text.setFill(Color.BLACK);

            StackPane root = new StackPane(text);
            root.setStyle("-fx-background-radius: 20; -fx-background-color: rgba(255, 255, 255, 0.9); -fx-padding: 20px;");
            root.setOpacity(0);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);
            toastStage.show();

            Timeline fadeInTimeline = new Timeline();
            KeyFrame fadeInKey1 = new KeyFrame(Duration.millis(200), new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 1));
            fadeInTimeline.getKeyFrames().add(fadeInKey1);
            fadeInTimeline.setOnFinished((ae) -> new Thread(() -> {
                try {
                    Thread.sleep(toastDelay);
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
                Timeline fadeOutTimeline = new Timeline();
                KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(500), new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 0));
                fadeOutTimeline.getKeyFrames().add(fadeOutKey1);
                fadeOutTimeline.setOnFinished((aeb) -> toastStage.close());
                fadeOutTimeline.play();
            }).start());
            fadeInTimeline.play();
        }

    }

    private void setUpButtons() {

        HBox hBoxRow0 = new HBox();
        hBoxRow0.setLayoutX(toolbar.getOpenArea().getMinX());
        hBoxRow0.setLayoutY(toolbar.getOpenArea().getMinY());

        HBox hBoxRow1 = new HBox();
        hBoxRow1.setLayoutX(toolbar.getDoorArea().getMinX());
        hBoxRow1.setLayoutY(toolbar.getDoorArea().getMinY());

        HBox hBoxRow2 = new HBox();
        hBoxRow2.setLayoutX(toolbar.getArtifactArea().getMinX());
        hBoxRow2.setLayoutY(toolbar.getArtifactArea().getMinY());

        HBox hBoxRow3 = new HBox();
        hBoxRow3.setLayoutX(toolbar.getTeleportArea().getMinX());
        hBoxRow3.setLayoutY(toolbar.getTeleportArea().getMinY());

        HBox hBoxRow4 = new HBox();
        hBoxRow4.setLayoutX(toolbar.getExitArea().getMinX());
        hBoxRow4.setLayoutY(toolbar.getExitArea().getMinY());

        HBox hBoxRow5 = new HBox();
        hBoxRow5.setLayoutX(toolbar.getEistArea().getMinX());
        hBoxRow5.setLayoutY(toolbar.getEistArea().getMinY());

        mImportButton = new Button();
        mImportButton.setOpacity(0);
        mImportButton.setMinHeight(mGridDimension);
        mImportButton.setMinWidth(mGridDimension);

        mImportButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> mMenuHint = "Import built-in level");

        mImportButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");
        mImportButton.setOnAction(e -> displayImportLevelChoiceDialog());

        mOpenButton = new Button();
        mOpenButton.setOpacity(0);
        mOpenButton.setMinHeight(mGridDimension);
        mOpenButton.setMinWidth(mGridDimension);

        mOpenButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> mMenuHint = "Open user level");

        mOpenButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");
        mOpenButton.setOnAction(e -> displayOpenUserLevelDialog());

        mSaveButton = new Button();
        mSaveButton.setOpacity(0);
        mSaveButton.setMinHeight(mGridDimension);
        mSaveButton.setMinWidth(mGridDimension);

        mSaveButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> mMenuHint = "Save editor state");

        mSaveButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");
        mSaveButton.setOnAction(e -> saveEditor(true));

        mSaveAsButton = new Button();
        mSaveAsButton.setOpacity(0);
        mSaveAsButton.setMinHeight(mGridDimension);
        mSaveAsButton.setMinWidth(mGridDimension);

        mSaveAsButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> mMenuHint = "Save as user level");

        mSaveAsButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");
        mSaveAsButton.setOnAction(e -> {
            saveEditor();
            displaySaveLevelAsChoiceDialog();
        });

        mToolsButton = new Button();
        mToolsButton.setOpacity(0);
        mToolsButton.setMinHeight(mGridDimension);
        mToolsButton.setMinWidth(mGridDimension);

        mToolsButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> mMenuHint = "Display tools window");

        mToolsButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");
        mToolsButton.setOnAction(e -> displayTools());

        hBoxRow0.getChildren().add(mImportButton);
        hBoxRow0.getChildren().add(mSaveButton);
        hBoxRow0.getChildren().add(mOpenButton);
        hBoxRow0.getChildren().add(mSaveAsButton);
        hBoxRow0.getChildren().add(mToolsButton);
        hBoxRow0.setSpacing(0);

        mDoorButton = new Button();
        mDoorButton.setOpacity(0);
        mDoorButton.setMinHeight(mGridDimension);
        mDoorButton.setMinWidth(mGridDimension);

        mDoorButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> mMenuHint = "Import built-in level");

        mDoorButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");
        mDoorButton.setOnAction(e -> displayImportLevelChoiceDialog());


        mDoorButton = new Button();
        mDoorButton.setOpacity(0);
        mDoorButton.setMinHeight(mFrameDimension);
        mDoorButton.setMinWidth(mFrameDimension);

        mDoorButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> mMenuHint = "Click path to add door\nRight click rotates");

        mDoorButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");

        mDoorButton.setOnMouseClicked(event -> {
            MouseButton button = event.getButton();
            if (button == MouseButton.PRIMARY) {
                toolbar.setSelection(SELECTION_DOOR);
            } else if (button == MouseButton.SECONDARY) {
                toolbar.setSelection(SELECTION_DOOR);
                if (toolbar.getDoorOrientation() == ORIENTATION_HORIZONTAL) {
                    toolbar.setDoorOrientation(ORIENTATION_VERTICAL);
                } else {
                    toolbar.setDoorOrientation(ORIENTATION_HORIZONTAL);
                }
            }
        });

        mSlotButton = new Button();
        mSlotButton.setOpacity(0);
        mSlotButton.setMinHeight(mFrameDimension);
        mSlotButton.setMinWidth(mFrameDimension);

        mSlotButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> mMenuHint = "Click path to add slot\nRight click rotates");

        mSlotButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");

        mSlotButton.setOnMouseClicked(event -> {
            MouseButton button = event.getButton();
            if (button == MouseButton.PRIMARY) {
                toolbar.setSelection(SELECTION_SLOT);
            } else if (button == MouseButton.SECONDARY) {
                toolbar.setSelection(SELECTION_SLOT);
                if (toolbar.getSlotOrientation() == ORIENTATION_HORIZONTAL) {
                    toolbar.setSlotOrientation(ORIENTATION_VERTICAL);
                } else {
                    toolbar.setSlotOrientation(ORIENTATION_HORIZONTAL);
                }
            }
        });

        hBoxRow1.getChildren().add(mDoorButton);
        hBoxRow1.getChildren().add(mSlotButton);
        hBoxRow1.setSpacing(0);

        mArtifactButton = new Button();
        mArtifactButton.setOpacity(0);
        mArtifactButton.setMinHeight(mFrameDimension);
        mArtifactButton.setMinWidth(mFrameDimension);

        mArtifactButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> mMenuHint = "Click path to add artifact");

        mArtifactButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");

        mArtifactButton.setOnMouseClicked(event -> toolbar.setSelection(SELECTION_ARTIFACT));

        mKeyButton = new Button();
        mKeyButton.setOpacity(0);
        mKeyButton.setMinHeight(mFrameDimension);
        mKeyButton.setMinWidth(mFrameDimension);

        mKeyButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> mMenuHint = "Click path to add key");

        mKeyButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");

        mKeyButton.setOnMouseClicked(event -> toolbar.setSelection(SELECTION_KEY));

        hBoxRow2.getChildren().add(mArtifactButton);
        hBoxRow2.getChildren().add(mKeyButton);
        hBoxRow2.setSpacing(0);

        mTeleportButton = new Button();
        mTeleportButton.setOpacity(0);
        mTeleportButton.setMinHeight(mFrameDimension);
        mTeleportButton.setMinWidth(mFrameDimension);

        mTeleportButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> {
                    if (mTeleports.size() < 2) {
                        mMenuHint = "Click path to add teleport";
                    } else {
                        mMenuHint = "2 teleports allowed";
                    }
                });

        mTeleportButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");

        mTeleportButton.setOnMouseClicked(event -> {
            if (mTeleports.size() < 2) {
                toolbar.setSelection(SELECTION_TELEPORT);
            }
        });

        mOrnamentButton = new Button();
        mOrnamentButton.setOpacity(0);
        mOrnamentButton.setMinHeight(mFrameDimension);
        mOrnamentButton.setMinWidth(mFrameDimension);

        mOrnamentButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> mMenuHint = "Click path to add\nornament");

        mOrnamentButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");

        mOrnamentButton.setOnMouseClicked(event -> toolbar.setSelection(SELECTION_ORNAMENT));

        hBoxRow3.getChildren().add(mTeleportButton);
        hBoxRow3.getChildren().add(mOrnamentButton);
        hBoxRow3.setSpacing(0);

        mExitButton = new Button();
        mExitButton.setOpacity(0);
        mExitButton.setMinHeight(mFrameDimension);
        mExitButton.setMinWidth(mFrameDimension);

        mExitButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> mMenuHint = "Click path to move exit");

        mExitButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");

        mExitButton.setOnMouseClicked(event -> toolbar.setSelection(SELECTION_EXIT));

        mArrowButton = new Button();
        mArrowButton.setOpacity(0);
        mArrowButton.setMinHeight(mFrameDimension);
        mArrowButton.setMinWidth(mFrameDimension);

        mArrowButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> mMenuHint = "Click path to add arrow\nRight click rotates");

        mArrowButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");

        mArrowButton.setOnMouseClicked(event -> {
            MouseButton button = event.getButton();
            if (button == MouseButton.PRIMARY) {
                toolbar.setSelection(SELECTION_ARROW);
            } else if (button == MouseButton.SECONDARY) {
                toolbar.setSelection(SELECTION_ARROW);
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
        });

        hBoxRow4.getChildren().add(mExitButton);
        hBoxRow4.getChildren().add(mArrowButton);
        hBoxRow4.setSpacing(0);

        mEistButton = new Button();
        mEistButton.setOpacity(0);
        mEistButton.setMinHeight(mFrameDimension);
        mEistButton.setMinWidth(mFrameDimension);

        mEistButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> mMenuHint = "Click path to move Eist\nRight click rotates");

        mEistButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");

        mEistButton.setOnMouseClicked(event -> {
            MouseButton button = event.getButton();
            if (button == MouseButton.PRIMARY) {
                toolbar.setSelection(SELECTION_EIST);
            } else if (button == MouseButton.SECONDARY) {
                toolbar.setSelection(SELECTION_EIST);
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
                }
            }
        });

        mClearButton = new Button();
        mClearButton.setOpacity(0);
        mClearButton.setMinHeight(mFrameDimension);
        mClearButton.setMinWidth(mFrameDimension);

        mClearButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> mMenuHint = "Click object to delete");

        mClearButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> mMenuHint = "");

        mClearButton.setOnMouseClicked(event -> toolbar.setSelection(SELECTION_CLEAR));

        hBoxRow5.getChildren().add(mEistButton);
        hBoxRow5.getChildren().add(mClearButton);
        hBoxRow5.setSpacing(0);

        ((Group) mEditorScene.getRoot()).getChildren().add(hBoxRow0);
        ((Group) mEditorScene.getRoot()).getChildren().add(hBoxRow1);
        ((Group) mEditorScene.getRoot()).getChildren().add(hBoxRow2);
        ((Group) mEditorScene.getRoot()).getChildren().add(hBoxRow3);
        ((Group) mEditorScene.getRoot()).getChildren().add(hBoxRow4);
        ((Group) mEditorScene.getRoot()).getChildren().add(hBoxRow5);

        disableButtons(false);
    }

    private void disableButtons(boolean disable) {
        mImportButton.setDisable(disable);
        mSaveButton.setDisable(disable);
        mSaveAsButton.setDisable(disable);
        mDoorButton.setDisable(disable);
        mSlotButton.setDisable(disable);
        mArtifactButton.setDisable(disable);
        mKeyButton.setDisable(disable);
        mTeleportButton.setDisable(disable);
        mOrnamentButton.setDisable(disable);
        mExitButton.setDisable(disable);
        mArrowButton.setDisable(disable);
        mEistButton.setDisable(disable);
        mClearButton.setDisable(disable);
        mOpenButton.setDisable(disable);
        mToolsButton.setDisable(disable);
    }

    String lvlToString(int level) {
        return (level < 10) ? "0" + String.valueOf(level) : String.valueOf(level);
    }

    private void displayTools() {

        Stage stage = new Stage();
        stage.initOwner(mEditorStage);
        stage.setResizable(false);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.WINDOW_MODAL);

        stage.setTitle("Tools");
        stage.setWidth(mEditorStage.getWidth() / 3);
        stage.setHeight(mEditorStage.getHeight() * 0.95);

        Text hint = new Text();
        hint.setFont(messageFont);
        hint.setFill(Color.WHITE);

        hint.setText("Select action below:");

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-padding: 20px;");
        root.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    stage.close();
                    break;
                default:
                    break;
            }
        });

        VBox buttonsBox = new VBox();
        buttonsBox.setSpacing(15);
        buttonsBox.setMinWidth(root.getMinWidth());
        buttonsBox.setAlignment(Pos.CENTER);

        final Button buttonExit = new Button();
        buttonExit.setFont(menuFont);
        buttonExit.setStyle("-fx-text-fill: white;");
        buttonExit.setBackground(mButtonBackground);
        buttonExit.setMinWidth(mButtonWidth);
        buttonExit.setMinHeight(mGridDimension);
        buttonExit.setText("Exit editor");

        buttonExit.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Closes the level editor"));

        buttonExit.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        buttonExit.setOnAction(e -> Platform.exit());

        final Button button1 = new Button();
        button1.setFont(menuFont);
        button1.setStyle("-fx-text-fill: white;");
        button1.setBackground(mButtonBackground);
        button1.setMinWidth(mButtonWidth);
        button1.setMinHeight(mGridDimension);
        button1.setText("Delete user level");

        button1.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Deletes selected user-defined level"));

        button1.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        button1.setOnAction(e -> {
            stage.close();
            displayDeleteUserLevelDialog(mEditorStage);
        });

        final Button button2 = new Button();
        button2.setFont(menuFont);
        button2.setStyle("-fx-text-fill: white;");
        button2.setBackground(mButtonBackground);
        button2.setMinWidth(mButtonWidth);
        button2.setMinHeight(mGridDimension);
        button2.setText("Clear user levels");

        button2.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Deletes all user-defined levels"));

        button2.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        button2.setOnAction(e -> {
            stage.close();
            displayClearAllUserLevelsDialog(mEditorStage);
        });

        final Button buttonClose = new Button();
        buttonClose.setFont(menuFont);
        buttonClose.setStyle("-fx-text-fill: white;");
        buttonClose.setBackground(mButtonBackground);
        buttonClose.setMinWidth(mButtonWidth);
        buttonClose.setMinHeight(mGridDimension);
        buttonClose.setText("Close");

        buttonClose.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Closes this window"));

        buttonClose.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        buttonClose.setOnAction(e -> stage.close());

        final Button button4 = new Button();
        button4.setFont(menuFont);
        button4.setStyle("-fx-text-fill: white;");
        button4.setBackground(mButtonBackground);
        button4.setMinWidth(mButtonWidth);
        button4.setMinHeight(mGridDimension);
        button4.setText("Import user levels");

        button4.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Imports a set of levels from external file"));

        button4.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        button4.setOnAction(e -> {
            stage.close();
            importLevelsFromZip(mEditorStage);
        });

        final Button button5 = new Button();
        button5.setFont(menuFont);
        button5.setStyle("-fx-text-fill: white;");
        button5.setBackground(mButtonBackground);
        button5.setMinWidth(mButtonWidth);
        button5.setMinHeight(mGridDimension);
        button5.setText("Export user levels");

        button5.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Exports user levels to a file"));

        button5.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        button5.setOnAction(e -> {
            stage.close();
            if (userLevels().size() > 0) {
                stage.close();
                displayExportLevelsDialog(mEditorStage);
            } else {
                Toast.makeText(mEditorStage, "No user-defined levels found", TOAST_LENGTH_SHORT);

            }
        });

        final Button buttonName = new Button();
        buttonName.setFont(menuFont);
        buttonName.setStyle("-fx-text-fill: white;");
        buttonName.setBackground(mButtonBackground);
        buttonName.setMinWidth(mButtonWidth);
        buttonName.setMinHeight(mGridDimension);
        buttonName.setText("Name your levels");

        buttonName.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Give the set of levels a name"));

        buttonName.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        buttonName.setOnAction(e -> {
            stage.close();
            if (userLevels().size() > 0) {
                displaySetNameDialog(mEditorStage);
            } else {
                Toast.makeText(mEditorStage, "At least 1 saved, user-defined level required", TOAST_LENGTH_LONG);
            }
        });

        final Button buttonUpload = new Button();
        buttonUpload.setFont(menuFont);
        buttonUpload.setStyle("-fx-text-fill: white;");
        buttonUpload.setBackground(mButtonBackground);
        buttonUpload.setMinWidth(mButtonWidth);
        buttonUpload.setMinHeight(mGridDimension);
        buttonUpload.setText("Upload a bitmap");

        buttonUpload.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Uploads external bitmap to the editor"));

        buttonUpload.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        buttonUpload.setOnAction(e -> {
            stage.close();
            uploadBitmap();
        });

        buttonsBox.getChildren().add(hint);

        buttonsBox.getChildren().add(buttonExit);
        buttonsBox.getChildren().add(button1);
        buttonsBox.getChildren().add(button2);
        buttonsBox.getChildren().add(button4);
        buttonsBox.getChildren().add(button5);
        buttonsBox.getChildren().add(buttonName);
        buttonsBox.getChildren().add(buttonUpload);
        buttonsBox.getChildren().add(buttonClose);

        root.getChildren().add(buttonsBox);

        stage.show();

    }

    private void displaySettings() {

        Stage stage = new Stage();
        stage.initOwner(mGameStage);
        stage.setResizable(false);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.WINDOW_MODAL);

        stage.setTitle("Tools");
        stage.setWidth(mGameStage.getWidth() / 3);
        stage.setHeight(mGameStage.getHeight() * 0.95);

        Text hint = new Text();
        hint.setFont(messageFont);
        hint.setFill(Color.WHITE);

        hint.setText("Select action below:");

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-padding: 20px;");
        root.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    stage.close();
                    break;
                case D:
                    stage.close();
                    displayDevKeyDialog();
                    break;
                default:
                    break;
            }
        });

        VBox buttonsBox = new VBox();
        buttonsBox.setSpacing(15);
        buttonsBox.setMinWidth(root.getMinWidth());
        buttonsBox.setAlignment(Pos.CENTER);

        final Button buttonSize = new Button();
        buttonSize.setFont(menuFont);
        buttonSize.setStyle("-fx-text-fill: white;");
        buttonSize.setBackground(mButtonBackground);
        buttonSize.setMinWidth(mButtonWidth);
        buttonSize.setMinHeight(mGridDimension);
        buttonSize.setText("Board dimensions");

        buttonSize.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Switches the widow size (restart required)"));

        buttonSize.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        buttonSize.setOnAction(e -> {
            stage.close();
            displaySizeDialog();
        });

        final Button buttonClearAll = new Button();
        buttonClearAll.setFont(menuFont);
        buttonClearAll.setStyle("-fx-text-fill: white;");
        buttonClearAll.setBackground(mButtonBackground);
        buttonClearAll.setMinWidth(mButtonWidth);
        buttonClearAll.setMinHeight(mGridDimension);
        buttonClearAll.setText("Reset the game");

        buttonClearAll.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Clears settings and locally saved scores"));

        buttonClearAll.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        buttonClearAll.setOnAction(e -> {
            stage.close();
            displayClearAllDialog();
        });

        final Button buttonFinish = new Button();
        buttonFinish.setFont(menuFont);
        buttonFinish.setStyle("-fx-text-fill: white;");
        buttonFinish.setBackground(mButtonBackground);
        buttonFinish.setMinWidth(mButtonWidth);
        buttonFinish.setMinHeight(mGridDimension);
        buttonFinish.setText("Exit game");

        buttonFinish.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Closes the game"));

        buttonFinish.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        buttonFinish.setOnAction(e -> {
            stage.close();
            if(mResultsChanged) {
                gameExit(false);
            } else {
                Platform.exit();
            }
        });

        final Button buttonClose = new Button();
        buttonClose.setFont(menuFont);
        buttonClose.setStyle("-fx-text-fill: white;");
        buttonClose.setBackground(mButtonBackground);
        buttonClose.setMinWidth(mButtonWidth);
        buttonClose.setMinHeight(mGridDimension);
        buttonClose.setText("Close");

        buttonClose.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Closes this window"));

        buttonClose.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        buttonClose.setOnAction(e -> stage.close());

        final Button buttonImport = new Button();
        buttonImport.setFont(menuFont);
        buttonImport.setStyle("-fx-text-fill: white;");
        buttonImport.setBackground(mButtonBackground);
        buttonImport.setMinWidth(mButtonWidth);
        buttonImport.setMinHeight(mGridDimension);
        buttonImport.setText("Import user levels");

        buttonImport.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Imports levels from external file"));

        buttonImport.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        buttonImport.setOnAction(e -> {
            stage.close();
            importLevelsFromZip(mGameStage);
            loadLevel(0);
        });

        final Button buttonRestore = new Button();
        buttonRestore.setFont(menuFont);
        buttonRestore.setStyle("-fx-text-fill: white;");
        buttonRestore.setBackground(mButtonBackground);
        buttonRestore.setMinWidth(mButtonWidth);
        buttonRestore.setMinHeight(mGridDimension);
        buttonRestore.setText("Restore levels");

        buttonRestore.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Clears all user-defined levels"));

        buttonRestore.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        buttonRestore.setOnAction(e -> {
            stage.close();
            displayClearAllUserLevelsDialog(mGameStage);
            loadLevel(0);
        });

        final Button buttonScores = new Button();
        buttonScores.setFont(menuFont);
        buttonScores.setStyle("-fx-text-fill: white;");
        buttonScores.setBackground(mButtonBackground);
        buttonScores.setMinWidth(mButtonWidth);
        buttonScores.setMinHeight(mGridDimension);
        buttonScores.setText("Level scores");

        buttonScores.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Displays levels best results"));

        buttonScores.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        buttonScores.setOnAction(e -> {
            stage.close();
            displayHighScores();
        });

        final Button buttonManagePlayers = new Button();
        buttonManagePlayers.setFont(menuFont);
        buttonManagePlayers.setStyle("-fx-text-fill: white;");
        buttonManagePlayers.setBackground(mButtonBackground);
        buttonManagePlayers.setMinWidth(mButtonWidth);
        buttonManagePlayers.setMinHeight(mGridDimension);
        buttonManagePlayers.setText("Manage players");

        buttonManagePlayers.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Create or delete player"));

        buttonManagePlayers.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        buttonManagePlayers.setOnAction(e -> {
            stage.close();
            displayManagePlayerDialog();
        });

        final Button buttonLogOut = new Button();
        buttonLogOut.setFont(menuFont);
        buttonLogOut.setStyle("-fx-text-fill: white;");
        buttonLogOut.setBackground(mButtonBackground);
        buttonLogOut.setMinWidth(mButtonWidth);
        buttonLogOut.setMinHeight(mGridDimension);
        buttonLogOut.setText("Log me out");

        buttonLogOut.addEventHandler(MouseEvent.MOUSE_ENTERED,
                e -> hint.setText("Disconnect the Hall of Fame"));

        buttonLogOut.addEventHandler(MouseEvent.MOUSE_EXITED,
                e -> hint.setText("Select action below:"));
        buttonLogOut.setOnAction(e -> {
            stage.close();
            playerLogOut();
        });

        buttonsBox.getChildren().add(hint);

        buttonsBox.getChildren().add(buttonFinish);
        buttonsBox.getChildren().add(buttonSize);
        buttonsBox.getChildren().add(buttonClearAll);
        buttonsBox.getChildren().add(buttonImport);
        buttonsBox.getChildren().add(buttonRestore);
        buttonsBox.getChildren().add(buttonScores);
        buttonsBox.getChildren().add(buttonManagePlayers);
        buttonsBox.getChildren().add(buttonLogOut);
        buttonsBox.getChildren().add(buttonClose);

        root.getChildren().add(buttonsBox);

        stage.show();
    }

    private void displayHoF(String scores) {

        Text header = new Text();
        header.setFont(infoFont);
        header.setFill(Color.color(0, 1, 1, 1));

        Text contentText = new Text();
        contentText.setFont(playerFont);
        contentText.setFill(Color.WHITE);

        List<String> entryLines = new ArrayList<>();

        if (!scores.equals("user_set")) {
            if (!scores.isEmpty()) {

                String rows[] = scores.split(":");
                int pos = 1;
                for (String row : rows) {

                    String[] thisRow = row.split(",");
                    String singleRow = pos + ". " + thisRow[0].toUpperCase() +
                            ": " +
                            thisRow[1] +
                            " levels, " +
                            thisRow[2] +
                            " turns";
                    entryLines.add(singleRow);
                    pos++;
                }
                header.setText("Hall of Fame");
                //contentText.setText(allRows.toString());
            } else {
                header.setText("Couldn't load scores\n");
            }
        } else {
            header.setText("Hall of Fame unavailable");
            entryLines.add("You're playing an user-defined set.\nRestore default levels to access the Hall of Fame.\nNote: this will CLEAR current level scores!\n ");
        }

        Stage stage = new Stage();
        stage.initOwner(mGameStage);
        stage.setResizable(false);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.WINDOW_MODAL);

        stage.setWidth(mFrameDimension * 6);
        stage.setHeight(mGameStage.getHeight() * 0.95);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-padding: " + mGridDimension + ";");
        root.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(root);
        scene.getStylesheets().add("stylesheet.css");
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    stage.close();
                    break;
                case ENTER:
                    stage.close();
                    break;
                default:
                    break;
            }
        });

        VBox contentBox = new VBox();
        contentBox.setSpacing(20);
        contentBox.setMinWidth(root.getMinWidth());
        contentBox.setAlignment(Pos.TOP_CENTER);
        contentBox.getChildren().add(header);

        VBox scoreLines = new VBox();
        scoreLines.setMinWidth(contentBox.getWidth());
        for (String line : entryLines) {
            Text text = new Text(line);
            text.setFont(playerFont);
            if (line.toUpperCase().contains(" " + mPlayer.toUpperCase() + ": ")) {
                text.setFill(Color.color(0.5, 1, 0.5, 1));
            } else {

                text.setFill(Color.WHITE);
            }
            scoreLines.getChildren().add(text);
        }
        contentBox.getChildren().add(scoreLines);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefSize(stage.getWidth(), stage.getHeight());
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setContent(contentText);
        contentBox.getChildren().add(scrollPane);

        final Button buttonLogout = new Button();
        buttonLogout.setFont(menuFont);
        buttonLogout.setStyle("-fx-text-fill: white;");
        buttonLogout.setBackground(mButtonBackgroundNarrow);
        buttonLogout.setMinWidth(mButtonWidthNarrow);
        buttonLogout.setMinHeight(mGridDimension);
        buttonLogout.setText("Log out");
        buttonLogout.setOnAction(e -> {
            stage.close();
            playerLogOut();
        });

        final Button buttonWebsite = new Button();
        buttonWebsite.setFont(menuFont);
        buttonWebsite.setStyle("-fx-text-fill: white;");
        buttonWebsite.setBackground(mButtonBackgroundNarrow);
        buttonWebsite.setMinWidth(mButtonWidthNarrow);
        buttonWebsite.setMinHeight(mGridDimension);
        buttonWebsite.setText("Details");
        buttonWebsite.setOnAction(e -> {
            stage.close();

            Platform.runLater(this::openWebsite);

        });

        final Button buttonClose = new Button();
        buttonClose.setFont(menuFont);
        buttonClose.setStyle("-fx-text-fill: white;");
        buttonClose.setBackground(mButtonBackgroundNarrow);
        buttonClose.setMinWidth(mButtonWidthNarrow);
        buttonClose.setMinHeight(mGridDimension);
        buttonClose.setText("Close");
        buttonClose.setOnAction(e -> {
            stage.close();
        });

        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(15);
        buttonsBox.setMinWidth(root.getMinWidth());
        buttonsBox.setAlignment(Pos.CENTER);

        buttonsBox.getChildren().add(buttonLogout);
        buttonsBox.getChildren().add(buttonWebsite);
        buttonsBox.getChildren().add(buttonClose);

        contentBox.getChildren().add(buttonsBox);

        root.getChildren().add(contentBox);

        stage.show();
    }

    private void clearScores() {
        prefs.putInt("level", 1);
        prefs.putInt("achieved", 1);
        mSelectedLevel = 1;
        for (int i = 1; i < 41; i++) {
            prefs.putInt(lvlToString(i) + "best", 0);
        }
    }

    private void uploadBitmap() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a picture to upload");
        String initialDir = prefs.get("importPath", "");
        if (!initialDir.isEmpty() && new File(initialDir).exists()) {
            fileChooser.setInitialDirectory(new File(initialDir));
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("png files", "amulet.png", "board.png", "door_h.png",
                        "door_v.png", "exit_closed.png", "exit_open.png", "key.png", "ladder_h.png", "ladder_v.png", "ornament.png"));

        File selectedFile = fileChooser.showOpenDialog(mEditorStage);

        if (selectedFile != null) {

            prefs.put("importPath", selectedFile.toString().split(selectedFile.getName())[0]);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("File upload");
            alert.setHeaderText("Replacing the '" + selectedFile.getName() + "' file");
            alert.setContentText("Are you sure?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {

                try {
                    saveEditor();
                    copyFile(selectedFile, new File(System.getProperty("user.home") + "/.EistReturns/levels/editor-data/" + selectedFile.getName()));
                    Toast.makeText(mEditorStage, "File " + selectedFile.getName() + " uploaded", TOAST_LENGTH_SHORT);
                    loadEditor();

                } catch (IOException e) {
                    displayExceptionAlert("Error uploading file", e);
                }

            }

        } else {
            Toast.makeText(mEditorStage, "No file selected", TOAST_LENGTH_SHORT);
        }

    }

    void gameExit(boolean askForConfirmation){

        if(askForConfirmation) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit game");
            if (mResultsChanged && !mPlayer.isEmpty()) {
                alert.setHeaderText("This will quit current game.\nResults will be stored in the Hall of Fame.");
            } else {
                alert.setHeaderText("This will quit current game. ");
            }
            alert.setContentText("Are you sure you want to exit?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (mResultsChanged && !mPlayer.isEmpty() && !mPass.isEmpty() && !mDevMode) {
                    updateHallScore(mPlayer, mPass, true);
                } else {
                    mGameStage.close();
                    Platform.exit();
                }
            }
        } else {
            if (!mPlayer.isEmpty() && !mPass.isEmpty() && !mDevMode) {
                updateHallScore(mPlayer, mPass, true);
            } else {
                mGameStage.close();
                Platform.exit();
            }
        }

    }

    private void openWebsite(){
        Thread thread = new Thread(() -> {

            try {
                java.awt.Desktop.getDesktop().browse(new URI("http://nwg.pl/eist/index.php?uname=" + mPlayer));
            } catch (URISyntaxException e1) {
                System.out.println("URISyntaxException " + e1);
            } catch (IOException e2) {
                System.out.println("IOException " + e2);
            }
            System.out.println("www thread finished");
        });
        thread.start();
    }
}
