package game;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.*;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

    private Font infoFont;
    private Font levelFont;
    private Font turnsFont;

    @Override
    public void start(Stage stage) throws Exception {

        setBoard();

        stage.setTitle("Eist returns");
        stage.getIcons().add(new Image("/images/common/eist.png"));
        stage.setResizable(false);
        if(mDimensionDivider == 1.0){
            stage.setFullScreen(true);
        }

        Group root = new Group();
        Scene mScene = new Scene(root, mSceneWidth, mSceneHeight);
        stage.setScene(mScene);
        Canvas canvas = new Canvas(mSceneWidth, mSceneHeight);
        root.getChildren().add(canvas);
        gc = canvas.getGraphicsContext2D();

        infoFont = Font.font("SansSerif", FontWeight.NORMAL, 60 / mDimensionDivider * rem);
        levelFont = Font.font("SansSerif", FontWeight.NORMAL, 48 / mDimensionDivider * rem);
        turnsFont = Font.font("SansSerif", FontWeight.NORMAL, 30 / mDimensionDivider * rem);
        gc.setFont(infoFont);

        eist = new Player();
        ladder = new Ladder();
        exit = new Exit();
        pad = new Pad();

        loadCommonGraphics();

        mSelectedLevel = prefs.getInt("achieved", 1);

        loadLevel(mCurrentLevel);

        mScene.setOnMouseClicked(this::handleMouseEvent);

        lastEistFrameChangeTime = System.nanoTime();
        lastArtifactFrameChangeTime = System.nanoTime();
        lastFallingFrameChangeTime = System.nanoTime();

        new AnimationTimer() {

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

                if (mCurrentFallingFrame != null && now - lastFallingFrameChangeTime > FRAME_DURATION_FALLING) {
                    lastFallingFrameChangeTime = now;

                    if (mCurrentFallingFrame < 8) {

                        mCurrentFallingFrame++;

                    } else {

                        mCurrentFallingFrame = null;

                        if(mCurrentLevel > 0 && !mMuteSound) {
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
                 * Let's use the averaged fps value here for now, but preserve the Utils.getInstantFPS() just in case.
                 * We need the current fps value to adjust the animation speed according to the current performance.
                 */
                mFps = getAverageFPS();
            }
        }.start();

        stage.show();
    }

    private void drawBoard() {

        gc.clearRect(0, 0, mSceneWidth, mSceneHeight);

        gc.drawImage(mBoardImg, 0, 0, mSceneWidth, mSceneHeight);

        /*
         * On level0 switch intro messages
         */
        if(mCurrentLevel == 0) {

            switch(mArtifacts.size()) {
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
            gc.fillText("LEVEL " + lvlNumberToString, columns[27], rows[2]);
        } else {

            gc.drawImage(mBoardImg, 0, 0, mSceneWidth, mSceneHeight);
        }

        if(mMuteMusic){
            gc.drawImage(mMutedMusicImg, columns[30], rows[11], mGridDimension, mGridDimension);
        }
        if(mMuteSound){
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

                    if(mCurrentLevel > 0){

                        if(mArtifacts.size() > 0) {

                            if(!mMuteSound) {
                                fxArtifact.play();
                            }

                        } else {

                            if(!mMuteSound) {
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

                    if(mCurrentLevel > 0 && !mMuteSound) {
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

                    if(mCurrentLevel > 0 && !mMuteSound) {
                        fxKey.play();
                    }

                    mKeys.remove(key);
                    eist.setKeys(eist.getKeys() + 1);
                    break;
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

                        if(mCurrentLevel > 0 && !mMuteSound) {
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

                if(mTurnsCounter < mTurnsBest || mTurnsBest == 0) {
                    String lvlNumberToString = (mCurrentLevel < 10) ? "0" + String.valueOf(mCurrentLevel) : String.valueOf(mCurrentLevel);
                    prefs.putInt(lvlNumberToString + "best", mTurnsCounter);
                    if(mTurnsCounter < mTurnsBest) {
                        displayNewBestAlert(mTurnsBest, mTurnsCounter);
                    }
                }

                mCurrentLevel++;

                if(mCurrentLevel > mAchievedLevel) {
                    prefs.putInt("achieved", mCurrentLevel);
                }
                prefs.putInt("level", mCurrentLevel);

                mCurrentFallingFrame = 0;

                if(mCurrentLevel > 0 && !mMuteSound) {
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
                    if(ladder.getSlotIdx() != null) {
                        if (mSlots.get(ladder.getSlotIdx()).getArea().contains(new Point2D(eist.detectionPoint1X, eist.detectionPoint1Y))
                                && mSlots.get(ladder.getSlotIdx()).getArea().contains(new Point2D(eist.detectionPoint2X, eist.detectionPoint2Y))) {

                            mCurrentFallingFrame = null;
                        }
                    }
                    // Just left foot off the path
                    if(leftOut && !rightOut) {
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
                    if(rightOut && !leftOut) {
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
        /*
         * Uncomment for testing if your system keeps 60 FPS rate. See comments in the Start class.
         */
        gc.setFill(Color.WHITE);
        //gc.fillText(String.valueOf((int) mFps), columns[0], rows[18]);

        gc.setFont(infoFont);
        gc.fillText(String.valueOf(mCurrentLevel), columns[28], rows[12]);
        gc.fillText(String.valueOf(eist.getKeys()), columns[28], rows[14]);
        gc.setFont(turnsFont);
        gc.fillText("Turns: " + mTurnsCounter, columns[27], rows[15]);
        if(mTurnsBest > 0) {
            gc.fillText("Best: " + mTurnsBest, columns[27], rows[16]);
        } else {
            gc.fillText("Best: -", columns[27], rows[16]);
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

        if(mCurrentLevel > 0 && !mMuteSound) {
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
