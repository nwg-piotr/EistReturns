package game;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

abstract class Sprites extends Utils {

    /**
     * Actually there will be just a single object of this class.
     */
    static class Player {

        boolean isMoving;
        double rotation;
        double x;
        double y;
        int detectionPoint1X;
        int detectionPoint1Y;
        int detectionPoint2X;
        int detectionPoint2Y;

        private Integer direction;
        private Integer turning; // TURNING_NOT / _RIGHT / _LEFT / _BACK
        private Integer keys;
        private int lifes;
        private Rectangle2D area;
        private Point2D center;
        private Point2D sensor;
        private Point2D end_point; // Coordinates at which Eist should be placed at the end of the current maneuver

        void setDirection(int value) {
            switch(value) {
                case DIR_RIGHT:
                case DIR_DOWN:
                case DIR_LEFT:
                case DIR_UP:
                    direction = value;
            }
        }

        void setTurning(int value) {
            switch(value) {
                case TURNING_NOT:
                case TURNING_RIGHT:
                case TURNING_LEFT:
                case TURNING_BACK:
                    turning = value;
            }
        }

        void setKeys(int value) {
            keys = value;
        }

        void setCenter(Point2D point2D) {
            center = point2D;
        }


        void setEndPoint(Point2D point2D) {
            end_point = point2D;
        }

        int getDirection() {
            return direction != null ? direction : 0;
        }

        int getTurning() {
            return turning != null ? turning : 0;
        }

        int getKeys() {
            return keys != null ? keys : 0;
        }

        Point2D getCenter() {
            return center;
        }

        Point2D getEndPoint() {
            return end_point;
        }
    }

    static class Arrow {

        private double posX;
        private double posY;
        private int direction;
        private Rectangle2D area;

        void setPosX(double x_value) {
            posX = x_value;
        }

        void setPosY(double y_value) {
            posY = y_value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        void setDirection(int dir_value) {
            direction = dir_value;
        }

        double getPosX() {
            return posX;
        }

        double getPosY() {
            return posY;
        }

        Rectangle2D getArea() {
            return  area;
        }

        int getDirection() {
            return direction;
        }
    }

    static class Artifact {

        private double posX;
        private double posY;
        private Rectangle2D area;

        void setPosX(double x_value) {
            posX = x_value;
        }

        void setPosY(double y_value) {
            posY = y_value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        double getPosX() {
            return posX;
        }

        double getPosY() {
            return posY;
        }

        Rectangle2D getArea() {
            return  area;
        }
    }

    static class Ornament {

        private double posX;
        private double posY;

        void setPosX(double x_value) {
            posX = x_value;
        }

        void setPosY(double y_value) {
            posY = y_value;
        }

        double getPosX() {
            return posX;
        }

        double getPosY() {
            return posY;
        }
    }

    static class Teleport {

        private double posX;
        private double posY;
        private Rectangle2D area;

        void setPosX(double x_value) {
            posX = x_value;
        }

        void setPosY(double y_value) {
            posY = y_value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        double getPosX() {
            return posX;
        }

        double getPosY() {
            return posY;
        }

        Rectangle2D getArea() {
            return  area;
        }

    }

    static class Key {

        private double posX;
        private double posY;
        private Rectangle2D area;

        void setPosX(double x_value) {
            posX = x_value;
        }

        void setPosY(double y_value) {
            posY = y_value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        double getPosX() {
            return posX;
        }

        double getPosY() {
            return posY;
        }

        Rectangle2D getArea() {
            return  area;
        }

    }

    static class Door {

        private double posX;
        private double posY;
        private Rectangle2D area;
        private int orientation;

        void setPosX(double x_value) {
            posX = x_value;
        }

        void setPosY(double y_value) {
            posY = y_value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        void setOrientation(int o_value) {
            orientation = o_value;
        }

        double getPosX() {
            return posX;
        }

        double getPosY() {
            return posY;
        }

        Rectangle2D getArea() {
            return  area;
        }

        int getOrientation() {
            return orientation;
        }

    }

    static class Slot {

        private double posX;
        private double posY;
        private Rectangle2D area;
        private int orientation; // 0 = horizontal, 1 = vertical

        void setPosX(double x_value) {
            posX = x_value;
        }

        void setPosY(double y_value) {
            posY = y_value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        void setOrientation(int orientation_value) {
            orientation = orientation_value;
        }

        double getPosX() {
            return posX;
        }

        double getPosY() {
            return posY;
        }

        Rectangle2D getArea() {
            return  area;
        }

        int getOrientation() {
            return orientation;
        }
    }

