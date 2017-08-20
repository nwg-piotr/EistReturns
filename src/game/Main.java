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

public class Main extends Utils {

    private GraphicsContext graphicsContext;

    private double mEistRotation = 0;

    private final int FRAME_LAST_IDX = 7;
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

        loadCommonGraphics();

        loadLevel(1);

        eist = new Player();

        /*
         * (Temporarily) initialize starting values. Later we'll need the loadLevel(int level) method.
         */
        mEistX = columns[1];
        mEistY = rows[1];

        eist.setDirection(DIR_RIGHT);

        mEistImage = mEistRight;

        mScene.setOnMouseClicked(this::handleMouseEvents);

        lastEistFrameChangeTime = System.nanoTime();

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

    private void drawBoard(GraphicsContext gc, int eist_frame) {

        gc.clearRect(0, 0, mSceneWidth, mSceneHeight);

        gc.drawImage(mBoard, 0, 0, mSceneWidth, mSceneHeight);



        /*
         * Switch the sprites source graphics according to the movement direction. It could have been just rotated,
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
        if(mArrows != null && mArrows.size() > 0) {

            Integer collision = null;
            for(Arrow arrow : mArrows) {

                Image image;
                switch(arrow.getDirection()){
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
            }

        }
        if(mEistRotation != 0) {
            gc.save();
            Rotate r = new Rotate(mEistRotation, mEistX + mGridDimension, mEistY + mGridDimension);
            gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
            gc.drawImage(mEistImage, 120 * eist_frame, 0, 120, 120, mEistX, mEistY, mFrameDimension, mFrameDimension);
            gc.restore();
        } else {
            gc.drawImage(mEistImage, 120 * eist_frame, 0, 120, 120, mEistX, mEistY, mFrameDimension, mFrameDimension);
        }
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
        eist.setArea(new Rectangle2D(mEistX, mEistY, mFrameDimension, mFrameDimension));
        eist.setCenter(new Point2D(mEistX + mGridDimension, mEistY + mGridDimension));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
