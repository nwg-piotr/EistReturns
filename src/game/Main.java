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
import game.Sprites.Key;
import game.Sprites.Door;
import game.Sprites.Ladder;

public class Main extends Utils {

    private GraphicsContext graphicsContext;

    private final int FRAME_LAST_IDX = 7;
    private final double FRAME_DURATION_EIST = 90000000;
    private final double FRAME_DURATION_ARTIFACT = 135000000;
    private int mCurrentEistFrame = 0;
    private int mCurrentArtifactFrame = 0;

    private long lastEistFrameChangeTime;
    private long lastArtifactFrameChangeTime;

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
        graphicsContext = canvas.getGraphicsContext2D();

        Font theFont = Font.font("SansSerif", FontWeight.NORMAL, 20);
        graphicsContext.setFont(theFont);
        graphicsContext.setFill(Color.WHITE);

        loadCommonGraphics();



        eist = new Player();
        ladder = new Ladder();

        loadLevel(40);

        /*
         * (Temporarily) initialize starting values. Later we'll need the loadLevel(int level) method.
         */
        eist.x = columns[11];
        eist.y = rows[8];

        eist.setDirection(DIR_LEFT);
        eist.setKeys(0);

        mEistImage = mEistRight;

        mScene.setOnMouseClicked(this::handleMouseEvents);

        lastEistFrameChangeTime = System.nanoTime();
        lastArtifactFrameChangeTime = System.nanoTime();

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

                drawBoard(graphicsContext, mCurrentEistFrame, mCurrentArtifactFrame);
                updateBoard();

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

    private void drawBoard(GraphicsContext gc, int eist_frame, int artifact_frame) {

        gc.clearRect(0, 0, mSceneWidth, mSceneHeight);

        gc.drawImage(mBoard, 0, 0, mSceneWidth, mSceneHeight);

        /*
         * Switch the Eists source graphics according to the movement direction. It could have been just rotated,
         * but I wanted the light to always come from the right side. Oh, ok: almost always. The bitmap will need
         * rotation while turning, and sometimes while falling down. Missing chiaroscuro should be unnoticeable.
         */
        switch (eist.getDirection()) {
            case DIR_RIGHT:
                mEistImage = mEistRight;
                break;

            case DIR_DOWN:
                mEistImage = mEistDown;
                break;

            case DIR_LEFT:
                mEistImage = mEistLeft;
                break;

            case DIR_UP:
                mEistImage = mEistUp;
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
                        image = mArrowRight;
                        break;
                    case DIR_DOWN:
                        image = mArrowDown;
                        break;
                    case DIR_LEFT:
                        image = mArrowLeft;
                        break;
                    case DIR_UP:
                        image = mArrowUp;
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
        if (mArtifact != null && mArtifacts.size() > 0) {

            for (Artifact artifact : mArtifacts) {

                gc.drawImage(mArtifact, 160 * artifact_frame, 0, 160, 160, artifact.getPosX(), artifact.getPosY(), mFrameDimension, mFrameDimension);

                if (artifact.getArea().contains(eist.getCenter())) {

                    mArtifacts.remove(artifact);
                    break;
                }
            }
        }

        /*
         * Draw keys
         */
        if (mKey != null && mKeys.size() > 0) {

            for (Key key : mKeys) {

                gc.drawImage(mKey, key.getPosX(), key.getPosY(), mFrameDimension, mFrameDimension);

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
                    image = mDoorH;
                } else {
                    image = mDoorV;
                }
                gc.drawImage(image, door.getPosX(), door.getPosY(), mFrameDimension, mFrameDimension);

                if (door.getArea().contains(eist.getCenter())) {

                    if (eist.getKeys() > 0) {

                        eist.setKeys(eist.getKeys() - 1);
                        System.out.println("Keys left: " + eist.getKeys());
                        doorToRemove = door;

                    } else {

                        if(!mDisableDoorReaction) {
                            reactToDoor(door);
                        }
                    }
                }
            }
            if(doorToRemove != null) {
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
         * Draw Eist
         */
        if (eist.rotation != 0) {
            gc.save();
            Rotate r = new Rotate(eist.rotation, eist.x + mGridDimension, eist.y + mGridDimension);
            gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
            gc.drawImage(mEistImage, 120 * eist_frame, 0, 120, 120, eist.x, eist.y, mFrameDimension, mFrameDimension);
            gc.restore();
        } else {
            gc.drawImage(mEistImage, 120 * eist_frame, 0, 120, 120, eist.x, eist.y, mFrameDimension, mFrameDimension);
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
