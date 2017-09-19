package game;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Start extends Utils {

    Image mBackground;
    Stage splashStage;

    Task<Void> sleeper;

    @Override
    public void start(Stage stage) throws Exception {

        splashStage = stage;

        System.setProperty("quantum.multithreaded", "false");
        System.out.println("\nSetting quantum.multithreaded false");

        setSplash();

        stage.setTitle("Eist returns");
        stage.getIcons().add(new Image("/images/common/eist.png"));
        stage.setResizable(false);
        stage.initStyle(StageStyle.UTILITY);

        Group root = new Group();
        Scene scene = new Scene(root, mSplashWidth, mSplashHeight);
        stage.setScene(scene);
        Canvas canvas = new Canvas(mSplashWidth, mSplashHeight);
        root.getChildren().add(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        mBackground =  new Image("images/common/splash.png");

        gc.drawImage(mBackground, 0, 0, mSplashWidth, mSplashHeight);

        scene.setOnMouseClicked(this::handleMouse);

        sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println(e.toString());
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(event -> {
            play();

        });
        new Thread(sleeper).start();

        stage.show();

    }

    private void handleMouse(MouseEvent event) {

        Point2D pointClicked = new Point2D(event.getSceneX(), event.getSceneY());
        if(pointClicked.getX() > mSplashCenterX) {
            sleeper.cancel();
            play();
        }

    }

    private void play(String... args) {

        /*
         * On some systems (Dual Intel/Nvidia graphics + Linux) it happens to JavaFX
         * not to keep FPS at 60 value, which results in extremely high refresh rate,
         * and high resources consumption. This workaround seems to help.
         * See: https://stackoverflow.com/a/45827990/4040598
         */
        Platform.runLater(new Runnable() {
            public void run() {
                try {
                    new Main().start(new Stage());
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        splashStage.close();
    }
}