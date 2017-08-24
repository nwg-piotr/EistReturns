package game;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.*;

import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import game.Sprites.Player;
import game.Sprites.Arrow;
import game.Sprites.Artifact;
import game.Sprites.Teleport;
import game.Sprites.Key;
import game.Sprites.Door;
import game.Sprites.Ladder;
import game.Sprites.Slot;
import game.Sprites.Exit;
import game.Sprites.Pad;

public class Main extends Utils {

    private int mCurrentLevel = 1;
    private GraphicsContext gc;

    private final int FRAME_LAST_IDX = 7;
    private final double FRAME_DURATION_EIST = 90000000;
    private final double FRAME_DURATION_ARTIFACT = 135000000;
    private final double FRAME_DURATION_FALLING = 90000000;
    private int mCurrentEistFrame = 0;
    private int mCurrentArtifactFrame = 0;
    private Integer mCurrentFallingFrame = null;

    private long lastEistFrameChangeTime;
    private long lastArtifactFrameChangeTime;
    private long lastFallingFrameChangeTime;

    private double mFps = 0;

    @Override
    public void start(Stage stage) throws Exception {

        setBoardDimensions();

        stage.setTitle("Eist returns");
        stage.getIcons().add(new Image("images/icons/eist.png"));
        stage.setResizable(false);

        Group root = new Group();
        Scene mScene = new Scene(root, mSceneWidth, mSceneHeight);
        stage.setScene(mScene);
        Canvas canvas = new Canvas(mSceneWidth, mSceneHeight);
        root.getChildren().add(canvas);
        gc = canvas.getGraphicsContext2D();

        Font theFont = Font.font("SansSerif", FontWeight.NORMAL, 20);
        gc.setFont(theFont);
        gc.setFill(Color.WHITE);

        eist = new Player();
        ladder = new Ladder();
        exit = new Exit();
        pad = new Pad();

        loadCommonGraphics();

        loadLevel(mCurrentLevel);

        /*
         * (Temporarily) initialize starting values. Later we'll need the loadLevel(int level) method.
         */
        eist.setKeys(0);

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

                if (eist.isFalling &&
                        now - lastFallingFrameChangeTime > FRAME_DURATION_FALLING) {
                    lastFallingFrameChangeTime = now;
                    if (mCurrentFallingFrame != null) {
                        mCurrentFallingFrame++;
                    }

                    if (mCurrentFallingFrame != null) {
                        if (mCurrentFallingFrame < 7) {
                            mCurrentFallingFrame++;
                        } else {
                            mCurrentFallingFrame = null;
                            //loadLevel(mCurrentLevel);
                        }
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

                gc.drawImage(mArtifactImg, 160 * mCurrentEistFrame, 0, 160, 160, artifact.getPosX(), artifact.getPosY(), mFrameDimension, mFrameDimension);

                if (artifact.getArea().contains(eist.getCenter())) {

                    mArtifacts.remove(artifact);
                    break;
                }
            }
        }

        /*
         * Draw teleports
         */
        if (mTeleportImg != null && mTeleports.size() > 0) {

            for (Teleport teleport : mTeleports) {

                gc.drawImage(mTeleportImg, 160 * mCurrentArtifactFrame, 0, 160, 160, teleport.getPosX(), teleport.getPosY(), mFrameDimension, mFrameDimension);

                if (teleport.getArea().contains(eist.getCenter())) {

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

                    mKeys.remove(key);
                    eist.setKeys(eist.getKeys() + 1);
                    System.out.println("Keys owned: " + eist.getKeys());
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

                        eist.setKeys(eist.getKeys() - 1);
                        System.out.println("Keys left: " + eist.getKeys());
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

                eist.isMoving = false;
            }
        }

        /*
         * Draw Eist
         */
        if (!eist.isFalling) {

            if (eist.rotation != 0) {
                gc.save();
                Rotate r = new Rotate(eist.rotation, eist.x + mGridDimension, eist.y + mGridDimension);
                gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
                gc.drawImage(mEistImg, 120 * mCurrentEistFrame, 0, 120, 120, eist.x, eist.y, mFrameDimension, mFrameDimension);
                gc.restore();

            } else {
                gc.drawImage(mEistImg, 120 * mCurrentEistFrame, 0, 120, 120, eist.x, eist.y, mFrameDimension, mFrameDimension);
            }

            try {
                PixelReader pixelReader = mBoardImg.getPixelReader();
                    /*
                     * Detect black pixel ahead
                     */
                if (pixelReader.getArgb((int) eist.getCenter().getX(), (int) eist.getCenter().getY()) == -16777216) {
                    /*
                     * Check if not over occupied slot or all slots empty
                     */
                    if (ladder.getSlotIdx() == null || !mSlots.get(ladder.getSlotIdx()).getArea().contains(eist.getCenter())) {
                        /*
                         * stepped off the path, start falling
                         */
                        eist.isFalling = true;
                        mCurrentFallingFrame = 0;
                    }
                } else {
                    eist.isFalling = false;
                    mCurrentFallingFrame = null;
                }
            } catch (Exception e) {
                System.out.println("Exception intercepted (pixelReader): " + e);
                eist.isMoving = false;
            }
        }

        if (eist.isMoving) {

            if (eist.isFalling) {

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
                } else {
                    eist.isFalling = false;
                }
            }
        }

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

        /*
         * Just for testing purposes:
         */
        gc.fillText(String.valueOf((int) mFps), columns[0], rows[18]);
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
        eist.setArea(new Rectangle2D(eist.x, eist.y, mFrameDimension, mFrameDimension));
        eist.setCenter(new Point2D(eist.x + mGridDimension, eist.y + mGridDimension));

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
                                    if (eist.rotation > -180) {
                                        eist.rotation = -180 + (180 * (left / mRotationRadius));
                                    }
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
