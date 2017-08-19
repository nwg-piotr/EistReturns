package game;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.*;

import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import game.Sprites.Player;

public class Main extends Utils {

    private GraphicsContext graphicsContext;

    private Image board;
    private Image eistImage;
    private Image eistRight;
    private Image eistDown;
    private Image eistLeft;
    private Image eistUp;

    private final int FRAMES_NUMBER = 7;
    private final double FRAME_DURATION_EIST = 90000000;
    private int mCurrentEistFrame = 0;

    private long lastEistFrameChangeTime;

    private double mFps = 0;

    private double mEistX;
    private double mEistY;

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

        loadGraphics();

        eist = new Player();

        /*
         * (Temporarily) initialize starting values. Later we'll need the loadLevel(int level) method.
         */
        mEistX = columns[1];
        mEistY = rows[1];

        eist.setDirection(DIR_RIGHT);

        eistImage = eistRight;

        mScene.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleMouseEvents(event);
            }
        });

        lastEistFrameChangeTime = System.nanoTime();

        new AnimationTimer() {

            @Override
            public void handle(long now) {

                if (now - lastEistFrameChangeTime > FRAME_DURATION_EIST && eist.isMoving) {
                    lastEistFrameChangeTime = now;
                    mCurrentEistFrame++;

                    if (mCurrentEistFrame > FRAMES_NUMBER) {
                        mCurrentEistFrame = 0;
                    }
                }

                drawBoard(graphicsContext, mCurrentEistFrame);
                updateBoard();

                if (lastUpdate > 0) {
                    long nanosElapsed = now - lastUpdate;
                    double frameRate = 1000000000.0 / nanosElapsed;
                    index %= frameRates.length;
                    frameRates[index++] = frameRate;
                }
                lastUpdate = now;

                /*
                 * Let's use the averaged fps value here for now, but preserve the Utils.getAverageFPS() just in case.
                 * We need the current fps value to adjust the animation speed according to the current performance.
                 */
                mFps = getAverageFPS();
            }
        }.start();

        stage.show();
    }

    private void loadGraphics() {

        board = new Image("images/boards/04.png", mSceneWidth, mSceneHeight, true, true);
        eistRight = new Image("images/sprites/eist_right.png");
        eistDown = new Image("images/sprites/eist_down.png");
        eistLeft = new Image("images/sprites/eist_left.png");
        eistUp = new Image("images/sprites/eist_up.png");
    }

    private void drawBoard(GraphicsContext gc, int eist_frame) {

        gc.clearRect(0, 0, mSceneWidth, mSceneHeight);

        gc.drawImage(board, 0, 0, mSceneWidth, mSceneHeight);

        eist.setArea(new Rectangle2D(mEistX, mEistY, mFrameDimension, mFrameDimension));

        /*
         * Switch the sprites source graphics according to the movement direction. It could have been just rotated,
         * but I wanted the light to always come from the right side.
         */
        switch (eist.getDirection()) {
            case DIR_RIGHT:
                eistImage = eistRight;
                break;
            case DIR_DOWN:
                eistImage = eistDown;
                break;
            case DIR_LEFT:
                eistImage = eistLeft;
                break;
            case DIR_UP:
                eistImage = eistUp;
                break;
            default:
                break;
        }
        gc.drawImage(eistImage, 120 * eist_frame, 0, 120, 120, mEistX, mEistY, mFrameDimension, mFrameDimension);
        /*
         * Just for testing purposes:
         */
        gc.fillText(String.valueOf((int) mFps), columns[0], rows[18]);
    }

    private void updateBoard() {

        /*
         * The walkSpeedPerSecond value depends on the board dimension.
         * The movement speed depends on it and the current FPS.
         */
        if (eist.isMoving) {
            switch (eist.getDirection()) {
                case DIR_RIGHT:
                    mEistX = mEistX + (walkSpeedPerSecond / mFps);
                    break;
                case DIR_DOWN:
                    mEistY = mEistY + (walkSpeedPerSecond / mFps);
                    break;
                case DIR_LEFT:
                    mEistX = mEistX - (walkSpeedPerSecond / mFps);
                    break;
                case DIR_UP:
                    mEistY = mEistY - (walkSpeedPerSecond / mFps);
                    break;
                default:
                    break;
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