    static class Ladder {

        private Integer slotIdx;

        void setSlotIdx(Integer value) {
            slotIdx = value;
        }

        Integer getSlotIdx() {
            return slotIdx;
        }
    }

    static class Exit {

        private double posX;
        private double posY;
        private Rectangle2D area;

        void setPosX(double x_value) {
            posX = x_value;
        }

        void setPosY(double y_value) {
            posY = y_value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        double getPosX() {
            return posX;
        }

        double getPosY() {
            return posY;
        }

        Rectangle2D getArea() {
            return  area;
        }
    }

    static class Pad {

        private Integer selection;
        private Rectangle2D buttonUp;
        private Rectangle2D buttonDown;
        private Rectangle2D buttonLeft;
        private Rectangle2D buttonRight;
        private Rectangle2D buttonClear;

        void setSelection(Integer value) {
            selection = value;
        }

        Integer getSelection() {
            return selection;
        }

        void setButtonUp(Rectangle2D rectangle2D) {
            buttonUp = rectangle2D;
        }

        void setButtonDown(Rectangle2D rectangle2D) {
            buttonDown = rectangle2D;
        }

        void setButtonLeft(Rectangle2D rectangle2D) {
            buttonLeft = rectangle2D;
        }

        void setButtonRight(Rectangle2D rectangle2D) {
            buttonRight = rectangle2D;
        }

        void setButtonClear(Rectangle2D rectangle2D) {
            buttonClear = rectangle2D;
        }

        Rectangle2D getButtonUp() {
            return  buttonUp;
        }

        Rectangle2D getButtonDown() {
            return  buttonDown;
        }

        Rectangle2D getButtonLeft() {
            return  buttonLeft;
        }

        Rectangle2D getButtonRight() {
            return  buttonRight;
        }

        Rectangle2D getButtonClear() {
            return  buttonClear;
        }
    }

    static class Toolbar {

        private Integer doorOrientation;
        private Integer slotOrientation;
        private Integer arrowDirection;
        private Integer selection;

        private Rectangle2D doorArea;
        private Rectangle2D slotArea;
        private Rectangle2D artifactArea;
        private Rectangle2D keyArea;
        private Rectangle2D teleportArea;
        private Rectangle2D exitArea;
        private Rectangle2D arrowArea;
        private Rectangle2D ornamentArea;
        private Rectangle2D clearArea;

        void setDoorOrientation(Integer value) {
            doorOrientation = value;
        }

        void setSlotOrientation(Integer value) {
            slotOrientation = value;
        }

        void setArrowDirection(Integer value) {
            arrowDirection = value;
        }

        void setSelection(Integer value) {
            selection = value;
        }

        void setDoorArea(Rectangle2D rectangle2D) {
            doorArea = rectangle2D;
        }

        void setSlotArea(Rectangle2D rectangle2D) {
            slotArea = rectangle2D;
        }

        void setArtifactArea(Rectangle2D rectangle2D) {
            artifactArea = rectangle2D;
        }

        void setKeyArea(Rectangle2D rectangle2D) {
            keyArea = rectangle2D;
        }

        void setTeleportArea(Rectangle2D rectangle2D) {
            teleportArea = rectangle2D;
        }
        void setExitArea(Rectangle2D rectangle2D) {
            exitArea = rectangle2D;
        }

        void setArrowArea(Rectangle2D rectangle2D) {
            arrowArea = rectangle2D;
        }

        void setOrnamentArea(Rectangle2D rectangle2D) {
            ornamentArea = rectangle2D;
        }

        void setClearArea(Rectangle2D rectangle2D) {
            clearArea = rectangle2D;
        }

        Integer getArrowDirection() {
            return arrowDirection;
        }

        Integer getSlotOrientation() {
            return slotOrientation;
        }

        Integer getDoorOrientation() {
            return doorOrientation;
        }

        Integer getSelection() {
            return selection;
        }

        Rectangle2D getDoorArea() {
            return doorArea;
        }

        Rectangle2D getSlotArea() {
            return slotArea;
        }

        Rectangle2D getArtifactArea() {
            return artifactArea;
        }

        Rectangle2D getKeyArea() {
            return keyArea;
        }

        Rectangle2D getTeleportArea() {
            return  teleportArea;
        }

        Rectangle2D getExitArea() {
            return  exitArea;
        }

        Rectangle2D getArrowArea() {
            return  arrowArea;
        }

        Rectangle2D getOrnamentArea() {
            return  ornamentArea;
        }

        Rectangle2D getClearArea() {
            return  clearArea;
        }
    }
}