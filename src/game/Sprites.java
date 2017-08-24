package game;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

abstract class Sprites extends Utils {

    /**
     * Actually there will be just a single object of this class.
     */
    static class Player {

        boolean isMoving;
        boolean isFalling;
        double rotation;
        double x;
        double y;

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

        void setLifes(int value) {
            lifes = value;
        }

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        void setCenter(Point2D point2D) {
            center = point2D;
        }

        void setSensor(Point2D point2D) {
            sensor = point2D;
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

        int getLifes() {
            return lifes;
        }

        Rectangle2D getArea() {
            return  area;
        }

        Point2D getCenter() {
            return center;
        }

        Point2D getSensor() {
            return sensor;
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

        private double marginX;
        private double marginY;

        void setMarginX(double x_value) {
            marginX = x_value;
        }

        void setMarginY(double y_value) {
            marginY = y_value;
        }

        double getMarginX() {
            return marginX;
        }

        double getMarginY() {
            return marginY;
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

    static class Life {

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
}