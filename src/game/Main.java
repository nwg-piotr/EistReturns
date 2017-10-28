package game;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.*;

import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import game.Sprites.Player;
import game.Sprites.Arrow;
import game.Sprites.Artifact;
import game.Sprites.Ornament;
import game.Sprites.Teleport;
import game.Sprites.Key;
import game.Sprites.Door;
import game.Sprites.Ladder;
import game.Sprites.Slot;
import game.Sprites.Exit;
import game.Sprites.Pad;

public class Main extends Utils {

    private GraphicsContext gc;

    private final int FRAME_LAST_IDX = 7;
    private final double FRAME_DURATION_EIST = 90000000;
    private final double FRAME_DURATION_ARTIFACT = 135000000;
    private final double FRAME_DURATION_FALLING = 60000000;


    private long lastEistFrameChangeTime;
    private long lastArtifactFrameChangeTime;
    private long lastFallingFrameChangeTime;

    private double mFps = 0;

    private boolean mShowFps = false;

    private AnimationTimer animationTimer;
    private boolean mTrackMainWasPlaying;
    private boolean mTrackLevelWasPlaying;

    @Override
    public void start(Stage stage) throws Exception {

        mGameStage = stage;

        setBoard();

        mGameStage.setTitle("Eist returns");
        mGameStage.getIcons().add(new Image("/images/common/eist.png"));
        mGameStage.setResizable(false);
        if (mDimensionDivider == 1.0) {
            mGameStage.setFullScreen(true);
        }

        Group root = new Group();
        Scene mScene = new Scene(root, mSceneWidth, mSceneHeight);
        mGameStage.setScene(mScene);


        mGameStage.setOnCloseRequest(event -> {
            System.out.println("Window close requested");
            event.consume();
            mGameStage.close();
            Platform.exit();
        });

        Canvas canvas = new Canvas(mSceneWidth, mSceneHeight);
        root.getChildren().add(canvas);
        gc = canvas.getGraphicsContext2D();

        initializeFonts();

        gc.setFont(infoFont);

        eist = new Player();
        ladder = new Ladder();
        exit = new Exit();
        pad = new Pad();

        loadCommonGraphics();

        mSelectedLevel = prefs.getInt("achieved", 1);

        loadLevel(mCurrentLevel);

        mScene.setOnMouseClicked(this::handleMouseEvent);

        mScene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case F:
                    mShowFps = !mShowFps;
                    break;
                default:
                    break;
            }
        });

        lastEistFrameChangeTime = System.nanoTime();
        lastArtifactFrameChangeTime = System.nanoTime();
        lastFallingFrameChangeTime = System.nanoTime();

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {

                if (now - lastEistFrameChangeTime > FRAME_DURATION_EIST && eist.isMoving) {
                    lastEistFrameChangeTime = now;
                    mCurrentEistFrame++;

                    if (mCurrentEistFrame > FRAME_LAST_IDX) {
                        mCurrentEistFrame = 0;
                    }
                }

                if (now - lastArtifactFrameChangeTime > FRAME_DURATION_ARTIFACT) {
                    lastArtifactFrameChangeTime = now;
                    mCurrentArtifactFrame++;

                    if (mCurrentArtifactFrame > FRAME_LAST_IDX) {
                        mCurrentArtifactFrame = 0;
                    }
                }

                /*
                Count falldown frames (only once, 0 to 7th frame);
                (mCurrentFallingFrame != null) means: falldown animation in progress
                */
                if (mCurrentFallingFrame != null && now - lastFallingFrameChangeTime > FRAME_DURATION_FALLING) {
                    lastFallingFrameChangeTime = now;

                    if (mCurrentFallingFrame < 8) {

                        mCurrentFallingFrame++;

                    } else {

                        /*
                        Falldown finished
                         */
                        mCurrentFallingFrame = null;

                        if (mCurrentLevel > 0 && !mMuteSound) {
                            fxLevelLost.setBalance(calculateBalance(eist.x));
                            fxLevelLost.play();
                        }

                        loadLevel(mCurrentLevel);
                    }
                }

                updateBoard();

                drawBoard();

                if (lastUpdate > 0) {
                    long nanosElapsed = now - lastUpdate;
                    double frameRate = 1000000000.0 / nanosElapsed;
                    index %= frameRates.length;
                    frameRates[index++] = frameRate;
                }
                lastUpdate = now;

                /*
                 * Possibly binding the animation speed to current FPS value is unnecessary in JavaFX...
                 */
                mFps = getAverageFPS();
            }
        };
        animationTimer.start();

        /*
         * Handle the game window minimization:
         * Stop animation timer, pause and media player / start animation, resume sound when restored.
         */
        mGameStage.iconifiedProperty().addListener((ov, t, t1) -> {

            if(t1){

                if(trackMainPlayer != null) {
                    boolean playing = trackMainPlayer.getStatus().equals(MediaPlayer.Status.PLAYING);
                    mTrackMainWasPlaying = playing;
                    if(playing) {
                        trackMainPlayer.pause();
                    }
                }
                if(trackLevelPlayer != null) {
                    boolean playing = trackLevelPlayer.getStatus().equals(MediaPlayer.Status.PLAYING);
                    mTrackLevelWasPlaying = playing;
                    if(playing) {
                        trackLevelPlayer.pause();
                    }
                }
                animationTimer.stop();

            } else {

                if (mTrackMainWasPlaying) {
                    trackMainPlayer.play();
                }
                if (mTrackLevelWasPlaying) {
                    trackLevelPlayer.play();
                }
                animationTimer.start();
            }
        });

        mGameStage.show();

        mPlayer = prefs.get("user", "");
        mPass = prefs.get("pass", "");

        if(!mPlayer.isEmpty() && !mPass.isEmpty()){
            playerLogin(mPlayer, mPass);
        }
    }

    private void drawBoard() {

        gc.clearRect(0, 0, mSceneWidth, mSceneHeight);

        gc.drawImage(mBoardImg, 0, 0, mSceneWidth, mSceneHeight);

        /*
         * On level0 switch intro messages
         */
        if (mCurrentLevel == 0) {

            switch (mArtifacts.size()) {
                case 3:
                    gc.drawImage(mIntro01, 0, 0, mSceneWidth, mSceneHeight);
                    break;
                case 2:
                    gc.drawImage(mIntro02, 0, 0, mSceneWidth, mSceneHeight);
                    break;
                case 1:
                    gc.drawImage(mIntro03, 0, 0, mSceneWidth, mSceneHeight);
                    break;
                case 0:
                    gc.drawImage(mIntro04, 0, 0, mSceneWidth, mSceneHeight);
                    break;
            }
            String lvlNumberToString = (mSelectedLevel < 10) ? "0" + String.valueOf(mSelectedLevel) : String.valueOf(mSelectedLevel);
            gc.setFont(levelFont);
            gc.setFill(Color.color(0, 1, 1, 1));
            gc.fillText("LEVEL " + lvlNumberToString, columns[27], rows[1] + mHalfGridDimension);
        } else {

            gc.drawImage(mBoardImg, 0, 0, mSceneWidth, mSceneHeight);
        }

        if (mMuteMusic) {
            gc.drawImage(mMutedMusicImg, columns[30], rows[11], mGridDimension, mGridDimension);
        }
        if (mMuteSound) {
            gc.drawImage(mMutedSoundImg, columns[30], rows[13], mGridDimension, mGridDimension);
        }

        /*
         * Switch the Eists source graphics according to the movement direction. It could have been just rotated,
         * but I wanted the light to always come from the right side. Oh, ok: almost always. The bitmap will need
         * rotation while turning, and sometimes while falling down. Missing chiaroscuro should be unnoticeable.
         */
        switch (eist.getDirection()) {
            case DIR_RIGHT:
                mEistImg = mEistRightImg;
                break;

            case DIR_DOWN:
                mEistImg = mEistDownImg;
                break;

            case DIR_LEFT:
                mEistImg = mEistLeftImg;
                break;

            case DIR_UP:
                mEistImg = mEistUpImg;
                break;

            default:
                break;
        }

        /*
         * Draw arrows
         */
        if (mArrows != null && mArrows.size() > 0) {

            for (Arrow arrow : mArrows) {

                Image image;
                switch (arrow.getDirection()) {
                    case DIR_RIGHT:
                        image = mArrowRightImg;
                        break;
                    case DIR_DOWN:
                        image = mArrowDownImg;
                        break;
                    case DIR_LEFT:
                        image = mArrowLeftImg;
                        break;
                    case DIR_UP:
                        image = mArrowUpImg;
                        break;
                    default:
                        image = null;
                }
                gc.drawImage(image, arrow.getPosX(), arrow.getPosY(), mFrameDimension, mFrameDimension);

                if (arrow.getArea().contains(eist.getCenter())) {

                    reactToArrow(arrow);
                    break;
                }
            }
        }

        /*
         * Draw artifacts
         */
        if (mArtifactImg != null && mArtifacts.size() > 0) {

            for (Artifact artifact : mArtifacts) {

                gc.drawImage(mArtifactImg, 160 * mCurrentArtifactFrame, 0, 160, 160, artifact.getPosX(), artifact.getPosY(), mFrameDimension, mFrameDimension);

                if (artifact.getArea().contains(eist.getCenter())) {

                    mArtifacts.remove(artifact);

                    if (mCurrentLevel > 0) {

                        if (mArtifacts.size() > 0) {

                            if (!mMuteSound) {
                                fxArtifact.setBalance(calculateBalance(eist.x));
                                fxArtifact.play();
                            }

                        } else {

                            if (!mMuteSound) {
                                fxExit.play();
                            }
                        }
                    }
                    break;
                }
            }
        }

        /*
         * Draw ornaments
         */
        if (mOrnamentImg != null && mOrnaments.size() > 0) {

            for (Ornament ornament : mOrnaments) {

                gc.drawImage(mOrnamentImg, 160 * mCurrentArtifactFrame, 0, 160, 160, ornament.getPosX(), ornament.getPosY(), mFrameDimension, mFrameDimension);
            }
        }

        /*
         * Draw teleports
         */
        if (mTeleportImg != null && mTeleports.size() > 0) {

            for (Teleport teleport : mTeleports) {

                gc.drawImage(mTeleportImg, 160 * mCurrentArtifactFrame, 0, 160, 160, teleport.getPosX(), teleport.getPosY(), mFrameDimension, mFrameDimension);

                if (teleport.getArea().contains(eist.getCenter())) {

                    if (mCurrentLevel > 0 && !mMuteSound) {
                        fxTeleport.setBalance(calculateBalance(eist.x));
                        fxTeleport.play();
                    }

                    if (mTeleports.indexOf(teleport) == 0) {
                        switch (eist.getDirection()) {
                            case DIR_RIGHT:
                                eist.x = mTeleports.get(1).getPosX() + mGridDimension;
                                eist.y = mTeleports.get(1).getPosY();
                                break;

                            case DIR_LEFT:
                                eist.x = mTeleports.get(1).getPosX() - mGridDimension;
                                eist.y = mTeleports.get(1).getPosY();
                                break;

                            case DIR_UP:
                                eist.x = mTeleports.get(1).getPosX();
                                eist.y = mTeleports.get(1).getPosY() - mGridDimension;
                                break;

                            case DIR_DOWN:
                                eist.x = mTeleports.get(1).getPosX();
                                eist.y = mTeleports.get(1).getPosY() + mGridDimension;
                                break;
                        }

                    } else {

                        switch (eist.getDirection()) {
                            case DIR_RIGHT:
                                eist.x = mTeleports.get(0).getPosX() + mGridDimension;
                                eist.y = mTeleports.get(0).getPosY();
                                break;

                            case DIR_LEFT:
                                eist.x = mTeleports.get(0).getPosX() - mGridDimension;
                                eist.y = mTeleports.get(0).getPosY();
                                break;

                            case DIR_UP:
                                eist.x = mTeleports.get(0).getPosX();
                                eist.y = mTeleports.get(0).getPosY() - mGridDimension;
                                break;

                            case DIR_DOWN:
                                eist.x = mTeleports.get(0).getPosX();
                                eist.y = mTeleports.get(0).getPosY() + mGridDimension;
                                break;
                        }
                    }
                }
            }
        }

        /*
         * Draw keys
         */
        if (mKeyImg != null && mKeys.size() > 0) {

            for (Key key : mKeys) {

                gc.drawImage(mKeyImg, key.getPosX(), key.getPosY(), mFrameDimension, mFrameDimension);

                if (key.getArea().contains(eist.getCenter())) {

                    if (mCurrentLevel > 0 && !mMuteSound) {
                        fxKey.setBalance(calculateBalance(eist.x));
                        fxKey.play();
                    }

                    mKeys.remove(key);
                    eist.setKeys(eist.getKeys() + 1);
                    break;
                }
            }
        }

        /*
         * Draw slots
         */
        if (mSlots != null && mSlots.size() > 0) {
            for (Slot slot : mSlots) {
                if (slot.getOrientation() == ORIENTATION_HORIZONTAL) {
                    gc.drawImage(mSlotHImg, slot.getPosX(), slot.getPosY(), mGridDimension, mFrameDimension);
                } else {
                    gc.drawImage(mSlotVImg, slot.getPosX(), slot.getPosY(), mFrameDimension, mGridDimension);
                }
            }
        }

        /*
         * Draw doors
         */
        if (mDoors != null && mDoors.size() > 0) {

            Door doorToRemove = null;

            for (Door door : mDoors) {

                Image image;
                if (door.getOrientation() == ORIENTATION_HORIZONTAL) {
                    image = mDoorHImg;
                } else {
                    image = mDoorVImg;
                }
                gc.drawImage(image, door.getPosX(), door.getPosY(), mFrameDimension, mFrameDimension);

                if (door.getArea().contains(eist.getCenter())) {

                    if (eist.getKeys() > 0) {

                        if (mCurrentLevel > 0 && !mMuteSound) {
                            fxDoor.setBalance(calculateBalance(door.getPosX()));
                            fxDoor.play();
                        }

                        eist.setKeys(eist.getKeys() - 1);
                        doorToRemove = door;

                    } else {

                        if (!mDisableDoorReaction) {
                            reactToDoor(door);
                        }
                    }
                }
            }
            if (doorToRemove != null) {
                mDoors.remove(doorToRemove);
            }
            mDisableDoorReaction = false;
            for (Door door : mDoors) {
                if (door.getArea().contains(eist.getCenter())) {
                    mDisableDoorReaction = true;
                }
            }
        }

        /*
         * Draw ladder
         */
        Integer currentSlotIdx = ladder.getSlotIdx();
        if (currentSlotIdx != null) {
            Slot activeSlot = mSlots.get(currentSlotIdx);
            if (mSlots.get(currentSlotIdx).getOrientation() == 0) {
                mLadderImg = mLadderHImg;
                gc.drawImage(mLadderImg, activeSlot.getPosX(), activeSlot.getPosY(), mGridDimension, mFrameDimension);
            } else {
                mLadderImg = mLadderVImg;
                gc.drawImage(mLadderImg, activeSlot.getPosX(), activeSlot.getPosY(), mFrameDimension, mGridDimension);
            }
        } else {
            mLadderImg = mLadderHImg;
            gc.drawImage(mLadderImg, columns[29], rows[3], mGridDimension, mFrameDimension);
        }

        /*
         * Draw exit
         */
        if (mArtifacts.size() > 0) {
            gc.drawImage(mExitClosedImg, exit.getPosX(), exit.getPosY(), mFrameDimension, mFrameDimension);

        } else {

            gc.drawImage(mExitOpenImg, exit.getPosX(), exit.getPosY(), mFrameDimension, mFrameDimension);

            /*
             * Check if exit reached
             */
            if (exit.getArea().contains(eist.getCenter())) {

                /*
                if (mTurnsCounter < mTurnsBest || mTurnsBest == 0) {
                    String lvlNumberToString = (mCurrentLevel < 10) ? "0" + String.valueOf(mCurrentLevel) : String.valueOf(mCurrentLevel);
                    prefs.putInt(lvlNumberToString + "best", mTurnsCounter);
                    if (mTurnsCounter < mTurnsBest) {
                        displayNewBestAlert(mTurnsBest, mTurnsCounter);
                    }
                }
                */
                prefs.putInt(lvlToString(mCurrentLevel) + "best", mTurnsCounter);

                if (mCurrentLevel + 1 < MAX_LEVEL) {
                    mCurrentLevel++;
                } else {
                    mGameFinished = true;
                    mCurrentLevel = 0;
                }

                if (mCurrentLevel > mAchievedLevel && !mDevMode) {
                    prefs.putInt("achieved", mCurrentLevel);
                    mAchievedLevel = mCurrentLevel;
                }
                if(!mPlayer.isEmpty() && !mPass.isEmpty() && !mDevMode) {
                    updateHallScore(mPlayer, mPass, lvlToString(mCurrentLevel - 1), mTurnsCounter);
                }
                prefs.putInt("level", mCurrentLevel);

                mCurrentFallingFrame = 0;

                if (mCurrentLevel > 0 && !mMuteSound) {
                    fxLevelUp.play();
                }

                loadLevel(mCurrentLevel);
            }
        }

        /*
         * Draw Eist
         */
        if (mCurrentFallingFrame == null) {

            if (eist.rotation != 0) {
                gc.save();
                Rotate r = new Rotate(eist.rotation, eist.x + mGridDimension, eist.y + mGridDimension);
                gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
                gc.drawImage(mEistImg, 120 * mCurrentEistFrame, 0, 120, 120, eist.x, eist.y, mFrameDimension, mFrameDimension);
                gc.restore();

            } else {
                gc.drawImage(mEistImg, 120 * mCurrentEistFrame, 0, 120, 120, eist.x, eist.y, mFrameDimension, mFrameDimension);
            }
        }


        // Detect black pixel below
        if (mCurrentFallingFrame == null) {
            try {

                boolean leftOut = pixelReader.getArgb(eist.detectionPoint1X, eist.detectionPoint1Y) == -16777216;
                boolean rightOut = pixelReader.getArgb(eist.detectionPoint2X, eist.detectionPoint2Y) == -16777216;
                if (leftOut || rightOut) {

                    // Stepped off the path, start falling...
                    mCurrentFallingFrame = 0;

                    // Wait! Are we on the ladder?
                    if (ladder.getSlotIdx() != null) {
                        if (mSlots.get(ladder.getSlotIdx()).getArea().contains(new Point2D(eist.detectionPoint1X, eist.detectionPoint1Y))
                                && mSlots.get(ladder.getSlotIdx()).getArea().contains(new Point2D(eist.detectionPoint2X, eist.detectionPoint2Y))) {

                            mCurrentFallingFrame = null;
                        }
                    }
                    // Just left foot off the path
                    if (leftOut && !rightOut) {
                        switch (eist.getDirection()) {
                            case DIR_RIGHT:
                                eist.y = eist.y - mHalfGridDimension;
                                break;
                            case DIR_LEFT:
                                eist.y = eist.y + mHalfGridDimension;
                                break;
                            case DIR_UP:
                                eist.x = eist.x - mHalfGridDimension;
                                break;
                            case DIR_DOWN:
                                eist.x = eist.x + mHalfGridDimension;
                                break;
                        }
                    }
                    // Just right foot off the path
                    if (rightOut && !leftOut) {
                        switch (eist.getDirection()) {
                            case DIR_RIGHT:
                                eist.y = eist.y + mHalfGridDimension;
                                break;
                            case DIR_LEFT:
                                eist.y = eist.y - mHalfGridDimension;
                                break;
                            case DIR_UP:
                                eist.x = eist.x + mHalfGridDimension;
                                break;
                            case DIR_DOWN:
                                eist.x = eist.x - mHalfGridDimension;
                                break;
                        }
                    }

                } else {
                    mCurrentFallingFrame = null;
                }

            } catch (Exception e) {
                e.printStackTrace();
                eist.isMoving = false;
            }
        }

        if (eist.isMoving) {

            if (mCurrentFallingFrame != null) {

                switch (eist.getDirection()) {
                    case DIR_RIGHT:
                        gc.drawImage(mEistFallingRightImg, 160 * mCurrentFallingFrame, 0, 160, 160, eist.x, eist.y, mFrameDimension, mFrameDimension);
                        break;

                    case DIR_DOWN:
                        gc.drawImage(mEistFallingDownImg, 160 * mCurrentFallingFrame, 0, 160, 160, eist.x, eist.y, mFrameDimension, mFrameDimension);
                        break;

                    case DIR_LEFT:
                        gc.drawImage(mEistFallingLeftImg, 160 * mCurrentFallingFrame, 0, 160, 160, eist.x, eist.y, mFrameDimension, mFrameDimension);
                        break;

                    case DIR_UP:
                        gc.drawImage(mEistFallingUpImg, 160 * mCurrentFallingFrame, 0, 160, 160, eist.x, eist.y, mFrameDimension, mFrameDimension);
                        break;
                }
            }
            //gc.setFill(Color.WHITE);
            //gc.fillOval(eist.detectionPoint1X - 1, eist.detectionPoint1Y - 1, 2, 2);
            //gc.fillOval(eist.detectionPoint2X - 1, eist.detectionPoint2Y - 1, 2, 2);

        /*
         * Draw game pad selection;
         */
            if (pad.getSelection() != null) {

                Rectangle2D button;
                Image image;
                switch (pad.getSelection()) {
                    case DIR_RIGHT:
                        button = pad.getButtonRight();
                        image = mSelRightImg;
                        break;

                    case DIR_LEFT:
                        button = pad.getButtonLeft();
                        image = mSelLeftImg;
                        break;

                    case DIR_UP:
                        button = pad.getButtonUp();
                        image = mSelUpImg;
                        break;

                    case DIR_DOWN:
                        button = pad.getButtonDown();
                        image = mSelDownImg;
                        break;

                    case DIR_CLEAR:
                        button = pad.getButtonClear();
                        image = mSelClearImg;
                        break;

                    default:
                        button = pad.getButtonClear();
                        image = mSelClearImg;
                        break;
                }
                gc.drawImage(image, button.getMinX(), button.getMinY(), button.getWidth(), button.getHeight());
            }
        }

        gc.setFill(Color.WHITE);
        gc.setFont(infoFont);
        gc.fillText(" L " + lvlToString(mCurrentLevel), columns[27], rows[12]);
        gc.drawImage(mKeyImg, columns[26] + mHalfGridDimension, rows[12] + mHalfGridDimension, mFrameDimension, mFrameDimension);
        gc.fillText(String.valueOf(eist.getKeys()), columns[28], rows[14]);
        gc.setFont(turnsFont);
        gc.fillText("Turns: " + mTurnsCounter, columns[27], rows[15]);
        if (mTurnsBest > 0) {
            gc.fillText("Best: " + mTurnsBest, columns[27], rows[16]);
        } else {
            gc.fillText("Best: -", columns[27], rows[16]);
        }
        if (mShowFps) {
            gc.fillText("FPS: " + String.valueOf((int) mFps), columns[27], rows[17]);
        }
        if(mCurrentLevel == 0) {
            gc.setFill(Color.color(0.5, 1, 0.5, 1));
            if (!mPlayer.isEmpty()) {
                gc.setFont(playerFont);
                String displayName = mPlayer.toUpperCase();
                if(displayName.length() > 16) {
                    gc.fillText(displayName.substring(0, 16).toUpperCase() + "...", columns[27], rows[2]);
                } else {
                    gc.setFill(Color.color(0.5, 1, 0.5, 1));
                    gc.fillText(displayName.toUpperCase(), columns[27], rows[2]);
                }
            } else {
                gc.setFont(playerFont);
                gc.fillText("Hall of Fame login", columns[27], rows[2]);
            }
        }
    }

    private void updateBoard() {

        /*
         * The walkingSpeedPerSecond value depends on the board dimension.
         * The movement speed depends on it and the current FPS.
         */
        if (eist.isMoving) {
            switch (eist.getDirection()) {
                case DIR_RIGHT:
                    eist.x = eist.x + (walkingSpeedPerSecond / mFps);
                    break;

                case DIR_DOWN:
                    eist.y = eist.y + (walkingSpeedPerSecond / mFps);
                    break;

                case DIR_LEFT:
                    eist.x = eist.x - (walkingSpeedPerSecond / mFps);
                    break;

                case DIR_UP:
                    eist.y = eist.y - (walkingSpeedPerSecond / mFps);
                    break;

                default:
                    break;
            }
        }
        Point2D center = new Point2D(eist.x + mGridDimension, eist.y + mGridDimension);
        eist.setCenter(center);
        /*
         * Calculate points to check if black pixel below (triggers falling down)
         */
        switch (eist.getDirection()) {
            case DIR_RIGHT:
                eist.detectionPoint1X = (int) center.getX() + mDetectionOffset;
                eist.detectionPoint1Y = (int) center.getY() - mDetectionOffset;
                eist.detectionPoint2X = (int) center.getX() + mDetectionOffset;
                eist.detectionPoint2Y = (int) center.getY() + mDetectionOffset;
                break;

            case DIR_DOWN:
                eist.detectionPoint1X = (int) center.getX() + mDetectionOffset;
                eist.detectionPoint1Y = (int) center.getY() + mDetectionOffset;
                eist.detectionPoint2X = (int) center.getX() - mDetectionOffset;
                eist.detectionPoint2Y = (int) center.getY() + mDetectionOffset;
                break;

            case DIR_LEFT:
                eist.detectionPoint1X = (int) center.getX() - mDetectionOffset;
                eist.detectionPoint1Y = (int) center.getY() + mDetectionOffset;
                eist.detectionPoint2X = (int) center.getX() - mDetectionOffset;
                eist.detectionPoint2Y = (int) center.getY() - mDetectionOffset;
                break;

            case DIR_UP:
                eist.detectionPoint1X = (int) center.getX() - mDetectionOffset;
                eist.detectionPoint1Y = (int) center.getY() - mDetectionOffset;
                eist.detectionPoint2X = (int) center.getX() + mDetectionOffset;
                eist.detectionPoint2Y = (int) center.getY() - mDetectionOffset;
                break;
        }

        /*
         * Rotate Eist on arrows (also on doors in the future).
         * At the end of the maneuver - place him exactly on the endPoint.
         */
        int turning = eist.getTurning();
        if (turning != 0) {

            double endX = eist.getEndPoint().getX();
            double endY = eist.getEndPoint().getY();

            switch (eist.getDirection()) {
                case DIR_RIGHT:
                    switch (eist.getTurning()) {
                        case TURNING_RIGHT:
                            if (eist.x < endX) {
                                double left = Math.abs(endX - eist.x);
                                if (eist.rotation < 90) {
                                    eist.rotation = 90 - (90 * (left / mRotationRadius));
                                }
                            } else {
                                eist.rotation = 0;
                                eist.setDirection(DIR_DOWN);
                                eist.setTurning(TURNING_NOT);
                                eist.x = endX;
                                eist.y = endY;
                            }
                            break;

                        case TURNING_LEFT:
                            if (eist.x < endX) {
                                double left = Math.abs(eist.x - endX);
                                if (eist.rotation > -90) {
                                    eist.rotation = -90 + (90 * (left / mRotationRadius));
                                }
                            } else {
                                eist.rotation = 0;
                                eist.setDirection(DIR_UP);
                                eist.setTurning(TURNING_NOT);
                                eist.x = endX;
                                eist.y = endY;
                            }
                            break;

                        case TURNING_BACK:
                            if (eist.x < endX) {
                                double left = Math.abs(eist.x - endX);
                                if (turnRight) {
                                    if (eist.rotation < 180) {
                                        eist.rotation = 180 - (180 * (left / mRotationRadius));
                                    }
                                } else {
                                    if (eist.rotation > -180) {
                                        eist.rotation = -180 + (180 * (left / mRotationRadius));
                                    }
                                }
                            } else {
                                eist.rotation = 0;
                                eist.setDirection(DIR_LEFT);
                                eist.setTurning(TURNING_NOT);
                                eist.x = endX;
                                eist.y = endY;
                            }
                            break;
                    }
                    break;

                case DIR_LEFT:
                    switch (eist.getTurning()) {
                        case TURNING_LEFT:
                            if (eist.x > endX) {
                                double left = Math.abs(endX - eist.x);
                                if (eist.rotation < 90) {
                                    eist.rotation = -90 + (90 * (left / mRotationRadius));
                                }
                            } else {
                                eist.rotation = 0;
                                eist.setDirection(DIR_DOWN);
                                eist.setTurning(TURNING_NOT);
                                eist.x = endX;
                                eist.y = endY;
                            }
                            break;

                        case TURNING_RIGHT:
                            if (eist.x > endX) {
                                double left = Math.abs(eist.x - endX);
                                if (eist.rotation > -90) {
                                    eist.rotation = 90 - (90 * (left / mRotationRadius));
                                }
                            } else {
                                eist.rotation = 0;
                                eist.setDirection(DIR_UP);
                                eist.setTurning(TURNING_NOT);
                                eist.x = endX;
                                eist.y = endY;
                            }
                            break;

                        case TURNING_BACK:
                            if (eist.x > endX) {
                                double left = Math.abs(eist.x - endX);
                                if (turnRight) {
                                    if (eist.rotation < 180) {
                                        eist.rotation = 180 - (180 * (left / mRotationRadius));
                                    }
                                } else {
                                    if (eist.rotation > -180) {
                                        eist.rotation = -180 + (180 * (left / mRotationRadius));
                                    }
                                }
                            } else {
                                eist.rotation = 0;
                                eist.setDirection(DIR_RIGHT);
                                eist.setTurning(TURNING_NOT);
                                eist.x = endX;
                                eist.y = endY;
                            }
                            break;
                    }
                    break;

                case DIR_DOWN:
                    switch (eist.getTurning()) {
                        case TURNING_RIGHT:
                            if (eist.y < endY) {
                                double left = Math.abs(endY - eist.y);
                                if (eist.rotation < 90) {
                                    eist.rotation = 90 - (90 * (left / mRotationRadius));
                                }
                            } else {
                                eist.rotation = 0;
                                eist.setDirection(DIR_LEFT);
                                eist.setTurning(TURNING_NOT);
                                eist.x = endX;
                                eist.y = endY;
                            }
                            break;

                        case TURNING_LEFT:
                            if (eist.y < endY) {
                                double left = Math.abs(endY - eist.y);
                                if (eist.rotation > -90) {
                                    eist.rotation = -90 + (90 * (left / mRotationRadius));
                                }
                            } else {
                                eist.rotation = 0;
                                eist.setDirection(DIR_RIGHT);
                                eist.setTurning(TURNING_NOT);
                                eist.x = endX;
                                eist.y = endY;
                            }
                            break;

                        case TURNING_BACK:
                            if (eist.y < endY) {
                                double left = Math.abs(endY - eist.y);
                                if (turnRight) {
                                    if (eist.rotation < 180) {
                                        eist.rotation = 180 - (180 * (left / mRotationRadius));
                                    }
                                } else {
                                    eist.rotation = -180 + (180 * (left / mRotationRadius));
                                }
                            } else {
                                eist.rotation = 0;
                                eist.setDirection(DIR_UP);
                                eist.setTurning(TURNING_NOT);
                                eist.x = endX;
                                eist.y = endY;
                            }
                            break;
                    }
                    break;

                case DIR_UP:
                    switch (eist.getTurning()) {
                        case TURNING_RIGHT:
                            if (eist.y > endY) {
                                double left = Math.abs(endY - eist.y);
                                if (eist.rotation < 90) {
                                    eist.rotation = 90 - (90 * (left / mRotationRadius));
                                }
                            } else {
                                eist.rotation = 0;
                                eist.setDirection(DIR_RIGHT);
                                eist.setTurning(TURNING_NOT);
                                eist.x = endX;
                                eist.y = endY;
                            }
                            break;

                        case TURNING_LEFT:
                            if (eist.y > endY) {
                                double left = Math.abs(endY - eist.y);
                                if (eist.rotation > -90) {
                                    eist.rotation = -90 + (90 * (left / mRotationRadius));
                                }
                            } else {
                                eist.rotation = 0;
                                eist.setDirection(DIR_LEFT);
                                eist.setTurning(TURNING_NOT);
                                eist.x = endX;
                                eist.y = endY;
                            }
                            break;

                        case TURNING_BACK:
                            if (eist.y > endY) {
                                double left = Math.abs(endY - eist.y);
                                if (turnRight) {
                                    if (eist.rotation < 180) {
                                        eist.rotation = 180 - (180 * (left / mRotationRadius));
                                    }
                                } else {
                                    if (eist.rotation > -180) {
                                        eist.rotation = -180 + (180 * (left / mRotationRadius));
                                    }
                                }
                            } else {
                                eist.rotation = 0;
                                eist.setDirection(DIR_DOWN);
                                eist.setTurning(TURNING_NOT);
                                eist.x = endX;
                                eist.y = endY;
                            }
                            break;
                    }
                    break;
            }
        }
    }

    private void reactToArrow(Arrow arrow) {
        /*
         * In the updateBoard method: Keep turning until the point reached.
         */
        eist.setEndPoint(new Point2D(arrow.getPosX(), arrow.getPosY()));

        turnRight = getRandomBoolean();

        mTurnsCounter++;

        switch (eist.getDirection()) {
            case DIR_RIGHT:
                switch (arrow.getDirection()) {
                    case DIR_DOWN:
                        eist.setTurning(TURNING_RIGHT);
                        break;

                    case DIR_UP:
                        eist.setTurning(TURNING_LEFT);
                        break;

                    case DIR_LEFT:
                        eist.setTurning(TURNING_BACK);
                        break;

                    default:
                        break;
                }
                break;

            case DIR_DOWN:
                switch (arrow.getDirection()) {
                    case DIR_LEFT:
                        eist.setTurning(TURNING_RIGHT);
                        break;

                    case DIR_RIGHT:
                        eist.setTurning(TURNING_LEFT);
                        break;

                    case DIR_UP:
                        eist.setTurning(TURNING_BACK);
                        break;

                    default:
                        break;
                }
                break;

            case DIR_LEFT:
                switch (arrow.getDirection()) {
                    case DIR_DOWN:
                        eist.setTurning(TURNING_LEFT);
                        break;

                    case DIR_UP:
                        eist.setTurning(TURNING_RIGHT);
                        break;

                    case DIR_RIGHT:
                        eist.setTurning(TURNING_BACK);
                        break;

                    default:
                        break;
                }
                break;

            case DIR_UP:
                switch (arrow.getDirection()) {
                    case DIR_RIGHT:
                        eist.setTurning(TURNING_RIGHT);
                        break;

                    case DIR_LEFT:
                        eist.setTurning(TURNING_LEFT);
                        break;

                    case DIR_DOWN:
                        eist.setTurning(TURNING_BACK);
                        break;
                }
                break;
        }
        mArrows.remove(arrow);
    }

    private void reactToDoor(Door door) {

        mTurnsCounter++;

        if (mCurrentLevel > 0 && !mMuteSound) {
            fxBounce.setBalance(calculateBalance(eist.x));
            fxBounce.play();
        }

        turnRight = getRandomBoolean();

        switch (eist.getDirection()) {
            case DIR_RIGHT: {
                eist.setEndPoint(new Point2D(door.getPosX(), door.getPosY()));
                break;
            }
            case DIR_LEFT: {
                eist.setEndPoint(new Point2D(door.getPosX(), door.getPosY()));
                break;
            }
            case DIR_DOWN: {
                eist.setEndPoint(new Point2D(door.getPosX(), door.getPosY()));
                break;
            }
            case DIR_UP: {
                eist.setEndPoint(new Point2D(door.getPosX(), door.getPosY()));
                break;
            }
        }
        eist.setTurning(TURNING_BACK);

        mDisableDoorReaction = true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
