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

        Integer direction;
        Integer turning; // TURNING_NOT / _RIGHT / _LEFT / _BACK
        Rectangle2D area;
        Point2D center;
        Point2D end_point; // Coordinates at which Eist should be placed at the end of the current maneuver

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

        void setArea(Rectangle2D rectangle2D) {
            area = rectangle2D;
        }

        void setCenter(Point2D point2D) {
            center = point2D;
        }

        void setEndPoint(Point2D point2D) {
            end_point = point2D;
        }

        int getDirection() {
            if(direction != null) {
                return direction;
            } else {
                return 0;
            }
        }

        int getTurning() {
            if(turning != null) {
                return turning;
            } else {
                return 0;
            }
        }

        Rectangle2D getArea() {
            return  area;
        }

        Point2D getCenter() {
            return center;
        }

        Point2D getEndPoint() {
            return end_point;
        }
    }

    static class Arrow {

        double posX;
        double posY;
        int direction;
        Rectangle2D area;

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

        double posX;
        double posY;
        Rectangle2D area;

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

        double marginX;
        double marginY;

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

        double posX;
        double posY;
        Rectangle2D area;

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

        double posX;
        double posY;
        Rectangle2D area;

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

        double posX;
        double posY;
        Rectangle2D area;
        int orientation;

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

        double posX;
        double posY;
        Rectangle2D area;
        int orientation; // 0 = horizontal, 1 = vertical

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

    static class Life {

        double posX;
        double posY;
        Rectangle2D area;

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